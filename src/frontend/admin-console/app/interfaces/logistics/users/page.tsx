"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Users, CheckCircle2, XCircle, Shield, AlertCircle } from "lucide-react"
import { UserSearchBar } from "@/components/logistics/user-search-bar"
import { UserTable } from "@/components/logistics/user-table"
import { UserFormDialog } from "@/components/logistics/user-form-dialog"
import { userService } from "@/lib/services/user.service"
import type { User, CreateUserRequest, UpdateUserRequest } from "@/types/common"

export default function UsersPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<"create" | "edit">("create")
  const [selectedUser, setSelectedUser] = useState<User | null>(null)

  // Load users on mount
  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await userService.getAllUsers()
      setUsers(data)
    } catch (error) {
      console.error("Failed to load users:", error)
      setError("Failed to load users. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  const showSuccess = (message: string) => {
    setSuccessMessage(message)
    setTimeout(() => setSuccessMessage(null), 3000)
  }

  const showError = (message: string) => {
    setError(message)
    setTimeout(() => setError(null), 5000)
  }

  const handleCreateUser = () => {
    setDialogMode("create")
    setSelectedUser(null)
    setDialogOpen(true)
  }

  const handleEditUser = (user: User) => {
    setDialogMode("edit")
    setSelectedUser(user)
    setDialogOpen(true)
  }

  const handleDeleteUser = async (userId: string) => {
    if (!confirm("Are you sure you want to delete this user?")) {
      return
    }

    try {
      await userService.deleteUser(userId)
      showSuccess("User deleted successfully.")
      loadUsers()
    } catch (error) {
      console.error("Failed to delete user:", error)
      showError("Failed to delete user. Please try again.")
    }
  }

  const handleResetPassword = async (userId: string) => {
    const newPassword = prompt("Enter new password for this user:")
    if (!newPassword) {
      return
    }

    try {
      await userService.updatePassword(userId, { password: newPassword })
      showSuccess("Password updated successfully.")
    } catch (error) {
      console.error("Failed to update password:", error)
      showError("Failed to update password. Please try again.")
    }
  }

  const handleFormSubmit = async (data: CreateUserRequest | UpdateUserRequest) => {
    try {
      console.log("Form submission data:", data)
      if (dialogMode === "create") {
        console.log("Creating user with data:", data)
        await userService.createUser(data as CreateUserRequest)
        showSuccess("User created successfully.")
      } else if (selectedUser) {
        console.log("Updating user", selectedUser.id, "with data:", data)
        await userService.updateUser(selectedUser.id, data as UpdateUserRequest)
        showSuccess("User updated successfully.")
      }
      loadUsers()
    } catch (error) {
      console.error("Failed to save user:", error)
      throw error
    }
  }

  const filteredUsers = users.filter(
    (user) =>
      user.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.lastName?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-gray-500">Loading users...</p>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">User Management</h1>
        <Button onClick={handleCreateUser}>
          <Users className="mr-2 h-4 w-4" />
          Add New User
        </Button>
      </div>

      {/* Success Message */}
      {successMessage && (
        <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg flex items-center gap-2">
          <CheckCircle2 className="h-5 w-5" />
          <span>{successMessage}</span>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg flex items-center gap-2">
          <AlertCircle className="h-5 w-5" />
          <span>{error}</span>
        </div>
      )}

      {/* Search Bar */}
      <UserSearchBar
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
      />

      {/* Users Table */}
      <UserTable
        users={filteredUsers}
        onEditUser={handleEditUser}
        onDeleteUser={handleDeleteUser}
        onResetPassword={handleResetPassword}
      />

      {/* User Form Dialog */}
      <UserFormDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        onSubmit={handleFormSubmit}
        user={selectedUser}
        mode={dialogMode}
      />
    </div>
  )
}
