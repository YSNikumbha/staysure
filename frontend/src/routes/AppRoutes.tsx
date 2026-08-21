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
import { AboutPage } from '../pages/AboutPage';
import { ContactPage } from '../pages/ContactPage';
import { ForOwnersPage } from '../pages/ForOwnersPage';
import { PublicPgDetailsPage } from '../pages/PublicPgDetailsPage';
import { WishlistPage } from '../pages/WishlistPage';
import BookingsPage from '../pages/BookingsPage';
import BookingDetailPage from '../pages/BookingDetailPage';
import BookingKycPage from '../pages/BookingKycPage';
import OwnerBookingsPage from '../pages/OwnerBookingsPage';
import OwnerBookingDetailPage from '../pages/OwnerBookingDetailPage';
import OwnerTenantsPage from '../pages/OwnerTenantsPage';
import OwnerTenantDetailPage from '../pages/OwnerTenantDetailPage';
import MyPgPage from '../pages/MyPgPage';
import OwnerRentPage from '../pages/OwnerRentPage';
import OwnerRentDetailPage from '../pages/OwnerRentDetailPage';
import RentPage from '../pages/RentPage';
import RentInvoiceDetailPage from '../pages/RentInvoiceDetailPage';
import ComplaintsPage from '../pages/ComplaintsPage';
import CreateComplaintPage from '../pages/CreateComplaintPage';
import ComplaintDetailPage from '../pages/ComplaintDetailPage';
import OwnerComplaintsPage from '../pages/OwnerComplaintsPage';
import OwnerComplaintDetailPage from '../pages/OwnerComplaintDetailPage';
import OwnerMaintenancePage from '../pages/OwnerMaintenancePage';
import OwnerMaintenanceDetailPage from '../pages/OwnerMaintenanceDetailPage';
import NoticesPage from '../pages/NoticesPage';
import NoticeDetailPage from '../pages/NoticeDetailPage';
import OwnerNoticesPage from '../pages/OwnerNoticesPage';
import OwnerNoticeFormPage from '../pages/OwnerNoticeFormPage';
import FoodPage from '../pages/FoodPage';
import OwnerFoodPage from '../pages/OwnerFoodPage';
import VisitorsPage from '../pages/VisitorsPage';
import CreateVisitorPage from '../pages/CreateVisitorPage';
import VisitorDetailPage from '../pages/VisitorDetailPage';
import OwnerVisitorsPage from '../pages/OwnerVisitorsPage';
import OwnerVisitorDetailPage from '../pages/OwnerVisitorDetailPage';
import NotificationsPage from '../pages/NotificationsPage';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleProtectedRoute } from './RoleProtectedRoute';

export function AppRoutes() {
  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/find-pg" element={<FindPgPage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/for-owners" element={<ForOwnersPage />} />
        <Route path="/contact" element={<ContactPage />} />
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
          path="/bookings"
          element={
            <ProtectedRoute>
              <BookingsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/bookings/:id"
          element={
            <ProtectedRoute>
              <BookingDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/bookings/:id/kyc"
          element={
            <ProtectedRoute>
              <BookingKycPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-pg"
          element={
            <ProtectedRoute>
              <MyPgPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/rent"
          element={
            <ProtectedRoute>
              <RentPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/rent/:id"
          element={
            <ProtectedRoute>
              <RentInvoiceDetailPage />
            </ProtectedRoute>
          }
        />
        <Route path="/complaints" element={<ProtectedRoute><ComplaintsPage /></ProtectedRoute>} />
        <Route path="/complaints/new" element={<ProtectedRoute><CreateComplaintPage /></ProtectedRoute>} />
        <Route path="/complaints/:id" element={<ProtectedRoute><ComplaintDetailPage /></ProtectedRoute>} />
        <Route path="/notices" element={<ProtectedRoute><NoticesPage /></ProtectedRoute>} />
        <Route path="/notices/:id" element={<ProtectedRoute><NoticeDetailPage /></ProtectedRoute>} />
        <Route path="/food" element={<ProtectedRoute><FoodPage /></ProtectedRoute>} />
        <Route path="/visitors" element={<ProtectedRoute><VisitorsPage /></ProtectedRoute>} />
        <Route path="/visitors/new" element={<ProtectedRoute><CreateVisitorPage /></ProtectedRoute>} />
        <Route path="/visitors/:id" element={<ProtectedRoute><VisitorDetailPage /></ProtectedRoute>} />
        <Route path="/notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />
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
          path="/owner/bookings"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerBookingsPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/bookings/:id"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerBookingDetailPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/tenants"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerTenantsPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/tenants/:id"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerTenantDetailPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/rent"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerRentPage />
            </RoleProtectedRoute>
          }
        />
        <Route
          path="/owner/rent/:id"
          element={
            <RoleProtectedRoute role="PG_OWNER">
              <OwnerRentDetailPage />
            </RoleProtectedRoute>
          }
        />
        <Route path="/owner/complaints" element={<RoleProtectedRoute role="PG_OWNER"><OwnerComplaintsPage /></RoleProtectedRoute>} />
        <Route path="/owner/complaints/:id" element={<RoleProtectedRoute role="PG_OWNER"><OwnerComplaintDetailPage /></RoleProtectedRoute>} />
        <Route path="/owner/maintenance" element={<RoleProtectedRoute role="PG_OWNER"><OwnerMaintenancePage /></RoleProtectedRoute>} />
        <Route path="/owner/maintenance/:id" element={<RoleProtectedRoute role="PG_OWNER"><OwnerMaintenanceDetailPage /></RoleProtectedRoute>} />
        <Route path="/owner/notices" element={<RoleProtectedRoute role="PG_OWNER"><OwnerNoticesPage /></RoleProtectedRoute>} />
        <Route path="/owner/notices/new" element={<RoleProtectedRoute role="PG_OWNER"><OwnerNoticeFormPage /></RoleProtectedRoute>} />
        <Route path="/owner/notices/:id/edit" element={<RoleProtectedRoute role="PG_OWNER"><OwnerNoticeFormPage /></RoleProtectedRoute>} />
        <Route path="/owner/food" element={<RoleProtectedRoute role="PG_OWNER"><OwnerFoodPage /></RoleProtectedRoute>} />
        <Route path="/owner/visitors" element={<RoleProtectedRoute role="PG_OWNER"><OwnerVisitorsPage /></RoleProtectedRoute>} />
        <Route path="/owner/visitors/:id" element={<RoleProtectedRoute role="PG_OWNER"><OwnerVisitorDetailPage /></RoleProtectedRoute>} />
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
