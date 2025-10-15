import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { Navbar } from "@/components/navbar";
import { DynamicFloatingNav } from "@/components/dynamic-floating-nav";
import { AuthProvider } from "@/components/providers/session-provider";
import { Suspense } from "react";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "SLMS - Smart Logistics Management System",
  description: "Admin console for warehouse, logistics, and customer service management",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <AuthProvider>
          <Navbar />
          <Suspense fallback={null}>
            <DynamicFloatingNav />
          </Suspense>
          <main className="px-60 py-8">
            {children}
          </main>
        </AuthProvider>
      </body>
    </html>
  );
}
