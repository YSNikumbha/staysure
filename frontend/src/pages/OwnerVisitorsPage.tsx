import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ownerVisitorsApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate } from '../utils/operations';

export default function OwnerVisitorsPage() {
  const query = useQuery({ queryKey: ['owner-visitors'], queryFn: ownerVisitorsApi.list });
  const visitors = query.data ?? [];
  return (
    <OwnerShell title="Visitors" eyebrow="Operations">
      <div className="owner-stack">
        <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load visitors') : null} />
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Visitor requests</p><h2>{visitors.length} visitor request{visitors.length === 1 ? '' : 's'}</h2></div></div>
          {query.isLoading ? <div className="owner-skeleton-card" /> : null}
          {!query.isLoading && visitors.length === 0 ? <EmptyState title="No visitor requests yet." /> : null}
          {visitors.length > 0 ? <div className="table-wrap"><table><thead><tr><th>Visitor</th><th>Tenant</th><th>PG</th><th>Visit Date</th><th>Expected Time</th><th>Status</th><th className="table-actions">Action</th></tr></thead><tbody>
            {visitors.map((visitor) => <tr key={visitor.id}><td>{visitor.visitorName}<br /><span className="muted-copy">{visitor.visitorPhone}</span></td><td>{visitor.tenantName}</td><td>{visitor.propertyName}</td><td>{formatDate(visitor.visitDate)}</td><td>{visitor.expectedArrivalTime} - {visitor.expectedDepartureTime}</td><td><StatusBadge status={visitor.status} /></td><td className="table-actions"><Link to={`/owner/visitors/${visitor.id}`}>Manage</Link></td></tr>)}
          </tbody></table></div> : null}
        </section>
      </div>
    </OwnerShell>
  );
}
