export type UserRole =
    | 'logistics_manager'
    | 'warehouse_staff'
    | 'customer_service'
    | 'driver'
    | 'customer';

export interface User {
    id: string;
    email: string;
    name: string;
    role: UserRole;
}

export interface SignUpData {
    name: string;
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
    role: UserRole;
}

export interface LoginData {
    username: string;
    password: string;
}

export const ROLE_LABELS: Record<UserRole, string> = {
    logistics_manager: 'Logistics Manager',
    warehouse_staff: 'Warehouse Staff',
    customer_service: 'Customer Service',
    driver: 'Driver/Carrier',
    customer: 'Customer'
};

export const ROLE_DESCRIPTIONS: Record<UserRole, string> = {
    logistics_manager: 'Monitor KPIs, manage carriers, and optimize shipping strategies',
    warehouse_staff: 'Process orders, generate labels, and manage shipments',
    customer_service: 'Track orders, resolve issues, and support customers',
    driver: 'Manage delivery routes and update delivery status',
    customer: 'Track packages and manage delivery preferences'
};