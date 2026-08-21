import { useQuery } from '@tanstack/react-query';
import { Bell, Building2, ChevronDown, Heart, LayoutDashboard, LogOut, Menu, Search, ShieldCheck, Wallet, X } from 'lucide-react';
import type { ReactNode } from 'react';
import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { PublicFooter } from '../components/PublicFooter';
import { notificationsApi } from '../api/operations.api';
import { useAuthStore } from '../store/authStore';

type AppLayoutProps = {
  children: ReactNode;
};

export function AppLayout({ children }: AppLayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isAuthenticated, roles, logout } = useAuthStore();
  const [menuOpen, setMenuOpen] = useState(false);
  const unreadQuery = useQuery({
    queryKey: ['notifications-unread-count'],
    queryFn: notificationsApi.unreadCount,
    enabled: isAuthenticated
  });
  const unreadCount = unreadQuery.data ?? 0;

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isOwner = roles.includes('PG_OWNER');
  const isAdmin = roles.includes('SUPER_ADMIN');
  const ownerRoute = isAuthenticated && isOwner ? '/owner/dashboard' : '/owner/apply';
  const isOwnerWorkspace = location.pathname.startsWith('/owner/dashboard')
    || location.pathname.startsWith('/owner/pgs')
    || location.pathname.startsWith('/owner/bookings')
    || location.pathname.startsWith('/owner/tenants')
    || location.pathname.startsWith('/owner/rent')
    || location.pathname.startsWith('/owner/complaints')
    || location.pathname.startsWith('/owner/maintenance')
    || location.pathname.startsWith('/owner/notices')
    || location.pathname.startsWith('/owner/food')
    || location.pathname.startsWith('/owner/visitors');
  const isAdminWorkspace = location.pathname.startsWith('/admin');

  if (isOwnerWorkspace || isAdminWorkspace) {
    return (
      <div className="app-shell app-shell--workspace">
        <main className="content content--workspace">{children}</main>
      </div>
    );
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink to="/" className="brand">
          StaySure
        </NavLink>
        <nav className="topnav topnav--desktop" aria-label="Primary navigation">
          <NavLink to="/find-pg">Find PG</NavLink>
          <NavLink to="/about">About</NavLink>
          <NavLink to="/for-owners">For Owners</NavLink>
          <NavLink to="/contact">Contact</NavLink>
          {isAuthenticated ? (
            <>
              <NavLink to="/wishlist">Wishlist</NavLink>
              <NavLink to="/bookings">My Bookings</NavLink>
              <NavLink to="/rent">Rent</NavLink>
              <NavLink to="/complaints">Complaints</NavLink>
              <NavLink to="/notices">Notices</NavLink>
              <NavLink to="/food">Food</NavLink>
              <NavLink to="/visitors">Visitors</NavLink>
            </>
          ) : (
            <NavLink to="/wishlist" aria-label="Wishlist"><Heart size={17} /></NavLink>
          )}
        </nav>
        <div className="topbar-user topbar-user--desktop">
          {user ? (
            <>
              {isOwner ? (
                <Link className="secondary-link compact-button" to="/owner/dashboard">
                  <LayoutDashboard size={16} />
                  Owner Dashboard
                </Link>
              ) : (
                <Link className="secondary-link compact-button" to="/owner/apply">
                  <Building2 size={16} />
                  List Your PG
                </Link>
              )}
              {isAdmin ? (
                <Link className="secondary-link compact-button" to="/admin/dashboard">
                  <ShieldCheck size={16} />
                  Admin
                </Link>
              ) : null}
              <Link className="icon-link notification-link" to="/notifications" aria-label="Notifications">
                <Bell size={17} />
                {unreadCount > 0 ? <span>{unreadCount}</span> : null}
              </Link>
              <details className="user-menu">
                <summary>
                  <span className="avatar">{user.firstName.charAt(0).toUpperCase()}</span>
                  <span>{user.firstName}</span>
                  <ChevronDown size={15} />
                </summary>
                <div className="user-menu__panel">
                  <Link to="/profile">Profile</Link>
                  <Link to="/my-pg">My PG</Link>
                  <Link to="/rent">Rent</Link>
                  <Link to="/complaints">Complaints</Link>
                  <Link to="/notices">Notices</Link>
                  <Link to="/food">Food</Link>
                  <Link to="/visitors">Visitors</Link>
                  <Link to="/wishlist">Wishlist</Link>
                  <Link to="/bookings">My Bookings</Link>
                  <Link to="/notifications">Notifications {unreadCount > 0 ? `(${unreadCount})` : ''}</Link>
                  {!isOwner ? <Link to="/owner/apply">Become an Owner</Link> : null}
                  {isOwner ? <Link to="/owner/dashboard">Owner Dashboard</Link> : null}
                  {isAdmin ? <Link to="/admin/dashboard">Admin Dashboard</Link> : null}
                  <button type="button" onClick={handleLogout}>
                    <LogOut size={16} />
                    Logout
                  </button>
                </div>
              </details>
            </>
          ) : (
            <>
              <Link className="secondary-link compact-button" to="/login">Login</Link>
              <Link className="primary-link compact-button" to="/register">Sign Up</Link>
              <Link className="secondary-link compact-button" to={ownerRoute}>
                <Building2 size={16} />
                List Your PG
              </Link>
            </>
          )}
        </div>
        <button
          className="mobile-menu-button"
          type="button"
          aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((open) => !open)}
        >
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
        <div className={`mobile-menu ${menuOpen ? 'mobile-menu--open' : ''}`}>
          <nav aria-label="Mobile navigation">
            <NavLink to="/find-pg"><Search size={17} /> Find PG</NavLink>
            <NavLink to="/about">About</NavLink>
            <NavLink to="/for-owners">For Owners</NavLink>
            <NavLink to="/contact">Contact</NavLink>
            <NavLink to="/compare">Compare</NavLink>
            {isAuthenticated ? (
              <>
                <NavLink to="/wishlist">Wishlist</NavLink>
                <NavLink to="/bookings">My Bookings</NavLink>
                <NavLink to="/rent"><Wallet size={17} /> Rent</NavLink>
                <NavLink to="/complaints">Complaints</NavLink>
                <NavLink to="/notices">Notices</NavLink>
                <NavLink to="/food">Food</NavLink>
                <NavLink to="/visitors">Visitors</NavLink>
                <NavLink to="/notifications"><Bell size={17} /> Notifications {unreadCount > 0 ? `(${unreadCount})` : ''}</NavLink>
                <NavLink to="/my-pg">My PG</NavLink>
                <NavLink to="/profile">Profile</NavLink>
                {!isOwner ? <NavLink to="/owner/apply">Become an Owner</NavLink> : null}
                {isOwner ? <NavLink to="/owner/dashboard">Owner Dashboard</NavLink> : null}
                {isAdmin ? <NavLink to="/admin/dashboard">Admin Dashboard</NavLink> : null}
                <button type="button" onClick={handleLogout}>
                  <LogOut size={17} />
                  Logout
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login">Login</NavLink>
                <NavLink to="/register">Sign Up</NavLink>
                <NavLink to={ownerRoute}><Building2 size={17} /> List Your PG</NavLink>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="content">{children}</main>
      <PublicFooter />
    </div>
  );
}
