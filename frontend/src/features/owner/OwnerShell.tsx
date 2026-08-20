import { ArrowLeft, BedDouble, Building2, LayoutDashboard, LogOut, Menu, UserRound, UsersRound, X } from 'lucide-react';
import type { ReactNode } from 'react';
import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

type OwnerShellProps = {
  title?: string;
  eyebrow?: string;
  actions?: ReactNode;
  children: ReactNode;
};

const ownerLinks = [
  { to: '/owner/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/owner/pgs', label: 'My PGs', icon: Building2 },
  { to: '/owner/bookings', label: 'Bookings', icon: BedDouble },
  { to: '/owner/tenants', label: 'Tenants', icon: UsersRound },
  { to: '/profile', label: 'Profile', icon: UserRound }
];

export function OwnerShell({ title = 'Owner Portal', eyebrow = 'StaySure', actions, children }: OwnerShellProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [drawerOpen, setDrawerOpen] = useState(false);

  useEffect(() => {
    setDrawerOpen(false);
  }, [location.pathname]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="owner-portal">
      <aside className="owner-portal-sidebar" aria-label="Owner navigation">
        <OwnerSidebarContent onLogout={handleLogout} />
      </aside>

      <section className="owner-portal-main">
        <header className="owner-portal-topbar">
          <button
            className="mobile-menu-button owner-drawer-toggle"
            type="button"
            aria-label="Open owner navigation"
            aria-expanded={drawerOpen}
            onClick={() => setDrawerOpen(true)}
          >
            <Menu size={21} />
          </button>
          <div>
            <p className="eyebrow">{eyebrow}</p>
            <h1>{title}</h1>
          </div>
          <div className="owner-topbar-actions">
            {actions}
            <span className="owner-avatar" title={user?.firstName ?? 'Owner'}>
              {(user?.firstName?.[0] ?? 'O').toUpperCase()}
            </span>
          </div>
        </header>
        {children}
      </section>

      {drawerOpen ? (
        <div className="owner-drawer-backdrop" role="presentation">
          <aside className="owner-drawer" role="dialog" aria-modal="true" aria-label="Owner navigation menu">
            <div className="section-heading">
              <div>
                <p className="eyebrow">StaySure</p>
                <h2>Owner Portal</h2>
              </div>
              <button className="icon-button" type="button" onClick={() => setDrawerOpen(false)} aria-label="Close owner navigation">
                <X size={18} />
              </button>
            </div>
            <OwnerSidebarContent onLogout={handleLogout} />
          </aside>
        </div>
      ) : null}
    </div>
  );
}

function OwnerSidebarContent({ onLogout }: { onLogout: () => void }) {
  return (
    <>
      <div className="owner-sidebar-brand">
        <Link to="/" className="brand">StaySure</Link>
        <span>Owner Portal</span>
      </div>
      <nav className="owner-sidebar-nav">
        {ownerLinks.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink to={item.to} key={item.to}>
              <Icon size={18} />
              {item.label}
            </NavLink>
          );
        })}
      </nav>
      <div className="owner-sidebar-footer">
        <Link to="/" className="secondary-link compact-button">
          <ArrowLeft size={16} />
          Back to Website
        </Link>
        <button className="secondary-button compact-button" type="button" onClick={onLogout}>
          <LogOut size={16} />
          Logout
        </button>
      </div>
    </>
  );
}
