"use client";

import { useState, useEffect, useRef } from 'react';
import { Package, Truck, MessageCircle, X, Send, Loader2, Plus, Clock, Trash2 } from 'lucide-react';

// Conversation type
interface Conversation {
  id: number;
  title: string;
  messages: Message[];
  createdAt: string;
  lastMessageAt: string;
}

interface Message {
  id: number;
  type: 'text' | 'tracking' | 'options';
  content: string;
  isBot: boolean;
  timestamp: string;
  tracking?: string;
  status?: string;
  options?: string[];
  onOptionClick?: (option: string) => void;
}

// ChatMessage Component
function ChatMessage({ message, isBot }: { message: Message; isBot: boolean }) {
  const renderContent = () => {
    if (message.type === 'text') {
      return <p className="text-sm leading-relaxed whitespace-pre-line">{message.content}</p>;
    }

    if (message.type === 'tracking') {
      return (
        <div className="space-y-2">
          <p className="text-sm font-medium">{message.content}</p>
          <div className="bg-white rounded-lg p-3 border border-gray-200">
            <div className="flex items-center gap-2 mb-2">
              <Package className="h-4 w-4 text-blue-600" />
              <span className="text-xs font-medium text-gray-600">Tracking: {message.tracking}</span>
            </div>
            <div className="flex items-center gap-2">
              <Truck className="h-4 w-4 text-green-600" />
              <span className="text-xs text-gray-600">{message.status}</span>
            </div>
          </div>
        </div>
      );
    }

    if (message.type === 'options') {
      return (
        <div className="space-y-2">
          <p className="text-sm">{message.content}</p>
          <div className="flex flex-wrap gap-2">
            {message.options?.map((option: string, idx: number) => (
              <button
                key={idx}
                onClick={() => message.onOptionClick?.(option)}
                className="px-3 py-1.5 bg-white border border-gray-300 rounded-lg text-xs font-medium hover:bg-gray-50 transition-colors"
              >
                {option}
              </button>
            ))}
          </div>
        </div>
      );
    }

    return null;
  };

  return (
    <div className={`flex ${isBot ? 'justify-start' : 'justify-end'} mb-4`}>
      <div className={`max-w-[80%] rounded-lg px-4 py-2 ${
        isBot
          ? 'bg-gray-100 text-gray-900'
          : 'bg-black text-white'
      }`}>
        {renderContent()}
        <span className="text-xs opacity-60 mt-1 block">
          {message.timestamp}
        </span>
      </div>
    </div>
  );
}

