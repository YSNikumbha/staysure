import { useEffect, useState } from 'react';
import { bookingApi } from '../api/booking.api';
import type { Booking, BookingStatus } from '../types/booking';

const STATUS_LABELS: Record<BookingStatus, string> = {
  REQUESTED: 'Requested',
  APPROVED: 'Approved',
  AWAITING_KYC: 'Awaiting KYC',
  KYC_VERIFICATION: 'KYC Verification',
  AWAITING_DEPOSIT: 'Awaiting Deposit',
  AWAITING_AGREEMENT: 'Awaiting Agreement',
  CONFIRMED: 'Confirmed',
  REJECTED: 'Rejected',
  CANCELLED: 'Cancelled',
  CHECKED_IN: 'Checked In',
  CHECKED_OUT: 'Checked Out'
};

const STATUS_COLORS: Record<BookingStatus, string> = {
  REQUESTED: 'bg-blue-100 text-blue-800',
  APPROVED: 'bg-green-100 text-green-800',
  AWAITING_KYC: 'bg-yellow-100 text-yellow-800',
  KYC_VERIFICATION: 'bg-yellow-100 text-yellow-800',
  AWAITING_DEPOSIT: 'bg-orange-100 text-orange-800',
  AWAITING_AGREEMENT: 'bg-purple-100 text-purple-800',
  CONFIRMED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
  CHECKED_IN: 'bg-green-100 text-green-800',
  CHECKED_OUT: 'bg-gray-100 text-gray-800'
};

export default function BookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadBookings();
  }, []);

  const loadBookings = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await bookingApi.list();
      setBookings(data);
    } catch (err) {
      setError('Failed to load bookings');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    try {
      await bookingApi.cancel(id);
      await loadBookings();
    } catch (err) {
      alert('Failed to cancel booking');
      console.error(err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">Loading bookings...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-red-600">{error}</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">My Bookings</h1>

      {bookings.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">No bookings yet</p>
        </div>
      ) : (
        <div className="bg-white shadow-md rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Booking #</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">PG</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Room</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Bed</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Move-in</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {bookings.map((booking) => (
                <tr key={booking.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {booking.bookingNumber}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {booking.propertyName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {booking.roomNumber}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {booking.bedNumber}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {new Date(booking.moveInDate).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${STATUS_COLORS[booking.status]}`}>
                      {STATUS_LABELS[booking.status]}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <a href={`/bookings/${booking.id}`} className="text-blue-600 hover:text-blue-900 mr-3">
                      View
                    </a>
                    {['REQUESTED', 'APPROVED', 'AWAITING_KYC', 'KYC_VERIFICATION', 'AWAITING_DEPOSIT', 'AWAITING_AGREEMENT', 'CONFIRMED'].includes(booking.status) && (
                      <button onClick={() => handleCancel(booking.id)} className="text-red-600 hover:text-red-900">
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}