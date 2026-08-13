import { useQuery } from '@tanstack/react-query';
import { Eye } from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import type { OwnerVerificationStatus } from '../../types/owner';
import { getApiErrorMessage } from '../../utils/apiError';

const statuses: Array<OwnerVerificationStatus | 'ALL'> = ['ALL', 'PENDING', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED', 'SUSPENDED'];

export function AdminOwnersPage() {
  const [searchParams] = useSearchParams();
  const status = searchParams.get('status') as OwnerVerificationStatus | 'ALL' | null;
  const selectedStatus = statuses.includes(status ?? 'ALL') ? status ?? 'ALL' : 'ALL';
  const ownersQuery = useQuery({
    queryKey: ['admin-owners', selectedStatus],
    queryFn: () => adminApi.owners(selectedStatus)
  });

  return (
    <div className="stack">
      <PageHeader eyebrow="Administration" title="Owner Applications" />
      <section className="surface">
        <div className="segmented">
          {statuses.map((item) => (
            <Link
              className={selectedStatus === item ? 'active' : ''}
              key={item}
              to={item === 'ALL' ? '/admin/owners' : `/admin/owners?status=${item}`}
            >
              {item.replaceAll('_', ' ')}
            </Link>
          ))}
        </div>
        {ownersQuery.isError ? <p>{getApiErrorMessage(ownersQuery.error, 'Unable to load owner applications')}</p> : null}
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Business</th>
                <th>Applicant</th>
                <th>Email</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {(ownersQuery.data ?? []).map((owner) => (
                <tr key={owner.id}>
                  <td>{owner.businessName}</td>
                  <td>{owner.user.firstName} {owner.user.lastName}</td>
                  <td>{owner.user.email}</td>
                  <td><StatusBadge status={owner.verificationStatus} /></td>
                  <td className="table-actions">
                    <Link className="icon-link" to={`/admin/owners/${owner.id}`} title="Open owner application" aria-label="Open owner application">
                      <Eye size={16} />
                    </Link>
                  </td>
                </tr>
              ))}
              {!ownersQuery.isLoading && (ownersQuery.data ?? []).length === 0 ? (
                <tr>
                  <td colSpan={5}>No owner applications found.</td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
