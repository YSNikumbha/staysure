import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ownerNoticesApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, label } from '../utils/operations';

export default function OwnerNoticesPage() {
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['owner-notices'], queryFn: ownerNoticesApi.list });
  const publish = useMutation({ mutationFn: (id: number) => ownerNoticesApi.publish(id), onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['owner-notices'] }) });
  const archive = useMutation({ mutationFn: (id: number) => ownerNoticesApi.archive(id), onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['owner-notices'] }) });
  const notices = query.data ?? [];
  const error = query.error ?? publish.error ?? archive.error;
  return (
    <OwnerShell title="Notices" eyebrow="Operations" actions={<Link className="primary-link compact-button" to="/owner/notices/new">New Notice</Link>}>
      <div className="owner-stack">
        <FormMessage message={error ? getApiErrorMessage(error, 'Unable to load notices') : null} />
        <section className="surface owner-panel">
          <div className="section-heading"><div><p className="eyebrow">PG notices</p><h2>{notices.length} notice{notices.length === 1 ? '' : 's'}</h2></div></div>
          {query.isLoading ? <div className="owner-skeleton-card" /> : null}
          {!query.isLoading && notices.length === 0 ? <EmptyState title="No notices yet." description="Create notices for active tenants of your PGs." /> : null}
          {notices.length > 0 ? (
            <div className="table-wrap"><table><thead><tr><th>Title</th><th>PG</th><th>Type</th><th>Priority</th><th>Status</th><th>Published</th><th>Expires</th><th className="table-actions">Actions</th></tr></thead><tbody>
              {notices.map((notice) => (
                <tr key={notice.id}><td>{notice.title}</td><td>{notice.propertyName}</td><td>{label(notice.noticeType)}</td><td><StatusBadge status={notice.priority} /></td><td><StatusBadge status={notice.status} /></td><td>{formatDate(notice.publishedAt)}</td><td>{formatDate(notice.expiresAt)}</td><td className="table-actions"><div className="action-row"><Link to={`/owner/notices/${notice.id}/edit`}>Edit</Link>{notice.status !== 'PUBLISHED' && notice.status !== 'ARCHIVED' ? <button className="secondary-button compact-button" type="button" onClick={() => publish.mutate(notice.id)}>Publish</button> : null}{notice.status !== 'ARCHIVED' ? <button className="danger-button compact-button" type="button" onClick={() => archive.mutate(notice.id)}>Archive</button> : null}</div></td></tr>
              ))}
            </tbody></table></div>
          ) : null}
        </section>
      </div>
    </OwnerShell>
  );
}
