'use client';

/**
 * Debug component that logs user state to console
 * Add this to any page to see user state in console
 */

import { useEffect } from 'react';
import { useUser, useUserRoles, useIsAuthenticated } from '../hooks/use-user';

export function UserStateDebugger() {
  const user = useUser();
  const roles = useUserRoles();
  const isAuthenticated = useIsAuthenticated();

  useEffect(() => {
    console.log('========================================');
    console.log('🔍 USER STATE DEBUGGER');
    console.log('========================================');
    console.log('✅ Is Authenticated:', isAuthenticated);
    console.log('👤 User Data:', user);
    console.log('🎭 Roles:', roles);
    console.log('========================================');
  }, [user, roles, isAuthenticated]);

  // Re-log whenever user state changes
  useEffect(() => {
    if (user) {
      console.log('🔄 User state updated:', {
        username: user.username,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        roles: user.roles,
      });
    } else {
      console.log('❌ No user logged in');
    }
  }, [user]);

  return null; // This component doesn't render anything
}
