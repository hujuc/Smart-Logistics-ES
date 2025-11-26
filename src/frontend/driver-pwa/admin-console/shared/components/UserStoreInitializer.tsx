'use client';

/**
 * Client component to initialize user store from localStorage
 * Add this component to your root layout to enable user state management
 *
 * @example
 * // In your root layout.tsx:
 * import { UserStoreInitializer } from '@/../../shared/components/UserStoreInitializer';
 *
 * export default function RootLayout({ children }) {
 *   return (
 *     <html>
 *       <body>
 *         <UserStoreInitializer />
 *         {children}
 *       </body>
 *     </html>
 *   );
 * }
 */

import { useEffect } from 'react';
import { initializeUserStore, setupUserStoreSync } from '../lib/user-store';

export function UserStoreInitializer() {
  useEffect(() => {
    // Load user data from localStorage on mount
    initializeUserStore();

    // Set up cross-tab synchronization
    const cleanup = setupUserStoreSync();

    // Cleanup on unmount
    return cleanup;
  }, []);

  // This component doesn't render anything
  return null;
}
