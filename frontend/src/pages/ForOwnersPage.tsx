import { ArrowRight, BedDouble, Building2, ClipboardCheck, FileText, Home, ShieldCheck, UsersRound } from 'lucide-react';
import { Link } from 'react-router-dom';
import { SectionHeader } from '../components/SectionHeader';

const ownerCapabilities = [
  {
    icon: Home,
    title: 'Manage Properties',
    text: 'Create and maintain PG details, address, pricing, food availability and rules.'
  },
  {
    icon: BedDouble,
    title: 'Manage Floors, Rooms & Beds',
    text: 'Keep inventory structured from floors to rooms to individual bed records.'
  },
  {
    icon: ClipboardCheck,
    title: 'Receive Booking Requests',
    text: 'Review user booking requests and reserve beds through the owner approval flow.'
  },
  {
    icon: ShieldCheck,
    title: 'Tenant KYC Workflow',
    text: 'Review tenant documents and move bookings through the onboarding process.'
  },
  {
    icon: FileText,
    title: 'Deposits & Agreements',
    text: 'Record security deposits and issue rental agreements before confirmation.'
  },
  {
    icon: UsersRound,
    title: 'Manage Tenants',
    text: 'Track upcoming and active tenants after confirmation and check-in.'
  }
];

const ownerSteps = [
  'Create account',
  'Apply as PG owner',
  'Get verified',
  'Add property',
  'Submit PG verification',
  'Start receiving bookings'
];

export function ForOwnersPage() {
  return (
    <div className="info-page">
      <section className="info-hero info-hero--owner">
        <p className="eyebrow">For PG owners</p>
        <h1>Manage your PG smarter with StaySure.</h1>
        <p>
          List your property, manage real inventory and handle booking-to-check-in workflows from
          one owner workspace.
        </p>
        <div className="hero-actions">
          <Link className="primary-link" to="/owner/apply">Become a PG Owner</Link>
          <Link className="secondary-link" to="/login">Login</Link>
        </div>
      </section>

      <section className="info-section">
        <SectionHeader
          eyebrow="Owner workspace"
          title="Everything needed for the current StaySure workflow"
          description="The owner experience focuses on property management, verification, booking approvals and tenant onboarding."
        />
        <div className="value-grid value-grid--three">
          {ownerCapabilities.map((item) => {
            const Icon = item.icon;
            return (
              <article className="value-card" key={item.title}>
                <span className="value-icon"><Icon size={22} /></span>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </article>
            );
          })}
        </div>
      </section>

      <section className="info-section landing-section--band">
        <SectionHeader
          eyebrow="How owners start"
          title="From account to public listing"
          description="This follows the actual StaySure owner and PG verification flow."
        />
        <div className="owner-steps">
          {ownerSteps.map((step, index) => (
            <div className="owner-step" key={step}>
              <span>{index + 1}</span>
              <strong>{step}</strong>
            </div>
          ))}
        </div>
      </section>

      <section className="owner-cta-section">
        <div>
          <p className="eyebrow">Ready when your PG is ready</p>
          <h2>Start with owner verification.</h2>
          <p>After approval, you can add PG details and submit complete properties for verification.</p>
        </div>
        <Link className="primary-link" to="/owner/apply">List Your PG <ArrowRight size={18} /></Link>
      </section>
    </div>
  );
}
