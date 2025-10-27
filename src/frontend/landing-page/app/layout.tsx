import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "SLMS - Smart Logistics Management System",
  description: "Streamline your shipping operations with real-time tracking, route optimization, and comprehensive analytics",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
