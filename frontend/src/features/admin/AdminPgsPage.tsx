import { useQuery } from '@tanstack/react-query';
import { Eye } from 'lucide-react';
import { Link } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import { getApiErrorMessage } from '../../utils/apiError';

type AdminPgsPageProps = {
  pendingOnly?: boolean;
};

export function AdminPgsPage({ pendingOnly = false }: AdminPgsPageProps) {
  const pgsQuery = useQuery({
    queryKey: ['admin-pgs', pendingOnly],
    queryFn: pendingOnly ? adminApi.pendingPgs : adminApi.pgs
  });

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Administration"
        title={pendingOnly ? 'Pending PG Verification' : 'PG Properties'}
        actions={
          <div className="segmented">
            <Link className={!pendingOnly ? 'active' : ''} to="/admin/pgs">All PGs</Link>
            <Link className={pendingOnly ? 'active' : ''} to="/admin/pgs/pending">Pending</Link>
          </div>
        }
      />
      <section className="surface">
        {pgsQuery.isError ? <p>{getApiErrorMessage(pgsQuery.error, 'Unable to load PGs')}</p> : null}
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>PG Name</th>
                <th>Owner</th>
                <th>City</th>
                <th>Submitted</th>
                <th>Verification</th>
                <th>Status</th>
                <th>Rooms</th>
                <th>Beds</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {(pgsQuery.data ?? []).map((pg) => (
                <tr key={pg.id}>
                  <td>{pg.name}</td>
                  <td>{pg.ownerName}</td>
                  <td>{pg.city}</td>
                  <td>{pg.submittedForVerificationAt ? new Date(pg.submittedForVerificationAt).toLocaleDateString() : 'Not submitted'}</td>
                  <td><StatusBadge status={pg.verificationStatus} /></td>
                  <td><StatusBadge status={pg.status} /></td>
                  <td>{pg.roomCount}</td>
                  <td>{pg.bedCount}</td>
                  <td className="table-actions">
                    <Link className="icon-link" to={`/admin/pgs/${pg.id}`} title="Review PG" aria-label="Review PG">
                      <Eye size={16} />
                    </Link>
                  </td>
                </tr>
              ))}
              {!pgsQuery.isLoading && (pgsQuery.data ?? []).length === 0 ? (
                <tr>
                  <td colSpan={9}>No PGs found.</td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
