import type { Metadata } from "next";
import "./globals.css";

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
        {children}
      </body>
    </html>
  );
}
