import { ArrowRight, Building2, ShieldCheck, UserPlus } from 'lucide-react';
import { FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export function HomePage() {
  const { isAuthenticated, roles } = useAuthStore();
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const submitSearch = (event: FormEvent) => {
    event.preventDefault();
    const params = new URLSearchParams();
    if (search.trim()) {
      params.set('search', search.trim());
    }
    navigate(`/find-pg${params.toString() ? `?${params.toString()}` : ''}`);
  };

  return (
    <div className="home-grid">
      <section className="home-hero">
        <p className="eyebrow">StaySure</p>
        <h1>Find Your PG</h1>
        <form className="home-search" onSubmit={submitSearch}>
          <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by city, area, or PG name" />
          <button className="primary-button" type="submit">Find PG</button>
        </form>
        <div className="hero-actions">
          <Link className="primary-link" to="/find-pg">
            <ArrowRight size={18} />
            Browse PGs
          </Link>
          {isAuthenticated ? (
            <>
              <Link className="primary-link" to="/profile">
                <ShieldCheck size={18} />
                Profile
              </Link>
              <Link className="secondary-link" to={roles.includes('PG_OWNER') ? '/owner/dashboard' : '/owner/apply'}>
                <Building2 size={18} />
                Owner
              </Link>
            </>
          ) : (
            <>
              <Link className="primary-link" to="/register">
                <UserPlus size={18} />
                Register
              </Link>
              <Link className="secondary-link" to="/login">
                <ArrowRight size={18} />
                Login
              </Link>
            </>
          )}
        </div>
      </section>
      <section className="surface home-panel">
        <h2>StaySure Flow</h2>
        <ol className="flow-list">
          <li>Verified owners publish completed PGs</li>
          <li>Search by city, area, budget, amenities, and availability</li>
          <li>Save favourites and compare PGs side by side</li>
          <li>Open verified listings with current room availability</li>
        </ol>
      </section>
    </div>
  );
}
