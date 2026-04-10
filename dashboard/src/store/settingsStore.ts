import { create } from "zustand";
import { persist } from "zustand/middleware";

interface SettingsStore {
    liveChartUpdates: boolean;
    soundAlerts: boolean;
    wsEnabled: boolean;
    backendUrl: string;
    mlServiceUrl: string;
    mqttUrl: string;
    redisUrl: string;
    toggleLiveChartUpdates: () => void;
    toggleSoundAlerts: () => void;
    toggleWs: () => void;
}

export const useSettingsStore = create<SettingsStore>()(
    persist(
        (set) => ({
            liveChartUpdates: true,
            soundAlerts: false,
            wsEnabled: true,
            backendUrl: "http://localhost:8080",
            mlServiceUrl: "http://localhost:5000",
            mqttUrl: "mqtt://localhost:1883",
            redisUrl: "redis://localhost:6379",
            toggleLiveChartUpdates: () => set((s) => ({ liveChartUpdates: !s.liveChartUpdates })),
            toggleSoundAlerts: () => set((s) => ({ soundAlerts: !s.soundAlerts })),
            toggleWs: () => set((s) => ({ wsEnabled: !s.wsEnabled })),
        }),
        { name: "pipeline-settings" }
    )
);
