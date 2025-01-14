import plugin from "tailwindcss/plugin";

export const shadcnPlugin = plugin(
  function ({ addBase }) {
    addBase({
      ":root": {
        "--background": "60, 8.33%, 95.29%",
        "--foreground": "216, 18.07%, 16.27%",
        "--card": "60, 8.33%, 95.29%",
        "--card-foreground": "216, 18.07%, 16.27%",
        "--popover": "0 0% 100%",
        "--popover-foreground": "222.2 84% 4.9%",
        "--primary": "215.56, 15.61%, 33.92%",
        "--primary-foreground": "60, 8.33%, 95.29%",
        "--secondary": "224, 12.4%, 76.27%",
        "--secondary-foreground": "215.56, 15.61%, 33.92%",
        "--muted": "0, 0%, 90.98%",
        "--muted-foreground": "224, 12.4%, 76.27%",
        "--accent-primary": "204.18, 57.26%, 45.88%",
        "--accent-primary-mid": "204.12, 77.27%, 25.88%",
        "--accent-primary-light": "203.81, 86.3%, 85.69%",
        "--accent-primary-dark": "201.18, 23.94%, 13.92%",
        "--accent-secondary": "41.25, 100%, 47.06%",
        "--accent-secondary-mid": "15.79, 77.87%, 52.16%",
        "--accent-secondary-light": "37.83, 47.92%, 81.18%",
        "--accent-secondary-dark": "15.65, 18.4%, 24.51%",
        "--destructive": "0 84.2% 60.2%",
        "--destructive-foreground": "210 40% 98%",
        "--border": "60, 8.33%, 95.29%",
        "--input": "60, 8.33%, 95.29%",
        "--ring": "216, 18.07%, 16.27%",
        "--chart-1": "12 76% 61%",
        "--chart-2": "173 58% 39%",
        "--chart-3": "197 37% 24%",
        "--chart-4": "43 74% 66%",
        "--chart-5": "27 87% 67%",
        "--radius": "0.5rem",
      },
      ".dark": {
        "--background": "216, 18.07%, 16.27%",
        "--foreground": "60, 8.33%, 95.29%",
        "--card": "216, 18.07%, 16.27%",
        "--card-foreground": "60, 8.33%, 95.29%",
        "--popover": "222.2 84% 4.9%",
        "--popover-foreground": "210 40% 98%",
        "--primary": "224, 12.4%, 76.27%",
        "--primary-foreground": "216, 18.07%, 16.27%",
        "--secondary": "215.56, 15.61%, 33.92%",
        "--secondary-foreground": "224, 12.4%, 76.27%",
        "--muted": "218.57, 12.5%, 21.96%",
        "--muted-foreground": "215.56, 15.61%, 33.92%",
        "--accent-primary": "204.18, 57.26%, 45.88%",
        "--accent-primary-mid": "204.12, 77.27%, 25.88%",
        "--accent-primary-light": "203.81, 86.3%, 85.69%",
        "--accent-primary-dark": "201.18, 23.94%, 13.92%",
        "--accent-secondary": "41.25, 100%, 47.06%",
        "--accent-secondary-mid": "15.79, 77.87%, 52.16%",
        "--accent-secondary-light": "37.83, 47.92%, 81.18%",
        "--accent-secondary-dark": "15.65, 18.4%, 24.51%",
        "--destructive": "0 62.8% 30.6%",
        "--destructive-foreground": "210 40% 98%",
        "--border": "216, 18.07%, 26.27%",
        "--input": "216, 18.07%, 26.27%",
        "--ring": "60, 8.33%, 95.29%",
        "--chart-1": "220 70% 50%",
        "--chart-2": "160 60% 45%",
        "--chart-3": "30 80% 55%",
        "--chart-4": "280 65% 60%",
        "--chart-5": "340 75% 55%",
      },
    });
    addBase({
      "*": {
        "@apply border-border": {},
      },
      body: {
        "@apply bg-background text-foreground": {},
      },
    });
    addBase({
      ":root": {
        "--sidebar-background": "60, 8.33%, 95.29%",
        "--sidebar-foreground": "216, 18.07%, 16.27%",
        "--sidebar-primary": "215.56, 15.61%, 33.92%",
        "--sidebar-primary-foreground": "60, 8.33%, 95.29%",
        "--sidebar-muted": "0, 0%, 90.98%",
        "--sidebar-muted-foreground": "224, 12.4%, 76.27%",
        "--sidebar-border": "60, 8.33%, 95.29%",
        "--sidebar-ring": "216, 18.07%, 16.27%",
      },
      ".dark": {
        "--sidebar-background": "216, 18.07%, 16.27%",
        "--sidebar-foreground": "60, 8.33%, 95.29%",
        "--sidebar-primary": "224, 12.4%, 76.27%",
        "--sidebar-primary-foreground": "216, 18.07%, 16.27%",
        "--sidebar-muted": "218.57, 12.5%, 21.96%",
        "--sidebar-muted-foreground": "215.56, 15.61%, 33.92%",
        "--sidebar-border": "216, 18.07%, 26.27%",
        "--sidebar-ring": "217.2 91.2% 59.8%",
      },
    });
  },
  {
    theme: {
      extend: {
        colors: {
          background: "hsl(var(--background))",
          foreground: "hsl(var(--foreground))",
          card: {
            DEFAULT: "hsl(var(--card))",
            foreground: "hsl(var(--card-foreground))",
          },
          popover: {
            DEFAULT: "hsl(var(--popover))",
            foreground: "hsl(var(--popover-foreground))",
          },
          primary: {
            DEFAULT: "hsl(var(--primary))",
            foreground: "hsl(var(--primary-foreground))",
          },
          secondary: {
            DEFAULT: "hsl(var(--secondary))",
            foreground: "hsl(var(--secondary-foreground))",
          },
          muted: {
            DEFAULT: "hsl(var(--muted))",
            foreground: "hsl(var(--muted-foreground))",
          },
          accent: {
            primary: {
              DEFAULT: "hsl(var(--accent-primary))",
              mid: "hsl(var(--accent-primary-mid))",
              light: "hsl(var(--accent-primary-light))",
              dark: "hsl(var(--accent-primary-dark))",
            },
            secondary: {
              DEFAULT: "hsl(var(--accent-secondary))",
              mid: "hsl(var(--accent-secondary-mid))",
              light: "hsl(var(--accent-secondary-light))",
              dark: "hsl(var(--accent-secondary-dark))",
            },
          },
          destructive: {
            DEFAULT: "hsl(var(--destructive))",
            foreground: "hsl(var(--destructive-foreground))",
          },
          border: "hsl(var(--border))",
          input: "hsl(var(--input))",
          ring: "hsl(var(--ring))",
          chart: {
            "1": "hsl(var(--chart-1))",
            "2": "hsl(var(--chart-2))",
            "3": "hsl(var(--chart-3))",
            "4": "hsl(var(--chart-4))",
            "5": "hsl(var(--chart-5))",
          },
          sidebar: {
            DEFAULT: "hsl(var(--sidebar-background))",
            foreground: "hsl(var(--sidebar-foreground))",
            primary: "hsl(var(--sidebar-primary))",
            "primary-foreground": "hsl(var(--sidebar-primary-foreground))",
            muted: "hsl(var(--sidebar-muted))",
            "accent-foreground": "hsl(var(--sidebar-muted-foreground))",
            border: "hsl(var(--sidebar-border))",
            ring: "hsl(var(--sidebar-ring))",
          },
        },
        borderRadius: {
          lg: "var(--radius)",
          md: "calc(var(--radius) - 2px)",
          sm: "calc(var(--radius) - 4px)",
        },
      },
    },
  }
);
