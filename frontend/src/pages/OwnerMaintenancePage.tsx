import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ownerMaintenanceApi } from '../api/operations.api';
import { propertyApi } from '../api/property.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { OperationalPriority } from '../types/operations';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, label } from '../utils/operations';

const priorities: OperationalPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export default function OwnerMaintenancePage() {
  const queryClient = useQueryClient();
  const [propertyId, setPropertyId] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<OperationalPriority>('MEDIUM');
  const tasksQuery = useQuery({ queryKey: ['owner-maintenance'], queryFn: ownerMaintenanceApi.list });
  const propertiesQuery = useQuery({ queryKey: ['owner-properties'], queryFn: propertyApi.listProperties });
  const createTask = useMutation({
    mutationFn: () => ownerMaintenanceApi.create({ propertyId: Number(propertyId), title, description, priority }),
    onSuccess: async () => {
      setTitle('');
      setDescription('');
      await queryClient.invalidateQueries({ queryKey: ['owner-maintenance'] });
    }
  });
  const tasks = tasksQuery.data ?? [];

  return (
    <OwnerShell title="Maintenance" eyebrow="Operations">
      <div className="owner-stack">
        <FormMessage message={(tasksQuery.error || propertiesQuery.error || createTask.error) ? getApiErrorMessage(tasksQuery.error ?? propertiesQuery.error ?? createTask.error, 'Unable to load maintenance') : null} />
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">New task</p><h2>Create maintenance task</h2></div></div>
          <form className="form-grid two-column" onSubmit={(event) => { event.preventDefault(); createTask.mutate(); }}>
            <label>Property<select value={propertyId} onChange={(event) => setPropertyId(event.target.value)} required><option value="">Select property</option>{(propertiesQuery.data ?? []).map((property) => <option value={property.id} key={property.id}>{property.name}</option>)}</select></label>
            <label>Priority<select value={priority} onChange={(event) => setPriority(event.target.value as OperationalPriority)}>{priorities.map((item) => <option value={item} key={item}>{label(item)}</option>)}</select></label>
            <label className="form-span">Title<input value={title} onChange={(event) => setTitle(event.target.value)} required /></label>
            <label className="form-span">Description<textarea rows={3} value={description} onChange={(event) => setDescription(event.target.value)} required /></label>
            <button className="primary-button" type="submit" disabled={!propertyId || !title.trim() || !description.trim() || createTask.isPending}>Create Task</button>
          </form>
        </section>
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Tasks</p><h2>{tasks.length} maintenance task{tasks.length === 1 ? '' : 's'}</h2></div></div>
          {tasksQuery.isLoading ? <div className="owner-skeleton-card" /> : null}
          {!tasksQuery.isLoading && tasks.length === 0 ? <EmptyState title="No maintenance tasks yet." description="Create a task manually or from a complaint." /> : null}
          {tasks.length > 0 ? (
            <div className="table-wrap"><table><thead><tr><th>Task</th><th>PG</th><th>Priority</th><th>Status</th><th>Scheduled</th><th className="table-actions">Action</th></tr></thead><tbody>
              {tasks.map((task) => <tr key={task.id}><td>{task.taskNumber}<br /><span className="muted-copy">{task.title}</span></td><td>{task.propertyName}</td><td><StatusBadge status={task.priority} /></td><td><StatusBadge status={task.status} /></td><td>{formatDate(task.scheduledDate)}</td><td className="table-actions"><Link to={`/owner/maintenance/${task.id}`}>View</Link></td></tr>)}
            </tbody></table></div>
          ) : null}
        </section>
      </div>
    </OwnerShell>
  );
}
