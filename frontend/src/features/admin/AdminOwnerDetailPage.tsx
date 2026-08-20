import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Ban, BriefcaseBusiness, CheckCircle2, CircleSlash, FileText, Mail, Phone, UserRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { EmptyState } from '../../components/EmptyState';
import { FormMessage } from '../../components/FormMessage';
import { StatusBadge } from '../../components/StatusBadge';
import type { OwnerVerificationStatus } from '../../types/owner';
import { getApiErrorMessage } from '../../utils/apiError';
import { toAssetUrl } from '../../utils/assets';
import { AdminReviewDialog } from './AdminReviewDialog';
import { AdminShell } from './AdminShell';

type OwnerDialogAction = 'verify' | 'reject' | 'suspend';

export function AdminOwnerDetailPage() {
  const { id } = useParams();
  const ownerId = Number(id);
  const queryClient = useQueryClient();
  const [dialogAction, setDialogAction] = useState<OwnerDialogAction | null>(null);
  const [dialogText, setDialogText] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const ownerQuery = useQuery({
    queryKey: ['admin-owner', id],
    queryFn: () => adminApi.owner(id!),
    enabled: Boolean(id)
  });

  const invalidate = async () => {
    await queryClient.invalidateQueries({ queryKey: ['admin-owner', id] });
    await queryClient.invalidateQueries({ queryKey: ['admin-owners'] });
  };

  const verifyMutation = useMutation({
    mutationFn: (remarks?: string) => adminApi.verifyOwner(ownerId, remarks),
    onSuccess: async () => {
      setSuccessMessage('Owner application verified.');
      closeDialog();
      await invalidate();
    },
    onError: (err) => setActionError(getApiErrorMessage(err, 'Unable to verify owner'))
  });

  const rejectMutation = useMutation({
    mutationFn: (reason: string) => adminApi.rejectOwner(ownerId, reason),
    onSuccess: async () => {
      setSuccessMessage('Owner application rejected.');
      closeDialog();
      await invalidate();
    },
    onError: (err) => setActionError(getApiErrorMessage(err, 'Unable to reject owner'))
  });

  const suspendMutation = useMutation({
    mutationFn: (reason: string) => adminApi.suspendOwner(ownerId, reason),
    onSuccess: async () => {
      setSuccessMessage('Owner profile suspended.');
      closeDialog();
      await invalidate();
    },
    onError: (err) => setActionError(getApiErrorMessage(err, 'Unable to suspend owner'))
  });

  const closeDialog = () => {
    setDialogAction(null);
    setDialogText('');
    setActionError(null);
  };

  const openDialog = (action: OwnerDialogAction) => {
    setDialogAction(action);
    setDialogText('');
    setActionError(null);
    setSuccessMessage(null);
  };

  const submitDialog = () => {
    if (!dialogAction) {
      return;
    }
    if ((dialogAction === 'reject' || dialogAction === 'suspend') && !dialogText.trim()) {
      setActionError('Reason is required.');
      return;
    }
    if (dialogAction === 'verify') {
      verifyMutation.mutate(dialogText.trim() || undefined);
    }
    if (dialogAction === 'reject') {
      rejectMutation.mutate(dialogText.trim());
    }
    if (dialogAction === 'suspend') {
      suspendMutation.mutate(dialogText.trim());
    }
  };

  if (ownerQuery.isLoading) {
    return (
      <AdminShell title="Owner Review" eyebrow="Owner verification">
        <div className="admin-stack">
          <div className="admin-skeleton-card" />
          <div className="admin-skeleton-card" />
        </div>
      </AdminShell>
    );
  }

  if (ownerQuery.isError || !ownerQuery.data) {
    return (
      <AdminShell title="Owner Review" eyebrow="Owner verification">
        <div className="route-state">{getApiErrorMessage(ownerQuery.error, 'Unable to load owner application')}</div>
      </AdminShell>
    );
  }

  const { profile, documents } = ownerQuery.data;
  const actionPending = verifyMutation.isPending || rejectMutation.isPending || suspendMutation.isPending;
  const dialogConfig = dialogAction ? ownerDialogConfig(dialogAction) : null;

  return (
    <AdminShell
      title={profile.businessName}
      eyebrow="Owner review"
      actions={<Link className="secondary-link compact-button" to="/admin/owners">Back</Link>}
    >
      <div className="admin-stack">
        <FormMessage message={successMessage} tone="success" />

        <section className="admin-review-header">
          <div>
            <p className="eyebrow">Application status</p>
            <h2>{profile.user.firstName} {profile.user.lastName}</h2>
            <p>{profile.user.email} · {profile.user.phone}</p>
          </div>
          <StatusBadge status={profile.verificationStatus} />
        </section>

        <section className="admin-detail-columns">
          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Applicant</p>
                <h2>Contact Information</h2>
              </div>
              <UserRound size={20} />
            </div>
            <div className="detail-grid detail-grid--compact">
              <AdminInfo icon={<Mail size={16} />} label="Email" value={profile.user.email} />
              <AdminInfo icon={<Phone size={16} />} label="Phone" value={profile.user.phone} />
              <AdminInfo icon={<Phone size={16} />} label="Alternate Phone" value={profile.alternatePhone || 'Not provided'} />
              <AdminInfo icon={<Mail size={16} />} label="Business Email" value={profile.businessEmail || 'Not provided'} />
            </div>
          </div>

          <div className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Business</p>
                <h2>Application Information</h2>
              </div>
              <BriefcaseBusiness size={20} />
            </div>
            <div className="detail-grid detail-grid--compact">
              <AdminInfo icon={<BriefcaseBusiness size={16} />} label="Experience" value={`${profile.experienceYears ?? 0} years`} />
              <AdminInfo icon={<FileText size={16} />} label="Submitted" value={new Date(profile.createdAt).toLocaleString()} />
              <AdminInfo icon={<FileText size={16} />} label="Reviewed" value={profile.verifiedAt ? new Date(profile.verifiedAt).toLocaleString() : 'Not reviewed'} />
              <AdminInfo icon={<FileText size={16} />} label="Remarks" value={profile.verificationRemarks || 'None'} />
            </div>
          </div>
        </section>

        {profile.description ? (
          <section className="surface admin-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Owner notes</p>
                <h2>Description</h2>
              </div>
            </div>
            <p className="muted-copy">{profile.description}</p>
          </section>
        ) : null}

        <section className="surface admin-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Submitted documents</p>
              <h2>Verification Documents</h2>
            </div>
            <FileText size={20} />
          </div>
          {documents.length === 0 ? (
            <EmptyState title="No documents uploaded." description="Owner verification documents will appear here once submitted." />
          ) : (
            <div className="admin-document-grid">
              {documents.map((document) => (
                <article className="admin-document-card" key={document.id}>
                  <div>
                    <p className="eyebrow">{document.documentType.replaceAll('_', ' ')}</p>
                    <h3>{document.originalFileName ?? 'Document'}</h3>
                    <p>{document.documentNumber || 'No reference number provided'}</p>
                  </div>
                  <StatusBadge status={document.verificationStatus} />
                  <a className="secondary-link compact-button" href={toAssetUrl(document.documentUrl)} target="_blank" rel="noreferrer">View</a>
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
          <div className="admin-action-grid">
            {canVerifyOwner(profile.verificationStatus) ? (
              <button className="primary-button" type="button" onClick={() => openDialog('verify')} disabled={actionPending}>
                <CheckCircle2 size={18} />
                Approve Owner
              </button>
            ) : null}
            {canRejectOwner(profile.verificationStatus) ? (
              <button className="warning-button" type="button" onClick={() => openDialog('reject')} disabled={actionPending}>
                <Ban size={18} />
                Reject Application
              </button>
            ) : null}
            {profile.verificationStatus === 'VERIFIED' ? (
              <button className="danger-button" type="button" onClick={() => openDialog('suspend')} disabled={actionPending}>
                <CircleSlash size={18} />
                Suspend Owner
              </button>
            ) : null}
            {!canVerifyOwner(profile.verificationStatus) && !canRejectOwner(profile.verificationStatus) && profile.verificationStatus !== 'VERIFIED' ? (
              <p className="muted-copy">No owner review actions are available for this status.</p>
            ) : null}
          </div>
        </section>

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
            isPending={actionPending}
            onRemarksChange={setDialogText}
            onCancel={closeDialog}
            onConfirm={submitDialog}
          />
        ) : null}
      </div>
    </AdminShell>
  );
}

function AdminInfo({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div>
      <span>{icon}{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function canVerifyOwner(status: OwnerVerificationStatus) {
  return status === 'PENDING' || status === 'UNDER_REVIEW' || status === 'REJECTED';
}

function canRejectOwner(status: OwnerVerificationStatus) {
  return status === 'PENDING' || status === 'UNDER_REVIEW';
}

function ownerDialogConfig(action: OwnerDialogAction) {
  if (action === 'verify') {
    return {
      title: 'Approve PG Owner?',
      description: 'This will allow the user to access PG owner functionality according to existing backend rules.',
      actionLabel: 'Approve Owner',
      remarksLabel: 'Verification remarks',
      required: false,
      tone: 'primary' as const,
      icon: <CheckCircle2 size={20} />
    };
  }
  if (action === 'reject') {
    return {
      title: 'Reject Application',
      description: 'Provide a clear reason so the applicant understands why the application was rejected.',
      actionLabel: 'Reject Application',
      remarksLabel: 'Reason',
      required: true,
      tone: 'warning' as const,
      icon: <Ban size={20} />
    };
  }
  return {
    title: 'Suspend Owner',
    description: 'This suspends an existing owner profile using the current admin API.',
    actionLabel: 'Suspend Owner',
    remarksLabel: 'Reason',
    required: true,
    tone: 'danger' as const,
    icon: <CircleSlash size={20} />
  };
}
