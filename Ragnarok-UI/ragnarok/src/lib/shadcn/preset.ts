import type { Config } from "tailwindcss";
import { shadcnPlugin } from "./plugin";
import animatePlugin from "tailwindcss-animate";

export const shadcnPreset = {
  darkMode: ["class"],
  content: [],
  plugins: [animatePlugin, shadcnPlugin],
} satisfies Config;
