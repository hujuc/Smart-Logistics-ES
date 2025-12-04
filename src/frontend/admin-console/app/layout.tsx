import type { Metadata } from "next";
import "./globals.css";
import { Navbar } from "@/components/navbar";
import { DynamicFloatingNav } from "@/components/dynamic-floating-nav";
import { Suspense } from "react";
import { UserStoreInitializer } from "@shared/components/UserStoreInitializer";
import { UserStateDebugger } from "@shared/components/UserStateDebugger";
import { ObservabilityProvider } from "@shared/components/observability/ObservabilityProvider";
import { AdminChatbot } from "@/components/admin-chatbot";

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
      <body className="antialiased">
        <ObservabilityProvider serviceName="slms-admin-console">
          <UserStoreInitializer />
          <UserStateDebugger />
          <Navbar />
          <Suspense fallback={null}>
            <DynamicFloatingNav />
          </Suspense>
          <main className="px-4 md:pt-10 lg:pt-10 xl:pt-10 sm:pt-6 xs:pt-6 sm:px-6 md:pl-24 md:pr-8 lg:pl-32 lg:pr-12 xl:pl-40 xl:pr-16">
            {children}
          </main>
          <AdminChatbot />
        </ObservabilityProvider>
      </body>
    </html>
  );
}
