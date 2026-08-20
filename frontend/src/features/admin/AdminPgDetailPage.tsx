import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Ban, BedDouble, Building2, CheckCircle2, Eye, FileClock, Home, MapPin, MessageSquareWarning, WalletCards } from 'lucide-react';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
import { StatusBadge } from '../../components/StatusBadge';
import type { AdminPropertyDetails, PropertyVerificationStatus } from '../../types/property';
import { getApiErrorMessage } from '../../utils/apiError';
import { toAssetUrl } from '../../utils/assets';
import { AdminReviewDialog } from './AdminReviewDialog';
import { AdminShell } from './AdminShell';

type PgDialogAction = 'start-review' | 'verify' | 'reject' | 'request-changes';

export function AdminPgDetailPage() {
  const { id } = useParams();
  const pgId = Number(id);
  const queryClient = useQueryClient();
  const [dialogAction, setDialogAction] = useState<PgDialogAction | null>(null);
  const [dialogText, setDialogText] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const pgQuery = useQuery({
    queryKey: ['admin-pg', pgId],
    queryFn: () => adminApi.pg(pgId),
    enabled: Number.isFinite(pgId)
  });

  const invalidate = async () => {
    await queryClient.invalidateQueries({ queryKey: ['admin-pg', pgId] });
    await queryClient.invalidateQueries({ queryKey: ['admin-pgs'] });
  };

  const startReview = useMutation({
    mutationFn: (remarks?: string) => adminApi.startPgReview(pgId, remarks),
    onSuccess: async () => {
      setSuccessMessage('PG review started.');
      closeDialog();
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to start review'))
  });

  const verify = useMutation({
    mutationFn: (remarks?: string) => adminApi.verifyPg(pgId, remarks),
    onSuccess: async () => {
      setSuccessMessage('PG verified.');
      closeDialog();
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to verify PG'))
  });

  const reject = useMutation({
    mutationFn: (remarks: string) => adminApi.rejectPg(pgId, remarks),
    onSuccess: async () => {
      setSuccessMessage('PG rejected.');
      closeDialog();
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to reject PG'))
  });

  const requestChanges = useMutation({
    mutationFn: (remarks: string) => adminApi.requestPgChanges(pgId, remarks),
    onSuccess: async () => {
      setSuccessMessage('Changes requested.');
      closeDialog();
      await invalidate();
    },
    onError: (error) => setActionError(getApiErrorMessage(error, 'Unable to request changes'))
  });

  const closeDialog = () => {
    setDialogAction(null);
    setDialogText('');
    setActionError(null);
  };

  const openDialog = (action: PgDialogAction) => {
    setDialogAction(action);
    setDialogText('');
    setActionError(null);
    setSuccessMessage(null);
  };

  const submitDialog = () => {
    if (!dialogAction) {
      return;
    }
    if ((dialogAction === 'reject' || dialogAction === 'request-changes') && !dialogText.trim()) {
      setActionError('Remarks are required.');
      return;
    }
    if (dialogAction === 'start-review') {
      startReview.mutate(dialogText.trim() || undefined);
    }
    if (dialogAction === 'verify') {
      verify.mutate(dialogText.trim() || undefined);
    }
    if (dialogAction === 'reject') {
      reject.mutate(dialogText.trim());
    }
    if (dialogAction === 'request-changes') {
      requestChanges.mutate(dialogText.trim());
    }
  };

  if (pgQuery.isLoading) {
    return (
      <AdminShell title="PG Review" eyebrow="PG verification">
        <div className="admin-stack">
          <div className="admin-skeleton-card" />
          <div className="admin-skeleton-card" />
        </div>
      </AdminShell>
    );
  }

  if (pgQuery.isError || !pgQuery.data) {
    return (
      <AdminShell title="PG Review" eyebrow="PG verification">
        <div className="route-state">{getApiErrorMessage(pgQuery.error, 'Unable to load PG')}</div>
      </AdminShell>
    );
  }

  const { owner, propertyDetails, verificationHistory } = pgQuery.data;
  const property = propertyDetails.property;
  const pendingAction = startReview.isPending || verify.isPending || reject.isPending || requestChanges.isPending;
  const dialogConfig = dialogAction ? pgDialogConfig(dialogAction) : null;
  const coverImage = propertyDetails.images.find((image) => image.coverImage);
  const hasReviewActions = property.verificationStatus === 'PENDING' || property.verificationStatus === 'UNDER_REVIEW';

  return (
    <AdminShell
      title={property.name}
      eyebrow="PG verification"
      actions={<Link className="secondary-link compact-button" to="/admin/pgs/pending">Back</Link>}
    >
      <div className="admin-stack">
        <FormMessage message={successMessage} tone="success" />

        <section className="admin-review-header admin-review-header--property">
          <div>
            <p className="eyebrow">Property review</p>
            <h2>{property.name}</h2>
            <p>{owner.businessName} · {property.area}, {property.city}</p>
          </div>
          <div className="badge-row">
            <StatusBadge status={property.verificationStatus} />
            <StatusBadge status={property.status} />
          </div>
        </section>

        <section className="admin-detail-columns">
          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Overview</p>
                <h2>Property Summary</h2>
              </div>
              <Building2 size={20} />
            </div>
            {coverImage ? <img className="admin-property-cover" src={toAssetUrl(coverImage.imageUrl)} alt={property.name} /> : null}
            <p className="muted-copy">{property.description || 'No description provided.'}</p>
            <div className="detail-grid detail-grid--compact">
              <AdminFact label="Property Type" value={property.propertyType.replaceAll('_', ' ')} />
              <AdminFact label="Suitable For" value={property.genderType.replaceAll('_', ' ')} />
              <AdminFact label="Submitted" value={property.submittedForVerificationAt ? new Date(property.submittedForVerificationAt).toLocaleString() : 'Not submitted'} />
              <AdminFact label="Verified At" value={property.verifiedAt ? new Date(property.verifiedAt).toLocaleString() : 'Not verified'} />
            </div>
          </div>

          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Owner</p>
                <h2>Owner Information</h2>
              </div>
              <Home size={20} />
            </div>
            <div className="detail-grid detail-grid--compact">
              <AdminFact label="Business" value={owner.businessName} />
              <AdminFact label="Applicant" value={`${owner.user.firstName} ${owner.user.lastName}`} />
              <AdminFact label="Email" value={owner.user.email} />
              <AdminFact label="Phone" value={owner.user.phone} />
              <AdminFact label="Owner Status" value={owner.verificationStatus.replaceAll('_', ' ')} />
            </div>
          </div>
        </section>

        <section className="admin-review-grid">
          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Location</p>
                <h2>Address</h2>
              </div>
              <MapPin size={20} />
            </div>
            <div className="detail-grid detail-grid--compact">
              <AdminFact label="Address" value={`${property.addressLine1}${property.addressLine2 ? `, ${property.addressLine2}` : ''}`} />
              <AdminFact label="Area" value={property.area} />
              <AdminFact label="City" value={property.city} />
              <AdminFact label="State / Pincode" value={`${property.state} ${property.pincode}`} />
            </div>
          </div>

          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Pricing</p>
                <h2>Stay Terms</h2>
              </div>
              <WalletCards size={20} />
            </div>
            <div className="detail-grid detail-grid--compact">
              <AdminFact label="Starting Rent" value={`Rs ${Number(property.startingRent).toLocaleString()}`} />
              <AdminFact label="Security Deposit" value={`Rs ${Number(property.securityDeposit).toLocaleString()}`} />
              <AdminFact label="Notice Period" value={`${property.noticePeriodDays} days`} />
              <AdminFact label="Lock-in" value={`${property.lockInMonths} months`} />
              <AdminFact label="Entry Time" value={property.entryTime ? property.entryTime.slice(0, 5) : 'Not set'} />
              <AdminFact label="Food" value={property.foodAvailable ? 'Available' : 'Not available'} />
            </div>
          </div>
        </section>

        <section className="surface admin-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Inventory</p>
              <h2>Rooms and Beds</h2>
            </div>
            <BedDouble size={20} />
          </div>
          <div className="admin-submission-snapshot">
            <span><strong>{propertyDetails.counts.totalFloors}</strong> Floors</span>
            <span><strong>{propertyDetails.roomCount}</strong> Rooms</span>
            <span><strong>{propertyDetails.bedCount}</strong> Beds</span>
            <span><strong>{propertyDetails.availableBedCount}</strong> Available Beds</span>
            <span><strong>{propertyDetails.images.length}</strong> Images</span>
          </div>
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
                        <span>{room.sharingType.replaceAll('_', ' ')} · Rs {Number(room.monthlyRent).toLocaleString()} · {room.bedCount}/{room.capacity} beds</span>
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
                {floor.rooms.length === 0 ? <p className="muted-copy">No rooms on this floor.</p> : null}
              </div>
            ))}
            {propertyDetails.floors.length === 0 ? <EmptyState title="No floors added." /> : null}
          </div>
        </section>

        <section className="admin-review-grid">
          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Amenities</p>
                <h2>Selected Amenities</h2>
              </div>
            </div>
            {propertyDetails.amenities.length === 0 ? (
              <EmptyState title="No amenities selected." />
            ) : (
              <div className="amenity-grid">
                {propertyDetails.amenities.map((amenity) => <span className="amenity-pill" key={amenity.id}>{amenity.name}</span>)}
              </div>
            )}
          </div>

          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">House rules</p>
                <h2>Rules</h2>
              </div>
            </div>
            <div className="rule-grid">
              <RuleItem label="Visitors" enabled={propertyDetails.rules?.visitorAllowed ?? false} />
              <RuleItem label="Smoking" enabled={propertyDetails.rules?.smokingAllowed ?? false} />
              <RuleItem label="Alcohol" enabled={propertyDetails.rules?.alcoholAllowed ?? false} />
              <RuleItem label="Cooking" enabled={propertyDetails.rules?.cookingAllowed ?? false} />
              <RuleItem label="Late Entry" enabled={propertyDetails.rules?.lateEntryAllowed ?? false} />
              <div>
                <span>Gate closing</span>
                <strong>{propertyDetails.rules?.gateClosingTime ? propertyDetails.rules.gateClosingTime.slice(0, 5) : 'Not set'}</strong>
              </div>
              <div className="form-span">
                <span>Additional rules</span>
                <strong>{propertyDetails.rules?.additionalRules || 'None'}</strong>
              </div>
            </div>
          </div>
        </section>

        <section className="surface admin-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Gallery</p>
              <h2>Submitted Images</h2>
            </div>
          </div>
          {propertyDetails.images.length === 0 ? (
            <EmptyState title="No images uploaded." />
          ) : (
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
            </div>
          )}
        </section>

        <section className="surface admin-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Decision</p>
              <h2>Review Actions</h2>
            </div>
          </div>
          <FormMessage message={property.verificationRemarks || property.rejectionReason} tone={property.verificationStatus === 'REJECTED' ? 'error' : 'success'} />
          <div className="admin-action-grid">
            {property.verificationStatus === 'PENDING' ? (
              <button className="secondary-button" type="button" disabled={pendingAction} onClick={() => openDialog('start-review')}>
                <Eye size={18} />
                Start Review
              </button>
            ) : null}
            {canDecidePg(property.verificationStatus) ? (
              <button className="primary-button" type="button" disabled={pendingAction} onClick={() => openDialog('verify')}>
                <CheckCircle2 size={18} />
                Verify PG
              </button>
            ) : null}
            {property.verificationStatus === 'UNDER_REVIEW' ? (
              <button className="warning-button" type="button" disabled={pendingAction} onClick={() => openDialog('request-changes')}>
                <MessageSquareWarning size={18} />
                Request Changes
              </button>
            ) : null}
            {canDecidePg(property.verificationStatus) ? (
              <button className="danger-button" type="button" disabled={pendingAction} onClick={() => openDialog('reject')}>
                <Ban size={18} />
                Reject PG
              </button>
            ) : null}
            {!hasReviewActions ? (
              <p className="muted-copy">No PG review actions are available for this status.</p>
            ) : null}
          </div>
        </section>

        <VerificationHistoryPanel data={pgQuery.data} />

        {dialogConfig ? (
          <AdminReviewDialog
            title={dialogConfig.title}
            description={dialogConfig.description}
            actionLabel={dialogConfig.actionLabel}
            tone={dialogConfig.tone}
            icon={dialogConfig.icon}
            remarksLabel={dialogConfig.remarksLabel}
            remarksRequired={dialogConfig.required}
            remarksValue={dialogText}
            error={actionError}
            isPending={pendingAction}
            onRemarksChange={setDialogText}
            onCancel={closeDialog}
            onConfirm={submitDialog}
          />
        ) : null}
      </div>
    </AdminShell>
  );
}

