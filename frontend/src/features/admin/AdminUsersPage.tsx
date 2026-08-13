import { useQuery } from '@tanstack/react-query';
import { adminApi } from '../../api/admin.api';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import { getApiErrorMessage } from '../../utils/apiError';

export function AdminUsersPage() {
  const usersQuery = useQuery({ queryKey: ['admin-users'], queryFn: adminApi.users });

  return (
    <div className="stack">
      <PageHeader eyebrow="Administration" title="Users" />
      <section className="surface">
        {usersQuery.isError ? <p>{getApiErrorMessage(usersQuery.error, 'Unable to load users')}</p> : null}
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Status</th>
                <th>Roles</th>
              </tr>
            </thead>
            <tbody>
              {(usersQuery.data ?? []).map((user) => (
                <tr key={user.id}>
                  <td>{user.firstName} {user.lastName}</td>
                  <td>{user.email}</td>
                  <td>{user.phone}</td>
                  <td><StatusBadge status={user.status} /></td>
                  <td>{user.roles.join(', ')}</td>
                </tr>
              ))}
              {!usersQuery.isLoading && (usersQuery.data ?? []).length === 0 ? (
                <tr>
                  <td colSpan={5}>No users found.</td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
