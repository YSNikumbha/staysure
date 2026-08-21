import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ownerVisitorsApi } from '../api/operations.api';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, formatDateTime } from '../utils/operations';

export default function OwnerVisitorDetailPage() {
  const { id } = useParams();
  const visitorId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const query = useQuery({ queryKey: ['owner-visitor', visitorId], queryFn: () => ownerVisitorsApi.get(visitorId), enabled: Number.isFinite(visitorId) });
  const refresh = async () => { await queryClient.invalidateQueries({ queryKey: ['owner-visitor', visitorId] }); await queryClient.invalidateQueries({ queryKey: ['owner-visitors'] }); };
  const approve = useMutation({ mutationFn: () => ownerVisitorsApi.approve(visitorId), onSuccess: refresh });
  const reject = useMutation({ mutationFn: () => ownerVisitorsApi.reject(visitorId, remarks), onSuccess: refresh });
  const checkIn = useMutation({ mutationFn: () => ownerVisitorsApi.checkIn(visitorId), onSuccess: refresh });
  const checkOut = useMutation({ mutationFn: () => ownerVisitorsApi.checkOut(visitorId), onSuccess: refresh });
  if (query.isLoading) return <OwnerShell title="Visitor" eyebrow="Operations"><div className="owner-skeleton-card" /></OwnerShell>;
  if (query.isError || !query.data) return <OwnerShell title="Visitor" eyebrow="Operations"><div className="route-state">{getApiErrorMessage(query.error, 'Unable to load visitor')}</div></OwnerShell>;
  const visitor = query.data;
  const error = approve.error ?? reject.error ?? checkIn.error ?? checkOut.error;
  return (
    <OwnerShell title={visitor.visitorName} eyebrow={visitor.visitorNumber} actions={<div className="action-row"><Link className="secondary-link compact-button" to="/owner/visitors">Back</Link><StatusBadge status={visitor.status} /></div>}>
      <div className="owner-stack">
        <FormMessage message={error ? getApiErrorMessage(error, 'Unable to update visitor') : null} />
        <section className="surface owner-panel">
          <div className="detail-grid">
            <div><span>Tenant</span><strong>{visitor.tenantName}</strong></div>
            <div><span>PG</span><strong>{visitor.propertyName}</strong></div>
            <div><span>Phone</span><strong>{visitor.visitorPhone}</strong></div>
            <div><span>Relationship</span><strong>{visitor.relationship}</strong></div>
            <div><span>Visit Date</span><strong>{formatDate(visitor.visitDate)}</strong></div>
            <div><span>Expected</span><strong>{visitor.expectedArrivalTime} - {visitor.expectedDepartureTime}</strong></div>
            <div><span>Arrival</span><strong>{formatDateTime(visitor.actualArrivalTime)}</strong></div>
            <div><span>Departure</span><strong>{formatDateTime(visitor.actualDepartureTime)}</strong></div>
          </div>
          <p className="muted-copy">{visitor.purpose}</p>
          <label>Rejection/Action Remarks<textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} /></label>
          <div className="action-row">
            {visitor.status === 'REQUESTED' ? <button className="primary-button" type="button" onClick={() => approve.mutate()}>Approve</button> : null}
            {visitor.status === 'REQUESTED' ? <button className="danger-button" type="button" onClick={() => reject.mutate()}>Reject</button> : null}
            {visitor.status === 'APPROVED' ? <button className="primary-button" type="button" onClick={() => checkIn.mutate()}>Check In</button> : null}
            {visitor.status === 'CHECKED_IN' ? <button className="primary-button" type="button" onClick={() => checkOut.mutate()}>Check Out</button> : null}
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}
