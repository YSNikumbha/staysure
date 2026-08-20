import { Building2, CheckCircle2, ShieldCheck } from 'lucide-react';
import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

type AuthLayoutProps = {
  eyebrow: string;
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
  wide?: boolean;
};

export function AuthLayout({ eyebrow, title, subtitle, children, footer, wide = false }: AuthLayoutProps) {
  return (
    <section className="auth-layout">
      <aside className="auth-story" aria-label="StaySure benefits">
        <Link to="/" className="brand auth-story__brand">StaySure</Link>
        <div>
          <p className="eyebrow">Verified PG marketplace</p>
          <h2>Search, compare and move in with a clearer process.</h2>
          <p>
            StaySure connects public PG discovery with verified listings, real bed availability,
            booking requests and onboarding workflows.
          </p>
        </div>
        <div className="auth-benefit-grid">
          <span><ShieldCheck size={18} /> Verified PG workflow</span>
          <span><Building2 size={18} /> Owner-managed inventory</span>
          <span><CheckCircle2 size={18} /> KYC, deposit and agreement tracking</span>
        </div>
      </aside>

      <div className={`auth-box ${wide ? 'auth-box--wide' : ''}`}>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p className="auth-subtitle">{subtitle}</p>
        {children}
        {footer ? <div className="auth-links">{footer}</div> : null}
      </div>
    </section>
  );
}
