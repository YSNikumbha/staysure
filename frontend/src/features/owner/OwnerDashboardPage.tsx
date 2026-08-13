import { useQuery } from '@tanstack/react-query';
import { ownerApi } from '../../api/owner.api';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import { getApiErrorMessage } from '../../utils/apiError';
import { OwnerShell } from './OwnerShell';

export function OwnerDashboardPage() {
  const dashboardQuery = useQuery({
    queryKey: ['owner-dashboard'],
    queryFn: ownerApi.dashboard
  });

  if (dashboardQuery.isLoading) {
    return <div className="route-state">Loading</div>;
  }

  if (dashboardQuery.isError) {
    return <div className="route-state">{getApiErrorMessage(dashboardQuery.error, 'Unable to load owner dashboard')}</div>;
  }

  const dashboard = dashboardQuery.data;

  return (
    <OwnerShell>
      <div className="stack">
        <PageHeader eyebrow="Owner workspace" title="Owner Dashboard" />
        <section className="surface status-surface">
          <div>
            <h2>{dashboard?.businessName}</h2>
            <p>Owner ID: {dashboard?.ownerId}</p>
          </div>
          {dashboard ? <StatusBadge status={dashboard.verificationStatus} /> : null}
        </section>

        <section className="owner-metric-grid">
          <div className="metric-tile">
            <span>Total PGs</span>
            <strong>{dashboard?.totalPgs ?? 0}</strong>
          </div>
          <div className="metric-tile">
            <span>Active PGs</span>
            <strong>{dashboard?.activePgs ?? 0}</strong>
          </div>
          <div className="metric-tile">
            <span>Total Rooms</span>
            <strong>{dashboard?.totalRooms ?? 0}</strong>
          </div>
          <div className="metric-tile">
            <span>Total Beds</span>
            <strong>{dashboard?.totalBeds ?? 0}</strong>
          </div>
          <div className="metric-tile">
            <span>Available Beds</span>
            <strong>{dashboard?.availableBeds ?? 0}</strong>
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}
