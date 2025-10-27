"use client"

import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Mail, Edit, Trash2, Key } from "lucide-react"
import { User } from "@/types/common"

interface UserTableProps {
  users: User[]
  onEditUser?: (user: User) => void
  onDeleteUser?: (userId: string) => void
  onResetPassword?: (userId: string) => void
}

export function UserTable({
  users,
  onEditUser,
  onDeleteUser,
  onResetPassword,
}: UserTableProps) {
  const getRoleBadgeColor = (groups?: string[]) => {
    if (!groups || groups.length === 0) return "bg-gray-100 text-gray-700"

    const groupName = groups[0]
    if (groupName.includes("Logistics")) return "bg-blue-100 text-blue-700"
    if (groupName.includes("Warehouse")) return "bg-yellow-100 text-yellow-700"
    if (groupName.includes("Customer Service")) return "bg-green-100 text-green-700"
    if (groupName.includes("Drivers")) return "bg-purple-100 text-purple-700"
    if (groupName.includes("Customers")) return "bg-pink-100 text-pink-700"
    return "bg-gray-100 text-gray-700"
  }

  const getDisplayRole = (groups?: string[]) => {
    if (!groups || groups.length === 0) return "No Role"
    return groups.join(", ")
  }

  return (
    <div className="bg-white border rounded-lg shadow-sm">
      <div className="p-6">
        <h2 className="text-xl font-semibold text-black mb-4">
          User List ({users.length})
        </h2>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Username</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Role</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.map((user) => (
              <TableRow key={user.id}>
                <TableCell className="font-medium">{user.username}</TableCell>
                <TableCell>
                  {user.firstName} {user.lastName}
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-1 text-sm text-gray-600">
                    <Mail className="h-3 w-3" />
                    {user.email}
                  </div>
                </TableCell>
                <TableCell>
                  <span
                    className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${getRoleBadgeColor(
                      user.groups
                    )}`}
                  >
                    {getDisplayRole(user.groups)}
                  </span>
                </TableCell>
                <TableCell>
                  <span
                    className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                      user.enabled
                        ? "bg-green-100 text-green-700"
                        : "bg-red-100 text-red-700"
                    }`}
                  >
                    {user.enabled ? "Active" : "Disabled"}
                  </span>
                </TableCell>
                <TableCell>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onEditUser?.(user)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onResetPassword?.(user.id)}
                    >
                      <Key className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onDeleteUser?.(user.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {users.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No users found matching your search.
          </div>
        )}
      </div>
    </div>
  )
}
