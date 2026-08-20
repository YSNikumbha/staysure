import { useMutation, useQuery } from '@tanstack/react-query';
import { Banknote, Receipt, ShieldCheck } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { userRentApi } from '../api/rent.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';

export default function RentInvoiceDetailPage() {
  const { id } = useParams();
  const invoiceId = Number(id);
  const query = useQuery({ queryKey: ['user-rent-invoice', invoiceId], queryFn: () => userRentApi.get(invoiceId), enabled: Number.isFinite(invoiceId) });
  const downloadReceipt = useMutation({ mutationFn: (paymentId: number) => userRentApi.downloadReceipt(paymentId) });

  if (query.isLoading) return <div className="route-state">Loading rent invoice</div>;

  if (query.isError || !query.data) {
    return (
      <div className="stack">
        <PageHeader eyebrow="My rent" title="Rent Invoice" actions={<Link className="secondary-link" to="/rent">Back</Link>} />
        <section className="surface"><div className="route-state">{getApiErrorMessage(query.error, 'Unable to load rent invoice')}</div></section>
      </div>
    );
  }

  const invoice = query.data;

  return (
    <div className="stack">
      <PageHeader
        eyebrow={`${monthName(invoice.billingMonth)} ${invoice.billingYear}`}
        title={invoice.invoiceNumber}
        actions={<div className="action-row"><Link className="secondary-link" to="/rent">Back</Link><StatusBadge status={invoice.status} /></div>}
      />

      <FormMessage message={downloadReceipt.isError ? getApiErrorMessage(downloadReceipt.error, 'Unable to download receipt') : null} />

      <section className="surface status-surface">
        <div>
          <p className="eyebrow">Current balance</p>
          <h2>{formatMoney(invoice.balanceAmount)}</h2>
          <p>{invoice.propertyName} · Room {invoice.roomNumber} / {invoice.bedLabel}</p>
        </div>
        <Receipt size={28} />
      </section>

      <section className="surface">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Invoice</p>
            <h2>Charges and totals</h2>
          </div>
          <Banknote size={20} />
        </div>
        <div className="detail-grid">
          <div><span>Base Rent</span><strong>{formatMoney(invoice.baseRent)}</strong></div>
          <div><span>Maintenance</span><strong>{formatMoney(invoice.maintenanceCharge)}</strong></div>
          <div><span>Electricity</span><strong>{formatMoney(invoice.electricityCharge)}</strong></div>
          <div><span>Other</span><strong>{formatMoney(invoice.otherCharge)}</strong></div>
          <div><span>Late Fee</span><strong>{formatMoney(invoice.lateFee)}</strong></div>
          <div><span>Total</span><strong>{formatMoney(invoice.totalAmount)}</strong></div>
          <div><span>Paid</span><strong>{formatMoney(invoice.paidAmount)}</strong></div>
          <div><span>Due Date</span><strong>{formatDate(invoice.dueDate)}</strong></div>
        </div>
        {invoice.notes ? <p className="muted-copy">Notes: {invoice.notes}</p> : null}
      </section>

      {invoice.securityDeposit ? (
        <section className="surface">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Security deposit</p>
              <h2>Deposit ledger</h2>
            </div>
            <StatusBadge status={invoice.securityDeposit.status} />
          </div>
          <div className="detail-grid">
            <div><span>Required</span><strong>{formatMoney(invoice.securityDeposit.requiredAmount)}</strong></div>
            <div><span>Paid</span><strong>{formatMoney(invoice.securityDeposit.paidAmount)}</strong></div>
            <div><span>Remaining</span><strong>{formatMoney(invoice.securityDeposit.remainingAmount)}</strong></div>
            <div><span>Reference</span><strong>{invoice.securityDeposit.lastPaymentReference ?? '-'}</strong></div>
          </div>
        </section>
      ) : null}

      <section className="surface">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Payments</p>
            <h2>Payment history</h2>
          </div>
          <ShieldCheck size={20} />
        </div>
        {invoice.payments.length === 0 ? (
          <p className="muted-copy">No rent payments have been recorded for this invoice yet.</p>
        ) : (
          <div className="rent-payment-list">
            {invoice.payments.map((payment) => (
              <article className="rent-payment-row" key={payment.id}>
                <div>
                  <p className="eyebrow">{payment.paymentNumber}</p>
                  <h3>{formatMoney(payment.amount)}</h3>
                  <p>{payment.paymentMethod.replaceAll('_', ' ')} · {payment.paymentReference ?? 'No reference'} · {formatDate(payment.paymentDate)}</p>
                </div>
                <button className="secondary-button compact-button" type="button" onClick={() => downloadReceipt.mutate(payment.id)} disabled={downloadReceipt.isPending}>
                  Download receipt
                </button>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function monthName(month: number) {
  return new Date(2026, month - 1, 1).toLocaleString('en-IN', { month: 'long' });
}

function formatMoney(value: number) {
  return `Rs ${Number(value ?? 0).toLocaleString('en-IN')}`;
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('en-IN');
}
