import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { PageHeader } from '../../components/PageHeader';

export function AdminDashboardPage() {
  const usersQuery = useQuery({ queryKey: ['admin-users'], queryFn: adminApi.users });
  const ownersQuery = useQuery({ queryKey: ['admin-owners', 'PENDING'], queryFn: adminApi.pendingOwners });

  return (
    <div className="stack">
      <PageHeader eyebrow="Administration" title="Admin Dashboard" />
      <section className="metric-grid">
        <Link className="metric-tile" to="/admin/users">
          <span>Users</span>
          <strong>{usersQuery.data?.length ?? 0}</strong>
        </Link>
        <Link className="metric-tile" to="/admin/owners">
          <span>Pending owners</span>
          <strong>{ownersQuery.data?.length ?? 0}</strong>
        </Link>
      </section>
    </div>
  );
}
