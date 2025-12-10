import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/admin",
  assetPrefix: "/admin",
  trailingSlash: true,
  eslint: {
    ignoreDuringBuilds: true,
  },
  webpack: (config, { isServer }) => {
    // Add alias for @shared
    if (!config.resolve.alias) {
      config.resolve.alias = {};
    }
    config.resolve.alias['@shared'] = path.resolve('/shared');

    return config;
  },
};

export default nextConfig;