function AdminFact({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function RuleItem({ label, enabled }: { label: string; enabled: boolean }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{enabled ? 'Allowed' : 'Not allowed'}</strong>
    </div>
  );
}

function VerificationHistoryPanel({ data }: { data: AdminPropertyDetails }) {
  return (
    <section className="surface admin-panel">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Timeline</p>
          <h2>Verification History</h2>
        </div>
        <FileClock size={20} />
      </div>
      {data.verificationHistory.length === 0 ? (
        <EmptyState title="No verification history yet." />
      ) : (
        <div className="admin-history-list">
          {data.verificationHistory.map((item) => (
            <div className="admin-history-item" key={item.id}>
              <span><FileClock size={15} /></span>
              <div>
                <strong>{item.previousStatus ? item.previousStatus.replaceAll('_', ' ') : 'None'} → {item.newStatus.replaceAll('_', ' ')}</strong>
                <p>{item.remarks || 'No remarks'} · {new Date(item.createdAt).toLocaleString()}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function canDecidePg(status: PropertyVerificationStatus) {
  return status === 'PENDING' || status === 'UNDER_REVIEW';
}

function pgDialogConfig(action: PgDialogAction) {
  if (action === 'start-review') {
    return {
      title: 'Start PG Review?',
      description: 'This moves the submitted PG into admin review using the existing verification workflow.',
      actionLabel: 'Start Review',
      remarksLabel: 'Review remarks',
      required: false,
      tone: 'primary' as const,
      icon: <Eye size={20} />
    };
  }
  if (action === 'verify') {
    return {
      title: 'Verify This PG?',
      description: 'The property will become eligible for public discovery according to existing business rules.',
      actionLabel: 'Verify PG',
      remarksLabel: 'Verification remarks',
      required: false,
      tone: 'primary' as const,
      icon: <CheckCircle2 size={20} />
    };
  }
  if (action === 'request-changes') {
    return {
      title: 'Request Changes',
      description: 'Tell the owner what needs to be corrected before this PG can be verified.',
      actionLabel: 'Send Request',
      remarksLabel: 'Required changes',
      required: true,
      tone: 'warning' as const,
      icon: <MessageSquareWarning size={20} />
    };
  }
  return {
    title: 'Reject PG',
    description: 'Provide a clear reason for rejecting this PG verification request.',
    actionLabel: 'Reject PG',
    remarksLabel: 'Reason',
    required: true,
    tone: 'danger' as const,
    icon: <Ban size={20} />
  };
}
