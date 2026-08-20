import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Archive, BedDouble, Building2, CheckCircle2, Eye, Pencil, Plus, Settings } from 'lucide-react';
import type { ReactNode } from 'react';
import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { propertyApi } from '../../api/property.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
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

  const pgs = pgsQuery.data ?? [];
  const metrics = useMemo(() => ({
    total: pgs.length,
    active: pgs.filter((pg) => pg.status === 'ACTIVE').length,
    verified: pgs.filter((pg) => pg.verificationStatus === 'VERIFIED').length,
    availableBeds: pgs.reduce((sum, pg) => sum + pg.availableBedCount, 0)
  }), [pgs]);

  return (
    <OwnerShell
      title="My PGs"
      eyebrow="Property inventory"
      actions={<Link className="primary-link" to="/owner/pgs/new"><Plus size={17} />Create PG</Link>}
    >
      <div className="owner-stack">
        <section className="owner-hero-card owner-hero-card--compact">
          <div>
            <p className="eyebrow">Listings</p>
            <h2>Build and manage your PG inventory</h2>
            <p>Keep listing status, verification, rooms, beds, amenities, gallery and rules in sync before publishing.</p>
          </div>
          <Link className="secondary-link compact-button" to="/owner/dashboard">Dashboard</Link>
        </section>

        <section className="owner-kpi-grid owner-kpi-grid--tight">
          <PropertyMetric icon={<Building2 size={19} />} label="Total PGs" value={metrics.total} />
          <PropertyMetric icon={<CheckCircle2 size={19} />} label="Active" value={metrics.active} />
          <PropertyMetric icon={<CheckCircle2 size={19} />} label="Verified" value={metrics.verified} />
          <PropertyMetric icon={<BedDouble size={19} />} label="Available Beds" value={metrics.availableBeds} />
        </section>

        <FormMessage message={pgsQuery.isError ? getApiErrorMessage(pgsQuery.error, 'Unable to load PGs') : null} />
        <FormMessage message={archiveMutation.isError ? getApiErrorMessage(archiveMutation.error, 'Unable to archive PG') : null} />

        <section className="surface owner-panel">
          {pgsQuery.isLoading ? (
            <div className="owner-card-grid">
              {Array.from({ length: 3 }).map((_, index) => <div className="owner-skeleton-card" key={index} />)}
            </div>
          ) : null}

          {!pgsQuery.isLoading && pgs.length === 0 ? (
            <EmptyState
              title="No PGs created yet."
              description="Create your first PG, add floors, rooms, beds and a gallery, then submit it for verification."
              action={<Link className="primary-link" to="/owner/pgs/new">Create PG</Link>}
            />
          ) : null}

          {pgs.length > 0 ? (
            <div className="owner-property-grid">
              {pgs.map((pg) => (
                <article className="owner-property-card" key={pg.id}>
                  <Link className="owner-property-media" to={`/owner/pgs/${pg.id}`}>
                    {pg.coverImageUrl ? (
                      <img src={toAssetUrl(pg.coverImageUrl)} alt={pg.name} />
                    ) : (
                      <span>PG</span>
                    )}
                  </Link>
                  <div className="owner-property-body">
                    <div className="owner-property-title">
                      <div>
                        <h2>{pg.name}</h2>
                        <p>{pg.area}, {pg.city}</p>
                      </div>
                      <span className="badge-row">
                        <StatusBadge status={pg.status} />
                        <StatusBadge status={pg.verificationStatus} />
                      </span>
                    </div>
                    <div className="owner-property-stats">
                      <span><strong>{pg.roomCount}</strong> Rooms</span>
                      <span><strong>{pg.bedCount}</strong> Beds</span>
                      <span><strong>{pg.availableBedCount}</strong> Available</span>
                    </div>
                    <div className="owner-property-footer">
                      <span>Created {new Date(pg.createdAt).toLocaleDateString()}</span>
                      <div className="table-actions">
                        <Link className="icon-link" to={`/owner/pgs/${pg.id}`} title="View" aria-label={`View ${pg.name}`}>
                          <Eye size={16} />
                        </Link>
                        <Link className="icon-link" to={`/owner/pgs/${pg.id}/edit`} title="Edit" aria-label={`Edit ${pg.name}`}>
                          <Pencil size={16} />
                        </Link>
                        <Link className="icon-link" to={`/owner/pgs/${pg.id}/floors`} title="Manage inventory" aria-label={`Manage ${pg.name}`}>
                          <Settings size={16} />
                        </Link>
                        <button
                          className="icon-button"
                          type="button"
                          title="Archive"
                          aria-label={`Archive ${pg.name}`}
                          onClick={() => confirmArchive(pg.id, pg.name)}
                          disabled={archiveMutation.isPending}
                        >
                          <Archive size={16} />
                        </button>
                      </div>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          ) : null}
        </section>
      </div>
    </OwnerShell>
  );
}

function PropertyMetric({ icon, label, value }: { icon: ReactNode; label: string; value: number }) {
  return (
    <article className="owner-kpi-card">
      <span>{icon}</span>
      <div>
        <strong>{value}</strong>
        <p>{label}</p>
      </div>
    </article>
  );
}
