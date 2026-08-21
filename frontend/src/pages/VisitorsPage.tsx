import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { visitorsApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, label } from '../utils/operations';

export default function VisitorsPage() {
  const query = useQuery({ queryKey: ['visitors'], queryFn: visitorsApi.list });
  const visitors = query.data ?? [];
  return (
    <div className="stack">
      <PageHeader eyebrow="My PG" title="Visitors" actions={<Link className="primary-link" to="/visitors/new">Request Visitor</Link>} />
      <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load visitors') : null} />
      <section className="surface">
        {query.isLoading ? <div className="route-state">Loading visitors</div> : null}
        {!query.isLoading && visitors.length === 0 ? <EmptyState title="No visitor requests yet." description="Request approval before visitors arrive at your PG." /> : null}
        {visitors.length > 0 ? (
          <div className="table-wrap"><table><thead><tr><th>Visitor</th><th>Visit Date</th><th>Expected Time</th><th>Purpose</th><th>Status</th><th className="table-actions">Action</th></tr></thead><tbody>
            {visitors.map((visitor) => <tr key={visitor.id}><td>{visitor.visitorName}<br /><span className="muted-copy">{visitor.relationship}</span></td><td>{formatDate(visitor.visitDate)}</td><td>{visitor.expectedArrivalTime} - {visitor.expectedDepartureTime}</td><td>{visitor.purpose}</td><td><StatusBadge status={visitor.status} /></td><td className="table-actions"><Link to={`/visitors/${visitor.id}`}>View</Link></td></tr>)}
          </tbody></table></div>
        ) : null}
      </section>
    </div>
  );
}
