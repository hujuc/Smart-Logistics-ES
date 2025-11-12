import type { Metadata } from "next";
import "./globals.css";
import { UserStoreInitializer } from "@shared/components/UserStoreInitializer";

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
        <UserStoreInitializer />
        {children}
      </body>
    </html>
  );
}
