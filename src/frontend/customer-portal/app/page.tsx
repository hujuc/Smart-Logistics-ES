import { Package } from "lucide-react";

export default function Home() {
  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <header className="border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center gap-2">
            <Package className="h-8 w-8 text-black" />
            <span className="text-2xl font-bold text-black">SLMS</span>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center space-y-6">
          <div className="flex items-center justify-center gap-3 mb-6">
            <Package className="h-16 w-16 text-black" />
            <h1 className="text-6xl font-bold text-black">SLMS</h1>
          </div>
          <h2 className="text-3xl font-semibold text-black">Customer Portal</h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Track your packages and manage your deliveries
          </p>
          <div className="mt-8 p-6 bg-gray-50 rounded-lg border border-gray-200 max-w-md mx-auto">
            <p className="text-gray-600">
              This interface is under development. Please check back soon for full functionality.
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-200 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
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
    </div>
  );
}
