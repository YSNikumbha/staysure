import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { visitorsApi } from '../api/operations.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, formatDateTime } from '../utils/operations';

export default function VisitorDetailPage() {
  const { id } = useParams();
  const visitorId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const query = useQuery({ queryKey: ['visitor', visitorId], queryFn: () => visitorsApi.get(visitorId), enabled: Number.isFinite(visitorId) });
  const cancel = useMutation({ mutationFn: () => visitorsApi.cancel(visitorId, remarks), onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['visitor', visitorId] }); await queryClient.invalidateQueries({ queryKey: ['visitors'] }); } });
  if (query.isLoading) return <div className="route-state">Loading visitor</div>;
  if (query.isError || !query.data) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load visitor')}</div>;
  const visitor = query.data;
  return (
    <div className="stack">
      <PageHeader eyebrow={visitor.visitorNumber} title={visitor.visitorName} actions={<div className="action-row"><Link className="secondary-link" to="/visitors">Back</Link><StatusBadge status={visitor.status} /></div>} />
      <FormMessage message={cancel.isError ? getApiErrorMessage(cancel.error, 'Unable to cancel visitor') : null} />
      <section className="surface detail-grid">
        <div><span>Relationship</span><strong>{visitor.relationship}</strong></div>
        <div><span>Phone</span><strong>{visitor.visitorPhone}</strong></div>
        <div><span>Visit Date</span><strong>{formatDate(visitor.visitDate)}</strong></div>
        <div><span>Expected Time</span><strong>{visitor.expectedArrivalTime} - {visitor.expectedDepartureTime}</strong></div>
        <div><span>Actual Arrival</span><strong>{formatDateTime(visitor.actualArrivalTime)}</strong></div>
        <div><span>Actual Departure</span><strong>{formatDateTime(visitor.actualDepartureTime)}</strong></div>
      </section>
      <section className="surface">
        <p className="muted-copy">{visitor.purpose}</p>
        {(visitor.status === 'REQUESTED' || visitor.status === 'APPROVED') ? (
          <div className="form-grid">
            <label>Remarks<textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} /></label>
            <button className="danger-button" type="button" onClick={() => cancel.mutate()} disabled={cancel.isPending}>Cancel Request</button>
          </div>
        ) : null}
      </section>
    </div>
  );
}
