import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  // No basePath - landing page is at root /
  trailingSlash: false,
  eslint: {
    ignoreDuringBuilds: true,
  },
};

export default nextConfig;
