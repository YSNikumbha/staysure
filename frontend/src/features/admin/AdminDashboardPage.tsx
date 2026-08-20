import { useQuery } from '@tanstack/react-query';
import { Building2, CheckCircle2, ShieldCheck, UserCheck, UsersRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
import { StatusBadge } from '../../components/StatusBadge';
import type { OwnerProfile } from '../../types/owner';
import type { AdminPropertySummary } from '../../types/property';
import { getApiErrorMessage } from '../../utils/apiError';
import { AdminShell } from './AdminShell';

export function AdminDashboardPage() {
  const usersQuery = useQuery({ queryKey: ['admin-users'], queryFn: adminApi.users });
  const ownersQuery = useQuery({ queryKey: ['admin-owners', 'ALL'], queryFn: () => adminApi.owners('ALL') });
  const pendingOwnersQuery = useQuery({ queryKey: ['admin-owners', 'PENDING'], queryFn: adminApi.pendingOwners });
  const pgsQuery = useQuery({ queryKey: ['admin-pgs', false], queryFn: adminApi.pgs });
  const pendingPgsQuery = useQuery({ queryKey: ['admin-pgs', true], queryFn: adminApi.pendingPgs });

  const users = usersQuery.data ?? [];
  const owners = ownersQuery.data ?? [];
  const pendingOwners = pendingOwnersQuery.data ?? [];
  const pgs = pgsQuery.data ?? [];
  const pendingPgs = pendingPgsQuery.data ?? [];
  const verifiedPgs = pgs.filter((pg) => pg.verificationStatus === 'VERIFIED');
  const loading = usersQuery.isLoading || ownersQuery.isLoading || pendingOwnersQuery.isLoading || pgsQuery.isLoading || pendingPgsQuery.isLoading;
  const error = usersQuery.error ?? ownersQuery.error ?? pendingOwnersQuery.error ?? pgsQuery.error ?? pendingPgsQuery.error;

  return (
    <AdminShell title="Admin Dashboard" eyebrow="Administration">
      <div className="admin-stack">
        <section className="admin-hero-card">
          <div>
            <p className="eyebrow">Platform control</p>
            <h2>Review owners, PG verification and user access</h2>
            <p>Use real platform records to keep marketplace listings verified and safe.</p>
          </div>
          <ShieldCheck size={30} />
        </section>

        <FormMessage message={error ? getApiErrorMessage(error, 'Unable to load admin dashboard') : null} />

        <section className="admin-kpi-grid">
          <AdminKpi icon={<UsersRound size={20} />} label="Total Users" value={users.length} to="/admin/users" />
          <AdminKpi icon={<UserCheck size={20} />} label="PG Owners" value={owners.length} to="/admin/owners" />
          <AdminKpi icon={<ShieldCheck size={20} />} label="Pending Owner Applications" value={pendingOwners.length} to="/admin/owners?status=PENDING" />
          <AdminKpi icon={<Building2 size={20} />} label="Total PGs" value={pgs.length} to="/admin/pgs" />
          <AdminKpi icon={<ShieldCheck size={20} />} label="Pending PG Verifications" value={pendingPgs.length} to="/admin/pgs/pending" />
          <AdminKpi icon={<CheckCircle2 size={20} />} label="Verified PGs" value={verifiedPgs.length} to="/admin/pgs" />
        </section>

        {loading ? (
          <div className="admin-card-grid">
            {Array.from({ length: 3 }).map((_, index) => <div className="admin-skeleton-card" key={index} />)}
          </div>
        ) : null}

        <section className="admin-dashboard-columns">
          <AdminQueue
            title="Pending Owner Applications"
            emptyTitle="No owner applications are awaiting review."
            records={pendingOwners.slice(0, 5)}
            viewAllTo="/admin/owners?status=PENDING"
            render={(owner) => (
              <Link className="admin-list-row" to={`/admin/owners/${owner.id}`} key={owner.id}>
                <div>
                  <strong>{owner.businessName}</strong>
                  <span>{owner.user.firstName} {owner.user.lastName} · {owner.user.email}</span>
                </div>
                <StatusBadge status={owner.verificationStatus} />
              </Link>
            )}
          />

          <AdminQueue
            title="Pending PG Verifications"
            emptyTitle="No PGs are currently awaiting verification."
            records={pendingPgs.slice(0, 5)}
            viewAllTo="/admin/pgs/pending"
            render={(pg) => (
              <Link className="admin-list-row" to={`/admin/pgs/${pg.id}`} key={pg.id}>
                <div>
                  <strong>{pg.name}</strong>
                  <span>{pg.ownerName} · {pg.city}</span>
                </div>
                <StatusBadge status={pg.verificationStatus} />
              </Link>
            )}
          />
        </section>

        <section className="surface admin-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Recently verified</p>
              <h2>Verified PGs</h2>
            </div>
            <Link className="secondary-link compact-button" to="/admin/pgs">View all</Link>
          </div>
          {verifiedPgs.length === 0 ? (
            <EmptyState title="No verified PGs yet." description="Verified properties will appear here once admin review is complete." />
          ) : (
            <div className="admin-list admin-list--grid">
              {verifiedPgs.slice(0, 4).map((pg) => (
                <Link className="admin-list-row" to={`/admin/pgs/${pg.id}`} key={pg.id}>
                  <div>
                    <strong>{pg.name}</strong>
                    <span>{pg.ownerName} · {pg.city}</span>
                  </div>
                  <StatusBadge status={pg.status} />
                </Link>
              ))}
            </div>
          )}
        </section>
      </div>
    </AdminShell>
  );
}

function AdminKpi({ icon, label, value, to }: { icon: ReactNode; label: string; value: number; to: string }) {
  return (
    <Link className="admin-kpi-card" to={to}>
      <span>{icon}</span>
      <div>
        <strong>{value}</strong>
        <p>{label}</p>
      </div>
    </Link>
  );
}

function AdminQueue<T extends OwnerProfile | AdminPropertySummary>({
  title,
  emptyTitle,
  records,
  viewAllTo,
  render
}: {
  title: string;
  emptyTitle: string;
  records: T[];
  viewAllTo: string;
  render: (record: T) => ReactNode;
}) {
  return (
    <section className="surface admin-panel">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Review queue</p>
          <h2>{title}</h2>
        </div>
        <Link className="secondary-link compact-button" to={viewAllTo}>View all</Link>
      </div>
      {records.length === 0 ? (
        <EmptyState title={emptyTitle} />
      ) : (
        <div className="admin-list">{records.map((record) => render(record))}</div>
      )}
    </section>
  );
}
