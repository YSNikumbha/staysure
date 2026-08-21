import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { notificationsApi } from '../api/operations.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDateTime, label } from '../utils/operations';

export default function NotificationsPage() {
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: ['notifications'], queryFn: notificationsApi.list });
  const markRead = useMutation({ mutationFn: (id: number) => notificationsApi.markRead(id), onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['notifications'] }); await queryClient.invalidateQueries({ queryKey: ['notifications-unread-count'] }); } });
  const markAll = useMutation({ mutationFn: notificationsApi.markAllRead, onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['notifications'] }); await queryClient.invalidateQueries({ queryKey: ['notifications-unread-count'] }); } });
  const notifications = query.data ?? [];
  const error = query.error ?? markRead.error ?? markAll.error;
  return (
    <div className="stack">
      <PageHeader eyebrow="Updates" title="Notifications" actions={<button className="secondary-button" type="button" onClick={() => markAll.mutate()} disabled={markAll.isPending || notifications.every((item) => item.readAt)}>Mark all read</button>} />
      <FormMessage message={error ? getApiErrorMessage(error, 'Unable to load notifications') : null} />
      <section className="surface">
        {query.isLoading ? <div className="route-state">Loading notifications</div> : null}
        {!query.isLoading && notifications.length === 0 ? <EmptyState title="No notifications yet." description="Operational updates will appear here." /> : null}
        <div className="timeline-list">
          {notifications.map((notification) => (
            <article className="timeline-item" key={notification.id}>
              <div>
                <strong>{notification.title}</strong>
                <p>{notification.message} · {formatDateTime(notification.createdAt)}</p>
                <p className="muted-copy">{label(notification.type)}</p>
              </div>
              <div className="action-row">
                <StatusBadge status={notification.readAt ? 'READ' : 'UNREAD'} />
                {!notification.readAt ? <button className="secondary-button compact-button" type="button" onClick={() => markRead.mutate(notification.id)}>Mark read</button> : null}
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
