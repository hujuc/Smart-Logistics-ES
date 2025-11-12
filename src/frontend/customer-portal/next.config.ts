import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/customer",
  assetPrefix: "/customer",
  trailingSlash: true,
};

export default nextConfig;
