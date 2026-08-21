import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { foodApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import type { MealType } from '../types/operations';
import { getApiErrorMessage } from '../utils/apiError';
import { label, mealOrder, todayIso } from '../utils/operations';

export default function FoodPage() {
  const queryClient = useQueryClient();
  const [date, setDate] = useState(todayIso());
  const [ratings, setRatings] = useState<Record<MealType, number>>({ BREAKFAST: 5, LUNCH: 5, SNACKS: 5, DINNER: 5 });
  const [comments, setComments] = useState<Record<MealType, string>>({ BREAKFAST: '', LUNCH: '', SNACKS: '', DINNER: '' });
  const query = useQuery({ queryKey: ['food-menu', date], queryFn: () => foodApi.byDate(date) });
  const feedback = useMutation({
    mutationFn: (mealType: MealType) => foodApi.feedback({ menuDate: date, mealType, rating: ratings[mealType], comment: comments[mealType] || undefined }),
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['food-menu', date] })
  });
  const menusByMeal = useMemo(() => new Map((query.data ?? []).map((menu) => [menu.mealType, menu])), [query.data]);
  const error = query.error ?? feedback.error;
  return (
    <div className="stack">
      <PageHeader eyebrow="My PG" title="Food Menu" />
      <FormMessage message={error ? getApiErrorMessage(error, 'Unable to load food menu') : null} />
      <FormMessage tone="success" message={feedback.isSuccess ? 'Feedback submitted.' : null} />
      <section className="surface">
        <label>Date<input type="date" value={date} onChange={(event) => setDate(event.target.value)} /></label>
      </section>
      <section className="owner-card-grid">
        {query.isLoading ? <div className="route-state">Loading food menu</div> : null}
        {!query.isLoading && (query.data ?? []).length === 0 ? <EmptyState title="No menu has been added for this date." /> : null}
        {mealOrder.map((mealType) => {
          const menu = menusByMeal.get(mealType);
          if (!menu) return null;
          return (
            <article className="surface owner-panel" key={mealType}>
              <div className="section-heading"><div><p className="eyebrow">{label(mealType)}</p><h2>{menu.items}</h2></div></div>
              {menu.notes ? <p className="muted-copy">{menu.notes}</p> : null}
              <form className="inline-form" onSubmit={(event) => { event.preventDefault(); feedback.mutate(mealType); }}>
                <label>Rating<select value={ratings[mealType]} onChange={(event) => setRatings((current) => ({ ...current, [mealType]: Number(event.target.value) }))}>{[1, 2, 3, 4, 5].map((rating) => <option value={rating} key={rating}>{rating}</option>)}</select></label>
                <label>Comment<input value={comments[mealType]} onChange={(event) => setComments((current) => ({ ...current, [mealType]: event.target.value }))} /></label>
                <button className="primary-button" type="submit" disabled={feedback.isPending}>Submit Feedback</button>
              </form>
            </article>
          );
        })}
      </section>
    </div>
  );
}
