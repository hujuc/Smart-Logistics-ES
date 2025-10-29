import type { NextConfig } from "next";
import path from "path";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/customer",
  assetPrefix: "/customer",
  trailingSlash: false,
  webpack: (config) => {
    if (config.resolve) {
      config.resolve.alias = {
        ...config.resolve.alias,
        "@": path.resolve(__dirname, "./"),
      };
    }
    return config;
  },
};

export default nextConfig;
