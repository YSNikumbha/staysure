import { useQuery } from '@tanstack/react-query';
import { Search, UsersRound } from 'lucide-react';
import { useMemo, useState } from 'react';
import { adminApi } from '../../api/admin.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
import { StatusBadge } from '../../components/StatusBadge';
import type { RoleName, User, UserStatus } from '../../types/user';
import { getApiErrorMessage } from '../../utils/apiError';
import { AdminShell } from './AdminShell';

const userStatuses: Array<UserStatus | 'ALL'> = ['ALL', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'ARCHIVED'];
const roles: Array<RoleName | 'ALL'> = ['ALL', 'USER', 'PG_OWNER', 'SUPER_ADMIN'];

export function AdminUsersPage() {
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState<UserStatus | 'ALL'>('ALL');
  const [role, setRole] = useState<RoleName | 'ALL'>('ALL');
  const usersQuery = useQuery({ queryKey: ['admin-users'], queryFn: adminApi.users });
  const users = usersQuery.data ?? [];
  const visibleUsers = useMemo(() => filterUsers(users, search, status, role), [users, search, status, role]);

  const clearFilters = () => {
    setSearch('');
    setStatus('ALL');
    setRole('ALL');
  };

  return (
    <AdminShell title="Users" eyebrow="Administration">
      <div className="admin-stack">
        <section className="admin-hero-card admin-hero-card--compact">
          <div>
            <p className="eyebrow">User management</p>
            <h2>Review platform users and role assignments</h2>
            <p>This view uses the existing admin user list and exposes only safe account information.</p>
          </div>
          <UsersRound size={28} />
        </section>

        <FormMessage message={usersQuery.isError ? getApiErrorMessage(usersQuery.error, 'Unable to load users') : null} />

        <section className="surface admin-panel">
          <div className="admin-toolbar">
            <div>
              <p className="eyebrow">Accounts</p>
              <h2>{visibleUsers.length} user{visibleUsers.length === 1 ? '' : 's'}</h2>
            </div>
          </div>

          <div className="admin-filter-row">
            <label className="admin-search-field">
              <Search size={17} />
              <span className="sr-only">Search users</span>
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search name, email or phone" />
            </label>
            <label>
              Role
              <select value={role} onChange={(event) => setRole(event.target.value as RoleName | 'ALL')}>
                {roles.map((item) => <option key={item} value={item}>{item === 'ALL' ? 'All roles' : item.replaceAll('_', ' ')}</option>)}
              </select>
            </label>
            <label>
              Status
              <select value={status} onChange={(event) => setStatus(event.target.value as UserStatus | 'ALL')}>
                {userStatuses.map((item) => <option key={item} value={item}>{item === 'ALL' ? 'All statuses' : item.replaceAll('_', ' ')}</option>)}
              </select>
            </label>
            <button className="secondary-button" type="button" onClick={clearFilters}>Clear Filters</button>
          </div>

          {usersQuery.isLoading ? (
            <div className="admin-card-grid">
              {Array.from({ length: 4 }).map((_, index) => <div className="admin-skeleton-card" key={index} />)}
            </div>
          ) : null}

          {!usersQuery.isLoading && visibleUsers.length === 0 ? (
            <EmptyState
              title="No users match your search."
              description="Clear filters or try another search term."
              action={<button className="secondary-button compact-button" type="button" onClick={clearFilters}>Clear Filters</button>}
            />
          ) : null}

          {visibleUsers.length > 0 ? (
            <div className="admin-table-card">
              <div className="table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Phone</th>
                      <th>Status</th>
                      <th>Roles</th>
                      <th>Email Verified</th>
                      <th>Phone Verified</th>
                    </tr>
                  </thead>
                  <tbody>
                    {visibleUsers.map((user) => (
                      <tr key={user.id}>
                        <td><strong>{user.firstName} {user.lastName}</strong></td>
                        <td>{user.email}</td>
                        <td>{user.phone}</td>
                        <td><StatusBadge status={user.status} /></td>
                        <td><div className="badge-row">{user.roles.map((item) => <StatusBadge status={item} key={item} />)}</div></td>
                        <td>{user.emailVerified ? 'Yes' : 'No'}</td>
                        <td>{user.phoneVerified ? 'Yes' : 'No'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : null}
        </section>
      </div>
    </AdminShell>
  );
}

function filterUsers(users: User[], search: string, status: UserStatus | 'ALL', role: RoleName | 'ALL') {
  const value = search.trim().toLowerCase();
  return users.filter((user) => {
    const matchesSearch = !value || [
      user.firstName,
      user.lastName,
      user.email,
      user.phone
    ].some((item) => item.toLowerCase().includes(value));
    const matchesStatus = status === 'ALL' || user.status === status;
    const matchesRole = role === 'ALL' || user.roles.includes(role);
    return matchesSearch && matchesStatus && matchesRole;
  });
}
