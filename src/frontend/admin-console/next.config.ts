import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/admin",
  assetPrefix: "/admin",
  trailingSlash: true,
};

export default nextConfig;
