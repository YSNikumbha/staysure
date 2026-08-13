import { LogOut, ShieldCheck, UserRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

type AppLayoutProps = {
  children: ReactNode;
};

export function AppLayout({ children }: AppLayoutProps) {
  const navigate = useNavigate();
  const { user, isAuthenticated, roles, logout } = useAuthStore();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink to="/" className="brand">
          StaySure
        </NavLink>
        <nav className="topnav" aria-label="Primary navigation">
          <NavLink to="/find-pg">Find PG</NavLink>
          <NavLink to="/compare">Compare</NavLink>
          {isAuthenticated ? (
            <>
              <NavLink to="/profile">Profile</NavLink>
              <NavLink to="/wishlist">Wishlist</NavLink>
              {!roles.includes('PG_OWNER') ? <NavLink to="/owner/apply">Owner Application</NavLink> : null}
              {roles.includes('PG_OWNER') ? <NavLink to="/owner/dashboard">Owner</NavLink> : null}
              {roles.includes('SUPER_ADMIN') ? <NavLink to="/admin/dashboard">Admin</NavLink> : null}
            </>
          ) : (
            <>
              <NavLink to="/login">Login</NavLink>
              <NavLink to="/register">Register</NavLink>
            </>
          )}
        </nav>
        <div className="topbar-user">
          {user ? (
            <>
              <span className="user-chip">
                {roles.includes('SUPER_ADMIN') ? <ShieldCheck size={16} /> : <UserRound size={16} />}
                {user.firstName}
              </span>
              <button className="icon-button" type="button" onClick={handleLogout} title="Logout" aria-label="Logout">
                <LogOut size={18} />
              </button>
            </>
          ) : null}
        </div>
      </header>
      <main className="content">{children}</main>
    </div>
  );
}
