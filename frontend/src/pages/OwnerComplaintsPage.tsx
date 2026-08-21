import { useQuery } from '@tanstack/react-query';
import { AlertCircle, CheckCircle2, ClipboardList, Wrench } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ownerComplaintsApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { Complaint, ComplaintStatus } from '../types/operations';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, label } from '../utils/operations';

export default function OwnerComplaintsPage() {
  const query = useQuery({ queryKey: ['owner-complaints'], queryFn: ownerComplaintsApi.list });
  const complaints = query.data ?? [];

  return (
    <OwnerShell title="Complaints" eyebrow="Operations" actions={<Link className="secondary-link compact-button" to="/owner/maintenance">Maintenance</Link>}>
      <div className="owner-stack">
        <section className="owner-kpi-grid owner-kpi-grid--tight">
          <Metric icon={<AlertCircle size={18} />} label="Open" value={count(complaints, 'OPEN')} />
          <Metric icon={<Wrench size={18} />} label="In Progress" value={count(complaints, 'IN_PROGRESS')} />
          <Metric icon={<CheckCircle2 size={18} />} label="Resolved" value={count(complaints, 'RESOLVED')} />
          <Metric icon={<ClipboardList size={18} />} label="Total" value={complaints.length} />
        </section>
        <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load complaints') : null} />
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">Tenant issues</p><h2>{complaints.length} complaint{complaints.length === 1 ? '' : 's'}</h2></div></div>
          {query.isLoading ? <div className="owner-skeleton-card" /> : null}
          {!query.isLoading && complaints.length === 0 ? <EmptyState title="No complaints yet." description="Tenant complaints will appear here after active residents submit them." /> : null}
          {complaints.length > 0 ? (
            <div className="table-wrap">
              <table>
                <thead><tr><th>Complaint</th><th>Tenant</th><th>PG</th><th>Room</th><th>Category</th><th>Priority</th><th>Status</th><th>Created</th><th className="table-actions">Action</th></tr></thead>
                <tbody>
                  {complaints.map((complaint) => (
                    <tr key={complaint.id}>
                      <td>{complaint.complaintNumber}<br /><span className="muted-copy">{complaint.title}</span></td>
                      <td>{complaint.tenantName}</td>
                      <td>{complaint.propertyName}</td>
                      <td>{complaint.roomNumber ?? '-'}</td>
                      <td>{label(complaint.category)}</td>
                      <td><StatusBadge status={complaint.priority} /></td>
                      <td><StatusBadge status={complaint.status} /></td>
                      <td>{formatDate(complaint.createdAt)}</td>
                      <td className="table-actions"><Link to={`/owner/complaints/${complaint.id}`}>Review</Link></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>
      </div>
    </OwnerShell>
  );
}

function Metric({ icon, label: metricLabel, value }: { icon: ReactNode; label: string; value: number }) {
  return <article className="owner-kpi-card"><span>{icon}</span><div><strong>{value}</strong><p>{metricLabel}</p></div></article>;
}

function count(complaints: Complaint[], status: ComplaintStatus) {
  return complaints.filter((complaint) => complaint.status === status).length;
}
