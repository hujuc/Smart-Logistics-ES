"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { X } from "lucide-react"
import { User, CreateUserRequest, UpdateUserRequest } from "@/types/common"

interface UserFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (data: CreateUserRequest | UpdateUserRequest) => Promise<void>
  user?: User | null
  mode: "create" | "edit"
}

export function UserFormDialog({
  open,
  onOpenChange,
  onSubmit,
  user,
  mode,
}: UserFormDialogProps) {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    role: "",
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (mode === "edit" && user) {
      // Extract role from groups
      const role = user.groups && user.groups.length > 0
        ? getRoleFromGroup(user.groups[0])
        : ""

      setFormData({
        username: user.username || "",
        email: user.email || "",
        password: "",
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        role,
      })
    } else if (mode === "create") {
      setFormData({
        username: "",
        email: "",
        password: "",
        firstName: "",
        lastName: "",
        role: "DRIVER",
      })
    }
    setError(null)
  }, [user, mode, open])

  const getRoleFromGroup = (groupName: string): string => {
    if (!groupName) return ""
    if (groupName.includes("Logistics")) return "LOGISTICS_MANAGER"
    if (groupName.includes("Warehouse")) return "WAREHOUSE_STAFF"
    if (groupName.includes("Customer Service")) return "CUSTOMER_SERVICE_REP"
    if (groupName.includes("Drivers")) return "DRIVER"
    if (groupName.includes("Customers")) return "CUSTOMER"
    return ""
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    // Validate role is selected
    if (!formData.role) {
      setError("Please select a role for the user")
      return
    }

    setIsSubmitting(true)

    try {
      if (mode === "create") {
        const createData: CreateUserRequest = {
          username: formData.username,
          email: formData.email,
          password: formData.password,
          firstName: formData.firstName,
          lastName: formData.lastName,
          roles: [formData.role],
        }
        await onSubmit(createData)
      } else {
        const updateData: UpdateUserRequest = {
          username: formData.username,
          email: formData.email,
          firstName: formData.firstName,
          lastName: formData.lastName,
          roles: [formData.role],
        }
        await onSubmit(updateData)
      }
      onOpenChange(false)
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred")
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black bg-opacity-50"
        onClick={() => onOpenChange(false)}
      />

      {/* Dialog */}
      <div className="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <div>
            <h2 className="text-xl font-semibold text-black">
              {mode === "create" ? "Create New User" : "Edit User"}
            </h2>
            <p className="text-sm text-gray-500 mt-1">
              {mode === "create"
                ? "Add a new user to the system. All fields are required."
                : "Update user information."}
            </p>
          </div>
          <button
            onClick={() => onOpenChange(false)}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              value={formData.username}
              onChange={(e) =>
                setFormData({ ...formData, username: e.target.value })
              }
              required
              disabled={mode === "edit"}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) =>
                setFormData({ ...formData, email: e.target.value })
              }
              required
            />
          </div>

          {mode === "create" && (
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                value={formData.password}
                onChange={(e) =>
                  setFormData({ ...formData, password: e.target.value })
                }
                required
              />
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="firstName">First Name</Label>
            <Input
              id="firstName"
              value={formData.firstName}
              onChange={(e) =>
                setFormData({ ...formData, firstName: e.target.value })
              }
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="lastName">Last Name</Label>
            <Input
              id="lastName"
              value={formData.lastName}
              onChange={(e) =>
                setFormData({ ...formData, lastName: e.target.value })
              }
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="role">Role *</Label>
            <select
              id="role"
              value={formData.role}
              onChange={(e) =>
                setFormData({ ...formData, role: e.target.value })
              }
              className="flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm"
              required
            >
              <option value="">-- Select a role --</option>
              <option value="LOGISTICS_MANAGER">Logistics Manager</option>
              <option value="WAREHOUSE_STAFF">Warehouse Staff</option>
              <option value="CUSTOMER_SERVICE_REP">Customer Service Representative</option>
              <option value="DRIVER">Driver</option>
              <option value="CUSTOMER">Customer</option>
            </select>
          </div>

          {error && (
            <div className="text-sm text-red-600 bg-red-50 p-3 rounded">
              {error}
            </div>
          )}

          {/* Footer */}
          <div className="flex gap-3 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting} className="flex-1">
              {isSubmitting
                ? "Saving..."
                : mode === "create"
                ? "Create User"
                : "Update User"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
