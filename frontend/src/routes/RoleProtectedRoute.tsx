import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import type { RoleName } from '../types/user';
import { ProtectedRoute } from './ProtectedRoute';

type RoleProtectedRouteProps = {
  role: RoleName;
  children: ReactNode;
};

export function RoleProtectedRoute({ role, children }: RoleProtectedRouteProps) {
  const roles = useAuthStore((state) => state.roles);

  return (
    <ProtectedRoute>
      {roles.includes(role) ? children : <Navigate to="/profile" replace />}
    </ProtectedRoute>
  );
}
