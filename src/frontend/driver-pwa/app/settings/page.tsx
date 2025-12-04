"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import {
  User,
  Bell,
  Monitor,
  ArrowLeft,
  CheckCircle2,
  LogOut,
  Package,
} from "lucide-react"
import { useUser, useUserActions } from "@/shared/hooks/use-user"
import { clearAuth } from "@/shared/lib/auth"

export default function SettingsPage() {
  const user = useUser()
  const { clearUser } = useUserActions()
  const [saved, setSaved] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [settings, setSettings] = useState({
    // User Settings
    fullName: "",
    email: "",
    role: "",

    // Notification Settings
    pushNotifications: true,
    deliveryAlerts: true,
    routeUpdates: true,

    // Display Settings
    theme: "light",
    language: "English",
  })

  useEffect(() => {
    if (user) {
      setSettings(prev => ({
        ...prev,
        fullName: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username,
        email: user.email,
        role: user.roles?.[0] || "Driver",
      }))
    }
  }, [user])

  const handleSave = async () => {
    setLoading(true)
    setError(null)

    try {
      // TODO: Implement API call to save settings
      // For now, just show success message
      setSaved(true)
      setTimeout(() => setSaved(false), 3000)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save settings")
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = async () => {
    setLoading(true)
    try {
      // Call logout endpoint
      const response = await fetch('/api/landing-page/auth/logout', {
        method: 'POST',
        credentials: 'include',
      })

      if (!response.ok) {
        console.error('Logout failed:', response.statusText)
      }
    } catch (err) {
      console.error('Error during logout:', err)
    } finally {
      // Clear client-side auth regardless of API response
      clearAuth()
      clearUser()
      // Use window.location.href to redirect to landing page (outside basePath)
      window.location.href = '/'
    }
  }

  return (
    <div className="min-h-screen bg-white pb-6">
      {/* Header */}
      <div className="bg-black text-white p-4">
        <div className="flex items-center gap-2 mb-3">
          <Package className="h-5 w-5 text-white" />
          <span className="text-base font-bold text-white">SLMS</span>
        </div>
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Settings</h1>
            <p className="text-gray-300 text-sm mt-1">
              {user ? `${user.firstName} ${user.lastName}` : 'Driver'} • Driver
            </p>
          </div>
          <Link href="/" className="flex items-center gap-1 text-white hover:text-gray-300">
            <ArrowLeft className="w-5 h-5" />
            Back
          </Link>
        </div>
      </div>

      <div className="p-4 space-y-4">
        {saved && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-green-600" />
            <p className="text-green-800 font-medium">Settings saved successfully!</p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800 font-medium">{error}</p>
          </div>
        )}

        {/* User Profile Settings */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center gap-3 mb-4">
            <div className="h-10 w-10 bg-black rounded-lg flex items-center justify-center">
              <User className="h-5 w-5 text-white" />
            </div>
            <h2 className="text-lg font-bold text-black">User Profile</h2>
          </div>
          <div className="space-y-3">
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Full Name</label>
              <input
                type="text"
                value={settings.fullName}
                onChange={(e) => setSettings({ ...settings, fullName: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Email Address</label>
              <input
                type="email"
                value={settings.email}
                onChange={(e) => setSettings({ ...settings, email: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
              />
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Role</label>
              <input
                type="text"
                value={settings.role}
                disabled
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-600"
              />
            </div>
          </div>
        </div>

        {/* Notification Settings */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center gap-3 mb-4">
            <div className="h-10 w-10 bg-black rounded-lg flex items-center justify-center">
              <Bell className="h-5 w-5 text-white" />
            </div>
            <h2 className="text-lg font-bold text-black">Notifications</h2>
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
              <div>
                <p className="font-medium text-black text-sm">Push Notifications</p>
                <p className="text-xs text-gray-600 mt-0.5">Receive push notifications in the app</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={settings.pushNotifications}
                  onChange={(e) => setSettings({ ...settings, pushNotifications: e.target.checked })}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-black"></div>
              </label>
            </div>
            <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
              <div>
                <p className="font-medium text-black text-sm">Delivery Alerts</p>
                <p className="text-xs text-gray-600 mt-0.5">Get notified about delivery updates</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={settings.deliveryAlerts}
                  onChange={(e) => setSettings({ ...settings, deliveryAlerts: e.target.checked })}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-black"></div>
              </label>
            </div>
            <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
              <div>
                <p className="font-medium text-black text-sm">Route Updates</p>
                <p className="text-xs text-gray-600 mt-0.5">Get notified when routes change</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={settings.routeUpdates}
                  onChange={(e) => setSettings({ ...settings, routeUpdates: e.target.checked })}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-black"></div>
              </label>
            </div>
          </div>
        </div>

        {/* Display Settings */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="flex items-center gap-3 mb-4">
            <div className="h-10 w-10 bg-black rounded-lg flex items-center justify-center">
              <Monitor className="h-5 w-5 text-white" />
            </div>
            <h2 className="text-lg font-bold text-black">Display</h2>
          </div>
          <div className="space-y-3">
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Theme</label>
              <select
                value={settings.theme}
                onChange={(e) => setSettings({ ...settings, theme: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
              >
                <option value="light">Light</option>
                <option value="dark">Dark</option>
                <option value="auto">Auto</option>
              </select>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-700 block mb-1">Language</label>
              <select
                value={settings.language}
                onChange={(e) => setSettings({ ...settings, language: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
              >
                <option value="English">English</option>
                <option value="Portuguese">Portuguese</option>
                <option value="Spanish">Spanish</option>
                <option value="French">French</option>
              </select>
            </div>
          </div>
        </div>

        {/* Save Button */}
        <button
          onClick={handleSave}
          disabled={loading}
          className="w-full bg-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors disabled:bg-gray-400"
        >
          {loading ? "Saving..." : "Save Settings"}
        </button>

        {/* Logout Button */}
        <button
          onClick={handleLogout}
          disabled={loading}
          className="w-full bg-white border-2 border-gray-300 text-black py-3 rounded-lg font-semibold hover:bg-gray-50 transition-colors disabled:bg-gray-100 flex items-center justify-center gap-2"
        >
          <LogOut className="h-5 w-5" />
          Logout
        </button>
      </div>
    </div>
  )
}
