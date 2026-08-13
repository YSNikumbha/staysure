import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Ban, CheckCircle2, Eye, MessageSquareWarning } from 'lucide-react';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { FormMessage } from '../../components/FormMessage';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import { getApiErrorMessage } from '../../utils/apiError';
import { toAssetUrl } from '../../utils/assets';

export function AdminPgDetailPage() {
  const { id } = useParams();
  const pgId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);

  const pgQuery = useQuery({
    queryKey: ['admin-pg', pgId],
    queryFn: () => adminApi.pg(pgId),
    enabled: Number.isFinite(pgId)
  });

  const invalidate = async () => {
    setActionError(null);
    await queryClient.invalidateQueries({ queryKey: ['admin-pg', pgId] });
    await queryClient.invalidateQueries({ queryKey: ['admin-pgs'] });
  };

  const startReview = useMutation({
    mutationFn: () => adminApi.startPgReview(pgId, remarks || undefined),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to start review'))
  });

  const verify = useMutation({
    mutationFn: () => adminApi.verifyPg(pgId, remarks || undefined),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to verify PG'))
  });

  const reject = useMutation({
    mutationFn: () => adminApi.rejectPg(pgId, remarks),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to reject PG'))
  });

  const requestChanges = useMutation({
    mutationFn: () => adminApi.requestPgChanges(pgId, remarks),
    onSuccess: invalidate,
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to request changes'))
  });

  if (pgQuery.isLoading) {
    return <div className="route-state">Loading</div>;
  }

  if (pgQuery.isError || !pgQuery.data) {
    return <div className="route-state">{getApiErrorMessage(pgQuery.error, 'Unable to load PG')}</div>;
  }

  const { owner, propertyDetails, verificationHistory } = pgQuery.data;
  const property = propertyDetails.property;
  const pendingAction = startReview.isPending || verify.isPending || reject.isPending || requestChanges.isPending;

  const requireRemarks = (action: () => void) => {
    if (!remarks.trim()) {
      setActionError('Remarks are required.');
      return;
    }
    action();
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="PG verification"
        title={property.name}
        actions={<Link className="secondary-link" to="/admin/pgs/pending">Back</Link>}
      />

      <section className="surface status-surface">
        <div>
          <h2>{owner.businessName}</h2>
          <p>{owner.user.firstName} {owner.user.lastName} · Owner verification {owner.verificationStatus}</p>
        </div>
        <div className="badge-row">
          <StatusBadge status={property.verificationStatus} />
          <StatusBadge status={property.status} />
        </div>
      </section>

      <section className="surface detail-grid">
        <div>
          <span>City</span>
          <strong>{property.city}</strong>
        </div>
        <div>
          <span>Area</span>
          <strong>{property.area}</strong>
        </div>
        <div>
          <span>Rooms</span>
          <strong>{propertyDetails.roomCount}</strong>
        </div>
        <div>
          <span>Available beds</span>
          <strong>{propertyDetails.availableBedCount}</strong>
        </div>
      </section>

      <section className="surface">
        <h2>Review Action</h2>
        <div className="form-grid">
          <label>
            Remarks
            <textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} />
          </label>
          <FormMessage message={actionError} />
          <div className="action-row">
            <button className="secondary-button" type="button" disabled={pendingAction} onClick={() => startReview.mutate()}>
              <Eye size={18} />
              Start Review
            </button>
            <button className="primary-button" type="button" disabled={pendingAction} onClick={() => verify.mutate()}>
              <CheckCircle2 size={18} />
              Verify
            </button>
            <button className="warning-button" type="button" disabled={pendingAction} onClick={() => requireRemarks(() => requestChanges.mutate())}>
              <MessageSquareWarning size={18} />
              Request Changes
            </button>
            <button className="danger-button" type="button" disabled={pendingAction} onClick={() => requireRemarks(() => reject.mutate())}>
              <Ban size={18} />
              Reject
            </button>
          </div>
        </div>
      </section>

      <section className="surface">
        <h2>Overview</h2>
        <p className="muted-copy">{property.description || 'No description provided.'}</p>
        <div className="detail-grid">
          <div>
            <span>Address</span>
            <strong>{property.addressLine1}, {property.area}, {property.city}, {property.state} {property.pincode}</strong>
          </div>
          <div>
            <span>Pricing</span>
            <strong>Rs {Number(property.startingRent).toLocaleString()} · Deposit Rs {Number(property.securityDeposit).toLocaleString()}</strong>
          </div>
          <div>
            <span>Gender</span>
            <strong>{property.genderType.replaceAll('_', ' ')}</strong>
          </div>
          <div>
            <span>Property type</span>
            <strong>{property.propertyType.replaceAll('_', ' ')}</strong>
          </div>
        </div>
      </section>

      <section className="surface">
        <h2>Gallery</h2>
        <div className="gallery-grid">
          {propertyDetails.images.map((image) => (
            <article className="gallery-item" key={image.id}>
              <img src={toAssetUrl(image.imageUrl)} alt={image.category.replaceAll('_', ' ')} />
              <div className="gallery-meta">
                <span>{image.category.replaceAll('_', ' ')}</span>
                {image.coverImage ? <StatusBadge status="COVER" /> : null}
              </div>
            </article>
          ))}
          {propertyDetails.images.length === 0 ? <p>No images uploaded.</p> : null}
        </div>
      </section>

      <section className="surface">
        <h2>Amenities</h2>
        <div className="amenity-grid">
          {propertyDetails.amenities.map((amenity) => <span className="amenity-pill" key={amenity.id}>{amenity.name}</span>)}
          {propertyDetails.amenities.length === 0 ? <p>No amenities selected.</p> : null}
        </div>
      </section>

      <section className="surface">
        <h2>Floor / Room / Bed Structure</h2>
        <div className="inventory-tree">
          {propertyDetails.floors.map((floor) => (
            <div className="tree-floor" key={floor.id}>
              <div className="tree-row tree-row--floor">
                <div>
                  <strong>{floor.name}</strong>
                  <span>Floor {floor.floorNumber} · {floor.roomCount} rooms · {floor.bedCount} beds</span>
                </div>
                <StatusBadge status={floor.status} />
              </div>
              {floor.rooms.map((room) => (
                <div className="tree-room" key={room.id}>
                  <div className="tree-row">
                    <div>
                      <strong>Room {room.roomNumber}</strong>
                      <span>{room.sharingType.replaceAll('_', ' ')} · {room.bedCount}/{room.capacity} beds</span>
                    </div>
                    <StatusBadge status={room.status} />
                  </div>
                  <div className="bed-list">
                    {room.beds.map((bed) => (
                      <div className="bed-chip" key={bed.id}>
                        <span>{bed.bedLabel || `Bed ${bed.bedNumber}`}</span>
                        <StatusBadge status={bed.status} />
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          ))}
        </div>
      </section>

      <section className="surface">
        <h2>Verification History</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Previous</th>
                <th>New</th>
                <th>Remarks</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {verificationHistory.map((item) => (
                <tr key={item.id}>
                  <td>{item.previousStatus ? <StatusBadge status={item.previousStatus} /> : 'None'}</td>
                  <td><StatusBadge status={item.newStatus} /></td>
                  <td>{item.remarks || 'None'}</td>
                  <td>{new Date(item.createdAt).toLocaleString()}</td>
                </tr>
              ))}
              {verificationHistory.length === 0 ? (
                <tr>
                  <td colSpan={4}>No verification history yet.</td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
