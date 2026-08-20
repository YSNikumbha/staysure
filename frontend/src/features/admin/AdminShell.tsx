import { ArrowLeft, Building2, LayoutDashboard, LogOut, Menu, ShieldCheck, UserRound, UsersRound, X } from 'lucide-react';
import type { ReactNode } from 'react';
import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

type AdminShellProps = {
  title?: string;
  eyebrow?: string;
  actions?: ReactNode;
  children: ReactNode;
};

const adminLinks = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/admin/owners', label: 'Owners', icon: UsersRound },
  { to: '/admin/pgs/pending', label: 'PG Verification', icon: Building2 },
  { to: '/admin/users', label: 'Users', icon: UserRound }
];

export function AdminShell({ title = 'Admin Portal', eyebrow = 'StaySure', actions, children }: AdminShellProps) {
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
    <div className="admin-portal">
      <aside className="admin-portal-sidebar" aria-label="Admin navigation">
        <AdminSidebarContent onLogout={handleLogout} />
      </aside>

      <section className="admin-portal-main">
        <header className="admin-portal-topbar">
          <button
            className="mobile-menu-button admin-drawer-toggle"
            type="button"
            aria-label="Open admin navigation"
            aria-expanded={drawerOpen}
            onClick={() => setDrawerOpen(true)}
          >
            <Menu size={21} />
          </button>
          <div>
            <p className="eyebrow">{eyebrow}</p>
            <h1>{title}</h1>
          </div>
          <div className="admin-topbar-actions">
            {actions}
            <span className="admin-avatar" title={user?.firstName ?? 'Admin'}>
              {(user?.firstName?.[0] ?? 'A').toUpperCase()}
            </span>
          </div>
        </header>
        {children}
      </section>

      {drawerOpen ? (
        <div className="admin-drawer-backdrop" role="presentation">
          <aside className="admin-drawer" role="dialog" aria-modal="true" aria-label="Admin navigation menu">
            <div className="section-heading">
              <div>
                <p className="eyebrow">StaySure</p>
                <h2>Admin Portal</h2>
              </div>
              <button className="icon-button" type="button" onClick={() => setDrawerOpen(false)} aria-label="Close admin navigation">
                <X size={18} />
              </button>
            </div>
            <AdminSidebarContent onLogout={handleLogout} />
          </aside>
        </div>
      ) : null}
    </div>
  );
}

function AdminSidebarContent({ onLogout }: { onLogout: () => void }) {
  const location = useLocation();

  return (
    <>
      <div className="admin-sidebar-brand">
        <Link to="/" className="brand">StaySure</Link>
        <span>Admin Portal</span>
      </div>
      <nav className="admin-sidebar-nav">
        {adminLinks.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              className={({ isActive }) => isActive || isAdminRouteActive(item.to, location.pathname) ? 'active' : undefined}
              to={item.to}
              key={item.to}
            >
              <Icon size={18} />
              {item.label}
            </NavLink>
          );
        })}
      </nav>
      <div className="admin-sidebar-footer">
        <Link to="/profile" className="secondary-link compact-button">
          <ShieldCheck size={16} />
          Profile
        </Link>
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

function isAdminRouteActive(to: string, pathname: string) {
  if (to === '/admin/pgs/pending') {
    return pathname.startsWith('/admin/pgs');
  }
  return false;
}
