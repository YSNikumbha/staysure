import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { complaintsApi } from '../api/operations.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDateTime, label } from '../utils/operations';

export default function ComplaintDetailPage() {
  const { id } = useParams();
  const complaintId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const [comment, setComment] = useState('');
  const query = useQuery({ queryKey: ['complaint', complaintId], queryFn: () => complaintsApi.get(complaintId), enabled: Number.isFinite(complaintId) });
  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['complaint', complaintId] });
    await queryClient.invalidateQueries({ queryKey: ['complaints'] });
  };
  const cancel = useMutation({ mutationFn: () => complaintsApi.cancel(complaintId, remarks), onSuccess: refresh });
  const reopen = useMutation({ mutationFn: () => complaintsApi.reopen(complaintId, remarks), onSuccess: refresh });
  const close = useMutation({ mutationFn: () => complaintsApi.close(complaintId, remarks), onSuccess: refresh });
  const addComment = useMutation({ mutationFn: () => complaintsApi.comment(complaintId, comment), onSuccess: async () => { setComment(''); await refresh(); } });

  if (query.isLoading) return <div className="route-state">Loading complaint</div>;
  if (query.isError || !query.data) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load complaint')}</div>;
  const complaint = query.data;
  const error = cancel.error ?? reopen.error ?? close.error ?? addComment.error;

  return (
    <div className="stack">
      <PageHeader eyebrow={complaint.complaintNumber} title={complaint.title} actions={<div className="action-row"><Link className="secondary-link" to="/complaints">Back</Link><StatusBadge status={complaint.status} /></div>} />
      <FormMessage message={error ? getApiErrorMessage(error, 'Unable to update complaint') : null} />

      <section className="surface detail-grid">
        <div><span>PG</span><strong>{complaint.propertyName}</strong></div>
        <div><span>Room</span><strong>{complaint.roomNumber ?? '-'}</strong></div>
        <div><span>Category</span><strong>{label(complaint.category)}</strong></div>
        <div><span>Priority</span><strong><StatusBadge status={complaint.priority} /></strong></div>
      </section>

      <section className="surface">
        <div className="section-heading"><div><p className="eyebrow">Description</p><h2>Issue details</h2></div></div>
        <p className="muted-copy">{complaint.description}</p>
        <label>Remarks<textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} /></label>
        <div className="action-row action-row-spaced">
          {complaint.status === 'OPEN' ? <button className="danger-button" type="button" onClick={() => cancel.mutate()} disabled={cancel.isPending}>Cancel Complaint</button> : null}
          {complaint.status === 'RESOLVED' ? <button className="primary-button" type="button" onClick={() => close.mutate()} disabled={close.isPending}>Close</button> : null}
          {complaint.status === 'RESOLVED' ? <button className="secondary-button" type="button" onClick={() => reopen.mutate()} disabled={reopen.isPending}>Reopen</button> : null}
        </div>
      </section>

      <section className="surface">
        <div className="section-heading"><div><p className="eyebrow">Updates</p><h2>Comments</h2></div></div>
        <form className="inline-form" onSubmit={(event) => { event.preventDefault(); addComment.mutate(); }}>
          <label>Comment<input value={comment} onChange={(event) => setComment(event.target.value)} required /></label>
          <button className="primary-button" type="submit" disabled={addComment.isPending || !comment.trim()}>Add Comment</button>
        </form>
        <div className="timeline-list">
          {complaint.comments.map((item) => (
            <div className="timeline-item" key={item.id}><div><strong>{item.authorName}</strong><p>{item.comment} · {formatDateTime(item.createdAt)}</p></div></div>
          ))}
          {complaint.comments.length === 0 ? <p className="muted-copy">No comments yet.</p> : null}
        </div>
      </section>

      <section className="surface">
        <div className="section-heading"><div><p className="eyebrow">Timeline</p><h2>Status History</h2></div></div>
        <div className="timeline-list">
          {complaint.history.map((item) => (
            <div className="timeline-item" key={item.id}><div><strong>{label(item.newStatus)}</strong><p>{item.remarks || 'Status updated'} · {formatDateTime(item.createdAt)}</p></div></div>
          ))}
        </div>
      </section>
    </div>
  );
}
