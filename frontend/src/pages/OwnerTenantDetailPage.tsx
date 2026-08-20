import { useQuery } from '@tanstack/react-query';
import { BedDouble, CalendarClock, Home, IndianRupee, Mail, Phone, UserRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ownerTenantApi } from '../api/booking.api';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import { getApiErrorMessage } from '../utils/apiError';

export default function OwnerTenantDetailPage() {
  const { id } = useParams();
  const tenantId = Number(id);
  const query = useQuery({ queryKey: ['owner-tenant', tenantId], queryFn: () => ownerTenantApi.get(tenantId), enabled: Number.isFinite(tenantId) });

  if (query.isLoading) {
    return (
      <OwnerShell title="Tenant Details" eyebrow="Residents">
        <div className="owner-stack">
          <div className="owner-skeleton-card" />
          <div className="owner-skeleton-card" />
        </div>
      </OwnerShell>
    );
  }

  if (query.isError || !query.data) {
    return (
      <OwnerShell title="Tenant Details" eyebrow="Residents">
        <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load tenant')}</div>
      </OwnerShell>
    );
  }

  const tenant = query.data;
  const tenantName = `${tenant.user.firstName} ${tenant.user.lastName}`;

  return (
    <OwnerShell
      title={tenantName}
      eyebrow="Tenant details"
      actions={<Link className="secondary-link compact-button" to="/owner/tenants">Back</Link>}
    >
      <div className="owner-stack">
        <section className="owner-hero-card owner-hero-card--compact">
          <div className="owner-tenant-hero">
            <span className="owner-tenant-avatar owner-tenant-avatar--large">{tenant.user.firstName[0]}{tenant.user.lastName[0]}</span>
            <div>
              <p className="eyebrow">Tenant profile</p>
              <h2>{tenantName}</h2>
              <p>{tenant.property.name} · Room {tenant.room.roomNumber} / {tenant.bed.bedLabel || tenant.bed.bedNumber}</p>
            </div>
          </div>
          <StatusBadge status={tenant.status} />
        </section>

        <section className="owner-detail-columns">
          <div className="surface owner-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Contact</p>
                <h2>Tenant Information</h2>
              </div>
              <UserRound size={20} />
            </div>
            <div className="detail-grid detail-grid--compact">
              <InfoItem icon={<Phone size={16} />} label="Phone" value={tenant.user.phone} />
              <InfoItem icon={<Mail size={16} />} label="Email" value={tenant.user.email} />
              <InfoItem icon={<CalendarClock size={16} />} label="Joining" value={tenant.joiningDate ? new Date(tenant.joiningDate).toLocaleString() : 'Upcoming'} />
              <InfoItem icon={<CalendarClock size={16} />} label="Expected Checkout" value={tenant.expectedCheckoutDate ?? '-'} />
            </div>
          </div>

          <div className="surface owner-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Stay</p>
                <h2>Room and Bed</h2>
              </div>
              <Home size={20} />
            </div>
            <div className="detail-grid detail-grid--compact">
              <InfoItem icon={<Home size={16} />} label="PG" value={tenant.property.name} />
              <InfoItem icon={<BedDouble size={16} />} label="Room" value={tenant.room.roomNumber} />
              <InfoItem icon={<BedDouble size={16} />} label="Bed" value={tenant.bed.bedLabel || tenant.bed.bedNumber} />
              <InfoItem icon={<BedDouble size={16} />} label="Sharing" value={tenant.room.sharingType.replaceAll('_', ' ')} />
            </div>
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Financials</p>
              <h2>Booking Terms</h2>
            </div>
            <IndianRupee size={20} />
          </div>
          <div className="detail-grid">
            <div><span>Monthly Rent</span><strong>Rs {Number(tenant.room.monthlyRent).toLocaleString()}</strong></div>
            <div><span>Security Deposit</span><strong>Rs {Number(tenant.room.securityDeposit).toLocaleString()}</strong></div>
            <div><span>Booking</span><strong><Link to={`/owner/bookings/${tenant.bookingId}`}>Open booking</Link></strong></div>
            <div><span>Furnishing</span><strong>{tenant.room.furnishingType.replaceAll('_', ' ')}</strong></div>
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}

function InfoItem({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div>
      <span>{icon}{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
