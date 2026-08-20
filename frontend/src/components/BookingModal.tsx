import { useState } from 'react';
import { bookingApi } from '../api/booking.api';
import type { PublicPgDetails, PublicRoomAvailability } from '../types/property';

interface BookingModalProps {
  pg: PublicPgDetails;
  onClose: () => void;
  onSuccess: () => void;
}

export default function BookingModal({ pg, onClose, onSuccess }: BookingModalProps) {
  const [selectedRoom, setSelectedRoom] = useState<PublicRoomAvailability | null>(null);
  const [selectedBed, setSelectedBed] = useState<number | null>(null);
  const [moveInDate, setMoveInDate] = useState('');
  const [expectedMoveOutDate, setExpectedMoveOutDate] = useState('');
  const [remarks, setRemarks] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const availableRooms = pg.availableRooms.filter(room => room.availableBeds > 0);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRoom || !selectedBed || !moveInDate) {
      setError('Please select a room, bed and move-in date');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      await bookingApi.create({
        propertyId: pg.id,
        roomId: selectedRoom.roomId,
        bedId: selectedBed,
        moveInDate,
        expectedMoveOutDate: expectedMoveOutDate || moveInDate,
        remarks: remarks || undefined
      });
      onSuccess();
      onClose();
    } catch (err) {
      setError('Failed to create booking. Please try again.');
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold">Request Booking</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700 text-2xl">&times;</button>
        </div>

        <div className="mb-4 p-4 bg-blue-50 rounded">
          <h3 className="font-semibold mb-2">{pg.name}</h3>
          <p className="text-sm text-gray-600">{pg.area}, {pg.city}</p>
          <p className="text-sm mt-2">
            <span className="font-medium">Rent:</span> ₹{pg.startingRent.toLocaleString()}/month
          </p>
          <p className="text-sm">
            <span className="font-medium">Deposit:</span> ₹{pg.securityDeposit.toLocaleString()}
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Select Room *</label>
            <select
              value={selectedRoom?.roomId || ''}
              onChange={(e) => {
                const room = availableRooms.find(r => r.roomId === Number(e.target.value));
                setSelectedRoom(room || null);
                setSelectedBed(null);
              }}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            >
              <option value="">Select a room</option>
              {availableRooms.map((room) => (
                <option key={room.roomId} value={room.roomId}>
                  Room {room.roomNumber} - {room.sharingType.replaceAll('_', ' ')} (₹{room.monthlyRent.toLocaleString()}/month)
                </option>
              ))}
            </select>
          </div>

          {selectedRoom && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Select Bed *</label>
              <div className="grid grid-cols-2 gap-2">
                {Array.from({ length: selectedRoom.capacity }, (_, i) => {
                  const bedLabels = ['A', 'B', 'C', 'D'];
                  const bedLabel = bedLabels[i] || `Bed ${i + 1}`;
                  return (
                    <button
                      key={i}
                      type="button"
                      onClick={() => setSelectedBed(i + 1)}
                      className={`p-3 border-2 rounded-md text-center ${
                        selectedBed === i + 1
                          ? 'border-blue-500 bg-blue-50'
                          : 'border-gray-300 hover:border-gray-400'
                      }`}
                    >
                      <div className="font-medium">Bed {bedLabel}</div>
                      <div className="text-xs text-gray-600">Available</div>
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Move-in Date *</label>
            <input
              type="date"
              value={moveInDate}
              onChange={(e) => setMoveInDate(e.target.value)}
              min={new Date().toISOString().split('T')[0]}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Expected Move-out Date</label>
            <input
              type="date"
              value={expectedMoveOutDate}
              onChange={(e) => setExpectedMoveOutDate(e.target.value)}
              min={moveInDate}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Remarks (Optional)</label>
            <textarea
              value={remarks}
              onChange={(e) => setRemarks(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Any special requests or remarks"
            />
          </div>

          {selectedRoom && (
            <div className="bg-gray-50 p-4 rounded-md">
              <h4 className="font-semibold mb-2">Booking Summary</h4>
              <div className="space-y-1 text-sm">
                <p><span className="font-medium">Room:</span> {selectedRoom.roomNumber} ({selectedRoom.sharingType.replaceAll('_', ' ')})</p>
                <p><span className="font-medium">Bed:</span> {selectedBed ? String.fromCharCode(64 + selectedBed) : 'Not selected'}</p>
                <p><span className="font-medium">Monthly Rent:</span> ₹{selectedRoom.monthlyRent.toLocaleString()}</p>
                <p><span className="font-medium">Security Deposit:</span> ₹{selectedRoom.securityDeposit.toLocaleString()}</p>
              </div>
            </div>
          )}

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || !selectedRoom || !selectedBed || !moveInDate}
              className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {submitting ? 'Submitting...' : 'Request Booking'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}