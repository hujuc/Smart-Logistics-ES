import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/customer",
  assetPrefix: "/customer",
  trailingSlash: false,
};

export default nextConfig;
