import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { noticesApi } from '../api/operations.api';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, label } from '../utils/operations';

export default function NoticeDetailPage() {
  const { id } = useParams();
  const noticeId = Number(id);
  const query = useQuery({ queryKey: ['notice', noticeId], queryFn: () => noticesApi.get(noticeId), enabled: Number.isFinite(noticeId) });
  if (query.isLoading) return <div className="route-state">Loading notice</div>;
  if (query.isError || !query.data) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load notice')}</div>;
  const notice = query.data;
  return (
    <div className="stack">
      <PageHeader eyebrow={label(notice.noticeType)} title={notice.title} actions={<Link className="secondary-link" to="/notices">Back</Link>} />
      <section className="surface status-surface">
        <div>
          <p className="eyebrow">{notice.propertyName}</p>
          <h2>{notice.title}</h2>
          <p>Published {formatDate(notice.publishedAt)} · Expires {formatDate(notice.expiresAt)}</p>
        </div>
        <StatusBadge status={notice.priority} />
      </section>
      <section className="surface"><p className="muted-copy">{notice.content}</p></section>
    </div>
  );
}
