import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { bookingApi } from '../api/booking.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import type { BookingStatus } from '../types/booking';
import { getApiErrorMessage } from '../utils/apiError';
import { toAssetUrl } from '../utils/assets';

const cancellable: BookingStatus[] = ['REQUESTED', 'AWAITING_KYC', 'KYC_VERIFICATION', 'AWAITING_DEPOSIT', 'AWAITING_AGREEMENT'];

export default function BookingDetailPage() {
  const { id } = useParams();
  const bookingId = Number(id);
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['booking', bookingId], queryFn: () => bookingApi.getMine(bookingId), enabled: Number.isFinite(bookingId) });
  const cancel = useMutation({
    mutationFn: () => bookingApi.cancel(bookingId, 'Cancelled by user'),
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['booking', bookingId] })
  });
  const acceptAgreement = useMutation({
    mutationFn: () => bookingApi.acceptAgreement(bookingId),
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['booking', bookingId] })
  });

  if (query.isLoading) return <div className="route-state">Loading booking</div>;
  if (query.isError || !query.data) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load booking')}</div>;

  const booking = query.data;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Booking details"
        title={booking.bookingNumber}
        actions={<div className="action-row"><Link className="secondary-link" to="/bookings">Back</Link><StatusBadge status={booking.status} /></div>}
      />
      <FormMessage message={cancel.isError ? getApiErrorMessage(cancel.error, 'Unable to cancel booking') : null} />
      <FormMessage message={acceptAgreement.isError ? getApiErrorMessage(acceptAgreement.error, 'Unable to accept agreement') : null} />

      <section className="surface detail-grid">
        <div><span>PG</span><strong>{booking.property.name}</strong></div>
        <div><span>Location</span><strong>{booking.property.area}, {booking.property.city}</strong></div>
        <div><span>Room</span><strong>{booking.room.roomNumber} ({booking.room.sharingType.replaceAll('_', ' ')})</strong></div>
        <div><span>Bed</span><strong>{booking.bed.bedLabel || booking.bed.bedNumber}</strong></div>
        <div><span>Move-in</span><strong>{booking.moveInDate}</strong></div>
        <div><span>Expected move-out</span><strong>{booking.expectedMoveOutDate ?? 'Not set'}</strong></div>
        <div><span>Monthly rent</span><strong>Rs {Number(booking.monthlyRent).toLocaleString()}</strong></div>
        <div><span>Security deposit</span><strong>Rs {Number(booking.securityDeposit).toLocaleString()}</strong></div>
      </section>

      <section className="surface">
        <div className="section-heading">
          <h2>Next Steps</h2>
          <div className="action-row">
            {['AWAITING_KYC', 'KYC_VERIFICATION'].includes(booking.status) ? <Link className="primary-link" to={`/bookings/${booking.id}/kyc`}>Manage KYC</Link> : null}
            {booking.agreement?.status === 'ISSUED' ? <button className="primary-button" type="button" onClick={() => acceptAgreement.mutate()}>Accept Agreement</button> : null}
            {cancellable.includes(booking.status) ? <button className="danger-button" type="button" onClick={() => cancel.mutate()}>Cancel Booking</button> : null}
          </div>
        </div>
        <div className="detail-grid">
          <div><span>KYC</span><strong>{booking.documents.some((doc) => doc.verificationStatus === 'VERIFIED') ? 'In progress' : 'Pending'}</strong></div>
          <div><span>Deposit</span><strong>{booking.deposit?.status ?? 'Not started'}</strong></div>
          <div><span>Agreement</span><strong>{booking.agreement?.status ?? 'Not issued'}</strong></div>
          <div><span>Tenant</span><strong>{booking.tenant?.status ?? 'Not created'}</strong></div>
        </div>
      </section>

      {booking.agreement ? (
        <section className="surface">
          <h2>Agreement</h2>
          <div className="detail-grid">
            <div><span>Number</span><strong>{booking.agreement.agreementNumber}</strong></div>
            <div><span>Status</span><strong><StatusBadge status={booking.agreement.status} /></strong></div>
            <div><span>Start</span><strong>{booking.agreement.startDate}</strong></div>
            <div><span>End</span><strong>{booking.agreement.endDate ?? 'Not set'}</strong></div>
          </div>
          {booking.agreement.terms ? <p className="muted-copy">{booking.agreement.terms}</p> : null}
          {booking.agreement.documentUrl ? <a className="secondary-link" href={toAssetUrl(booking.agreement.documentUrl)} target="_blank" rel="noreferrer">View PDF</a> : null}
        </section>
      ) : null}

      <section className="surface">
        <h2>Status History</h2>
        <div className="timeline-list">
          {booking.history.map((item) => (
            <div className="timeline-item" key={item.id}>
              <StatusBadge status={item.newStatus} />
              <span>{new Date(item.createdAt).toLocaleString()}</span>
              {item.remarks ? <p>{item.remarks}</p> : null}
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
