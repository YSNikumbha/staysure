import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ownerTenantApi } from '../api/booking.api';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';

export default function OwnerTenantsPage() {
  const query = useQuery({ queryKey: ['owner-tenants'], queryFn: ownerTenantApi.list });

  if (query.isLoading) return <div className="route-state">Loading tenants</div>;
  if (query.isError) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load tenants')}</div>;
  const tenants = query.data ?? [];

  return (
    <div className="stack">
      <PageHeader eyebrow="Owner" title="Tenants" actions={<Link className="secondary-link" to="/owner/bookings">Bookings</Link>} />
      <section className="surface">
        {tenants.length === 0 ? <div className="empty-state">No tenant profiles yet.</div> : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Tenant</th><th>PG</th><th>Room / Bed</th><th>Joining</th><th>Expected Checkout</th><th>Status</th><th className="table-actions">Actions</th></tr></thead>
              <tbody>
                {tenants.map((tenant) => (
                  <tr key={tenant.id}>
                    <td>{tenant.user.firstName} {tenant.user.lastName}</td>
                    <td>{tenant.property.name}</td>
                    <td>{tenant.room.roomNumber} / {tenant.bed.bedLabel || tenant.bed.bedNumber}</td>
                    <td>{tenant.joiningDate ? new Date(tenant.joiningDate).toLocaleString() : '-'}</td>
                    <td>{tenant.expectedCheckoutDate ?? '-'}</td>
                    <td><StatusBadge status={tenant.status} /></td>
                    <td className="table-actions"><Link to={`/owner/tenants/${tenant.id}`}>View</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
