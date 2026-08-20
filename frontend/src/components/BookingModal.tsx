import { useMutation } from '@tanstack/react-query';
import { CalendarDays, CheckCircle2, X } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import type { FormEvent, ReactNode } from 'react';
import { bookingApi } from '../api/booking.api';
import type { PublicPgDetails } from '../types/property';
import { getApiErrorMessage } from '../utils/apiError';
import { FormMessage } from './FormMessage';

type BookingModalProps = {
  pg: PublicPgDetails;
  initialRoomId?: number;
  initialBedId?: number;
  onClose: () => void;
  onSuccess: (bookingId: number) => void;
};

export function BookingModal({ pg, initialRoomId, initialBedId, onClose, onSuccess }: BookingModalProps) {
  const firstRoom = pg.availableRooms.find((room) => room.beds.length > 0);
  const initialRoom = pg.availableRooms.find((room) => room.roomId === initialRoomId) ?? firstRoom;
  const initialBed = initialRoom?.beds.find((bed) => bed.id === initialBedId) ?? initialRoom?.beds[0];
  const [roomId, setRoomId] = useState<number | ''>(initialRoom?.roomId ?? '');
  const [bedId, setBedId] = useState<number | ''>(initialBed?.id ?? '');
  const [moveInDate, setMoveInDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [expectedMoveOutDate, setExpectedMoveOutDate] = useState('');
  const [remarks, setRemarks] = useState('');

  useEffect(() => {
    const nextRoom = pg.availableRooms.find((room) => room.roomId === initialRoomId) ?? firstRoom;
    const nextBed = nextRoom?.beds.find((bed) => bed.id === initialBedId) ?? nextRoom?.beds[0];
    setRoomId(nextRoom?.roomId ?? '');
    setBedId(nextBed?.id ?? '');
  }, [firstRoom, initialBedId, initialRoomId, pg.availableRooms]);

  const selectedRoom = useMemo(
    () => pg.availableRooms.find((room) => room.roomId === roomId),
    [pg.availableRooms, roomId]
  );
  const selectedBed = selectedRoom?.beds.find((bed) => bed.id === bedId);

  const mutation = useMutation({
    mutationFn: bookingApi.create,
    onSuccess: (booking) => onSuccess(booking.id)
  });

  const changeRoom = (nextRoomId: number) => {
    const nextRoom = pg.availableRooms.find((room) => room.roomId === nextRoomId);
    setRoomId(nextRoomId);
    setBedId(nextRoom?.beds[0]?.id ?? '');
  };

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!roomId || !bedId) return;
    mutation.mutate({
      propertyId: pg.id,
      roomId,
      bedId,
      moveInDate,
      expectedMoveOutDate: expectedMoveOutDate || undefined,
      remarks: remarks || undefined
    });
  };

  return (
    <div className="modal-backdrop" role="presentation">
      <div className="modal-panel booking-modal" role="dialog" aria-modal="true" aria-label="Request booking">
        <div className="booking-modal__header">
          <div>
            <p className="eyebrow">Request booking</p>
            <h2>{pg.name}</h2>
            <p>{pg.area}, {pg.city}</p>
          </div>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close booking modal">
            <X size={18} />
          </button>
        </div>

        {pg.availableRooms.length === 0 ? (
          <div className="empty-state empty-state--panel">No available beds can be booked right now.</div>
        ) : (
          <form className="booking-form" onSubmit={submit}>
            <section className="booking-step">
              <span>1</span>
              <div>
                <h3>Select Room</h3>
                <div className="booking-room-grid">
                  {pg.availableRooms.map((room) => (
                    <button
                      className={room.roomId === roomId ? 'booking-room-option booking-room-option--active' : 'booking-room-option'}
                      type="button"
                      key={room.roomId}
                      onClick={() => changeRoom(room.roomId)}
                      disabled={room.beds.length === 0}
                    >
                      <strong>{room.sharingType.replaceAll('_', ' ')}</strong>
                      <span>Room {room.roomNumber}</span>
                      <em>Rs {Number(room.monthlyRent).toLocaleString()} / month</em>
                      <small>{room.availableBeds} beds available</small>
                    </button>
                  ))}
                </div>
              </div>
            </section>

            <section className="booking-step">
              <span>2</span>
              <div>
                <h3>Select Available Bed</h3>
                <div className="booking-bed-grid">
                  {(selectedRoom?.beds ?? []).map((bed) => (
                    <button
                      className={bed.id === bedId ? 'bed-select-chip bed-select-chip--active' : 'bed-select-chip'}
                      type="button"
                      key={bed.id}
                      onClick={() => setBedId(bed.id)}
                    >
                      {bed.bedLabel || `Bed ${bed.bedNumber}`}
                    </button>
                  ))}
                </div>
              </div>
            </section>

            <section className="booking-step">
              <span>3</span>
              <div className="booking-date-grid">
                <h3 className="form-span">Move-in details</h3>
                <label>
                  Move-in date
                  <input type="date" min={new Date().toISOString().slice(0, 10)} value={moveInDate} onChange={(event) => setMoveInDate(event.target.value)} required />
                </label>
                <label>
                  Expected move-out
                  <input type="date" min={moveInDate} value={expectedMoveOutDate} onChange={(event) => setExpectedMoveOutDate(event.target.value)} />
                </label>
                <label className="form-span">
                  Remarks
                  <textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} placeholder="Optional note for the owner" />
                </label>
              </div>
            </section>

            <section className="booking-summary-panel">
              <h3>Booking Summary</h3>
              <div className="booking-summary-grid">
                <Summary label="PG" value={pg.name} />
                <Summary label="Room" value={selectedRoom ? `Room ${selectedRoom.roomNumber}` : 'Select room'} />
                <Summary label="Bed" value={selectedBed ? selectedBed.bedLabel || `Bed ${selectedBed.bedNumber}` : 'Select bed'} />
                <Summary label="Monthly Rent" value={`Rs ${Number(selectedRoom?.monthlyRent ?? 0).toLocaleString()}`} />
                <Summary label="Security Deposit" value={`Rs ${Number(selectedRoom?.securityDeposit ?? 0).toLocaleString()}`} />
                <Summary label="Move-in Date" value={moveInDate} icon={<CalendarDays size={15} />} />
              </div>
              <p><CheckCircle2 size={16} /> Rent and deposit are controlled by StaySure backend data.</p>
            </section>

            <FormMessage message={mutation.isError ? getApiErrorMessage(mutation.error, 'Unable to request booking') : null} />
            <div className="booking-modal__actions">
              <button className="secondary-button" type="button" onClick={onClose}>Cancel</button>
              <button className="primary-button" type="submit" disabled={mutation.isPending || !bedId}>
                {mutation.isPending ? 'Sending request' : 'Send Booking Request'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

function Summary({ label, value, icon }: { label: string; value: string; icon?: ReactNode }) {
  return (
    <div>
      <span>{icon}{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
