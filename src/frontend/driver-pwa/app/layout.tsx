import type { Metadata } from "next";
import "./globals.css";
import { UserStoreInitializer } from "@shared/components/UserStoreInitializer";
import { ChatWidget } from "@shared/components/chatbot/ChatWidget";
import { ObservabilityProvider } from "@shared/components/observability/ObservabilityProvider";

export const metadata: Metadata = {
  title: "SLMS - Driver Portal",
  description: "Manage delivery routes and update delivery status",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <ObservabilityProvider serviceName="slms-driver-pwa">
          <UserStoreInitializer />
          {children}
        <ChatWidget apiEndpoint="/api/driver/chat" />
        </ObservabilityProvider>
      </body>
    </html>
  );
}
