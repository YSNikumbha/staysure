import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { ownerTenantApi } from '../api/booking.api';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';

export default function OwnerTenantDetailPage() {
  const { id } = useParams();
  const tenantId = Number(id);
  const query = useQuery({ queryKey: ['owner-tenant', tenantId], queryFn: () => ownerTenantApi.get(tenantId), enabled: Number.isFinite(tenantId) });

  if (query.isLoading) return <div className="route-state">Loading tenant</div>;
  if (query.isError || !query.data) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load tenant')}</div>;
  const tenant = query.data;

  return (
    <div className="stack">
      <PageHeader eyebrow="Tenant details" title={`${tenant.user.firstName} ${tenant.user.lastName}`} actions={<Link className="secondary-link" to="/owner/tenants">Back</Link>} />
      <section className="surface detail-grid">
        <div><span>Status</span><strong><StatusBadge status={tenant.status} /></strong></div>
        <div><span>Phone</span><strong>{tenant.user.phone}</strong></div>
        <div><span>Email</span><strong>{tenant.user.email}</strong></div>
        <div><span>Booking</span><strong><Link to={`/owner/bookings/${tenant.bookingId}`}>Open booking</Link></strong></div>
        <div><span>PG</span><strong>{tenant.property.name}</strong></div>
        <div><span>Room</span><strong>{tenant.room.roomNumber}</strong></div>
        <div><span>Bed</span><strong>{tenant.bed.bedLabel || tenant.bed.bedNumber}</strong></div>
        <div><span>Joining</span><strong>{tenant.joiningDate ? new Date(tenant.joiningDate).toLocaleString() : '-'}</strong></div>
        <div><span>Expected Checkout</span><strong>{tenant.expectedCheckoutDate ?? '-'}</strong></div>
        <div><span>Monthly Rent</span><strong>Rs {Number(tenant.room.monthlyRent).toLocaleString()}</strong></div>
        <div><span>Security Deposit</span><strong>Rs {Number(tenant.room.securityDeposit).toLocaleString()}</strong></div>
        <div><span>Sharing</span><strong>{tenant.room.sharingType.replaceAll('_', ' ')}</strong></div>
      </section>
    </div>
  );
}
