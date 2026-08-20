import { useQuery } from '@tanstack/react-query';
import { AlertCircle, Banknote, Receipt, Wallet } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { userRentApi } from '../api/rent.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';

export default function RentPage() {
  const query = useQuery({ queryKey: ['user-rent'], queryFn: userRentApi.list });
  const invoices = query.data?.invoices ?? [];

  return (
    <div className="stack">
      <PageHeader eyebrow="My rent" title="Rent Dashboard" actions={<Link className="secondary-link" to="/my-pg">My PG</Link>} />

      <FormMessage message={query.isError ? getApiErrorMessage(query.error, 'Unable to load rent dashboard') : null} />

      <section className="metric-grid">
        <RentMetric icon={<Receipt size={19} />} label="Billed" value={formatMoney(query.data?.summary.totalRent ?? 0)} />
        <RentMetric icon={<Banknote size={19} />} label="Paid" value={formatMoney(query.data?.summary.collected ?? 0)} />
        <RentMetric icon={<Wallet size={19} />} label="Outstanding" value={formatMoney(query.data?.summary.outstanding ?? 0)} />
        <RentMetric icon={<AlertCircle size={19} />} label="Overdue" value={formatMoney(query.data?.summary.overdueAmount ?? 0)} />
      </section>

      {query.data?.summary.securityDeposit ? (
        <section className="surface">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Security deposit</p>
              <h2>Deposit ledger</h2>
            </div>
            <StatusBadge status={query.data.summary.securityDeposit.status} />
          </div>
          <div className="detail-grid">
            <div><span>Required</span><strong>{formatMoney(query.data.summary.securityDeposit.requiredAmount)}</strong></div>
            <div><span>Paid</span><strong>{formatMoney(query.data.summary.securityDeposit.paidAmount)}</strong></div>
            <div><span>Remaining</span><strong>{formatMoney(query.data.summary.securityDeposit.remainingAmount)}</strong></div>
            <div><span>Reference</span><strong>{query.data.summary.securityDeposit.lastPaymentReference ?? '-'}</strong></div>
          </div>
        </section>
      ) : null}

      <section className="surface">
        <div className="section-heading">
          <div>
            <p className="eyebrow">History</p>
            <h2>{invoices.length} invoice{invoices.length === 1 ? '' : 's'}</h2>
          </div>
        </div>

        {query.isLoading ? <div className="route-state">Loading rent invoices</div> : null}

        {!query.isLoading && invoices.length === 0 ? (
          <EmptyState
            title="No rent invoices yet."
            description="Your monthly rent invoices will appear here after the owner generates them."
          />
        ) : null}

        {invoices.length > 0 ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Invoice</th>
                  <th>PG</th>
                  <th>Period</th>
                  <th>Total</th>
                  <th>Paid</th>
                  <th>Balance</th>
                  <th>Due</th>
                  <th>Status</th>
                  <th className="table-actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                {invoices.map((invoice) => (
                  <tr key={invoice.id}>
                    <td>{invoice.invoiceNumber}</td>
                    <td>{invoice.propertyName}<br /><span className="muted-copy">Room {invoice.roomNumber} / {invoice.bedLabel}</span></td>
                    <td>{monthName(invoice.billingMonth)} {invoice.billingYear}</td>
                    <td>{formatMoney(invoice.totalAmount)}</td>
                    <td>{formatMoney(invoice.paidAmount)}</td>
                    <td>{formatMoney(invoice.balanceAmount)}</td>
                    <td>{formatDate(invoice.dueDate)}</td>
                    <td><StatusBadge status={invoice.status} /></td>
                    <td className="table-actions"><Link to={`/rent/${invoice.id}`}>View</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </div>
  );
}

function RentMetric({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <article className="metric-tile rent-metric-tile">
      <span>{icon} {label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function monthName(month: number) {
  return new Date(2026, month - 1, 1).toLocaleString('en-IN', { month: 'short' });
}

function formatMoney(value: number) {
  return `Rs ${Number(value ?? 0).toLocaleString('en-IN')}`;
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('en-IN');
}
