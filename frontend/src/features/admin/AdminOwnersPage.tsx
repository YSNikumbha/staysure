import { useQuery } from '@tanstack/react-query';
import { Eye, Search, UserCheck } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
import { StatusBadge } from '../../components/StatusBadge';
import type { OwnerProfile, OwnerVerificationStatus } from '../../types/owner';
import { getApiErrorMessage } from '../../utils/apiError';
import { AdminShell } from './AdminShell';

const statuses: Array<OwnerVerificationStatus | 'ALL'> = ['ALL', 'PENDING', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED', 'SUSPENDED'];

export function AdminOwnersPage() {
  const [searchParams] = useSearchParams();
  const [search, setSearch] = useState('');
  const status = searchParams.get('status') as OwnerVerificationStatus | 'ALL' | null;
  const selectedStatus = statuses.includes(status ?? 'ALL') ? status ?? 'ALL' : 'ALL';
  const ownersQuery = useQuery({
    queryKey: ['admin-owners', selectedStatus],
    queryFn: () => adminApi.owners(selectedStatus)
  });
  const owners = ownersQuery.data ?? [];
  const visibleOwners = useMemo(() => filterOwners(owners, search), [owners, search]);

  return (
    <AdminShell title="Owner Applications" eyebrow="Administration">
      <div className="admin-stack">
        <section className="admin-hero-card admin-hero-card--compact">
          <div>
            <p className="eyebrow">Owner verification</p>
            <h2>Review PG owner applications</h2>
            <p>Validate business details and submitted documents before granting owner access.</p>
          </div>
          <UserCheck size={28} />
        </section>

        <FormMessage message={ownersQuery.isError ? getApiErrorMessage(ownersQuery.error, 'Unable to load owner applications') : null} />

        <section className="surface admin-panel">
          <div className="admin-toolbar">
            <div>
              <p className="eyebrow">Applications</p>
              <h2>{visibleOwners.length} record{visibleOwners.length === 1 ? '' : 's'}</h2>
            </div>
            <label className="admin-search-field">
              <Search size={17} />
              <span className="sr-only">Search owner applications</span>
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search applicant, business, email or phone" />
            </label>
          </div>

          <div className="segmented segmented--wrap">
            {statuses.map((item) => (
              <Link
                className={selectedStatus === item ? 'active' : ''}
                key={item}
                to={item === 'ALL' ? '/admin/owners' : `/admin/owners?status=${item}`}
              >
                {item === 'ALL' ? 'All' : item.replaceAll('_', ' ')}
              </Link>
            ))}
          </div>

          {ownersQuery.isLoading ? (
            <div className="admin-card-grid">
              {Array.from({ length: 4 }).map((_, index) => <div className="admin-skeleton-card" key={index} />)}
            </div>
          ) : null}

          {!ownersQuery.isLoading && visibleOwners.length === 0 ? (
            <EmptyState
              title="No owner applications match the selected filters."
              description="Clear the search term or choose another status."
              action={search ? <button className="secondary-button compact-button" type="button" onClick={() => setSearch('')}>Clear Search</button> : null}
            />
          ) : null}

          {visibleOwners.length > 0 ? (
            <div className="admin-table-card">
              <div className="table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Business</th>
                      <th>Applicant</th>
                      <th>Contact</th>
                      <th>Submitted</th>
                      <th>Status</th>
                      <th className="table-actions">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {visibleOwners.map((owner) => (
                      <tr key={owner.id}>
                        <td><strong>{owner.businessName}</strong></td>
                        <td>{owner.user.firstName} {owner.user.lastName}</td>
                        <td>{owner.user.email}<br /><span className="muted-copy">{owner.user.phone}</span></td>
                        <td>{new Date(owner.createdAt).toLocaleDateString()}</td>
                        <td><StatusBadge status={owner.verificationStatus} /></td>
                        <td className="table-actions">
                          <Link className="icon-link" to={`/admin/owners/${owner.id}`} title="Review owner application" aria-label={`Review ${owner.businessName}`}>
                            <Eye size={16} />
                          </Link>
                        </td>
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

function filterOwners(owners: OwnerProfile[], search: string) {
  const value = search.trim().toLowerCase();
  if (!value) {
    return owners;
  }
  return owners.filter((owner) => [
    owner.businessName,
    owner.user.firstName,
    owner.user.lastName,
    owner.user.email,
    owner.user.phone,
    owner.businessEmail ?? '',
    owner.alternatePhone ?? ''
  ].some((item) => item.toLowerCase().includes(value)));
}
