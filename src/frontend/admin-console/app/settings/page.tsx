"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  User,
  Save,
  CheckCircle2,
  LogOut,
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
  })

  useEffect(() => {
    if (user) {
      setSettings(prev => ({
        ...prev,
        fullName: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username,
        email: user.email,
        role: user.roles?.[0] || "User",
      }))
    }
  }, [user])

  const handleSave = async () => {
    setLoading(true)
    setError(null)

    try {
      // Get access token
      const accessToken = localStorage.getItem('access_token') || document.cookie
        .split('; ')
        .find(row => row.startsWith('access_token='))
        ?.split('=')[1]

      if (!accessToken) {
        throw new Error('Not authenticated. Please log in again.')
      }

      // First, get current user info to retrieve user ID
      const meResponse = await fetch('/api/users/me', {
        credentials: 'include',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      })

      if (!meResponse.ok) {
        throw new Error('Failed to fetch user information')
      }

      const meData = await meResponse.json()
      const username = meData.username

      if (!username) {
        throw new Error("Username not found")
      }

      // Search for user by username to get the actual Keycloak UUID
      const searchResponse = await fetch(`/api/users/search?query=${encodeURIComponent(username)}`, {
        credentials: 'include',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      })

      if (!searchResponse.ok) {
        throw new Error('Failed to search for user')
      }

      const searchResults = await searchResponse.json()
      if (!searchResults || searchResults.length === 0) {
        throw new Error('User not found')
      }

      const userId = searchResults[0].id

      if (!userId) {
        throw new Error("User ID not found")
      }

      // Split full name into first and last name
      const nameParts = settings.fullName.trim().split(' ')
      const firstName = nameParts[0] || ''
      const lastName = nameParts.slice(1).join(' ') || ''

      const response = await fetch(`/api/users/${userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${accessToken}`,
        },
        credentials: 'include',
        body: JSON.stringify({
          email: settings.email,
          firstName: firstName,
          lastName: lastName,
        }),
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}))
        throw new Error(errorData.error || 'Failed to update profile')
      }

      // Update user data in localStorage
      if (user) {
        const updatedUser = {
          ...user,
          firstName: firstName,
          lastName: lastName,
          email: settings.email,
        }
        localStorage.setItem('user', JSON.stringify(updatedUser))
      }

      setSaved(true)
      setTimeout(() => setSaved(false), 3000)

      // Reload the page to fetch updated user data
      setTimeout(() => {
        window.location.reload()
      }, 1500)
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
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-black">Settings</h1>
          <p className="text-gray-600 mt-1">Manage your profile, notifications, and preferences</p>
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
