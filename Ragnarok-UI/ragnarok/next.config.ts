import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // config options here
  async rewrites() {
    return [
      {
        source: "/ws/chat", 
        destination: `${process.env.NEXT_PUBLIC_RAGNAROK_APP_URL}/ws/chat`,
      },
    ];
  },
};

export default nextConfig;

/*import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  webpack(config) {
    config.resolve.alias["@"] = __dirname + "/src";
    return config;
  },
};

export default nextConfig;
*/
