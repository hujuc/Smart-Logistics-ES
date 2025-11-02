# Shared Frontend Library

This directory contains shared code used across all SLMS frontends (landing-page, admin-console, customer-portal, driver-pwa).

## User Authentication State Management

### Overview

The shared library provides a lightweight Zustand-based state management solution for user information across all frontends. This keeps your existing token-based authentication (middleware checks, cookies, API Bearer tokens) intact while adding easy access to user data in components.

### Quick Start

1. **Install dependencies** (already added to all frontends):
   ```bash
   npm install zustand
   ```

2. **UserStoreInitializer is already added** to all frontend layouts - it automatically loads user data from localStorage on app mount.

3. **Use the hooks** in any component:

```tsx
import { useUser, useHasRole } from '@shared/hooks/use-user';

function MyComponent() {
  const user = useUser();
  const isAdmin = useHasRole('LOGISTICS_MANAGER');

  if (!user) return <div>Please log in</div>;

  return (
    <div>
      <h1>Welcome, {user.firstName || user.username}!</h1>
      <p>Email: {user.email}</p>
      {isAdmin && <AdminPanel />}
    </div>
  );
}
```

### Available Hooks

| Hook | Returns | Description |
|------|---------|-------------|
| `useUser()` | `UserData \| null` | Get current user data |
| `useUserRoles()` | `string[]` | Get user's roles array |
| `useHasRole(role)` | `boolean` | Check if user has specific role |
| `useIsAuthenticated()` | `boolean` | Check if user is logged in |
| `useUserLoading()` | `boolean` | Check if user data is loading |
| `useUserActions()` | `{ setUser, clearUser, loadUser }` | Get store actions |

### User Data Structure

```typescript
interface UserData {
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
}
```

### Common Use Cases

#### 1. Display User Information
```tsx
const user = useUser();
return <div>Welcome, {user?.firstName}!</div>;
```

#### 2. Role-Based Access Control
```tsx
const isAdmin = useHasRole('LOGISTICS_MANAGER');
if (!isAdmin) return <AccessDenied />;
```

#### 3. Logout
```tsx
const { clearUser } = useUserActions();
const handleLogout = () => {
  clearUser(); // Clears from store, localStorage, and cookies
  window.location.href = '/';
};
```

#### 4. Check Authentication
```tsx
const isAuthenticated = useIsAuthenticated();
if (!isAuthenticated) return <LoginPrompt />;
```

### Features

✓ **Reactive** - UI updates automatically when user data changes
✓ **Cross-tab sync** - Login/logout in one tab affects all tabs
✓ **Type-safe** - Full TypeScript support
✓ **Lightweight** - Zustand is only ~1kb
✓ **Compatible** - Works with existing token-based auth
✓ **No prop drilling** - Access user data anywhere in component tree

### Import Path

All imports from the shared library use the `@shared/*` alias:

```tsx
import { useUser } from '@shared/hooks/use-user';
import { UserStoreInitializer } from '@shared/components/UserStoreInitializer';
import { useUserStore } from '@shared/lib/user-store';
```

This alias is configured in each frontend's `tsconfig.json` and `next.config.ts`.

### Example Components

See `components/UserDisplay.example.tsx` for complete usage examples including:
- User welcome messages
- Role-based rendering
- Authentication checks
- Logout buttons
- Navigation menus

### Files

- `lib/user-store.ts` - Zustand store definition
- `lib/auth.ts` - Auth utility functions (get/set tokens)
- `hooks/use-user.ts` - Convenience React hooks
- `components/UserStoreInitializer.tsx` - Auto-initialization component
- `components/UserDisplay.example.tsx` - Usage examples

### Integration with Login

The login flow automatically populates the user store:

1. User logs in via Landing Page
2. `handleSuccessfulAuth()` stores user data in:
   - localStorage (for persistence)
   - Cookies (for middleware)
   - User store (for reactive state)
3. User is redirected to role-based frontend
4. UserStoreInitializer loads data from localStorage
5. Components can access user via hooks

### Troubleshooting

**User data not loading?**
- Check that UserStoreInitializer is in your layout
- Verify localStorage has 'user' key with valid JSON
- Check browser console for errors

**Hooks returning null?**
- User might not be logged in
- Check that login flow calls `setUser()`
- Verify UserStoreInitializer is mounted

**Cross-tab sync not working?**
- Ensure UserStoreInitializer is in layout (it sets up listeners)
- Check that localStorage events are not blocked

### Technical Details

- Store uses Zustand with vanilla store pattern
- Cross-tab sync via `storage` event listener
- SSR-safe (checks for `window` before accessing storage)
- Auto-cleanup of event listeners on unmount
