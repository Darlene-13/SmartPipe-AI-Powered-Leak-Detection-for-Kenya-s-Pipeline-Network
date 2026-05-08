import axios from "axios";

export const BASE_URL = "https://ai-pipeline-leak-detection-1.onrender.com";

export const api = axios.create({
    baseURL: BASE_URL,
    headers: { "Content-Type": "application/json" },
    timeout: 15000,
});

function getToken(): string | null {
    try {
        const raw = localStorage.getItem("pipeline-auth");
        if (!raw) return null;
        const parsed = JSON.parse(raw);
        return parsed?.state?.token ?? null;
    } catch {
        return null;
    }
}

api.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (res) => res,
    (err) => {
        if (err.response?.status === 401) {
            localStorage.removeItem("pipeline-auth");
            window.location.href = "/login";
        }
        return Promise.reject(err);
    }
);