/**
 * User Service - API calls for user management operations
 * Uses API_ENDPOINTS from api.ts for proper BFF endpoint configuration
 */

import { API_ENDPOINTS, apiFetch } from '../api';
import type { User, CreateUserRequest, UpdateUserRequest, PasswordUpdateRequest } from '@/types/common';

export const userService = {
  /**
   * Get all users
   */
  async getAllUsers(): Promise<User[]> {
    return apiFetch<User[]>(API_ENDPOINTS.logistics.users);
  },

  /**
   * Get user by ID
   */
  async getUserById(id: string): Promise<User> {
    return apiFetch<User>(API_ENDPOINTS.logistics.user(id));
  },

  /**
   * Create a new user
   */
  async createUser(user: CreateUserRequest): Promise<User> {
    return apiFetch<User>(API_ENDPOINTS.logistics.users, {
      method: 'POST',
      body: JSON.stringify(user),
    });
  },

  /**
   * Update an existing user
   */
  async updateUser(id: string, user: UpdateUserRequest): Promise<User> {
    return apiFetch<User>(API_ENDPOINTS.logistics.user(id), {
      method: 'PUT',
      body: JSON.stringify(user),
    });
  },

  /**
   * Update user password
   */
  async updatePassword(id: string, passwordData: PasswordUpdateRequest): Promise<{ message: string }> {
    return apiFetch<{ message: string }>(API_ENDPOINTS.logistics.userPassword(id), {
      method: 'PUT',
      body: JSON.stringify(passwordData),
    });
  },

  /**
   * Delete a user
   */
  async deleteUser(id: string): Promise<void> {
    await apiFetch<void>(API_ENDPOINTS.logistics.user(id), {
      method: 'DELETE',
    });
  },

  /**
   * Search users by query
   */
  async searchUsers(query: string): Promise<User[]> {
    return apiFetch<User[]>(API_ENDPOINTS.logistics.userSearch(query));
  },
};
