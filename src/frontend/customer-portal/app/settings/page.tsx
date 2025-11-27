"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  User,
  Bell,
  MapPin,
  Monitor,
  Save,
  CheckCircle2,
  Shield,
  LogOut,
} from "lucide-react"
import { useUser, useUserActions } from "@/shared/hooks/use-user"
import { clearAuth } from "@/shared/lib/auth"

export default function SettingsPage() {
  const router = useRouter()
  const user = useUser()
  const { clearUser } = useUserActions()
  const [saved, setSaved] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [settings, setSettings] = useState({
    // User Settings
    fullName: "",
    email: "",
    phone: "",

    // Delivery Preferences
    defaultAddress: "",
    deliveryInstructions: "",

    // Notification Settings
    emailNotifications: true,
    smsNotifications: true,
    deliveryAlerts: true,
    orderUpdates: true,

    // Display Settings
    theme: "light",
    language: "English",

    // Privacy Settings
    shareLocation: true,
    saveDeliveryHistory: true,
  })

  useEffect(() => {
    if (user) {
      setSettings(prev => ({
        ...prev,
        fullName: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username,
        email: user.email,
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
      router.push('/')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-black">Settings</h1>
          <p className="text-gray-600 mt-1">Manage your profile, delivery preferences, and notifications</p>
        </div>
        <Button
          variant="outline"
          onClick={handleLogout}
          disabled={loading}
          className="flex items-center gap-2"
        >
          <LogOut className="h-4 w-4" />
          Logout
        </Button>
      </div>

      {saved && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center gap-3">
          <CheckCircle2 className="h-5 w-5 text-green-600" />
          <p className="text-green-800 font-medium">Settings saved successfully!</p>
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center gap-3">
          <p className="text-red-800 font-medium">{error}</p>
        </div>
      )}

      {/* User Profile Settings */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-blue-100 rounded-lg flex items-center justify-center">
            <User className="h-6 w-6 text-blue-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">User Profile</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <Label htmlFor="fullName">Full Name</Label>
            <Input
              id="fullName"
              type="text"
              value={settings.fullName}
              onChange={(e) =>
                setSettings({ ...settings, fullName: e.target.value })
              }
              className="mt-2"
            />
          </div>
          <div>
            <Label htmlFor="email">Email Address</Label>
            <Input
              id="email"
              type="email"
              value={settings.email}
              onChange={(e) =>
                setSettings({ ...settings, email: e.target.value })
              }
              className="mt-2"
            />
          </div>
          <div>
            <Label htmlFor="phone">Phone Number</Label>
            <Input
              id="phone"
              type="tel"
              value={settings.phone}
              onChange={(e) =>
                setSettings({ ...settings, phone: e.target.value })
              }
              className="mt-2"
            />
          </div>
          <div>
            <Label htmlFor="password">Change Password</Label>
            <Input
              id="password"
              type="password"
              placeholder="••••••••"
              className="mt-2"
            />
          </div>
        </div>
      </div>

      {/* Delivery Preferences */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-orange-100 rounded-lg flex items-center justify-center">
            <MapPin className="h-6 w-6 text-orange-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">Delivery Preferences</h2>
        </div>
        <div className="space-y-4">
          <div>
            <Label htmlFor="defaultAddress">Default Delivery Address</Label>
            <Input
              id="defaultAddress"
              type="text"
              value={settings.defaultAddress}
              onChange={(e) =>
                setSettings({ ...settings, defaultAddress: e.target.value })
              }
              className="mt-2"
            />
            <p className="text-xs text-gray-500 mt-1">
              This address will be used by default for new deliveries
            </p>
          </div>
          <div>
            <Label htmlFor="deliveryInstructions">Delivery Instructions</Label>
            <textarea
              id="deliveryInstructions"
              value={settings.deliveryInstructions}
              onChange={(e) =>
                setSettings({
                  ...settings,
                  deliveryInstructions: e.target.value,
                })
              }
              className="mt-2 w-full min-h-[100px] rounded-md border bg-white px-3 py-2 text-sm text-black shadow-sm transition-colors placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950"
              placeholder="Any special instructions for delivery drivers..."
            />
            <p className="text-xs text-gray-500 mt-1">
              Help drivers find your location or specify where to leave packages
            </p>
          </div>
        </div>
      </div>

      {/* Notification Settings */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-yellow-100 rounded-lg flex items-center justify-center">
            <Bell className="h-6 w-6 text-yellow-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">Notifications</h2>
        </div>
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 border rounded-lg">
            <div>
              <p className="font-medium text-black">Email Notifications</p>
              <p className="text-sm text-gray-600">
                Receive updates and alerts via email
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={settings.emailNotifications}
                onChange={(e) =>
                  setSettings({
                    ...settings,
                    emailNotifications: e.target.checked,
                  })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
          <div className="flex items-center justify-between p-4 border rounded-lg">
            <div>
              <p className="font-medium text-black">SMS Notifications</p>
              <p className="text-sm text-gray-600">
                Receive text messages for important updates
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={settings.smsNotifications}
                onChange={(e) =>
                  setSettings({
                    ...settings,
                    smsNotifications: e.target.checked,
                  })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
          <div className="flex items-center justify-between p-4 border rounded-lg">
            <div>
              <p className="font-medium text-black">Delivery Alerts</p>
              <p className="text-sm text-gray-600">
                Get notified when packages are out for delivery
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={settings.deliveryAlerts}
                onChange={(e) =>
                  setSettings({ ...settings, deliveryAlerts: e.target.checked })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
          <div className="flex items-center justify-between p-4 border rounded-lg">
            <div>
              <p className="font-medium text-black">Order Updates</p>
              <p className="text-sm text-gray-600">
                Receive notifications about order status changes
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={settings.orderUpdates}
                onChange={(e) =>
                  setSettings({ ...settings, orderUpdates: e.target.checked })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
        </div>
      </div>

      {/* Display Settings */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-purple-100 rounded-lg flex items-center justify-center">
            <Monitor className="h-6 w-6 text-purple-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">Display</h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <Label htmlFor="theme">Theme</Label>
            <select
              id="theme"
              value={settings.theme}
              onChange={(e) =>
                setSettings({ ...settings, theme: e.target.value })
              }
              className="mt-2 w-full rounded-md border px-3 py-2 text-sm text-black bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="light">Light</option>
              <option value="dark">Dark</option>
              <option value="auto">Auto</option>
            </select>
          </div>
          <div>
            <Label htmlFor="language">Language</Label>
            <select
              id="language"
              value={settings.language}
              onChange={(e) =>
                setSettings({ ...settings, language: e.target.value })
              }
              className="mt-2 w-full rounded-md border px-3 py-2 text-sm text-black bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="English">English</option>
              <option value="Portuguese">Portuguese</option>
              <option value="Spanish">Spanish</option>
              <option value="French">French</option>
            </select>
          </div>
        </div>
      </div>

      {/* Privacy Settings */}
      <div className="bg-white border rounded-lg p-6 shadow-sm">
        <div className="flex items-center gap-3 mb-6">
          <div className="h-10 w-10 bg-green-100 rounded-lg flex items-center justify-center">
            <Shield className="h-6 w-6 text-green-600" />
          </div>
          <h2 className="text-xl font-semibold text-black">Privacy</h2>
        </div>
        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 border rounded-lg">
            <div>
              <p className="font-medium text-black">Share Location</p>
              <p className="text-sm text-gray-600">
                Allow delivery drivers to see your location for accurate deliveries
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={settings.shareLocation}
                onChange={(e) =>
                  setSettings({ ...settings, shareLocation: e.target.checked })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
          <div className="flex items-center justify-between p-4 border rounded-lg">
            <div>
              <p className="font-medium text-black">Save Delivery History</p>
              <p className="text-sm text-gray-600">
                Keep a record of your past deliveries and orders
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={settings.saveDeliveryHistory}
                onChange={(e) =>
                  setSettings({
                    ...settings,
                    saveDeliveryHistory: e.target.checked,
                  })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
        </div>
      </div>

      {/* Save Button */}
      <div className="flex justify-end gap-3">
        <Button variant="outline" disabled={loading}>Cancel</Button>
        <Button onClick={handleSave} disabled={loading}>
          <Save className="mr-2 h-4 w-4" />
          {loading ? "Saving..." : "Save Settings"}
        </Button>
      </div>
    </div>
  )
}
