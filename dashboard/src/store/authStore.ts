import { create } from "zustand";
import { persist } from "zustand/middleware";
import { api } from "@/lib/api";

interface User {
    username: string;
    role: "OPERATOR" | "VIEWER";
    firstName?: string;
    lastName?: string;
}

interface AuthStore {
    token: string | null;
    user: User | null;
    isAuthenticated: boolean;
    loading: boolean;
    error: string | null;
    login: (username: string, password: string) => Promise<void>;
    register: (data: { username: string; password: string; firstName: string; lastName: string }) => Promise<void>;
    logout: () => Promise<void>;
    clearError: () => void;
}

// Strips "ROLE_" prefix from backend role strings e.g. "ROLE_OPERATOR" → "OPERATOR"
const parseRole = (raw: string | undefined | null): "OPERATOR" | "VIEWER" => {
    if (!raw) return "VIEWER";
    const stripped = raw.replace("ROLE_", "").toUpperCase();
    return stripped === "OPERATOR" ? "OPERATOR" : "VIEWER";
};

export const useAuthStore = create<AuthStore>()(
    persist(
        (set, get) => ({
            token: null,
            user: null,
            isAuthenticated: false,
            loading: false,
            error: null,

            login: async (username, password) => {
                set({ loading: true, error: null });
                try {
                    const { data } = await api.post("/api/auth/login", { username, password });
                    const token = data.token ?? data.access_token ?? data.accessToken ?? null;
                    const rawRole = data.user?.role ?? data.role ?? null;
                    const user: User = {
                        username: data.user?.username ?? data.username ?? username,
                        role: parseRole(rawRole),
                        firstName: data.user?.firstName ?? data.user?.first_name ?? data.firstName ?? "",
                        lastName: data.user?.lastName ?? data.user?.last_name ?? data.lastName ?? "",
                    };
                    set({ token, user, isAuthenticated: true, loading: false });
                } catch (err: any) {
                    const msg =
                        err.response?.data?.message ??
                        err.response?.data?.error ??
                        err.response?.data?.detail ??
                        "Login failed. Check your credentials.";
                    set({ loading: false, error: msg });
                    throw new Error(msg);
                }
            },

            register: async ({ username, password, firstName, lastName }) => {
                set({ loading: true, error: null });
                try {
                    const { data } = await api.post("/api/auth/register", {
                        username,
                        password,
                        firstName,
                        lastName,
                    });
                    const token = data.token ?? data.access_token ?? data.accessToken ?? null;
                    const rawRole = data.user?.role ?? data.role ?? null;
                    const user: User = {
                        username: data.user?.username ?? data.username ?? username,
                        role: parseRole(rawRole),
                        firstName: data.user?.firstName ?? data.user?.first_name ?? firstName,
                        lastName: data.user?.lastName ?? data.user?.last_name ?? lastName,
                    };
                    set({ token, user, isAuthenticated: true, loading: false });
                } catch (err: any) {
                    const msg =
                        err.response?.data?.message ??
                        err.response?.data?.error ??
                        err.response?.data?.detail ??
                        "Registration failed. Username may already exist.";
                    set({ loading: false, error: msg });
                    throw new Error(msg);
                }
            },

            logout: async () => {
                try {
                    await api.post("/api/auth/logout");
                } catch {
                    // ignore errors on logout
                } finally {
                    set({ token: null, user: null, isAuthenticated: false, error: null });
                }
            },

            clearError: () => set({ error: null }),
        }),
        { name: "pipeline-auth" }
    )
);