import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { ownerFoodApi } from '../api/operations.api';
import { propertyApi } from '../api/property.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { MealType } from '../types/operations';
import { getApiErrorMessage } from '../utils/apiError';
import { label, mealOrder, todayIso } from '../utils/operations';

export default function OwnerFoodPage() {
  const queryClient = useQueryClient();
  const [propertyId, setPropertyId] = useState('');
  const [date, setDate] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType>('BREAKFAST');
  const [items, setItems] = useState('');
  const [notes, setNotes] = useState('');
  const propertiesQuery = useQuery({ queryKey: ['owner-properties'], queryFn: propertyApi.listProperties });
  const menusQuery = useQuery({ queryKey: ['owner-food-menus'], queryFn: ownerFoodApi.menus });
  const feedbackQuery = useQuery({ queryKey: ['owner-food-feedback', propertyId], queryFn: () => ownerFoodApi.feedback(propertyId ? Number(propertyId) : undefined) });
  const createMenu = useMutation({
    mutationFn: () => ownerFoodApi.createMenu({ propertyId: Number(propertyId), menuDate: date, mealType, items, notes: notes || undefined }),
    onSuccess: async () => {
      setItems('');
      setNotes('');
      await queryClient.invalidateQueries({ queryKey: ['owner-food-menus'] });
    }
  });
  const visibleMenus = useMemo(() => (menusQuery.data ?? []).filter((menu) => (!propertyId || menu.propertyId === Number(propertyId)) && menu.menuDate === date), [menusQuery.data, propertyId, date]);
  const error = propertiesQuery.error ?? menusQuery.error ?? feedbackQuery.error ?? createMenu.error;
  return (
    <OwnerShell title="Food Menu" eyebrow="Operations">
      <div className="owner-stack">
        <FormMessage message={error ? getApiErrorMessage(error, 'Unable to load food module') : null} />
        <FormMessage tone="success" message={createMenu.isSuccess ? 'Food menu saved.' : null} />
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Menu entry</p><h2>Add meal</h2></div></div>
          <form className="form-grid two-column" onSubmit={(event) => { event.preventDefault(); createMenu.mutate(); }}>
            <label>Property<select value={propertyId} onChange={(event) => setPropertyId(event.target.value)} required><option value="">Select property</option>{(propertiesQuery.data ?? []).map((property) => <option value={property.id} key={property.id}>{property.name}</option>)}</select></label>
            <label>Date<input type="date" value={date} onChange={(event) => setDate(event.target.value)} required /></label>
            <label>Meal<select value={mealType} onChange={(event) => setMealType(event.target.value as MealType)}>{mealOrder.map((meal) => <option value={meal} key={meal}>{label(meal)}</option>)}</select></label>
            <label className="form-span">Items<input value={items} onChange={(event) => setItems(event.target.value)} placeholder="Poha, tea, fruit" required /></label>
            <label className="form-span">Notes<textarea rows={3} value={notes} onChange={(event) => setNotes(event.target.value)} /></label>
            <button className="primary-button" type="submit" disabled={!propertyId || !items.trim() || createMenu.isPending}>Save Menu</button>
          </form>
        </section>
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">{date}</p><h2>Menus</h2></div></div>
          {menusQuery.isLoading ? <div className="owner-skeleton-card" /> : null}
          {!menusQuery.isLoading && visibleMenus.length === 0 ? <EmptyState title="No menu has been added for this date." /> : null}
          <div className="owner-card-grid">
            {visibleMenus.map((menu) => <article className="owner-list-row" key={menu.id}><div><span>{menu.propertyName}</span><strong>{label(menu.mealType)}</strong><p className="muted-copy">{menu.items}</p></div></article>)}
          </div>
        </section>
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Feedback</p><h2>Tenant food feedback</h2></div></div>
          {(feedbackQuery.data ?? []).length === 0 ? <EmptyState title="No food feedback yet." /> : null}
          <div className="table-wrap"><table><thead><tr><th>Tenant</th><th>PG</th><th>Date</th><th>Meal</th><th>Rating</th><th>Comment</th></tr></thead><tbody>
            {(feedbackQuery.data ?? []).map((feedback) => <tr key={feedback.id}><td>{feedback.tenantName}</td><td>{feedback.propertyName}</td><td>{feedback.menuDate}</td><td>{label(feedback.mealType)}</td><td><StatusBadge status={`${feedback.rating}/5`} /></td><td>{feedback.comment ?? '-'}</td></tr>)}
          </tbody></table></div>
        </section>
      </div>
    </OwnerShell>
  );
}
