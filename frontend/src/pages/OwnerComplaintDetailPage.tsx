import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ownerComplaintsApi, ownerMaintenanceApi } from '../api/operations.api';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDateTime, label } from '../utils/operations';

export default function OwnerComplaintDetailPage() {
  const { id } = useParams();
  const complaintId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const [comment, setComment] = useState('');
  const [assignedToText, setAssignedToText] = useState('');
  const query = useQuery({ queryKey: ['owner-complaint', complaintId], queryFn: () => ownerComplaintsApi.get(complaintId), enabled: Number.isFinite(complaintId) });
  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['owner-complaint', complaintId] });
    await queryClient.invalidateQueries({ queryKey: ['owner-complaints'] });
  };
  const acknowledge = useMutation({ mutationFn: () => ownerComplaintsApi.acknowledge(complaintId, remarks), onSuccess: refresh });
  const start = useMutation({ mutationFn: () => ownerComplaintsApi.start(complaintId, remarks), onSuccess: refresh });
  const resolve = useMutation({ mutationFn: () => ownerComplaintsApi.resolve(complaintId, remarks), onSuccess: refresh });
  const close = useMutation({ mutationFn: () => ownerComplaintsApi.close(complaintId, remarks), onSuccess: refresh });
  const addComment = useMutation({ mutationFn: () => ownerComplaintsApi.comment(complaintId, comment), onSuccess: async () => { setComment(''); await refresh(); } });
  const createTask = useMutation({
    mutationFn: () => ownerMaintenanceApi.create({
      propertyId: query.data!.propertyId,
      complaintId,
      title: query.data!.title,
      description: query.data!.description,
      priority: query.data!.priority,
      assignedToText: assignedToText || undefined,
      remarks: remarks || undefined
    }),
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['owner-maintenance'] })
  });

  if (query.isLoading) return <OwnerShell title="Complaint" eyebrow="Operations"><div className="owner-skeleton-card" /></OwnerShell>;
  if (query.isError || !query.data) return <OwnerShell title="Complaint" eyebrow="Operations"><div className="route-state">{getApiErrorMessage(query.error, 'Unable to load complaint')}</div></OwnerShell>;
  const complaint = query.data;
  const error = acknowledge.error ?? start.error ?? resolve.error ?? close.error ?? addComment.error ?? createTask.error;

  return (
    <OwnerShell title={complaint.complaintNumber} eyebrow="Complaint review" actions={<div className="action-row"><Link className="secondary-link compact-button" to="/owner/complaints">Back</Link><StatusBadge status={complaint.status} /></div>}>
      <div className="owner-stack">
        <FormMessage message={error ? getApiErrorMessage(error, 'Unable to update complaint') : null} />
        <FormMessage tone="success" message={createTask.isSuccess ? 'Maintenance task created.' : null} />
        <section className="surface owner-panel">
          <div className="detail-grid">
            <div><span>Tenant</span><strong>{complaint.tenantName}</strong></div>
            <div><span>PG</span><strong>{complaint.propertyName}</strong></div>
            <div><span>Room</span><strong>{complaint.roomNumber ?? '-'}</strong></div>
            <div><span>Priority</span><strong><StatusBadge status={complaint.priority} /></strong></div>
            <div><span>Category</span><strong>{label(complaint.category)}</strong></div>
            <div><span>Created</span><strong>{formatDateTime(complaint.createdAt)}</strong></div>
          </div>
          <div>
            <p className="eyebrow">Description</p>
            <p className="muted-copy">{complaint.description}</p>
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Actions</p><h2>Lifecycle</h2></div></div>
          <label>Remarks<textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} /></label>
          <div className="action-row">
            {complaint.status === 'OPEN' ? <button className="primary-button" type="button" onClick={() => acknowledge.mutate()}>Acknowledge</button> : null}
            {(complaint.status === 'ACKNOWLEDGED' || complaint.status === 'REOPENED') ? <button className="primary-button" type="button" onClick={() => start.mutate()}>Start Work</button> : null}
            {complaint.status === 'IN_PROGRESS' ? <button className="primary-button" type="button" onClick={() => resolve.mutate()}>Resolve</button> : null}
            {complaint.status === 'RESOLVED' ? <button className="secondary-button" type="button" onClick={() => close.mutate()}>Close</button> : null}
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Maintenance</p><h2>Create task from complaint</h2></div></div>
          <form className="inline-form" onSubmit={(event) => { event.preventDefault(); createTask.mutate(); }}>
            <label>Assigned To<input value={assignedToText} onChange={(event) => setAssignedToText(event.target.value)} placeholder="Plumber, electrician, staff name" /></label>
            <button className="secondary-button" type="submit" disabled={createTask.isPending}>Create Maintenance Task</button>
          </form>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Updates</p><h2>Comments</h2></div></div>
          <form className="inline-form" onSubmit={(event) => { event.preventDefault(); addComment.mutate(); }}>
            <label>Comment<input value={comment} onChange={(event) => setComment(event.target.value)} required /></label>
            <button className="primary-button" type="submit" disabled={!comment.trim() || addComment.isPending}>Add Update</button>
          </form>
          <div className="timeline-list">
            {complaint.comments.map((item) => <div className="timeline-item" key={item.id}><div><strong>{item.authorName}</strong><p>{item.comment} · {formatDateTime(item.createdAt)}</p></div></div>)}
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Timeline</p><h2>Status history</h2></div></div>
          <div className="timeline-list">
            {complaint.history.map((item) => <div className="timeline-item" key={item.id}><div><strong>{label(item.newStatus)}</strong><p>{item.remarks || 'Status updated'} · {formatDateTime(item.createdAt)}</p></div></div>)}
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}
