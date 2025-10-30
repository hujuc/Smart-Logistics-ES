import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  // No basePath - landing page is at root /
  trailingSlash: false,
};

export default nextConfig;
