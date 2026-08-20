import { useQuery } from '@tanstack/react-query';
import { BedDouble, Building2, CalendarCheck, CheckCircle2, ClipboardList, Home, KeyRound, ShieldCheck, UsersRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ownerBookingApi, ownerTenantApi } from '../../api/booking.api';
import { ownerApi } from '../../api/owner.api';
import { propertyApi } from '../../api/property.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
import { StatusBadge } from '../../components/StatusBadge';
import type { Booking, BookingStatus } from '../../types/booking';
import { getApiErrorMessage } from '../../utils/apiError';
import { OwnerShell } from './OwnerShell';

const actionableStatuses: BookingStatus[] = ['REQUESTED', 'KYC_VERIFICATION', 'AWAITING_DEPOSIT', 'AWAITING_AGREEMENT', 'CONFIRMED'];

export function OwnerDashboardPage() {
  const dashboardQuery = useQuery({
    queryKey: ['owner-dashboard'],
    queryFn: ownerApi.dashboard
  });
  const pgsQuery = useQuery({
    queryKey: ['owner-pgs'],
    queryFn: propertyApi.listProperties
  });
  const bookingsQuery = useQuery({
    queryKey: ['owner-bookings'],
    queryFn: ownerBookingApi.list
  });
  const tenantsQuery = useQuery({
    queryKey: ['owner-tenants'],
    queryFn: ownerTenantApi.list
  });

  const dashboard = dashboardQuery.data;
  const pgs = pgsQuery.data ?? [];
  const bookings = bookingsQuery.data ?? [];
  const tenants = tenantsQuery.data ?? [];
  const loading = dashboardQuery.isLoading || pgsQuery.isLoading || bookingsQuery.isLoading || tenantsQuery.isLoading;
  const error = dashboardQuery.error ?? pgsQuery.error ?? bookingsQuery.error ?? tenantsQuery.error;
  const recentBookings = bookings.filter((booking) => actionableStatuses.includes(booking.status)).slice(0, 5);
  const upcomingCheckIns = bookings.filter((booking) => booking.status === 'CONFIRMED').slice(0, 4);
  const activeTenants = tenants.filter((tenant) => tenant.status === 'ACTIVE').length;

  if (loading) {
    return (
      <OwnerShell title="Owner Dashboard" eyebrow="Owner workspace" actions={<Link className="primary-link" to="/owner/pgs/new">Add PG</Link>}>
        <div className="owner-kpi-grid">
          {Array.from({ length: 5 }).map((_, index) => <div className="owner-skeleton-card" key={index} />)}
        </div>
      </OwnerShell>
    );
  }

  return (
    <OwnerShell title="Owner Dashboard" eyebrow="Owner workspace" actions={<Link className="primary-link" to="/owner/pgs/new">Add PG</Link>}>
      <div className="owner-stack">
        {error ? <FormMessage message={getApiErrorMessage(error, 'Unable to load owner dashboard')} /> : null}

        <section className="owner-hero-card">
          <div>
            <p className="eyebrow">Business profile</p>
            <h2>{dashboard?.businessName ?? 'StaySure owner workspace'}</h2>
            <p>Manage verified PG listings, inventory, bookings and tenant onboarding from one place.</p>
          </div>
          {dashboard ? <StatusBadge status={dashboard.verificationStatus} /> : null}
        </section>

        {pgs.length === 0 ? (
          <EmptyState
            title="You haven't added a PG yet."
            description="Create your first property, add rooms and beds, then submit it for verification."
            action={<Link className="primary-link" to="/owner/pgs/new">Add Your First PG</Link>}
          />
        ) : null}

        <section className="owner-kpi-grid">
          <Kpi icon={<Building2 size={20} />} label="Total PGs" value={dashboard?.totalPgs ?? pgs.length} />
          <Kpi icon={<CheckCircle2 size={20} />} label="Active PGs" value={dashboard?.activePgs ?? pgs.filter((pg) => pg.status === 'ACTIVE').length} />
          <Kpi icon={<Home size={20} />} label="Total Rooms" value={dashboard?.totalRooms ?? 0} />
          <Kpi icon={<BedDouble size={20} />} label="Available Beds" value={dashboard?.availableBeds ?? 0} />
          <Kpi icon={<ClipboardList size={20} />} label="Booking Requests" value={countStatus(bookings, 'REQUESTED')} />
          <Kpi icon={<ShieldCheck size={20} />} label="Awaiting KYC" value={countStatus(bookings, 'AWAITING_KYC') + countStatus(bookings, 'KYC_VERIFICATION')} />
          <Kpi icon={<KeyRound size={20} />} label="Awaiting Deposit" value={countStatus(bookings, 'AWAITING_DEPOSIT')} />
          <Kpi icon={<UsersRound size={20} />} label="Active Tenants" value={activeTenants} />
        </section>

        <section className="owner-dashboard-columns">
          <div className="surface owner-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Properties</p>
                <h2>My Properties</h2>
              </div>
              <Link className="secondary-link compact-button" to="/owner/pgs">View all</Link>
            </div>
            <div className="owner-list">
              {pgs.slice(0, 4).map((pg) => (
                <Link className="owner-list-row" to={`/owner/pgs/${pg.id}`} key={pg.id}>
                  <div>
                    <strong>{pg.name}</strong>
                    <span>{pg.area}, {pg.city}</span>
                  </div>
                  <span className="badge-row">
                    <StatusBadge status={pg.status} />
                    <StatusBadge status={pg.verificationStatus} />
                  </span>
                </Link>
              ))}
              {pgs.length === 0 ? <p className="muted-copy">No properties yet.</p> : null}
            </div>
          </div>

          <div className="surface owner-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Booking workflow</p>
                <h2>Needs Attention</h2>
              </div>
              <Link className="secondary-link compact-button" to="/owner/bookings">Bookings</Link>
            </div>
            <div className="owner-list">
              {recentBookings.map((booking) => <BookingRow booking={booking} key={booking.id} />)}
              {recentBookings.length === 0 ? <p className="muted-copy">No booking actions pending.</p> : null}
            </div>
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Upcoming</p>
              <h2>Confirmed Check-ins</h2>
            </div>
            <CalendarCheck size={20} />
          </div>
          <div className="owner-list owner-list--grid">
            {upcomingCheckIns.map((booking) => <BookingRow booking={booking} key={booking.id} />)}
            {upcomingCheckIns.length === 0 ? <p className="muted-copy">No confirmed check-ins waiting right now.</p> : null}
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}

function Kpi({ icon, label, value }: { icon: ReactNode; label: string; value: number }) {
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

function BookingRow({ booking }: { booking: Booking }) {
  return (
    <Link className="owner-list-row" to={`/owner/bookings/${booking.id}`}>
      <div>
        <strong>{booking.bookingNumber}</strong>
        <span>{booking.user.firstName} {booking.user.lastName} · {booking.property.name}</span>
      </div>
      <StatusBadge status={booking.status} />
    </Link>
  );
}

function countStatus(bookings: Booking[], status: BookingStatus) {
  return bookings.filter((booking) => booking.status === status).length;
}
