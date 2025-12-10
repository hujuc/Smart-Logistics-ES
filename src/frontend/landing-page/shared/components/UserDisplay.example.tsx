'use client';

/**
 * EXAMPLE COMPONENT - Demonstrates how to use the user store hooks
 *
 * This file shows various usage patterns for accessing user data.
 * Copy these patterns into your own components as needed.
 */

import { useUser, useUserRoles, useHasRole, useIsAuthenticated, useUserActions } from '../hooks/use-user';

/**
 * Example 1: Display user information
 */
export function UserWelcome() {
  const user = useUser();

  if (!user) {
    return <div>Please log in</div>;
  }

  return (
    <div className="p-4 bg-gray-100 rounded">
      <h2 className="text-xl font-bold">
        Welcome, {user.firstName || user.username}!
      </h2>
      <p className="text-gray-600">{user.email}</p>
    </div>
  );
}

/**
 * Example 2: Role-based rendering
 */
export function AdminPanel() {
  const isAdmin = useHasRole('LOGISTICS_MANAGER');

  if (!isAdmin) {
    return <div>Access denied. Admin only.</div>;
  }

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold">Admin Panel</h2>
      {/* Admin-only content */}
    </div>
  );
}

/**
 * Example 3: Check multiple roles
 */
export function WarehouseFeatures() {
  const roles = useUserRoles();
  const hasWarehouseAccess = roles.includes('WAREHOUSE_STAFF') || roles.includes('LOGISTICS_MANAGER');

  if (!hasWarehouseAccess) {
    return null;
  }

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold">Warehouse Features</h2>
      {/* Warehouse features */}
    </div>
  );
}

/**
 * Example 4: Authentication check
 */
export function ProtectedContent() {
  const isAuthenticated = useIsAuthenticated();

  if (!isAuthenticated) {
    return (
      <div className="p-4 text-center">
        <p>Please log in to view this content</p>
      </div>
    );
  }

  return (
    <div className="p-4">
      {/* Protected content */}
      <p>This content is only visible to authenticated users</p>
    </div>
  );
}

/**
 * Example 5: Logout button
 */
export function LogoutButton() {
  const { clearUser } = useUserActions();

  const handleLogout = () => {
    // Clear user from store and storage
    clearUser();

    // Redirect to landing page
    window.location.href = '/';
  };

  return (
    <button
      onClick={handleLogout}
      className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
    >
      Logout
    </button>
  );
}

/**
 * Example 6: Display user roles as badges
 */
export function UserRoleBadges() {
  const roles = useUserRoles();

  if (roles.length === 0) {
    return null;
  }

  return (
    <div className="flex gap-2">
      {roles.map((role) => (
        <span
          key={role}
          className="px-2 py-1 text-xs font-semibold bg-blue-100 text-blue-800 rounded"
        >
          {role}
        </span>
      ))}
    </div>
  );
}

/**
 * Example 7: Conditional navigation
 */
export function UserNavigation() {
  const user = useUser();
  const isAdmin = useHasRole('LOGISTICS_MANAGER');

  return (
    <nav className="flex gap-4">
      <a href="/">Home</a>

      {user ? (
        <>
          <a href="/dashboard">Dashboard</a>
          {isAdmin && <a href="/admin">Admin</a>}
          <a href="/profile">Profile</a>
        </>
      ) : (
        <a href="/">Login</a>
      )}
    </nav>
  );
}
