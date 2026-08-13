import { create } from 'zustand';
import { authApi } from '../api/auth.api';
import { tokenStorage } from '../api/tokenStorage';
import { userApi } from '../api/user.api';
import type { LoginInput, RegisterInput } from '../types/auth';
import type { RoleName, User } from '../types/user';

type AuthState = {
  user: User | null;
  isAuthenticated: boolean;
  isHydrating: boolean;
  roles: RoleName[];
  hydrate: () => Promise<void>;
  login: (input: LoginInput) => Promise<void>;
  register: (input: RegisterInput) => Promise<void>;
  logout: () => Promise<void>;
  setUser: (user: User) => void;
};

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: Boolean(tokenStorage.getAccessToken()),
  isHydrating: true,
  roles: [],

  async hydrate() {
    const token = tokenStorage.getAccessToken();
    if (!token) {
      set({ user: null, roles: [], isAuthenticated: false, isHydrating: false });
      return;
    }
    try {
      const user = await userApi.me();
      set({ user, roles: user.roles, isAuthenticated: true, isHydrating: false });
    } catch {
      tokenStorage.clear();
      set({ user: null, roles: [], isAuthenticated: false, isHydrating: false });
    }
  },

  async login(input) {
    const payload = await authApi.login(input);
    tokenStorage.set(payload.accessToken, payload.refreshToken);
    set({ user: payload.user, roles: payload.user.roles, isAuthenticated: true });
  },

  async register(input) {
    const payload = await authApi.register(input);
    tokenStorage.set(payload.accessToken, payload.refreshToken);
    set({ user: payload.user, roles: payload.user.roles, isAuthenticated: true });
  },

  async logout() {
    const refreshToken = tokenStorage.getRefreshToken();
    tokenStorage.clear();
    set({ user: null, roles: [], isAuthenticated: false });
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        tokenStorage.clear();
      }
    }
  },

  setUser(user) {
    set({ user, roles: user.roles, isAuthenticated: true });
  }
}));
