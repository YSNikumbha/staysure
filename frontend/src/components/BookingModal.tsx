import { useMutation } from '@tanstack/react-query';
import type { FormEvent } from 'react';
import { useMemo, useState } from 'react';
import { bookingApi } from '../api/booking.api';
import type { PublicPgDetails } from '../types/property';
import { getApiErrorMessage } from '../utils/apiError';
import { FormMessage } from './FormMessage';
import { StatusBadge } from './StatusBadge';

type BookingModalProps = {
  pg: PublicPgDetails;
  onClose: () => void;
  onSuccess: (bookingId: number) => void;
};

export function BookingModal({ pg, onClose, onSuccess }: BookingModalProps) {
  const firstRoom = pg.availableRooms[0];
  const firstBed = firstRoom?.beds[0];
  const [roomId, setRoomId] = useState<number | ''>(firstRoom?.roomId ?? '');
  const selectedRoom = useMemo(
    () => pg.availableRooms.find((room) => room.roomId === roomId),
    [pg.availableRooms, roomId]
  );
  const [bedId, setBedId] = useState<number | ''>(firstBed?.id ?? '');
  const [moveInDate, setMoveInDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [expectedMoveOutDate, setExpectedMoveOutDate] = useState('');
  const [remarks, setRemarks] = useState('');

  const mutation = useMutation({
    mutationFn: bookingApi.create,
    onSuccess: (booking) => onSuccess(booking.id)
  });

  const changeRoom = (value: string) => {
    const nextRoomId = Number(value);
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
      <div className="modal-panel" role="dialog" aria-modal="true" aria-label="Request booking">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Booking request</p>
            <h2>{pg.name}</h2>
          </div>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close">X</button>
        </div>
        <form className="form-grid" onSubmit={submit}>
          <label>
            Room
            <select value={roomId} onChange={(event) => changeRoom(event.target.value)} required>
              {pg.availableRooms.map((room) => (
                <option key={room.roomId} value={room.roomId}>
                  Room {room.roomNumber} - {room.sharingType.replaceAll('_', ' ')} - Rs {Number(room.monthlyRent).toLocaleString()}
                </option>
              ))}
            </select>
          </label>
          <label>
            Bed
            <select value={bedId} onChange={(event) => setBedId(Number(event.target.value))} required>
              {(selectedRoom?.beds ?? []).map((bed) => (
                <option key={bed.id} value={bed.id}>
                  {bed.bedLabel || `Bed ${bed.bedNumber}`}
                </option>
              ))}
            </select>
          </label>
          <div className="detail-grid form-span">
            <div>
              <span>Monthly rent</span>
              <strong>Rs {Number(selectedRoom?.monthlyRent ?? 0).toLocaleString()}</strong>
            </div>
            <div>
              <span>Security deposit</span>
              <strong>Rs {Number(selectedRoom?.securityDeposit ?? 0).toLocaleString()}</strong>
            </div>
            <div>
              <span>Available beds</span>
              <strong>{selectedRoom?.availableBeds ?? 0}</strong>
            </div>
            <div>
              <span>Status</span>
              <strong><StatusBadge status="VERIFIED" /></strong>
            </div>
          </div>
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
            <textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} />
          </label>
          <FormMessage message={mutation.isError ? getApiErrorMessage(mutation.error, 'Unable to request booking') : null} />
          <div className="action-row form-span">
            <button className="primary-button" type="submit" disabled={mutation.isPending || !bedId}>Request Booking</button>
            <button className="secondary-button" type="button" onClick={onClose}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}
