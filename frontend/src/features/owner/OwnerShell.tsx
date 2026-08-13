import { Building2, LayoutDashboard, UserRound } from 'lucide-react';
import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';

type OwnerShellProps = {
  children: ReactNode;
};

export function OwnerShell({ children }: OwnerShellProps) {
  return (
    <div className="owner-layout">
      <aside className="owner-sidebar" aria-label="Owner navigation">
        <NavLink to="/owner/dashboard">
          <LayoutDashboard size={17} />
          Dashboard
        </NavLink>
        <NavLink to="/owner/pgs">
          <Building2 size={17} />
          My PGs
        </NavLink>
        <NavLink to="/profile">
          <UserRound size={17} />
          Profile
        </NavLink>
      </aside>
      <div className="owner-main">{children}</div>
    </div>
  );
}