// ChatInput Component
function ChatInput({
  onSendMessage,
  disabled,
  isTyping
}: {
  onSendMessage: (text: string) => void;
  disabled: boolean;
  isTyping: boolean;
}) {
  const [input, setInput] = useState('');

  const handleSubmit = () => {
    if (input.trim() && !disabled) {
      onSendMessage(input.trim());
      setInput('');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <div className="border-t border-gray-200 p-4 bg-white">
      {isTyping && (
        <div className="flex items-center gap-2 mb-2 text-xs text-gray-500">
          <Loader2 className="h-3 w-3 animate-spin" />
          <span>Bot is typing...</span>
        </div>
      )}
      <div className="flex gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Type your message..."
          disabled={disabled}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent disabled:bg-gray-50 disabled:cursor-not-allowed text-sm"
        />
        <button
          onClick={handleSubmit}
          disabled={disabled || !input.trim()}
          className="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <Send className="h-4 w-4" />
        </button>
      </div>
    </div>
  );
}

// Main ChatBot Component
export default function ChatBot() {
  const STORAGE_KEY = 'slms_chatbot_state';

  const getInitialMessages = (): Message[] => [
    {
      id: 1,
      type: 'text',
      content: 'Welcome to Smart Logistics Management System! 👋 I\'m here to help you manage your shipments efficiently.',
      isBot: true,
      timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
    },
    {
      id: 2,
      type: 'options',
      content: 'What would you like to do?',
      options: ['Track my order', 'Learn about SLMS', 'Report an issue', 'Create account'],
      isBot: true,
      timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
    }
  ];

  // Load state from localStorage
  const loadState = () => {
    if (typeof window === 'undefined') return null;

    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        // Convert date strings back to Date objects
        return {
          ...parsed,
          conversations: parsed.conversations.map((conv: any) => ({
            ...conv,
            createdAt: conv.createdAt,
            lastMessageAt: conv.lastMessageAt
          }))
        };
      }
    } catch (error) {
      console.error('Error loading chatbot state:', error);
    }
    return null;
  };

  const [isOpen, setIsOpen] = useState(false);
  const [hasNewMessage, setHasNewMessage] = useState(false);
  const [conversations, setConversations] = useState<Conversation[]>([
    {
      id: 1,
      title: 'New Conversation',
      messages: getInitialMessages(),
      createdAt: new Date().toISOString(),
      lastMessageAt: new Date().toISOString()
    }
  ]);
  const [currentConversationId, setCurrentConversationId] = useState<number>(1);
  const [showHistory, setShowHistory] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load state from localStorage after component mounts
  useEffect(() => {
    const savedState = loadState();
    if (savedState) {
      setIsOpen(savedState.isOpen);
      setConversations(savedState.conversations);
      setCurrentConversationId(savedState.currentConversationId);
    }
  }, []);

  const currentConversation = conversations.find(c => c.id === currentConversationId);
  const messages = currentConversation?.messages || [];

  // Save state to localStorage
  useEffect(() => {
    if (typeof window === 'undefined') return;

    const state = {
      isOpen,
      conversations,
      currentConversationId
    };

    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch (error) {
      console.error('Error saving chatbot state:', error);
    }
  }, [isOpen, conversations, currentConversationId]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // New message notification
  useEffect(() => {
    const timer = setTimeout(() => {
      if (!isOpen) {
        setHasNewMessage(true);
      }
    }, 5000);

    return () => clearTimeout(timer);
  }, [isOpen]);

  const handleToggle = () => {
    setIsOpen(!isOpen);
    if (!isOpen) {
      setHasNewMessage(false);
    }
  };

  const simulateBotResponse = (userMessage: string) => {
    setIsTyping(true);

    setTimeout(() => {
      const lowerMessage = userMessage.toLowerCase();
      let botResponse: Message;

      if (lowerMessage.includes('track') || lowerMessage.includes('order')) {
        botResponse = {
          id: Date.now(),
          type: 'tracking',
          content: 'I found your shipment! Here\'s the current status:',
          tracking: 'SLMS-2025-789456',
          status: 'Out for Delivery - Expected today by 5:00 PM',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('learn') || lowerMessage.includes('about') || lowerMessage.includes('slms') || lowerMessage.includes('features')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'SLMS is a comprehensive logistics platform offering:\n\n✓ Real-time shipment tracking\n✓ Multi-carrier integration (FedEx, UPS, DHL)\n✓ Route optimization for drivers\n✓ Automated notifications\n✓ Performance analytics & reporting\n✓ Digital proof of delivery\n\nWould you like to create an account and experience it yourself?',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('report') || lowerMessage.includes('issue') || lowerMessage.includes('problem')) {
        botResponse = {
          id: Date.now(),
          type: 'options',
          content: 'I\'m sorry to hear you\'re experiencing an issue. What kind of problem are you reporting?',
          options: ['Wrong delivery address', 'Damaged package', 'Delivery delay', 'Package not received'],
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('wrong') || lowerMessage.includes('address')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'I understand you need to update the delivery address. Our customer service team can help you with that. Please log in to your account or contact us at support@slms.com with your tracking number, and we\'ll update the address immediately.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('damaged') || lowerMessage.includes('damage')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'I\'m sorry your package arrived damaged. To file a claim:\n\n1. Take photos of the damaged package and contents\n2. Keep all packaging materials\n3. Log in to your account and report the issue\n4. Our team will review and process your claim within 24-48 hours\n\nNeed immediate assistance? Call us at +1-800-SLMS-HELP',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('delay') || lowerMessage.includes('late')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'I apologize for the delay. Delivery delays can occur due to weather, traffic, or carrier issues. You can track your shipment in real-time through our customer portal. If you\'d like, I can help you check the current status - just provide your tracking number.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('not received') || lowerMessage.includes('missing')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'If your package shows as delivered but you haven\'t received it:\n\n1. Check with neighbors or building reception\n2. Look around your delivery location\n3. Verify the delivery address in your account\n4. Contact us within 48 hours at support@slms.com\n\nWe\'ll investigate with the carrier and our driver records including proof of delivery.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('create') || lowerMessage.includes('account') || lowerMessage.includes('sign up') || lowerMessage.includes('register')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'Great! Creating an account gives you access to:\n\n• Track all your orders in one place\n• Receive automatic delivery notifications\n• View detailed shipment history\n• Report issues directly through the portal\n• Manage delivery preferences\n\nClick the "Sign Up" button at the top of the page to get started. It only takes a minute!',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('carrier') || lowerMessage.includes('fedex') || lowerMessage.includes('ups') || lowerMessage.includes('dhl')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'SLMS integrates with major carriers including FedEx, UPS, and DHL. Our system automatically compares shipping rates and transit times to help you choose the most cost-effective option. Logistics managers can view carrier performance metrics in the dashboard.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('driver') || lowerMessage.includes('delivery') || lowerMessage.includes('route')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'Our drivers use the SLMS mobile app to access optimized delivery routes, reducing fuel consumption and delivery times. They can update delivery status in real-time and capture digital proof of delivery (signatures or photos) directly in the app.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('warehouse') || lowerMessage.includes('staff') || lowerMessage.includes('shipping')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'Warehouse staff can use SLMS to register new orders, generate shipping labels and packing slips, and confirm carrier pickups. The system streamlines the entire fulfillment process from order receipt to dispatch.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('notification') || lowerMessage.includes('alert') || lowerMessage.includes('update')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'SLMS sends automatic notifications via email or SMS for key events:\n\n• Order shipped\n• Out for delivery\n• Successfully delivered\n• Delivery exceptions\n• Carrier pickup confirmed\n\nYou can customize your notification preferences in your account settings.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('support') || lowerMessage.includes('contact') || lowerMessage.includes('help')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'Our customer support team is available 24/7 to assist you:\n\n📧 Email: support@slms.com\n📞 Phone: +1-800-SLMS-HELP\n💬 Live Chat: Available through your customer portal\n\nFor the fastest response, please have your tracking number ready when you contact us.',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else if (lowerMessage.includes('kpi') || lowerMessage.includes('analytics') || lowerMessage.includes('report')) {
        botResponse = {
          id: Date.now(),
          type: 'text',
          content: 'SLMS provides comprehensive analytics for logistics managers:\n\n📊 On-time delivery rates\n💰 Shipping costs per order\n🚚 Carrier performance comparison\n📈 Historical trends and KPIs\n📑 Exportable reports (PDF/Excel)\n\nLog in to access the full analytics dashboard!',
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      } else {
        botResponse = {
          id: Date.now(),
          type: 'options',
          content: 'I\'m here to help! Here are some common topics:',
          options: ['Track shipment', 'Report issue', 'Carrier information', 'Create account'],
          isBot: true,
          timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
        };
      }

      setConversations(prev => prev.map(conv =>
        conv.id === currentConversationId
          ? { ...conv, messages: [...conv.messages, botResponse], lastMessageAt: new Date().toISOString() }
          : conv
      ));
      setIsTyping(false);
    }, 1500);
  };

  const handleSendMessage = (text: string) => {
    const userMessage: Message = {
      id: Date.now(),
      type: 'text',
      content: text,
      isBot: false,
      timestamp: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
    };

    setConversations(prev => prev.map(conv =>
      conv.id === currentConversationId
        ? {
          ...conv,
          messages: [...conv.messages, userMessage],
          lastMessageAt: new Date().toISOString(),
          title: conv.messages.length === 2 ? text.substring(0, 30) + (text.length > 30 ? '...' : '') : conv.title
        }
        : conv
    ));
    simulateBotResponse(text);
  };

  const createNewConversation = () => {
    const newConv: Conversation = {
      id: Date.now(),
      title: 'New Conversation',
      messages: getInitialMessages(),
      createdAt: new Date().toISOString(),
      lastMessageAt: new Date().toISOString()
    };
    setConversations(prev => [newConv, ...prev]);
    setCurrentConversationId(newConv.id);
    setShowHistory(false);
  };

  const deleteConversation = (id: number) => {
    if (conversations.length === 1) {
      const newId = Date.now();
      setConversations([{
        id: newId,
        title: 'New Conversation',
        messages: getInitialMessages(),
        createdAt: new Date().toISOString(),
        lastMessageAt: new Date().toISOString()
      }]);
      setCurrentConversationId(newId);
    } else {
      setConversations(prev => prev.filter(c => c.id !== id));
      if (currentConversationId === id) {
        const remaining = conversations.filter(c => c.id !== id);
        setCurrentConversationId(remaining[0].id);
      }
    }
  };

  const formatConversationTime = (dateStr: string) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
  };

  // Update option click handlers when conversations change
  useEffect(() => {
    setConversations(prev => prev.map(conv => ({
      ...conv,
      messages: conv.messages.map(msg =>
        msg.type === 'options'
          ? { ...msg, onOptionClick: (option: string) => handleSendMessage(option) }
          : msg
      )
    })));
  }, [currentConversationId]);

  return (
    <>
      {/* Chat Panel */}
      {isOpen && (
        <div className="fixed bottom-21 right-6 z-50">
          {/* Sidebar - Conversation History */}
          {showHistory && (
            <div className="absolute bottom-0 right-96 w-64 h-[600px] border-r border-gray-200 flex flex-col bg-gray-50 rounded-tl-2xl rounded-bl-2xl border-t border-l border-b">
              <div className="p-4 border-b border-gray-200">
                <button
                  onClick={createNewConversation}
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800 transition-colors text-sm font-medium"
                >
                  <Plus className="h-4 w-4" />
                  New Chat
                </button>
              </div>

              <div className="flex-1 overflow-y-auto p-2">
                <div className="text-xs font-semibold text-gray-500 px-2 py-1 mb-1">
                  Chat History
                </div>
                {conversations.map((conv) => (
                  <div
                    key={conv.id}
                    className={`group relative p-3 rounded-lg mb-1 cursor-pointer transition-colors ${
                      conv.id === currentConversationId
                        ? 'bg-white border border-gray-300'
                        : 'hover:bg-gray-100'
                    }`}
                    onClick={() => {
                      setCurrentConversationId(conv.id);
                      setShowHistory(false);
                    }}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1 min-w-0">
                        <div className="text-sm font-medium text-gray-900 truncate">
                          {conv.title}
                        </div>
                        <div className="flex items-center gap-1 text-xs text-gray-500 mt-1">
                          <Clock className="h-3 w-3" />
                          {formatConversationTime(conv.lastMessageAt)}
                        </div>
                      </div>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          deleteConversation(conv.id);
                        }}
                        className="opacity-0 group-hover:opacity-100 p-1 hover:bg-red-50 rounded transition-opacity"
                      >
                        <Trash2 className="h-3.5 w-3.5 text-red-500" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Main Chat Area */}
          <div className="w-96 h-[600px] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col">
            <div className={`bg-black text-white px-6 py-4 flex items-center justify-between ${
              showHistory ? 'rounded-tr-2xl' : 'rounded-t-2xl'
            }`}>
              <div className="flex items-center gap-3">
                <button
                  onClick={() => setShowHistory(!showHistory)}
                  className="hover:bg-gray-800 p-2 rounded-lg transition-colors"
                  title="Conversation history"
                >
                  <Clock className="h-5 w-5" />
                </button>
                <div className="h-10 w-10 bg-white rounded-full flex items-center justify-center">
                  <Package className="h-6 w-6 text-black" />
                </div>
                <div>
                  <h3 className="font-semibold">SLMS Support</h3>
                  <div className="flex items-center gap-1">
                    <div className="h-2 w-2 bg-green-400 rounded-full"></div>
                    <span className="text-xs text-gray-300">Online</span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={createNewConversation}
                  className="hover:bg-gray-800 p-2 rounded-lg transition-colors"
                  title="New conversation"
                >
                  <Plus className="h-5 w-5" />
                </button>
                <button
                  onClick={() => setIsOpen(false)}
                  className="hover:bg-gray-800 p-2 rounded-lg transition-colors"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-4 bg-gray-50">
              {messages.map((message) => (
                <ChatMessage key={message.id} message={message} isBot={message.isBot} />
              ))}
              <div ref={messagesEndRef} />
            </div>

            <ChatInput
              onSendMessage={handleSendMessage}
              disabled={isTyping}
              isTyping={isTyping}
            />
          </div>
        </div>
      )}

      {/* Floating Button */}
      <button
        onClick={handleToggle}
        className="fixed bottom-6 right-6 h-14 w-14 bg-black text-white rounded-full shadow-2xl hover:bg-gray-800 transition-all duration-300 flex items-center justify-center z-40 hover:scale-110"
        aria-label={isOpen ? 'Close chat' : 'Open chat'}
      >
        {hasNewMessage && !isOpen && (
          <div className="absolute -top-1 -right-1 h-5 w-5 bg-red-500 rounded-full border-2 border-white flex items-center justify-center">
            <span className="text-xs font-bold">1</span>
          </div>
        )}
        {isOpen ? (
          <X className="h-6 w-6" />
        ) : (
          <MessageCircle className="h-6 w-6" />
        )}
      </button>
    </>
  );
}
