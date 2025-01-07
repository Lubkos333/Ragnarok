import { create } from "zustand";
import { persist } from "zustand/middleware";

interface OnboardingState {
  showOnboarding: boolean;
  lastVisit: number;
  setOnboarding: (status: boolean) => void;
  resetOnboarding: () => void;
}

const THIRTY_DAYS_IN_MS = 30 * 24 * 60 * 60 * 1000;

export const useOnboardingStore = create(
  persist<OnboardingState>(
    (set) => ({
      showOnboarding: true,
      lastVisit: Date.now(),
      setOnboarding: (status) =>
        set({ showOnboarding: status, lastVisit: Date.now() }),
      resetOnboarding: () =>
        set({ showOnboarding: true, lastVisit: Date.now() }),
    }),
    {
      name: "onboarding-storage",
      onRehydrateStorage: () => (state) => {
        if (state) {
          const currentTime = Date.now();
          if (currentTime - state.lastVisit > THIRTY_DAYS_IN_MS) {
            state.showOnboarding = true;
          }
        }
      },
    }
  )
);
