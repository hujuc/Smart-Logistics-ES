"use client";

import { useState, useEffect } from 'react';
import { Package, Truck, BarChart3, MapPin, CheckCircle, ArrowRight } from 'lucide-react';
import LoginModal from '@/components/auth/LoginModal';
import SignUpModal from '@/components/auth/SignUpModal';

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
              <div className="inline-block px-4 py-2 bg-gray-100 rounded-full text-sm font-medium text-gray-700 mb-4">
                Trusted by logistics companies worldwide
              </div>
              <h1 className="text-6xl font-bold text-black leading-tight">
                Complete Shipping & Logistics
                <br />
                <span className="text-gray-600">Management Platform</span>
              </h1>
              <p className="text-xl text-gray-600 max-w-3xl mx-auto leading-relaxed">
                Centralized software to streamline your entire shipping process from warehouse to delivery.
                Real-time tracking, route optimization, and comprehensive analytics in one powerful platform.
              </p>
              <div className="flex justify-center gap-4">
                <button
                    onClick={() => setShowSignUp(true)}
                    className="px-8 py-3 bg-black text-white rounded-lg font-semibold hover:bg-gray-800 transition-colors flex items-center gap-2 shadow-lg"
                >
                  Start Free Trial
                  <ArrowRight className="w-5 h-5" />
                </button>
                <button
                    onClick={() => setShowLogin(true)}
                    className="px-8 py-3 bg-white border-2 border-gray-300 text-black rounded-lg font-semibold hover:bg-gray-50 hover:border-gray-400 transition-colors"
                >
                  Login to Dashboard
                </button>
              </div>
              <div className="flex justify-center items-center gap-8 pt-8 text-sm text-gray-600">
                <div className="flex items-center gap-2">
                  <CheckCircle className="h-5 w-5 text-green-600" />
                  <span>No credit card required</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle className="h-5 w-5 text-green-600" />
                  <span>24/7 Support</span>
                </div>
                <div className="flex items-center gap-2">
                  <CheckCircle className="h-5 w-5 text-green-600" />
                  <span>Multi-carrier integration</span>
                </div>
              </div>
            </div>
          </section>

          {/* Features Section */}
          <section className="bg-gray-50 py-20">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="text-center mb-16">
                <h2 className="text-4xl font-bold text-black mb-4">
                  Everything you need to manage logistics
                </h2>
                <p className="text-lg text-gray-600 max-w-2xl mx-auto">
                  A single platform for managing shipments, carriers, and documentation from warehouse to customer
                </p>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                <div className="bg-white p-6 rounded-lg border border-gray-200 hover:shadow-lg transition-shadow">
                  <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
                    <Truck className="h-6 w-6 text-blue-600" />
                  </div>
                  <h3 className="text-xl font-semibold text-black mb-2">
                    Real-time Tracking
                  </h3>
                  <p className="text-gray-600 text-sm mb-4">
                    Track every shipment in real-time with live updates via carrier APIs. Customers can monitor their packages 24/7.
                  </p>
                  <ul className="text-xs text-gray-500 space-y-1">
                    <li>• Live location updates</li>
                    <li>• Estimated delivery times</li>
                    <li>• Automatic notifications</li>
                  </ul>
                </div>
                <div className="bg-white p-6 rounded-lg border border-gray-200 hover:shadow-lg transition-shadow">
                  <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center mb-4">
                    <MapPin className="h-6 w-6 text-green-600" />
                  </div>
                  <h3 className="text-xl font-semibold text-black mb-2">
                    Route Optimization
                  </h3>
                  <p className="text-gray-600 text-sm mb-4">
                    Containing algorithms that plan the most efficient delivery routes, minimizing fuel costs and delivery times.
                  </p>
                  <ul className="text-xs text-gray-500 space-y-1">
                    <li>• Automated route planning</li>
                    <li>• Reduced fuel consumption</li>
                    <li>• Faster delivery times</li>
                  </ul>
                </div>
                <div className="bg-white p-6 rounded-lg border border-gray-200 hover:shadow-lg transition-shadow">
                  <div className="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
                    <BarChart3 className="h-6 w-6 text-purple-600" />
                  </div>
                  <h3 className="text-xl font-semibold text-black mb-2">
                    Analytics & KPIs
                  </h3>
                  <p className="text-gray-600 text-sm mb-4">
                    Monitor on-time delivery rates, shipping costs, and carrier performance with exportable reports.
                  </p>
                  <ul className="text-xs text-gray-500 space-y-1">
                    <li>• Performance dashboards</li>
                    <li>• Cost analysis</li>
                    <li>• PDF/Excel exports</li>
                  </ul>
                </div>
                <div className="bg-white p-6 rounded-lg border border-gray-200 hover:shadow-lg transition-shadow">
                  <div className="h-12 w-12 bg-orange-100 rounded-lg flex items-center justify-center mb-4">
                    <CheckCircle className="h-6 w-6 text-orange-600" />
                  </div>
                  <h3 className="text-xl font-semibold text-black mb-2">
                    Digital POD
                  </h3>
                  <p className="text-gray-600 text-sm mb-4">
                    Drivers capture digital proof of delivery with signatures and photos directly in the mobile app.
                  </p>
                  <ul className="text-xs text-gray-500 space-y-1">
                    <li>• Digital signatures</li>
                    <li>• Photo evidence</li>
                    <li>• Instant verification</li>
                  </ul>
                </div>
              </div>
            </div>
          </section>

          {/* User Roles Section */}
          <section className="py-20 bg-white">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="text-center mb-16">
                <h2 className="text-4xl font-bold text-black mb-4">
                  Built for every stakeholder
                </h2>
                <p className="text-lg text-gray-600 max-w-2xl mx-auto">
                  Tailored interfaces and features for each role in your logistics chain
                </p>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-6">
                <div className="p-6 bg-gray-50 rounded-lg border border-gray-200 lg:col-span-2">
                  <div className="font-semibold text-lg mb-2">Logistics Manager</div>
                  <p className="text-sm text-gray-600 mb-3">
                    High-level overview, KPI monitoring, carrier comparison, and comprehensive reporting
                  </p>
                  <div className="text-xs text-gray-500">
                    Monitor operations • Optimize strategies • Generate reports
                  </div>
                </div>
                <div className="p-6 bg-gray-50 rounded-lg border border-gray-200 lg:col-span-2">
                  <div className="font-semibold text-lg mb-2">Warehouse Staff</div>
                  <p className="text-sm text-gray-600 mb-3">
                    Process orders, generate labels & documents, confirm carrier pickups
                  </p>
                  <div className="text-xs text-gray-500">
                    Register orders • Print labels • Track pickups
                  </div>
                </div>
                <div className="p-6 bg-gray-50 rounded-lg border border-gray-200 lg:col-span-2">
                  <div className="font-semibold text-lg mb-2">Customer Service</div>
                  <p className="text-sm text-gray-600 mb-3">
                    Track order status, handle delivery exceptions, manage customer issues
                  </p>
                  <div className="text-xs text-gray-500">
                    Search orders • Resolve issues • Update customers
                  </div>
                </div>
                <div className="p-6 bg-gray-50 rounded-lg border border-gray-200 lg:col-span-3">
                  <div className="font-semibold text-lg mb-2">Drivers & Carriers</div>
                  <p className="text-sm text-gray-600 mb-3">
                    Mobile app with delivery manifests, optimized routes, and digital POD capture. Integrated with major carriers: FedEx, UPS, DHL with automatic rate comparison
                  </p>
                  <div className="text-xs text-gray-500">
                    View routes • Compare rates • API integration • Capture signatures
                  </div>
                </div>
                <div className="p-6 bg-gray-50 rounded-lg border border-gray-200 lg:col-span-3">
                  <div className="font-semibold text-lg mb-2">Customers</div>
                  <p className="text-sm text-gray-600 mb-3">
                    Self-service portal for real-time tracking, notifications, and issue reporting
                  </p>
                  <div className="text-xs text-gray-500">
                    Track shipments • Get alerts • Report issues
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* Benefits Section */}
          <section className="bg-gray-50 py-20">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
                <div>
                  <h2 className="text-4xl font-bold text-black mb-6">
                    Why choose SLMS?
                  </h2>
                  <div className="space-y-6">
                    <div className="flex gap-4">
                      <div className="flex-shrink-0 h-12 w-12 bg-black rounded-lg flex items-center justify-center">
                        <CheckCircle className="h-6 w-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-lg mb-1">Reduce Operational Costs</h3>
                        <p className="text-gray-600 text-sm">
                          Optimize routes and compare carrier rates automatically to minimize shipping expenses
                        </p>
                      </div>
                    </div>
                    <div className="flex gap-4">
                      <div className="flex-shrink-0 h-12 w-12 bg-black rounded-lg flex items-center justify-center">
                        <CheckCircle className="h-6 w-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-lg mb-1">Improve Customer Satisfaction</h3>
                        <p className="text-gray-600 text-sm">
                          Ensure timely deliveries with real-time tracking and proactive notifications
                        </p>
                      </div>
                    </div>
                    <div className="flex gap-4">
                      <div className="flex-shrink-0 h-12 w-12 bg-black rounded-lg flex items-center justify-center">
                        <CheckCircle className="h-6 w-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-lg mb-1">Complete Supply Chain Visibility</h3>
                        <p className="text-gray-600 text-sm">
                          Monitor every shipment from warehouse to final destination in a single dashboard
                        </p>
                      </div>
                    </div>
                    <div className="flex gap-4">
                      <div className="flex-shrink-0 h-12 w-12 bg-black rounded-lg flex items-center justify-center">
                        <CheckCircle className="h-6 w-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-lg mb-1">Streamlined Documentation</h3>
                        <p className="text-gray-600 text-sm">
                          Auto-generate shipping labels, packing slips, and manifests with one click
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="bg-white p-8 rounded-2xl border border-gray-200 shadow-lg">
                  <div className="text-center mb-6">
                    <div className="text-5xl font-bold text-black mb-2">10,000+</div>
                    <div className="text-gray-600">Shipments tracked daily</div>
                  </div>
                  <div className="grid grid-cols-2 gap-4 mt-8">
                    <div className="text-center p-4 bg-gray-50 rounded-lg">
                      <div className="text-3xl font-bold text-black">98%</div>
                      <div className="text-xs text-gray-600 mt-1">On-time delivery</div>
                    </div>
                    <div className="text-center p-4 bg-gray-50 rounded-lg">
                      <div className="text-3xl font-bold text-black">50+</div>
                      <div className="text-xs text-gray-600 mt-1">Carrier integrations</div>
                    </div>
                    <div className="text-center p-4 bg-gray-50 rounded-lg">
                      <div className="text-3xl font-bold text-black">24/7</div>
                      <div className="text-xs text-gray-600 mt-1">Support available</div>
                    </div>
                    <div className="text-center p-4 bg-gray-50 rounded-lg">
                      <div className="text-3xl font-bold text-black">30%</div>
                      <div className="text-xs text-gray-600 mt-1">Cost reduction</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* CTA Section */}
          <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
            <div className="bg-black text-white rounded-2xl p-12 text-center">
              <h2 className="text-4xl font-bold mb-4">
                Ready to transform your logistics operations?
              </h2>
              <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
                Join thousands of businesses using SLMS to streamline shipping, reduce costs, and improve delivery performance
              </p>
              <div className="flex justify-center gap-4">
                <button
                    onClick={() => setShowSignUp(true)}
                    className="px-8 py-3 bg-white text-black rounded-lg font-semibold hover:bg-gray-100 transition-colors inline-flex items-center gap-2"
                >
                  Start Free Trial
                  <ArrowRight className="h-5 w-5" />
                </button>
                <button
                    onClick={() => setShowLogin(true)}
                    className="px-8 py-3 bg-transparent border-2 border-white text-white rounded-lg font-semibold hover:bg-white hover:text-black transition-colors"
                >
                  View Demo
                </button>
              </div>
              <p className="text-sm text-gray-400 mt-6">
                No credit card required • Cancel anytime • 14-day free trial
              </p>
            </div>
          </section>

          {/* Footer */}
          <footer className="border-t border-gray-200 bg-gray-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
                <div className="col-span-1 md:col-span-2">
                  <div className="flex items-center gap-2 mb-4">
                    <Package className="h-8 w-8 text-black" />
                    <span className="text-2xl font-bold text-black">SLMS</span>
                  </div>
                  <p className="text-gray-600 text-sm mb-4 max-w-md">
                    Smart Logistics Management System - Complete shipping and logistics platform
                    for businesses of all sizes. From warehouse to delivery, we've got you covered.
                  </p>
                  <div className="flex gap-4">
                    <div className="text-xs text-gray-500">
                      support@slms.com
                    </div>
                    <div className="text-xs text-gray-500">
                      +1-800-SLMS-HELP
                    </div>
                  </div>
                </div>
                <div>
                  <h4 className="font-semibold text-black mb-4">Product</h4>
                  <ul className="space-y-2 text-sm text-gray-600">
                    <li><a href="#features" className="hover:text-black transition-colors">Features</a></li>
                    <li><a href="#pricing" className="hover:text-black transition-colors">Pricing</a></li>
                    <li><a href="#integrations" className="hover:text-black transition-colors">Integrations</a></li>
                    <li><a href="#mobile" className="hover:text-black transition-colors">Mobile App</a></li>
                  </ul>
                </div>
                <div>
                  <h4 className="font-semibold text-black mb-4">Company</h4>
                  <ul className="space-y-2 text-sm text-gray-600">
                    <li><a href="#about" className="hover:text-black transition-colors">About Us</a></li>
                    <li><a href="#careers" className="hover:text-black transition-colors">Careers</a></li>
                    <li><a href="#contact" className="hover:text-black transition-colors">Contact</a></li>
                    <li><a href="#blog" className="hover:text-black transition-colors">Blog</a></li>
                  </ul>
                </div>
              </div>
              <div className="border-t border-gray-200 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
                <p className="text-gray-600 text-sm">
                  © 2025 SLMS - Smart Logistics Management System. All rights reserved.
                </p>
                <div className="flex gap-6 text-sm text-gray-600">
                  <a href="#privacy" className="hover:text-black transition-colors">Privacy Policy</a>
                  <a href="#terms" className="hover:text-black transition-colors">Terms of Service</a>
                  <a href="#cookies" className="hover:text-black transition-colors">Cookie Policy</a>
                </div>
              </div>
            </div>
          </footer>

          {/* Modals */}
          {showLogin && <LoginModal onClose={() => setShowLogin(false)} onSwitchToSignUp={() => { setShowLogin(false); setShowSignUp(true); }} />}
          {showSignUp && <SignUpModal onClose={() => setShowSignUp(false)} onSwitchToLogin={() => { setShowSignUp(false); setShowLogin(true); }} />}
        </div>
      </>
  );
}