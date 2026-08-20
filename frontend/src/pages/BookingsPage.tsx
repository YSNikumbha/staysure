import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { bookingApi } from '../api/booking.api';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';

export default function BookingsPage() {
  const query = useQuery({ queryKey: ['my-bookings'], queryFn: bookingApi.listMine });

  if (query.isLoading) return <div className="route-state">Loading bookings</div>;
  if (query.isError) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load bookings')}</div>;

  const bookings = query.data ?? [];

  return (
    <div className="stack">
      <PageHeader eyebrow="My bookings" title="Booking Requests" actions={<Link className="secondary-link" to="/my-pg">My PG</Link>} />
      <section className="surface">
        {bookings.length === 0 ? (
          <div className="empty-state">No booking requests yet.</div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Booking</th>
                  <th>PG</th>
                  <th>Room / Bed</th>
                  <th>Move-in</th>
                  <th>Status</th>
                  <th className="table-actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((booking) => (
                  <tr key={booking.id}>
                    <td>{booking.bookingNumber}</td>
                    <td>{booking.property.name}<br /><span className="muted-copy">{booking.property.area}, {booking.property.city}</span></td>
                    <td>Room {booking.room.roomNumber} / {booking.bed.bedLabel || booking.bed.bedNumber}</td>
                    <td>{booking.moveInDate}</td>
                    <td><StatusBadge status={booking.status} /></td>
                    <td className="table-actions"><Link to={`/bookings/${booking.id}`}>View</Link></td>
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
