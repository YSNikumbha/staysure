import { useQuery } from '@tanstack/react-query';
import { Building2, Eye, Search } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
import { StatusBadge } from '../../components/StatusBadge';
import type { AdminPropertySummary, PropertyVerificationStatus } from '../../types/property';
import { getApiErrorMessage } from '../../utils/apiError';
import { AdminShell } from './AdminShell';

type AdminPgsPageProps = {
  pendingOnly?: boolean;
};

const verificationStatuses: Array<PropertyVerificationStatus | 'ALL'> = ['ALL', 'PENDING', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED', 'CHANGES_REQUESTED', 'NOT_SUBMITTED'];

export function AdminPgsPage({ pendingOnly = false }: AdminPgsPageProps) {
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState<PropertyVerificationStatus | 'ALL'>(pendingOnly ? 'ALL' : 'ALL');
  const [city, setCity] = useState('ALL');
  const pgsQuery = useQuery({
    queryKey: ['admin-pgs', pendingOnly],
    queryFn: pendingOnly ? adminApi.pendingPgs : adminApi.pgs
  });
  const pgs = pgsQuery.data ?? [];
  const cities = useMemo(() => [...new Set(pgs.map((pg) => pg.city).filter(Boolean))].sort(), [pgs]);
  const visiblePgs = useMemo(() => filterPgs(pgs, search, status, city), [pgs, search, status, city]);

  const clearFilters = () => {
    setSearch('');
    setStatus('ALL');
    setCity('ALL');
  };

  return (
    <AdminShell title={pendingOnly ? 'Pending PG Verification' : 'PG Verification'} eyebrow="Administration">
      <div className="admin-stack">
        <section className="admin-hero-card admin-hero-card--compact">
          <div>
            <p className="eyebrow">Property review</p>
            <h2>Review properties submitted by PG owners</h2>
            <p>Inspect listing details, rooms, beds, amenities, gallery and rules before verification.</p>
          </div>
          <Building2 size={28} />
        </section>

        <FormMessage message={pgsQuery.isError ? getApiErrorMessage(pgsQuery.error, 'Unable to load PGs') : null} />

        <section className="surface admin-panel">
          <div className="admin-toolbar">
            <div>
              <p className="eyebrow">PG records</p>
              <h2>{visiblePgs.length} property record{visiblePgs.length === 1 ? '' : 's'}</h2>
            </div>
            <div className="segmented segmented--wrap">
              <Link className={!pendingOnly ? 'active' : ''} to="/admin/pgs">All PGs</Link>
              <Link className={pendingOnly ? 'active' : ''} to="/admin/pgs/pending">Pending</Link>
            </div>
          </div>

          <div className="admin-filter-row">
            <label className="admin-search-field">
              <Search size={17} />
              <span className="sr-only">Search PG verification records</span>
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search PG, owner or city" />
            </label>
            <label>
              Status
              <select value={status} onChange={(event) => setStatus(event.target.value as PropertyVerificationStatus | 'ALL')}>
                {verificationStatuses.map((item) => <option key={item} value={item}>{item === 'ALL' ? 'All statuses' : item.replaceAll('_', ' ')}</option>)}
              </select>
            </label>
            <label>
              City
              <select value={city} onChange={(event) => setCity(event.target.value)}>
                <option value="ALL">All cities</option>
                {cities.map((item) => <option key={item} value={item}>{item}</option>)}
              </select>
            </label>
            <button className="secondary-button" type="button" onClick={clearFilters}>Clear Filters</button>
          </div>

          {pgsQuery.isLoading ? (
            <div className="admin-card-grid">
              {Array.from({ length: 4 }).map((_, index) => <div className="admin-skeleton-card" key={index} />)}
            </div>
          ) : null}

          {!pgsQuery.isLoading && visiblePgs.length === 0 ? (
            <EmptyState
              title={pendingOnly ? 'No PGs are currently awaiting verification.' : 'No PGs match the selected filters.'}
              description="Try changing the status, city or search term."
              action={<button className="secondary-button compact-button" type="button" onClick={clearFilters}>Clear Filters</button>}
            />
          ) : null}

          {visiblePgs.length > 0 ? (
            <div className="admin-table-card">
              <div className="table-wrap">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>PG</th>
                      <th>Owner</th>
                      <th>Location</th>
                      <th>Submitted</th>
                      <th>Verification</th>
                      <th>Status</th>
                      <th>Rooms</th>
                      <th>Beds</th>
                      <th className="table-actions">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {visiblePgs.map((pg) => (
                      <tr key={pg.id}>
                        <td><strong>{pg.name}</strong></td>
                        <td>{pg.ownerName}</td>
                        <td>{pg.city}</td>
                        <td>{pg.submittedForVerificationAt ? new Date(pg.submittedForVerificationAt).toLocaleDateString() : 'Not submitted'}</td>
                        <td><StatusBadge status={pg.verificationStatus} /></td>
                        <td><StatusBadge status={pg.status} /></td>
                        <td>{pg.roomCount}</td>
                        <td>{pg.bedCount}</td>
                        <td className="table-actions">
                          <Link className="icon-link" to={`/admin/pgs/${pg.id}`} title="Review PG" aria-label={`Review ${pg.name}`}>
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

function filterPgs(
  pgs: AdminPropertySummary[],
  search: string,
  status: PropertyVerificationStatus | 'ALL',
  city: string
) {
  const value = search.trim().toLowerCase();
  return pgs.filter((pg) => {
    const matchesSearch = !value || [pg.name, pg.ownerName, pg.city].some((item) => item.toLowerCase().includes(value));
    const matchesStatus = status === 'ALL' || pg.verificationStatus === status;
    const matchesCity = city === 'ALL' || pg.city === city;
    return matchesSearch && matchesStatus && matchesCity;
  });
}
