import { ArrowRight, Building2, CheckCircle2, Search, ShieldCheck, UserRound } from 'lucide-react';
import { Link } from 'react-router-dom';
import { SectionHeader } from '../components/SectionHeader';

const tenantPoints = [
  'Search verified and active PG listings.',
  'Review pricing, amenities, room availability and rules before requesting a booking.',
  'Save PGs, compare options and track booking onboarding.'
];

const ownerPoints = [
  'Manage properties, floors, rooms, beds, amenities and gallery.',
  'Submit PGs for verification before public discovery.',
  'Review booking requests, KYC documents, deposits, agreements and tenants.'
];

export function AboutPage() {
  return (
    <div className="info-page">
      <section className="info-hero">
        <p className="eyebrow">About StaySure</p>
        <h1>Making PG discovery simpler and more transparent.</h1>
        <p>
          StaySure brings verified PG discovery, owner-managed inventory and tenant onboarding
          into one focused platform.
        </p>
        <div className="hero-actions">
          <Link className="primary-link" to="/find-pg">Find PG</Link>
          <Link className="secondary-link" to="/for-owners">List Your PG</Link>
        </div>
      </section>

      <section className="info-section info-section--split">
        <div>
          <p className="eyebrow">What is StaySure?</p>
          <h2>A PG marketplace with verification at the center.</h2>
        </div>
        <p>
          StaySure helps users discover public PG listings that are active and verified, while
          giving PG owners tools to maintain property details, inventory, booking requests and
          onboarding workflows.
        </p>
      </section>

      <section className="info-section">
        <SectionHeader
          eyebrow="The problem"
          title="PG search often lacks clarity"
          description="Users need accurate pricing, available beds and verified property details. Owners need a cleaner way to manage listings and tenant onboarding without exposing private internal data publicly."
        />
      </section>

      <section className="info-section">
        <div className="value-grid">
          <article className="value-card">
            <span className="value-icon"><UserRound size={22} /></span>
            <h3>For tenants</h3>
            <ul className="clean-list">
              {tenantPoints.map((item) => <li key={item}>{item}</li>)}
            </ul>
          </article>
          <article className="value-card">
            <span className="value-icon"><Building2 size={22} /></span>
            <h3>For PG owners</h3>
            <ul className="clean-list">
              {ownerPoints.map((item) => <li key={item}>{item}</li>)}
            </ul>
          </article>
        </div>
      </section>

      <section className="info-section landing-section--band">
        <SectionHeader
          eyebrow="Verification approach"
          title="Listings go public only after review"
          description="Owners submit completed PGs for verification. Admin review controls public visibility, and only verified active PGs appear in public discovery."
        />
        <div className="step-grid">
          <article className="step-card">
            <span><Search size={18} /></span>
            <h3>Complete listing</h3>
            <p>Owners add property details, inventory, amenities, rules and gallery.</p>
          </article>
          <article className="step-card">
            <span><ShieldCheck size={18} /></span>
            <h3>Submit for review</h3>
            <p>Verification status tracks pending, review, changes, rejection or approval.</p>
          </article>
          <article className="step-card">
            <span><CheckCircle2 size={18} /></span>
            <h3>Go public</h3>
            <p>Verified and active PGs become searchable with current availability.</p>
          </article>
        </div>
      </section>

      <section className="final-cta">
        <h2>Explore StaySure with confidence.</h2>
        <div className="hero-actions">
          <Link className="primary-link" to="/find-pg">Find PG <ArrowRight size={18} /></Link>
          <Link className="secondary-link" to="/owner/apply">List Your PG</Link>
        </div>
      </section>
    </div>
  );
}
