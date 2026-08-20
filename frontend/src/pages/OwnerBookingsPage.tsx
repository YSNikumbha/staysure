import { useQuery } from '@tanstack/react-query';
import { BedDouble, CalendarCheck, ClipboardList, FileCheck2, KeyRound, ShieldCheck, UsersRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ownerBookingApi } from '../api/booking.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { Booking, BookingStatus } from '../types/booking';
import { getApiErrorMessage } from '../utils/apiError';

type BookingFilter = 'ALL' | BookingStatus;

const filters: BookingFilter[] = ['ALL', 'REQUESTED', 'AWAITING_KYC', 'KYC_VERIFICATION', 'AWAITING_DEPOSIT', 'AWAITING_AGREEMENT', 'CONFIRMED', 'CHECKED_IN'];

export default function OwnerBookingsPage() {
  const [filter, setFilter] = useState<BookingFilter>('ALL');
  const query = useQuery({ queryKey: ['owner-bookings'], queryFn: ownerBookingApi.list });
  const bookings = query.data ?? [];
  const visibleBookings = useMemo(
    () => (filter === 'ALL' ? bookings : bookings.filter((booking) => booking.status === filter)),
    [bookings, filter]
  );

  return (
    <OwnerShell
      title="Bookings"
      eyebrow="Tenant onboarding"
      actions={<Link className="secondary-link compact-button" to="/owner/tenants">Tenants</Link>}
    >
      <div className="owner-stack">
        <section className="owner-hero-card owner-hero-card--compact">
          <div>
            <p className="eyebrow">Workflow queue</p>
            <h2>Move verified requests through onboarding</h2>
            <p>Review booking requests, KYC, deposits, agreements and confirmed check-ins from one queue.</p>
          </div>
          <ClipboardList size={28} />
        </section>

        <section className="owner-kpi-grid owner-kpi-grid--tight">
          <BookingMetric icon={<ClipboardList size={19} />} label="Requests" value={countStatus(bookings, 'REQUESTED')} />
          <BookingMetric icon={<ShieldCheck size={19} />} label="KYC Review" value={countStatus(bookings, 'KYC_VERIFICATION')} />
          <BookingMetric icon={<KeyRound size={19} />} label="Deposits" value={countStatus(bookings, 'AWAITING_DEPOSIT')} />
          <BookingMetric icon={<FileCheck2 size={19} />} label="Agreements" value={countStatus(bookings, 'AWAITING_AGREEMENT')} />
          <BookingMetric icon={<CalendarCheck size={19} />} label="Check-ins" value={countStatus(bookings, 'CONFIRMED')} />
          <BookingMetric icon={<UsersRound size={19} />} label="Active" value={countStatus(bookings, 'CHECKED_IN')} />
        </section>

        <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load owner bookings') : null} />

        <section className="surface owner-panel">
          <div className="owner-toolbar">
            <div>
              <p className="eyebrow">Booking requests</p>
              <h2>{visibleBookings.length} booking{visibleBookings.length === 1 ? '' : 's'}</h2>
            </div>
            <div className="segmented segmented--wrap">
              {filters.map((item) => (
                <button
                  className={filter === item ? 'active' : ''}
                  key={item}
                  type="button"
                  onClick={() => setFilter(item)}
                >
                  {item === 'ALL' ? 'All' : item.replaceAll('_', ' ')}
                </button>
              ))}
            </div>
          </div>

          {query.isLoading ? (
            <div className="owner-card-grid">
              {Array.from({ length: 4 }).map((_, index) => <div className="owner-skeleton-card" key={index} />)}
            </div>
          ) : null}

          {!query.isLoading && visibleBookings.length === 0 ? (
            <EmptyState
              title={bookings.length === 0 ? 'No booking requests yet.' : 'No bookings match this status.'}
              description={bookings.length === 0 ? 'New user requests will appear here after they select a real available bed.' : 'Try another status filter to review the rest of the queue.'}
            />
          ) : null}

          {visibleBookings.length > 0 ? (
            <div className="owner-booking-list">
              {visibleBookings.map((booking) => <OwnerBookingCard booking={booking} key={booking.id} />)}
            </div>
          ) : null}
        </section>
      </div>
    </OwnerShell>
  );
}

function OwnerBookingCard({ booking }: { booking: Booking }) {
  return (
    <article className="owner-booking-card">
      <div className="owner-booking-icon">
        <BedDouble size={20} />
      </div>
      <div className="owner-booking-main">
        <div className="owner-booking-title">
          <div>
            <p className="eyebrow">{booking.bookingNumber}</p>
            <h2>{booking.user.firstName} {booking.user.lastName}</h2>
            <p>{booking.property.name} · Room {booking.room.roomNumber} / {booking.bed.bedLabel || booking.bed.bedNumber}</p>
          </div>
          <StatusBadge status={booking.status} />
        </div>
        <div className="owner-booking-facts">
          <span>Move-in <strong>{booking.moveInDate}</strong></span>
          <span>Rent <strong>Rs {Number(booking.monthlyRent).toLocaleString()}</strong></span>
          <span>Deposit <strong>Rs {Number(booking.securityDeposit).toLocaleString()}</strong></span>
          <span>Next <strong>{nextActionLabel(booking.status)}</strong></span>
        </div>
      </div>
      <Link className="primary-link compact-button" to={`/owner/bookings/${booking.id}`}>Review</Link>
    </article>
  );
}

function BookingMetric({ icon, label, value }: { icon: ReactNode; label: string; value: number }) {
  return (
    <article className="owner-kpi-card">
      <span>{icon}</span>
      <div>
        <strong>{value}</strong>
        <p>{label}</p>
      </div>
    </article>
  );
}

function countStatus(bookings: Booking[], status: BookingStatus) {
  return bookings.filter((booking) => booking.status === status).length;
}

function nextActionLabel(status: BookingStatus) {
  switch (status) {
    case 'REQUESTED':
      return 'Approve or reject';
    case 'AWAITING_KYC':
      return 'Waiting for upload';
    case 'KYC_VERIFICATION':
      return 'Review KYC';
    case 'AWAITING_DEPOSIT':
      return 'Record deposit';
    case 'AWAITING_AGREEMENT':
      return 'Issue agreement';
    case 'CONFIRMED':
      return 'Check in';
    case 'CHECKED_IN':
      return 'Tenant active';
    case 'REJECTED':
      return 'Closed';
    case 'CANCELLED':
      return 'Closed';
    default:
      return 'Review';
  }
}
