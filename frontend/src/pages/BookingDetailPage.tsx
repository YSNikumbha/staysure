import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
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

export default function BookingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [booking, setBooking] = useState<Booking | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadBooking(Number(id));
    }
  }, [id]);

  const loadBooking = async (bookingId: number) => {
    try {
      setLoading(true);
      setError(null);
      const data = await bookingApi.get(bookingId);
      setBooking(data);
    } catch (err) {
      setError('Failed to load booking details');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!id || !confirm('Are you sure you want to cancel this booking?')) return;
    try {
      await bookingApi.cancel(Number(id));
      await loadBooking(Number(id));
    } catch (err) {
      alert('Failed to cancel booking');
      console.error(err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">Loading booking details...</div>
      </div>
    );
  }

  if (error || !booking) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-red-600">{error || 'Booking not found'}</div>
      </div>
    );
  }

  const getNextAction = () => {
    switch (booking.status) {
      case 'REQUESTED':
        return 'Waiting for owner approval';
      case 'APPROVED':
        return 'Upload KYC documents';
      case 'AWAITING_KYC':
        return 'Waiting for KYC verification';
      case 'KYC_VERIFICATION':
        return 'Waiting for deposit';
      case 'AWAITING_DEPOSIT':
        return 'Waiting for agreement';
      case 'AWAITING_AGREEMENT':
        return 'Review and accept agreement';
      case 'CONFIRMED':
        return 'Booking confirmed';
      case 'CHECKED_IN':
        return 'Checked in';
      default:
        return null;
    }
  };

  const nextAction = getNextAction();

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <Link to="/bookings" className="text-blue-600 hover:text-blue-900">
          ← Back to Bookings
        </Link>
      </div>

      <div className="bg-white shadow-md rounded-lg p-6 mb-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h1 className="text-3xl font-bold mb-2">Booking Details</h1>
            <p className="text-gray-600">Booking #{booking.bookingNumber}</p>
          </div>
          <span className={`px-3 py-1 inline-flex text-sm leading-5 font-semibold rounded-full ${STATUS_COLORS[booking.status]}`}>
            {STATUS_LABELS[booking.status]}
          </span>
        </div>

        {nextAction && (
          <div className="bg-blue-50 border-l-4 border-blue-400 p-4 mb-6">
            <p className="text-blue-700 font-medium">Next Action: {nextAction}</p>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h2 className="text-xl font-semibold mb-4">Property Details</h2>
            <div className="space-y-2">
              <p><span className="font-medium">PG:</span> {booking.propertyName}</p>
              <p><span className="font-medium">City:</span> {booking.propertyCity}</p>
              <p><span className="font-medium">Room:</span> {booking.roomNumber}</p>
              <p><span className="font-medium">Bed:</span> {booking.bedNumber}</p>
            </div>
          </div>

          <div>
            <h2 className="text-xl font-semibold mb-4">Booking Details</h2>
            <div className="space-y-2">
              <p><span className="font-medium">Move-in Date:</span> {new Date(booking.moveInDate).toLocaleDateString()}</p>
              <p><span className="font-medium">Expected Move-out:</span> {new Date(booking.expectedMoveOutDate).toLocaleDateString()}</p>
              <p><span className="font-medium">Monthly Rent:</span> ₹{booking.monthlyRent.toLocaleString()}</p>
              <p><span className="font-medium">Security Deposit:</span> ₹{booking.securityDepositAmount.toLocaleString()}</p>
            </div>
          </div>
        </div>

        {booking.rejectionReason && (
          <div className="mt-6 bg-red-50 border-l-4 border-red-400 p-4">
            <p className="text-red-700 font-medium">Rejection Reason:</p>
            <p className="text-red-600">{booking.rejectionReason}</p>
          </div>
        )}

        <div className="mt-6 flex gap-4">
          {['REQUESTED', 'APPROVED', 'AWAITING_KYC', 'KYC_VERIFICATION', 'AWAITING_DEPOSIT', 'AWAITING_AGREEMENT', 'CONFIRMED'].includes(booking.status) && (
            <button onClick={handleCancel} className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">
              Cancel Booking
            </button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Link to={`/bookings/${booking.id}/kyc`} className="bg-white shadow-md rounded-lg p-6 hover:shadow-lg transition-shadow">
          <h3 className="text-lg font-semibold mb-2">KYC Documents</h3>
          <p className="text-gray-600">Upload and manage your documents</p>
        </Link>

        <div className="bg-white shadow-md rounded-lg p-6">
          <h3 className="text-lg font-semibold mb-2">Deposit Status</h3>
          <p className="text-gray-600">Security deposit payment</p>
        </div>

        <div className="bg-white shadow-md rounded-lg p-6">
          <h3 className="text-lg font-semibold mb-2">Agreement</h3>
          <p className="text-gray-600">Rental agreement</p>
        </div>
      </div>
    </div>
  );
}