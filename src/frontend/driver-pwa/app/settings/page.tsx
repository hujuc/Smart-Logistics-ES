"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import {
  User,
  ArrowLeft,
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
        role: user.roles?.[0] || "Driver",
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
    <div className="min-h-screen bg-white pb-6">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-black">Settings</h1>
            <p className="text-gray-600 text-sm mt-1">
              {user ? `${user.firstName} ${user.lastName}` : 'Driver'} • Driver
            </p>
          </div>
          <Link href="/" className="flex items-center gap-1 text-black hover:text-gray-600">
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
