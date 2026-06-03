import { motion, AnimatePresence } from "framer-motion";
import { Settings, Menu } from "lucide-react";
import { useThemeStore } from "@/store/themeStore";
import { useSystemStore } from "@/store/systemStore";
import { useAuthStore } from "@/store/authStore";
import { cn } from "@/lib/utils";

const STATUS_CONFIG = {
  NORMAL_OPERATION: { label: "NORMAL OPERATION", color: "text-emerald-400", dot: "bg-emerald-400" },
  LEAK_DETECTED: { label: "LEAK DETECTED", color: "text-red-400", dot: "bg-red-400" },
  BLOCKAGE_DETECTED: { label: "BLOCKAGE DETECTED", color: "text-amber-400", dot: "bg-amber-400" },
  OFFLINE: { label: "OFFLINE", color: "text-gray-400", dot: "bg-gray-400" },
};

interface NavbarProps {
  onMenuClick: () => void;
}

export function Navbar({ onMenuClick }: NavbarProps) {
  const { theme, toggleTheme } = useThemeStore();
  const { status } = useSystemStore();
  const { user } = useAuthStore();
  const cfg = STATUS_CONFIG[status];

  const initials = user
    ? `${(user.firstName?.[0] ?? user.username[0])}${user.lastName?.[0] ?? ""}`.toUpperCase()
    : "OP";

  return (
    <nav className="sticky top-0 z-50 h-14 border-b border-border bg-card/80 backdrop-blur-xl flex items-center px-4 gap-4">
      <button
        onClick={onMenuClick}
        className="lg:hidden p-2 rounded-lg hover:bg-muted transition-colors"
      >
        <Menu className="w-4 h-4" />
      </button>

      <div className="flex items-center gap-2 mr-4">
        <div className="w-7 h-7 rounded-lg bg-primary flex items-center justify-center">
          <svg viewBox="0 0 24 24" fill="none" className="w-4 h-4 text-white" stroke="currentColor" strokeWidth={2}>
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5M2 12l10 5 10-5" />
          </svg>
        </div>
        <span className="font-mono font-bold text-sm tracking-wider hidden sm:block">
          PIPELINE<span className="text-primary">·AI</span>
        </span>
      </div>

      <AnimatePresence mode="wait">
        <motion.div
          key={status}
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.9 }}
          className={cn(
            "flex items-center gap-2 px-3 py-1.5 rounded-full border text-xs font-mono font-semibold tracking-wider",
            "bg-card border-border",
            cfg.color
          )}
        >
          <span className={cn("w-2 h-2 rounded-full status-pulse", cfg.dot)} />
          <span className="hidden sm:block">{cfg.label}</span>
          <span className="sm:hidden">{cfg.label.split(" ")[0]}</span>
        </motion.div>
      </AnimatePresence>

      <div className="flex-1" />

      <button
        onClick={toggleTheme}
        className="p-2 rounded-lg hover:bg-muted transition-colors"
        title={`Switch to ${theme === "dark" ? "light" : "dark"} mode`}
      >
        {theme === "dark" ? (
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="5" />
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" strokeLinecap="round" />
          </svg>
        ) : (
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        )}
      </button>

      <a href="/settings" className="p-2 rounded-lg hover:bg-muted transition-colors">
        <Settings className="w-4 h-4" />
      </a>

      <div className="w-8 h-8 rounded-full bg-primary/20 border border-primary/40 flex items-center justify-center">
        <span className="text-xs font-mono font-bold text-primary">{initials}</span>
      </div>
    </nav>
  );
}
