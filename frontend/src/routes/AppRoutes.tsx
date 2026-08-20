import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '../layouts/AppLayout';
import { LoginPage } from '../features/auth/LoginPage';
import { RegisterPage } from '../features/auth/RegisterPage';
import { ForgotPasswordPage } from '../features/auth/ForgotPasswordPage';
import { ResetPasswordPage } from '../features/auth/ResetPasswordPage';
import { ProfilePage } from '../features/user/ProfilePage';
import { OwnerApplyPage } from '../features/owner/OwnerApplyPage';
import { OwnerDashboardPage } from '../features/owner/OwnerDashboardPage';
import { OwnerPgDetailsPage } from '../features/owner/OwnerPgDetailsPage';
import { OwnerPgFormPage } from '../features/owner/OwnerPgFormPage';
import { OwnerPgsPage } from '../features/owner/OwnerPgsPage';
import { AdminDashboardPage } from '../features/admin/AdminDashboardPage';
import { AdminPgDetailPage } from '../features/admin/AdminPgDetailPage';
import { AdminPgsPage } from '../features/admin/AdminPgsPage';
import { AdminUsersPage } from '../features/admin/AdminUsersPage';
import { AdminOwnersPage } from '../features/admin/AdminOwnersPage';
import { AdminOwnerDetailPage } from '../features/admin/AdminOwnerDetailPage';
import { ComparePage } from '../pages/ComparePage';
import { FindPgPage } from '../pages/FindPgPage';
import { HomePage } from '../pages/HomePage';
import { PublicPgDetailsPage } from '../pages/PublicPgDetailsPage';
import { WishlistPage } from '../pages/WishlistPage';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleProtectedRoute } from './RoleProtectedRoute';

export function AppRoutes() {
  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/find-pg" element={<FindPgPage />} />
        <Route path="/pg/:slug" element={<PublicPgDetailsPage />} />
        <Route path="/compare" element={<ComparePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/wishlist"
          element={
            <ProtectedRoute>
              <WishlistPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/apply"
          element={
            <ProtectedRoute>
              <OwnerApplyPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/dashboard"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerDashboardPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/pgs"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerPgsPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/pgs/new"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerPgFormPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/pgs/:id"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerPgDetailsPage focus="overview" />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/pgs/:id/edit"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerPgFormPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/pgs/:id/floors"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerPgDetailsPage focus="floors" />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/pgs/:id/rooms"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerPgDetailsPage focus="rooms" />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/pgs/:id/beds"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerPgDetailsPage focus="beds" />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/dashboard"
          element={
            <RoleProtectedRoute role="SUPER_ADMIN">
              <AdminDashboardPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <RoleProtectedRoute role="SUPER_ADMIN">
              <AdminUsersPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/pgs"
          element={
            <RoleProtectedRoute role="SUPER_ADMIN">
              <AdminPgsPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/pgs/pending"
          element={
            <RoleProtectedRoute role="SUPER_ADMIN">
              <AdminPgsPage pendingOnly />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/pgs/:id"
          element={
            <RoleProtectedRoute role="SUPER_ADMIN">
              <AdminPgDetailPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/owners"
          element={
            <RoleProtectedRoute role="SUPER_ADMIN">
              <AdminOwnersPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/admin/owners/:id"
          element={
            <RoleProtectedRoute role="SUPER_ADMIN">
              <AdminOwnerDetailPage />
            </RoleProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppLayout>
  );
}
