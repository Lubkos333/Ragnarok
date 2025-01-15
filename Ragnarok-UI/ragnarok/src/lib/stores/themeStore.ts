import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

interface ThemeState {
  theme: "light" | "dark";
  toggleTheme: () => void;
  setTheme: (theme: "light" | "dark") => void;
}

const useThemeStore = create<ThemeState, [["zustand/persist", unknown]]>(
  persist(
    (set) => ({
      theme: "light",
      toggleTheme: () =>
        set((state) => {
          const newTheme = state.theme === "dark" ? "light" : "dark";
          document.documentElement.classList.toggle(
            "dark",
            newTheme === "dark"
          );
          return { theme: newTheme };
        }),
      setTheme: (theme) =>
        set(() => {
          document.documentElement.classList.toggle("dark", theme === "dark");
          return { theme };
        }),
    }),
    {
      name: "theme-storage",
      storage: createJSONStorage(() => localStorage),
    }
  )
);

export default useThemeStore;
