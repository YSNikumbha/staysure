export type RoleName = 'SUPER_ADMIN' | 'PG_OWNER' | 'USER';

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'ARCHIVED';

export type User = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  profileImageUrl?: string | null;
  status: UserStatus;
  emailVerified: boolean;
  phoneVerified: boolean;
  lastLoginAt?: string | null;
  roles: RoleName[];
  permissions: string[];
};

export type UpdateProfileInput = {
  firstName: string;
  lastName: string;
  phone: string;
  profileImageUrl?: string;
};

export type ChangePasswordInput = {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
};
