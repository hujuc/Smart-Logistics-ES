import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/driver",
  assetPrefix: "/driver",
  trailingSlash: false,
};

export default nextConfig;
