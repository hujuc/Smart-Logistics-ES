import type { Metadata } from "next";
import "./globals.css";
import { Navbar } from "@/components/navbar";
import { DynamicFloatingNav } from "@/components/dynamic-floating-nav";
import { Suspense } from "react";

export const metadata: Metadata = {
  title: "SLMS - Customer Portal",
  description: "Track your packages and manage delivery preferences",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <Navbar />
        <Suspense fallback={null}>
          <DynamicFloatingNav />
        </Suspense>
        <main className="pr-30 pl-40" style={{ marginBottom: "80px" }}>
          {children}
        </main>
      </body>
    </html>
  );
}
