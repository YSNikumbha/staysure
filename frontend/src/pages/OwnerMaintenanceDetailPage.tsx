import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ownerMaintenanceApi } from '../api/operations.api';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, formatDateTime } from '../utils/operations';

export default function OwnerMaintenanceDetailPage() {
  const { id } = useParams();
  const taskId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const query = useQuery({ queryKey: ['owner-maintenance-task', taskId], queryFn: () => ownerMaintenanceApi.get(taskId), enabled: Number.isFinite(taskId) });
  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['owner-maintenance-task', taskId] });
    await queryClient.invalidateQueries({ queryKey: ['owner-maintenance'] });
  };
  const start = useMutation({ mutationFn: () => ownerMaintenanceApi.start(taskId, remarks), onSuccess: refresh });
  const complete = useMutation({ mutationFn: () => ownerMaintenanceApi.complete(taskId, remarks), onSuccess: refresh });
  const cancel = useMutation({ mutationFn: () => ownerMaintenanceApi.cancel(taskId, remarks), onSuccess: refresh });
  if (query.isLoading) return <OwnerShell title="Maintenance" eyebrow="Operations"><div className="owner-skeleton-card" /></OwnerShell>;
  if (query.isError || !query.data) return <OwnerShell title="Maintenance" eyebrow="Operations"><div className="route-state">{getApiErrorMessage(query.error, 'Unable to load task')}</div></OwnerShell>;
  const task = query.data;
  const error = start.error ?? complete.error ?? cancel.error;
  return (
    <OwnerShell title={task.taskNumber} eyebrow="Maintenance" actions={<div className="action-row"><Link className="secondary-link compact-button" to="/owner/maintenance">Back</Link><StatusBadge status={task.status} /></div>}>
      <div className="owner-stack">
        <FormMessage message={error ? getApiErrorMessage(error, 'Unable to update task') : null} />
        <section className="surface owner-panel">
          <h2>{task.title}</h2>
          <p className="muted-copy">{task.description}</p>
          <div className="detail-grid">
            <div><span>PG</span><strong>{task.propertyName}</strong></div>
            <div><span>Room</span><strong>{task.roomNumber ?? '-'}</strong></div>
            <div><span>Complaint</span><strong>{task.complaintNumber ?? '-'}</strong></div>
            <div><span>Assigned To</span><strong>{task.assignedToText ?? '-'}</strong></div>
            <div><span>Scheduled</span><strong>{formatDate(task.scheduledDate)}</strong></div>
            <div><span>Completed</span><strong>{formatDateTime(task.completedAt)}</strong></div>
          </div>
          <label>Remarks<textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} /></label>
          <div className="action-row">
            {(task.status === 'PENDING' || task.status === 'SCHEDULED') ? <button className="primary-button" type="button" onClick={() => start.mutate()}>Start</button> : null}
            {(task.status === 'IN_PROGRESS' || task.status === 'SCHEDULED') ? <button className="primary-button" type="button" onClick={() => complete.mutate()}>Complete</button> : null}
            {(task.status !== 'COMPLETED' && task.status !== 'CANCELLED') ? <button className="danger-button" type="button" onClick={() => cancel.mutate()}>Cancel</button> : null}
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}
