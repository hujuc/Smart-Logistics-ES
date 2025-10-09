"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { FloatingNavbar } from "@/components/floating-navbar"
import { warehouseNavItems, logisticsNavItems, customerServiceNavItems } from "@/lib/nav-config"
import {
  User,
  Bell,
  Database,
  Monitor,
  Save,
  CheckCircle2,
} from "lucide-react"
import Link from "next/link"
import { useSearchParams } from "next/navigation"

export default function SettingsPage() {
  const searchParams = useSearchParams()
  const [saved, setSaved] = useState(false)
  const [currentNavItems, setCurrentNavItems] = useState(warehouseNavItems)
  const [settings, setSettings] = useState({
    // User Settings
    fullName: "John Doe",
    email: "john.doe@slms.com",
    role: "Warehouse Manager",
    
    // Notification Settings
    emailNotifications: true,
    pushNotifications: true,
    scanAlerts: true,
    
    // Display Settings
    theme: "light",
    language: "English",
    
    // System Settings
    autoBackup: true,
    scannerSound: true,
  })

  // Determine which navigation items to use based on the referring interface
  useEffect(() => {
    const from = searchParams.get('from')
    
    switch (from) {
      case 'logistics':
        setCurrentNavItems(logisticsNavItems)
        break
      case 'customer-service':
        setCurrentNavItems(customerServiceNavItems)
        break
      case 'warehouse':
      default:
        setCurrentNavItems(warehouseNavItems)
        break
    }
  }, [searchParams])

  const handleSave = () => {
    setSaved(true)
    setTimeout(() => setSaved(false), 3000)
  }

  return (
    <div className="min-h-screen bg-white">
      <FloatingNavbar navItems={currentNavItems} />
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        <div className="space-y-6">
          <div className="flex justify-between items-center">
            <h1 className="text-3xl font-bold text-black">Settings</h1>
            <Link href={currentNavItems[0].href}>
              <Button variant="outline">Back to {currentNavItems[0].label}</Button>
            </Link>
          </div>

          {saved && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center gap-3">
              <CheckCircle2 className="h-5 w-5 text-green-600" />
              <p className="text-green-800 font-medium">Settings saved successfully!</p>
            </div>
          )}

          {/* User Profile Settings */}
          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
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
                <Label htmlFor="role">Role</Label>
                <Input
                  id="role"
                  type="text"
                  value={settings.role}
                  disabled
                  className="mt-2 bg-gray-50"
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

          {/* Notification Settings */}
          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <div className="flex items-center gap-3 mb-6">
              <div className="h-10 w-10 bg-yellow-100 rounded-lg flex items-center justify-center">
                <Bell className="h-6 w-6 text-yellow-600" />
              </div>
              <h2 className="text-xl font-semibold text-black">Notifications</h2>
            </div>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
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
              <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <p className="font-medium text-black">Push Notifications</p>
                  <p className="text-sm text-gray-600">
                    Receive push notifications in the browser
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={settings.pushNotifications}
                    onChange={(e) =>
                      setSettings({
                        ...settings,
                        pushNotifications: e.target.checked,
                      })
                    }
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
              <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <p className="font-medium text-black">QR Scan Alerts</p>
                  <p className="text-sm text-gray-600">
                    Get notified when packages are scanned
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={settings.scanAlerts}
                    onChange={(e) =>
                      setSettings({ ...settings, scanAlerts: e.target.checked })
                    }
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
            </div>
          </div>

          {/* Display Settings */}
          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
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
                  className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
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
                  className="mt-2 w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="English">English</option>
                  <option value="Spanish">Spanish</option>
                  <option value="French">French</option>
                  <option value="German">German</option>
                </select>
              </div>
            </div>
          </div>

          {/* System Settings */}
          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <div className="flex items-center gap-3 mb-6">
              <div className="h-10 w-10 bg-green-100 rounded-lg flex items-center justify-center">
                <Database className="h-6 w-6 text-green-600" />
              </div>
              <h2 className="text-xl font-semibold text-black">System</h2>
            </div>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <p className="font-medium text-black">Auto Backup</p>
                  <p className="text-sm text-gray-600">
                    Automatically backup data daily
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={settings.autoBackup}
                    onChange={(e) =>
                      setSettings({ ...settings, autoBackup: e.target.checked })
                    }
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
              <div className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                <div>
                  <p className="font-medium text-black">Scanner Sound</p>
                  <p className="text-sm text-gray-600">
                    Play sound when QR code is scanned
                  </p>
                </div>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={settings.scannerSound}
                    onChange={(e) =>
                      setSettings({
                        ...settings,
                        scannerSound: e.target.checked,
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
            <Button variant="outline">Cancel</Button>
            <Button onClick={handleSave}>
              <Save className="mr-2 h-4 w-4" />
              Save Settings
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}
