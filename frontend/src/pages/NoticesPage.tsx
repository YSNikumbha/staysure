import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { noticesApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, label } from '../utils/operations';

export default function NoticesPage() {
  const query = useQuery({ queryKey: ['notices'], queryFn: noticesApi.list });
  const notices = query.data ?? [];
  return (
    <div className="stack">
      <PageHeader eyebrow="My PG" title="Notices" />
      <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load notices') : null} />
      <section className="surface">
        {query.isLoading ? <div className="route-state">Loading notices</div> : null}
        {!query.isLoading && notices.length === 0 ? <EmptyState title="No notices are currently available." description="Published PG notices will appear here." /> : null}
        <div className="owner-card-grid">
          {notices.map((notice) => (
            <article className="owner-list-row" key={notice.id}>
              <div>
                <span>{label(notice.noticeType)} · {formatDate(notice.publishedAt)}</span>
                <strong>{notice.title}</strong>
                <p className="muted-copy">{notice.content.slice(0, 140)}{notice.content.length > 140 ? '...' : ''}</p>
              </div>
              <div className="action-row"><StatusBadge status={notice.priority} /><Link className="primary-link compact-button" to={`/notices/${notice.id}`}>Read</Link></div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
