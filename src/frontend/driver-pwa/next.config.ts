import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  basePath: "/driver",
  assetPrefix: "/driver",
  trailingSlash: true,
};

export default nextConfig;
