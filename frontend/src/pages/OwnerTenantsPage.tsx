import { useQuery } from '@tanstack/react-query';
import { BedDouble, CalendarClock, Home, UserCheck, UsersRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ownerTenantApi } from '../api/booking.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { TenantProfile, TenantStatus } from '../types/booking';
import { getApiErrorMessage } from '../utils/apiError';

export default function OwnerTenantsPage() {
  const query = useQuery({ queryKey: ['owner-tenants'], queryFn: ownerTenantApi.list });
  const tenants = query.data ?? [];

  return (
    <OwnerShell
      title="Tenants"
      eyebrow="Residents"
      actions={<Link className="secondary-link compact-button" to="/owner/bookings">Bookings</Link>}
    >
      <div className="owner-stack">
        <section className="owner-hero-card owner-hero-card--compact">
          <div>
            <p className="eyebrow">Tenant profiles</p>
            <h2>Manage upcoming and active residents</h2>
            <p>Tenant profiles appear here only after a booking is confirmed, then become active after owner check-in.</p>
          </div>
          <UsersRound size={28} />
        </section>

        <section className="owner-kpi-grid owner-kpi-grid--tight">
          <TenantMetric icon={<CalendarClock size={19} />} label="Upcoming" value={countTenantStatus(tenants, 'UPCOMING')} />
          <TenantMetric icon={<UserCheck size={19} />} label="Active" value={countTenantStatus(tenants, 'ACTIVE')} />
          <TenantMetric icon={<Home size={19} />} label="Properties" value={new Set(tenants.map((tenant) => tenant.property.id)).size} />
          <TenantMetric icon={<BedDouble size={19} />} label="Occupied Beds" value={countTenantStatus(tenants, 'ACTIVE')} />
        </section>

        <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load tenants') : null} />

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Residents</p>
              <h2>{tenants.length} tenant{tenants.length === 1 ? '' : 's'}</h2>
            </div>
          </div>

          {query.isLoading ? (
            <div className="owner-card-grid">
              {Array.from({ length: 3 }).map((_, index) => <div className="owner-skeleton-card" key={index} />)}
            </div>
          ) : null}

          {!query.isLoading && tenants.length === 0 ? (
            <EmptyState
              title="No tenant profiles yet."
              description="Approved bookings become tenant profiles after KYC, deposit and agreement acceptance are complete."
              action={<Link className="secondary-link" to="/owner/bookings">Review Bookings</Link>}
            />
          ) : null}

          {tenants.length > 0 ? (
            <div className="owner-tenant-grid">
              {tenants.map((tenant) => (
                <article className="owner-tenant-card" key={tenant.id}>
                  <div className="owner-tenant-avatar">
                    {tenant.user.firstName[0]}{tenant.user.lastName[0]}
                  </div>
                  <div>
                    <div className="owner-tenant-title">
                      <h2>{tenant.user.firstName} {tenant.user.lastName}</h2>
                      <StatusBadge status={tenant.status} />
                    </div>
                    <p>{tenant.property.name}</p>
                    <div className="owner-booking-facts">
                      <span>Room <strong>{tenant.room.roomNumber}</strong></span>
                      <span>Bed <strong>{tenant.bed.bedLabel || tenant.bed.bedNumber}</strong></span>
                      <span>Joining <strong>{tenant.joiningDate ? new Date(tenant.joiningDate).toLocaleDateString() : 'Upcoming'}</strong></span>
                    </div>
                  </div>
                  <Link className="primary-link compact-button" to={`/owner/tenants/${tenant.id}`}>View</Link>
                </article>
              ))}
            </div>
          ) : null}
        </section>
      </div>
    </OwnerShell>
  );
}

function TenantMetric({ icon, label, value }: { icon: ReactNode; label: string; value: number }) {
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

function countTenantStatus(tenants: TenantProfile[], status: TenantStatus) {
  return tenants.filter((tenant) => tenant.status === status).length;
}
