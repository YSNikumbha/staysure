import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { BedDouble, CalendarCheck, CheckCircle2, FileCheck2, UserRound } from 'lucide-react';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ownerBookingApi } from '../api/booking.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { BookingStatus, PaymentMethod } from '../types/booking';
import { getApiErrorMessage } from '../utils/apiError';
import { toAssetUrl } from '../utils/assets';

const paymentMethods: PaymentMethod[] = ['CASH', 'UPI', 'BANK_TRANSFER', 'OTHER'];
const workflowSteps = [
  { label: 'Requested', status: 'REQUESTED' as BookingStatus },
  { label: 'KYC', status: 'KYC_VERIFICATION' as BookingStatus },
  { label: 'Deposit', status: 'AWAITING_DEPOSIT' as BookingStatus },
  { label: 'Agreement', status: 'AWAITING_AGREEMENT' as BookingStatus },
  { label: 'Confirmed', status: 'CONFIRMED' as BookingStatus },
  { label: 'Checked In', status: 'CHECKED_IN' as BookingStatus }
];

export default function OwnerBookingDetailPage() {
  const { id } = useParams();
  const bookingId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const [depositAmount, setDepositAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('UPI');
  const [paymentReference, setPaymentReference] = useState('');
  const [agreementTerms, setAgreementTerms] = useState('');
  const [agreementEndDate, setAgreementEndDate] = useState('');
  const [agreementFile, setAgreementFile] = useState<File | null>(null);
  const query = useQuery({ queryKey: ['owner-booking', bookingId], queryFn: () => ownerBookingApi.get(bookingId), enabled: Number.isFinite(bookingId) });
  const refresh = async () => queryClient.invalidateQueries({ queryKey: ['owner-booking', bookingId] });
  const approve = useMutation({ mutationFn: () => ownerBookingApi.approve(bookingId, remarks || undefined), onSuccess: refresh });
  const reject = useMutation({ mutationFn: () => ownerBookingApi.reject(bookingId, remarks), onSuccess: refresh });
  const verifyDoc = useMutation({ mutationFn: (docId: number) => ownerBookingApi.verifyDocument(bookingId, docId), onSuccess: refresh });
  const rejectDoc = useMutation({ mutationFn: (docId: number) => ownerBookingApi.rejectDocument(bookingId, docId, remarks), onSuccess: refresh });
  const recordDeposit = useMutation({
    mutationFn: () => ownerBookingApi.recordDeposit(bookingId, {
      amount: Number(depositAmount),
      paymentMethod,
      paymentReference: paymentReference || undefined,
      remarks: remarks || undefined
    }),
    onSuccess: async () => {
      setDepositAmount('');
      setPaymentReference('');
      await refresh();
    }
  });
  const issueAgreement = useMutation({
    mutationFn: () => ownerBookingApi.issueAgreement(bookingId, {
      endDate: agreementEndDate || undefined,
      terms: agreementTerms || undefined,
      file: agreementFile ?? undefined
    }),
    onSuccess: async () => {
      setAgreementFile(null);
      await refresh();
    }
  });
  const checkIn = useMutation({ mutationFn: () => ownerBookingApi.checkIn(bookingId), onSuccess: refresh });

  if (query.isLoading) {
    return (
      <OwnerShell title="Booking Details" eyebrow="Owner booking">
        <div className="owner-stack">
          <div className="owner-skeleton-card" />
          <div className="owner-skeleton-card" />
        </div>
      </OwnerShell>
    );
  }

  if (query.isError || !query.data) {
    return (
      <OwnerShell title="Booking Details" eyebrow="Owner booking">
        <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load booking')}</div>
      </OwnerShell>
    );
  }

  const booking = query.data;
  const error = approve.error ?? reject.error ?? verifyDoc.error ?? rejectDoc.error ?? recordDeposit.error ?? issueAgreement.error ?? checkIn.error;

  return (
    <OwnerShell
      title={booking.bookingNumber}
      eyebrow="Owner booking"
      actions={<div className="action-row"><Link className="secondary-link compact-button" to="/owner/bookings">Back</Link><StatusBadge status={booking.status} /></div>}
    >
      <div className="owner-stack">
        <FormMessage message={error ? getApiErrorMessage(error, 'Unable to update booking') : null} />

        <section className="owner-hero-card owner-hero-card--compact">
          <div>
            <p className="eyebrow">Current step</p>
            <h2>{nextActionLabel(booking.status)}</h2>
            <p>{booking.user.firstName} {booking.user.lastName} requested {booking.property.name}, room {booking.room.roomNumber}, bed {booking.bed.bedLabel || booking.bed.bedNumber}.</p>
          </div>
          <BedDouble size={28} />
        </section>

        <section className="surface owner-panel">
          <div className="owner-workflow-stepper">
            {workflowSteps.map((step, index) => {
              const activeIndex = activeWorkflowIndex(booking.status);
              const isCurrent = activeIndex === index;
              const isDone = activeIndex > index || booking.status === 'CHECKED_IN';
              return (
                <div className={`owner-workflow-step ${isCurrent ? 'owner-workflow-step--current' : ''} ${isDone ? 'owner-workflow-step--done' : ''}`} key={step.status}>
                  <span>{isDone ? <CheckCircle2 size={15} /> : index + 1}</span>
                  <strong>{step.label}</strong>
                </div>
              );
            })}
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Overview</p>
              <h2>Booking Details</h2>
            </div>
            <UserRound size={20} />
          </div>
          <div className="detail-grid">
            <div><span>Tenant</span><strong>{booking.user.firstName} {booking.user.lastName}</strong></div>
            <div><span>Phone</span><strong>{booking.user.phone}</strong></div>
            <div><span>PG</span><strong>{booking.property.name}</strong></div>
            <div><span>Room / Bed</span><strong>{booking.room.roomNumber} / {booking.bed.bedLabel || booking.bed.bedNumber}</strong></div>
            <div><span>Move-in</span><strong>{booking.moveInDate}</strong></div>
            <div><span>Expected Move-out</span><strong>{booking.expectedMoveOutDate ?? '-'}</strong></div>
            <div><span>Rent</span><strong>Rs {Number(booking.monthlyRent).toLocaleString()}</strong></div>
            <div><span>Deposit</span><strong>Rs {Number(booking.securityDeposit).toLocaleString()}</strong></div>
            <div><span>Tenant Profile</span><strong>{booking.tenant?.status ?? 'Not created'}</strong></div>
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Valid next actions</p>
              <h2>Owner Actions</h2>
            </div>
          </div>
          <label>
            Remarks
            <textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} placeholder="Add owner remarks or rejection reason" />
          </label>
          <div className="action-row action-row-spaced">
            {booking.status === 'REQUESTED' ? (
              <>
                <button className="primary-button" type="button" onClick={() => approve.mutate()} disabled={approve.isPending}>Approve</button>
                <button className="danger-button" type="button" onClick={() => reject.mutate()} disabled={!remarks.trim() || reject.isPending}>Reject</button>
              </>
            ) : null}
            {booking.status === 'CONFIRMED' ? <button className="primary-button" type="button" onClick={() => checkIn.mutate()} disabled={checkIn.isPending}>Check In</button> : null}
            {booking.status !== 'REQUESTED' && booking.status !== 'CONFIRMED' ? <p className="muted-copy">Use the relevant section below for this booking status.</p> : null}
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Required checks</p>
              <h2>KYC Documents</h2>
            </div>
            <span className="muted-copy">Verified government ID + PHOTO</span>
          </div>
          {booking.documents.length === 0 ? (
            <EmptyState title="No KYC documents uploaded yet." description="Documents uploaded by the user will appear here for owner verification." />
          ) : (
            <div className="owner-document-list">
              {booking.documents.map((doc) => (
                <article className="owner-document-card" key={doc.id}>
                  <div>
                    <p className="eyebrow">{doc.documentType.replaceAll('_', ' ')}</p>
                    <h3><a href={toAssetUrl(doc.documentUrl)} target="_blank" rel="noreferrer">{doc.originalFileName ?? 'View document'}</a></h3>
                    <p>{doc.rejectionReason ?? doc.documentNumber ?? 'No remarks'}</p>
                  </div>
                  <StatusBadge status={doc.verificationStatus} />
                  {doc.verificationStatus === 'PENDING' ? (
                    <div className="action-row">
                      <button className="primary-button compact-button" type="button" onClick={() => verifyDoc.mutate(doc.id)} disabled={verifyDoc.isPending}>Verify</button>
                      <button className="danger-button compact-button" type="button" onClick={() => rejectDoc.mutate(doc.id)} disabled={!remarks.trim() || rejectDoc.isPending}>Reject</button>
                    </div>
                  ) : null}
                </article>
              ))}
            </div>
          )}
        </section>

        {booking.status === 'AWAITING_DEPOSIT' || booking.deposit ? (
          <section className="surface owner-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Security deposit</p>
                <h2>Record Payment</h2>
              </div>
              <StatusBadge status={booking.deposit?.status ?? 'PENDING'} />
            </div>
            <div className="detail-grid">
              <div><span>Required</span><strong>Rs {Number(booking.deposit?.requiredAmount ?? booking.securityDeposit).toLocaleString()}</strong></div>
              <div><span>Paid</span><strong>Rs {Number(booking.deposit?.paidAmount ?? 0).toLocaleString()}</strong></div>
              <div><span>Remaining</span><strong>Rs {Number(booking.deposit?.remainingAmount ?? booking.securityDeposit).toLocaleString()}</strong></div>
              <div><span>Reference</span><strong>{booking.deposit?.lastPaymentReference ?? '-'}</strong></div>
            </div>
            {booking.status === 'AWAITING_DEPOSIT' ? (
              <form className="inline-form three-column" onSubmit={(event) => { event.preventDefault(); recordDeposit.mutate(); }}>
                <label>Amount<input type="number" min="1" value={depositAmount} onChange={(event) => setDepositAmount(event.target.value)} required /></label>
                <label>Method<select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value as PaymentMethod)}>{paymentMethods.map((item) => <option key={item}>{item.replaceAll('_', ' ')}</option>)}</select></label>
                <label>Reference<input value={paymentReference} onChange={(event) => setPaymentReference(event.target.value)} /></label>
                <button className="primary-button" type="submit" disabled={recordDeposit.isPending}>Record Deposit</button>
              </form>
            ) : null}
          </section>
        ) : null}

        {booking.status === 'AWAITING_AGREEMENT' || booking.agreement ? (
          <section className="surface owner-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Rental agreement</p>
                <h2>Agreement</h2>
              </div>
              {booking.agreement ? <StatusBadge status={booking.agreement.status} /> : null}
            </div>
            {booking.agreement ? (
              <div className="detail-grid">
                <div><span>Number</span><strong>{booking.agreement.agreementNumber}</strong></div>
                <div><span>Start</span><strong>{booking.agreement.startDate}</strong></div>
                <div><span>End</span><strong>{booking.agreement.endDate ?? 'Not set'}</strong></div>
                <div><span>Accepted</span><strong>{booking.agreement.acceptedAt ? new Date(booking.agreement.acceptedAt).toLocaleString() : '-'}</strong></div>
              </div>
            ) : null}
            {booking.status === 'AWAITING_AGREEMENT' ? (
              <form className="form-grid" onSubmit={(event) => { event.preventDefault(); issueAgreement.mutate(); }}>
                <label>End Date<input type="date" value={agreementEndDate} onChange={(event) => setAgreementEndDate(event.target.value)} /></label>
                <label className="form-span">Terms<textarea rows={4} value={agreementTerms} onChange={(event) => setAgreementTerms(event.target.value)} /></label>
                <label>PDF<input type="file" accept="application/pdf" onChange={(event) => setAgreementFile(event.target.files?.[0] ?? null)} /></label>
                <button className="primary-button" type="submit" disabled={issueAgreement.isPending}>Issue Agreement</button>
              </form>
            ) : null}
          </section>
        ) : null}

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Timeline</p>
              <h2>Status History</h2>
            </div>
            <CalendarCheck size={20} />
          </div>
          <div className="owner-history-list">
            {booking.history.map((item) => (
              <div className="owner-history-item" key={item.id}>
                <span><FileCheck2 size={15} /></span>
                <div>
                  <strong>{item.newStatus.replaceAll('_', ' ')}</strong>
                  <p>{item.remarks || 'Status updated'} · {new Date(item.createdAt).toLocaleString()}</p>
                </div>
              </div>
            ))}
            {booking.history.length === 0 ? <p className="muted-copy">No history recorded yet.</p> : null}
          </div>
        </section>
      </div>
    </OwnerShell>
  );
}

