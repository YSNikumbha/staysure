import { Building2, Heart, Search, Scale } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export function PublicFooter() {
  const { isAuthenticated, roles } = useAuthStore();
  const ownerRoute = isAuthenticated && roles.includes('PG_OWNER') ? '/owner/dashboard' : '/owner/apply';

  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <div className="site-footer__brand">
          <Link to="/" className="brand brand--footer">StaySure</Link>
          <p>Find verified PGs and manage your stay with confidence.</p>
        </div>

        <nav className="footer-nav" aria-label="Footer explore links">
          <h2>Explore</h2>
          <Link to="/find-pg"><Search size={16} /> Find PG</Link>
          <Link to="/wishlist"><Heart size={16} /> Wishlist</Link>
          <Link to="/compare"><Scale size={16} /> Compare</Link>
        </nav>

        <nav className="footer-nav" aria-label="Footer company links">
          <h2>Company</h2>
          <Link to="/about">About</Link>
          <Link to="/contact">Contact</Link>
        </nav>

        <nav className="footer-nav" aria-label="Footer owner links">
          <h2>For Owners</h2>
          <Link to="/for-owners"><Building2 size={16} /> List Your PG</Link>
          {roles.includes('PG_OWNER') ? <Link to={ownerRoute}>Owner Dashboard</Link> : null}
        </nav>
      </div>
      <div className="site-footer__bottom">
        <span>© StaySure</span>
        <span>Built for verified PG discovery and onboarding.</span>
      </div>
    </footer>
  );
}
