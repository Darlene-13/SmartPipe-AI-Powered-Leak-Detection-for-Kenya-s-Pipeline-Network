import axios from "axios";

export const BASE_URL = "https://ai-pipeline-leak-detection-1.onrender.com";

export const api = axios.create({
    baseURL: BASE_URL,
    headers: { "Content-Type": "application/json" },
    timeout: 15000,
});

api.interceptors.request.use((config) => {
    try {
        const raw = localStorage.getItem("pipeline-auth");
        if (raw) {
            const parsed = JSON.parse(raw);
            const token = parsed?.state?.token;
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }
    } catch {
        // no token
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