function activeWorkflowIndex(status: BookingStatus) {
  switch (status) {
    case 'REQUESTED':
      return 0;
    case 'AWAITING_KYC':
    case 'KYC_VERIFICATION':
      return 1;
    case 'AWAITING_DEPOSIT':
      return 2;
    case 'AWAITING_AGREEMENT':
      return 3;
    case 'CONFIRMED':
      return 4;
    case 'CHECKED_IN':
      return 5;
    case 'REJECTED':
    case 'CANCELLED':
    default:
      return -1;
  }
}

function nextActionLabel(status: BookingStatus) {
  switch (status) {
    case 'REQUESTED':
      return 'Approve or reject request';
    case 'AWAITING_KYC':
      return 'Waiting for tenant KYC upload';
    case 'KYC_VERIFICATION':
      return 'Review tenant KYC';
    case 'AWAITING_DEPOSIT':
      return 'Record security deposit';
    case 'AWAITING_AGREEMENT':
      return 'Issue rental agreement';
    case 'CONFIRMED':
      return 'Check tenant in';
    case 'CHECKED_IN':
      return 'Tenant is active';
    case 'REJECTED':
      return 'Booking rejected';
    case 'CANCELLED':
      return 'Booking cancelled';
    default:
      return 'Review booking';
  }
}
