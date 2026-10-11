import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Keep build and start on one output folder, separate from OneDrive's locked .next cache.
  distDir: process.env.NEXT_BUILD_OUTPUT || ".next-verify",
};

export default nextConfig;
