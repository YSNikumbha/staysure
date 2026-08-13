import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Archive, Eye, Pencil, Settings } from 'lucide-react';
import { Link } from 'react-router-dom';
import { propertyApi } from '../../api/property.api';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import { getApiErrorMessage } from '../../utils/apiError';
import { toAssetUrl } from '../../utils/assets';
import { OwnerShell } from './OwnerShell';

export function OwnerPgsPage() {
  const queryClient = useQueryClient();
  const pgsQuery = useQuery({
    queryKey: ['owner-pgs'],
    queryFn: propertyApi.listProperties
  });

  const archiveMutation = useMutation({
    mutationFn: propertyApi.archiveProperty,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['owner-pgs'] });
      await queryClient.invalidateQueries({ queryKey: ['owner-dashboard'] });
    }
  });

  const confirmArchive = (id: number, name: string) => {
    if (window.confirm(`Archive ${name}?`)) {
      archiveMutation.mutate(id);
    }
  };

  return (
    <OwnerShell>
      <div className="stack">
        <PageHeader
          eyebrow="Property inventory"
          title="My PGs"
          actions={<Link className="primary-link" to="/owner/pgs/new">Create PG</Link>}
        />
        <section className="surface">
          {pgsQuery.isError ? <p>{getApiErrorMessage(pgsQuery.error, 'Unable to load PGs')}</p> : null}
          {archiveMutation.isError ? <p className="form-message form-message--error">{getApiErrorMessage(archiveMutation.error, 'Unable to archive PG')}</p> : null}
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Cover</th>
                  <th>PG Name</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th>Verification</th>
                  <th>Rooms</th>
                  <th>Beds</th>
                  <th>Available</th>
                  <th>Created</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {(pgsQuery.data ?? []).map((pg) => (
                  <tr key={pg.id}>
                    <td>
                      {pg.coverImageUrl ? (
                        <img className="property-thumb" src={toAssetUrl(pg.coverImageUrl)} alt={pg.name} />
                      ) : (
                        <span className="property-thumb property-thumb--empty">PG</span>
                      )}
                    </td>
                    <td><Link to={`/owner/pgs/${pg.id}`}>{pg.name}</Link></td>
                    <td>{pg.area}, {pg.city}</td>
                    <td><StatusBadge status={pg.status} /></td>
                    <td><StatusBadge status={pg.verificationStatus} /></td>
                    <td>{pg.roomCount}</td>
                    <td>{pg.bedCount}</td>
                    <td>{pg.availableBedCount}</td>
                    <td>{new Date(pg.createdAt).toLocaleDateString()}</td>
                    <td className="table-actions">
                      <Link className="icon-link" to={`/owner/pgs/${pg.id}`} title="View" aria-label="View PG">
                        <Eye size={16} />
                      </Link>
                      <Link className="icon-link" to={`/owner/pgs/${pg.id}/edit`} title="Edit" aria-label="Edit PG">
                        <Pencil size={16} />
                      </Link>
                      <Link className="icon-link" to={`/owner/pgs/${pg.id}/floors`} title="Manage" aria-label="Manage PG">
                        <Settings size={16} />
                      </Link>
                      <button
                        className="icon-button"
                        type="button"
                        title="Archive"
                        aria-label="Archive PG"
                        onClick={() => confirmArchive(pg.id, pg.name)}
                        disabled={archiveMutation.isPending}
                      >
                        <Archive size={16} />
                      </button>
                    </td>
                  </tr>
                ))}
                {!pgsQuery.isLoading && (pgsQuery.data ?? []).length === 0 ? (
                  <tr>
                    <td colSpan={10}>No PGs created yet.</td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}
