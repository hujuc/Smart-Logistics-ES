"use client";

import { useState, useEffect } from 'react';
import { Package, Truck, BarChart3, MapPin, CheckCircle, ArrowRight } from 'lucide-react';
import LoginModal from '@/components/auth/LoginModal';
import SignUpModal from '@/components/auth/SignUpModal';
import ChatBot from '@/components/ChatBot';

export default function LandingPage() {
  const [showLogin, setShowLogin] = useState(false);
  const [showSignUp, setShowSignUp] = useState(false);

  // Lock body scroll when modal is open
  useEffect(() => {
    if (showLogin || showSignUp) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [showLogin, showSignUp]);

  return (
    <>
      <div className="min-h-screen bg-white">
        {/* Navigation */}
        <nav className="border-b border-gray-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center h-16">
              <div className="flex items-center gap-2">
                <Package className="h-8 w-8 text-black" />
                <span className="text-2xl font-bold text-black">SLMS</span>
              </div>
              <div className="flex items-center gap-4">
                <button
                  onClick={() => setShowLogin(true)}
                  className="px-4 py-2 text-black hover:text-gray-600 font-medium transition-colors"
                >
                  Login
                </button>
                <button
                  onClick={() => setShowSignUp(true)}
                  className="px-6 py-2 bg-black text-white rounded-lg font-semibold hover:bg-gray-800 transition-colors"
                >
                  Sign Up
                </button>
              </div>
            </div>
          </div>
        </nav>

        {/* Hero Section */}
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="text-center space-y-8">
            <h1 className="text-6xl font-bold text-black">
              Smart Logistics
              <br />
              Management System
            </h1>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Streamline your shipping operations with real-time tracking,
              route optimization, and comprehensive analytics
            </p>
            <div className="flex justify-center gap-4">
              <button
                onClick={() => setShowSignUp(true)}
                className="px-8 py-3 bg-black text-white rounded-lg font-semibold hover:bg-gray-800 transition-colors flex items-center gap-2"
              >
                Get Started
                <ArrowRight className="w-5 h-5" />
              </button>
              <button
                onClick={() => setShowLogin(true)}
                className="px-8 py-3 bg-white border border-gray-300 text-black rounded-lg font-semibold hover:bg-gray-50 transition-colors"
              >
                Login
              </button>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section className="bg-gray-50 py-20">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <h2 className="text-4xl font-bold text-black text-center mb-16">
              Everything you need to manage logistics
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              <div className="bg-white p-6 rounded-lg border border-gray-200">
                <Truck className="h-12 w-12 text-black mb-4" />
                <h3 className="text-xl font-semibold text-black mb-2">
                  Real-time Tracking
                </h3>
                <p className="text-gray-600">
                  Track every shipment in real-time with live updates and notifications
                </p>
              </div>
              <div className="bg-white p-6 rounded-lg border border-gray-200">
                <MapPin className="h-12 w-12 text-black mb-4" />
                <h3 className="text-xl font-semibold text-black mb-2">
                  Route Optimization
                </h3>
                <p className="text-gray-600">
                  AI-powered route planning to reduce costs and delivery times
                </p>
              </div>
              <div className="bg-white p-6 rounded-lg border border-gray-200">
                <BarChart3 className="h-12 w-12 text-black mb-4" />
                <h3 className="text-xl font-semibold text-black mb-2">
                  Analytics & Reports
                </h3>
                <p className="text-gray-600">
                  Comprehensive insights into your logistics performance
                </p>
              </div>
              <div className="bg-white p-6 rounded-lg border border-gray-200">
                <CheckCircle className="h-12 w-12 text-black mb-4" />
                <h3 className="text-xl font-semibold text-black mb-2">
                  Proof of Delivery
                </h3>
                <p className="text-gray-600">
                  Digital signatures and photos for verified deliveries
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* CTA Section */}
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="bg-black text-white rounded-2xl p-12 text-center">
            <h2 className="text-4xl font-bold mb-4">
              Ready to optimize your logistics?
            </h2>
            <p className="text-xl text-gray-300 mb-8">
              Join thousands of businesses streamlining their shipping operations
            </p>
            <button
              onClick={() => setShowSignUp(true)}
              className="px-8 py-3 bg-white text-black rounded-lg font-semibold hover:bg-gray-100 transition-colors"
            >
              Start Free Trial
            </button>
          </div>
        </section>

        {/* Footer */}
        <footer className="border-t border-gray-200 py-8">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between items-center">
              <div className="flex items-center gap-2">
                <Package className="h-6 w-6 text-black" />
                <span className="text-lg font-bold text-black">SLMS</span>
              </div>
              <p className="text-gray-600 text-sm">
                © 2025 SLMS. All rights reserved.
              </p>
            </div>
          </div>
        </footer>

        {/* Modals */}
        {showLogin && <LoginModal onClose={() => setShowLogin(false)} onSwitchToSignUp={() => { setShowLogin(false); setShowSignUp(true); }} />}
        {showSignUp && <SignUpModal onClose={() => setShowSignUp(false)} onSwitchToLogin={() => { setShowSignUp(false); setShowLogin(true); }} />}
      </div>

      {/* ChatBot Widget - Persists across navigation */}
      <ChatBot />
    </>
  );
}
