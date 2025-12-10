/**
 * AdminChatbot Component
 *
 * Dynamically selects the appropriate chatbot endpoint based on the user's role.
 * This is specific to the admin console where different roles have different BFF endpoints.
 */

'use client';

import { ChatWidget } from '@shared/components/chatbot/ChatWidget';
import { useUserStore } from '@shared/lib/user-store';

export function AdminChatbot() {
  const user = useUserStore((state) => state.user);

  // Determine the chatbot endpoint based on user's role
  const getChatEndpoint = (): string | null => {
    if (!user || !user.roles || user.roles.length === 0) {
      return null; // No chatbot if user is not authenticated or has no roles
    }

    // Check roles in priority order
    if (user.roles.includes('LOGISTICS_MANAGER')) {
      return '/api/logistics/chat';
    }
    if (user.roles.includes('WAREHOUSE_STAFF')) {
      return '/api/warehouse/chat';
    }
    if (user.roles.includes('CUSTOMER_SERVICE_REP')) {
      return '/api/customer-service/chat';
    }

    // User has a role but not one that has a chatbot endpoint
    return null;
  };

  const chatEndpoint = getChatEndpoint();

  // Only render chatbot if we have a valid endpoint
  if (!chatEndpoint) {
    return null;
  }

  return <ChatWidget apiEndpoint={chatEndpoint} />;
}
