import type { Config } from "tailwindcss";
import { shadcnPreset } from "./src/lib/shadcn-preset";

export default {
  presets: [shadcnPreset],
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
} satisfies Config;
