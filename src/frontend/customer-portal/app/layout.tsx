import type { Metadata } from "next";
import "./globals.css";
import { NotificationListener } from "@/components/notification-listener";
import { Navbar } from "@/components/navbar";
import { DynamicFloatingNav } from "@/components/dynamic-floating-nav";
import { Suspense } from "react";
import { UserStoreInitializer } from "@shared/components/UserStoreInitializer";
import { ChatWidget } from "@shared/components/chatbot/ChatWidget";
import { ObservabilityProvider } from "@shared/components/observability/ObservabilityProvider";

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
        <ObservabilityProvider serviceName="slms-customer-portal">
          <UserStoreInitializer />
          <NotificationListener />
          <Navbar />
          <Suspense fallback={null}>
            <DynamicFloatingNav />
          </Suspense>
          <main className="pr-30 pl-40" style={{ marginBottom: "80px" }}>
            {children}
          </main>
        </ObservabilityProvider>
        <ChatWidget apiEndpoint="/api/customer/chat" />
      </body>
    </html>
  );
}
