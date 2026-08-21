import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { complaintsApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, label } from '../utils/operations';

export default function ComplaintsPage() {
  const query = useQuery({ queryKey: ['complaints'], queryFn: complaintsApi.list });
  const complaints = query.data ?? [];

  return (
    <div className="stack">
      <PageHeader eyebrow="My stay" title="Complaints" actions={<Link className="primary-link" to="/complaints/new">Raise Complaint</Link>} />
      <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load complaints') : null} />
      <section className="surface">
        {query.isLoading ? <div className="route-state">Loading complaints</div> : null}
        {!query.isLoading && complaints.length === 0 ? (
          <EmptyState title="No complaints raised yet." description="Raise a complaint for room, food, internet, cleaning or maintenance issues." />
        ) : null}
        {complaints.length > 0 ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Complaint</th>
                  <th>Category</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th className="table-actions">Action</th>
                </tr>
              </thead>
              <tbody>
                {complaints.map((complaint) => (
                  <tr key={complaint.id}>
                    <td>{complaint.complaintNumber}<br /><span className="muted-copy">{complaint.title}</span></td>
                    <td>{label(complaint.category)}</td>
                    <td><StatusBadge status={complaint.priority} /></td>
                    <td><StatusBadge status={complaint.status} /></td>
                    <td>{formatDate(complaint.createdAt)}</td>
                    <td className="table-actions"><Link to={`/complaints/${complaint.id}`}>View</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </div>
  );
}
