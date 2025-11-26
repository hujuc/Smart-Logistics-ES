import type { Metadata } from "next";
import "./globals.css";
import { UserStoreInitializer } from "@shared/components/UserStoreInitializer";
import { UserStateDebugger } from "@shared/components/UserStateDebugger";
import { ChatWidget } from "@shared/components/chatbot/ChatWidget";

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
        <UserStoreInitializer />
        <UserStateDebugger />
        {children}
        <ChatWidget apiEndpoint="/api/landing-page/chat" skipAuth={true} />
      </body>
    </html>
  );
}
