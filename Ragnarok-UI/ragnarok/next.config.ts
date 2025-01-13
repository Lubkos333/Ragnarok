import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  async rewrites() {
    return [
      {
        source: "/ws/chat", 
        destination: "http://localhost:7777/ws/chat",
      },
    ];
  },
};

export default nextConfig;
