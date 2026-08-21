import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { bookingApi } from '../api/booking.api';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';

export default function MyPgPage() {
  const query = useQuery({ queryKey: ['my-pg'], queryFn: bookingApi.myPg, retry: false });

  if (query.isLoading) return <div className="route-state">Loading current PG</div>;
  if (query.isError) {
    return (
      <div className="stack">
        <PageHeader eyebrow="My PG" title="Current Stay" />
        <section className="surface"><div className="empty-state">{getApiErrorMessage(query.error, "You don't have an active PG yet.")}</div></section>
      </div>
    );
  }
  const booking = query.data!;
  return (
    <div className="stack">
      <PageHeader eyebrow="My PG" title={booking.property.name} actions={<Link className="secondary-link" to={`/bookings/${booking.id}`}>Booking</Link>} />
      <section className="surface detail-grid">
        <div><span>Status</span><strong><StatusBadge status={booking.status} /></strong></div>
        <div><span>Location</span><strong>{booking.property.area}, {booking.property.city}</strong></div>
        <div><span>Room</span><strong>{booking.room.roomNumber}</strong></div>
        <div><span>Bed</span><strong>{booking.bed.bedLabel || booking.bed.bedNumber}</strong></div>
        <div><span>Joining Date</span><strong>{booking.tenant?.joiningDate ? new Date(booking.tenant.joiningDate).toLocaleString() : 'Upcoming'}</strong></div>
        <div><span>Monthly Rent</span><strong>Rs {Number(booking.monthlyRent).toLocaleString()}</strong></div>
        <div><span>Security Deposit</span><strong>Rs {Number(booking.securityDeposit).toLocaleString()}</strong></div>
        <div><span>Expected Checkout</span><strong>{booking.expectedMoveOutDate ?? '-'}</strong></div>
      </section>
      <section className="surface">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Quick actions</p>
            <h2>Manage your stay</h2>
          </div>
        </div>
        <div className="action-row">
          <Link className="secondary-link" to="/rent">Rent</Link>
          <Link className="secondary-link" to="/complaints/new">Raise Complaint</Link>
          <Link className="secondary-link" to="/notices">Notices</Link>
          <Link className="secondary-link" to="/food">Food Menu</Link>
          <Link className="secondary-link" to="/visitors">Visitors</Link>
        </div>
      </section>
    </div>
  );
}
