import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ownerBookingApi } from '../api/booking.api';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';

export default function OwnerBookingsPage() {
  const query = useQuery({ queryKey: ['owner-bookings'], queryFn: ownerBookingApi.list });

  if (query.isLoading) return <div className="route-state">Loading owner bookings</div>;
  if (query.isError) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load owner bookings')}</div>;

  const bookings = query.data ?? [];

  return (
    <div className="stack">
      <PageHeader eyebrow="Owner" title="Booking Requests" actions={<Link className="secondary-link" to="/owner/tenants">Tenants</Link>} />
      <section className="surface">
        {bookings.length === 0 ? <div className="empty-state">No booking requests yet.</div> : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Booking</th><th>User</th><th>PG</th><th>Room / Bed</th><th>Status</th><th className="table-actions">Actions</th></tr></thead>
              <tbody>
                {bookings.map((booking) => (
                  <tr key={booking.id}>
                    <td>{booking.bookingNumber}</td>
                    <td>{booking.user.firstName} {booking.user.lastName}<br /><span className="muted-copy">{booking.user.phone}</span></td>
                    <td>{booking.property.name}</td>
                    <td>Room {booking.room.roomNumber} / {booking.bed.bedLabel || booking.bed.bedNumber}</td>
                    <td><StatusBadge status={booking.status} /></td>
                    <td className="table-actions"><Link to={`/owner/bookings/${booking.id}`}>Review</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
