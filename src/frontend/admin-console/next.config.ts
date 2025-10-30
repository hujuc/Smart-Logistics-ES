import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/admin",
  assetPrefix: "/admin",
  trailingSlash: false,
};

export default nextConfig;
