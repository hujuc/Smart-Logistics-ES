"use client";

import { SessionProvider } from "next-auth/react";
import type { ReactNode } from "react";

export function AuthProvider({ children }: { children: ReactNode }) {
  return (
    <SessionProvider basePath="/admin/api/auth">
      {children}
    </SessionProvider>
  );
}
