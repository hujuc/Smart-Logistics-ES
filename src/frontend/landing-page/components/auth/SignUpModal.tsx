import { useState } from 'react';
import { X, Mail, Lock, User, CheckCircle } from 'lucide-react';
import type { SignUpData, UserRole } from '@/types/auth';
import { ROLE_LABELS, ROLE_DESCRIPTIONS } from '@/types/auth';

interface SignUpModalProps {
    onClose: () => void;
    onSwitchToLogin: () => void;
}

export default function SignUpModal({ onClose, onSwitchToLogin }: SignUpModalProps) {
    const [step, setStep] = useState<1 | 2>(1);
    const [formData, setFormData] = useState<SignUpData>({
        name: '',
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'customer'
    });
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const validateEmail = (email: string): boolean => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    // const validateUsername = (username: string): boolean => {
    //     // Username should be 3-20 characters, alphanumeric and underscores
    //     const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
    //     return usernameRegex.test(username);
    // };

    // const validatePassword = (password: string): boolean => {
    //     // At least 8 characters, one uppercase, one lowercase, one number
    //     return password.length >= 8 &&
    //            /[A-Z]/.test(password) &&
    //            /[a-z]/.test(password) &&
    //            /[0-9]/.test(password);
    // };

    const handleStep1Submit = (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        // Validation
        if (!formData.name || !formData.username || !formData.email || !formData.password || !formData.confirmPassword) {
            setError('Please fill in all fields');
            return;
        }

        if (!formData.name.trim().includes(' ')) {
            setError('Please enter your full name (first and last name)');
            return;
        }

        // if (!validateUsername(formData.username)) {
        //     setError('Username must be 3-20 characters and contain only letters, numbers, and underscores');
        //     return;
        // }

        if (!validateEmail(formData.email)) {
            setError('Please enter a valid email address');
            return;
        }

        // if (!validatePassword(formData.password)) {
        //     setError('Password must be at least 8 characters and include uppercase, lowercase, and a number');
        //     return;
        // }

        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setStep(2);
    };

    const handleStep2Submit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        try {
            const { register: registerUser, handleSuccessfulAuth } = await import('@/lib/api');

            // Split name into first and last name
            const nameParts = formData.name.trim().split(' ');
            const firstName = nameParts[0] || '';
            const lastName = nameParts.slice(1).join(' ') || '';

            // Map frontend role to backend role format
            const roleMap: Record<UserRole, string> = {
                'logistics_manager': 'LOGISTICS_MANAGER',
                'warehouse_staff': 'WAREHOUSE_STAFF',
                'customer_service': 'CUSTOMER_SERVICE_REP',
                'driver': 'DRIVER',
                'customer': 'CUSTOMER'
            };

            const response = await registerUser({
                username: formData.username,
                email: formData.email,
                password: formData.password,
                firstName,
                lastName,
                roles: [roleMap[formData.role]],
            });

            // Store token and redirect based on role
            handleSuccessfulAuth(response);

        } catch (err: unknown) {
            const errorMessage = err instanceof Error ? err.message : 'Registration failed. Please try again.';
            setError(errorMessage);
            setIsLoading(false);
        }
    };

    const roles: UserRole[] = ['logistics_manager', 'warehouse_staff', 'customer_service', 'driver', 'customer'];

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto" style={{ zIndex: 9999 }}>
            <div className="bg-white rounded-lg max-w-2xl w-full p-8 relative my-8" onClick={(e) => e.stopPropagation()}>
                {/* Close Button */}
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
                >
                    <X className="w-6 h-6" />
                </button>

                {/* Header */}
                <div className="mb-8">
                    <h2 className="text-3xl font-bold text-black mb-2">Create your account</h2>
                    <p className="text-gray-600">
                        {step === 1 ? 'Enter your details to get started' : 'Select your role in the system'}
                    </p>
                </div>

                {/* Progress Indicator */}
                <div className="flex items-center justify-center mb-8">
                    <div className="flex items-center gap-2">
                        <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step >= 1 ? 'bg-black text-white' : 'bg-gray-200 text-gray-600'} font-semibold`}>
                            1
                        </div>
                        <div className={`w-16 h-1 ${step >= 2 ? 'bg-black' : 'bg-gray-200'}`} />
                        <div className={`w-8 h-8 rounded-full flex items-center justify-center ${step >= 2 ? 'bg-black text-white' : 'bg-gray-200 text-gray-600'} font-semibold`}>
                            2
                        </div>
                    </div>
                </div>

                {/* Step 1: Personal Information */}
                {step === 1 && (
                    <form onSubmit={handleStep1Submit} className="space-y-6">
                        {/* Error Message */}
                        {error && (
                            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                                {error}
                            </div>
                        )}

                        {/* Name Field */}
                        <div>
                            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                                Full Name
                            </label>
                            <div className="relative">
                                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                                <input
                                    id="name"
                                    type="text"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black"
                                    placeholder="John Doe"
                                />
                            </div>
                        </div>

                        {/* Username Field */}
                        <div>
                            <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
                                Username
                            </label>
                            <div className="relative">
                                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                                <input
                                    id="username"
                                    type="text"
                                    value={formData.username}
                                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black"
                                    placeholder="your_username"
                                />
                            </div>
                            <p className="text-xs text-gray-500 mt-1">3-20 characters, letters, numbers, and underscores only</p>
                        </div>

                        {/* Email Field */}
                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                                Email
                            </label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                                <input
                                    id="email"
                                    type="email"
                                    value={formData.email}
                                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black"
                                    placeholder="your.email@example.com"
                                />
                            </div>
                        </div>

                        {/* Password Field */}
                        <div>
                            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                                Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                                <input
                                    id="password"
                                    type="password"
                                    value={formData.password}
                                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black"
                                    placeholder="••••••••"
                                />
                            </div>
                            <p className="text-xs text-gray-500 mt-1">At least 8 characters with uppercase, lowercase, and a number</p>
                        </div>

                        {/* Confirm Password Field */}
                        <div>
                            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                                Confirm Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                                <input
                                    id="confirmPassword"
                                    type="password"
                                    value={formData.confirmPassword}
                                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-black"
                                    placeholder="••••••••"
                                />
                            </div>
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            className="w-full bg-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors"
                        >
                            Continue
                        </button>

                        {/* Switch to Login */}
                        <div className="text-center text-sm text-gray-600">
                            Already have an account?{' '}
                            <button
                                type="button"
                                onClick={onSwitchToLogin}
                                className="text-black font-semibold hover:text-gray-600"
                            >
                                Login
                            </button>
                        </div>
                    </form>
                )}

                {/* Step 2: Role Selection */}
                {step === 2 && (
                    <form onSubmit={handleStep2Submit} className="space-y-6">
                        {/* Error Message */}
                        {error && (
                            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
                                {error}
                            </div>
                        )}

                        <div className="space-y-3">
                            {roles.map((role) => (
                                <label
                                    key={role}
                                    className={`block p-4 border-2 rounded-lg cursor-pointer transition-all ${
                                        formData.role === role
                                            ? 'border-black bg-gray-50'
                                            : 'border-gray-200 hover:border-gray-300'
                                    }`}
                                >
                                    <div className="flex items-start gap-3">
                                        <input
                                            type="radio"
                                            name="role"
                                            value={role}
                                            checked={formData.role === role}
                                            onChange={(e) => setFormData({ ...formData, role: e.target.value as UserRole })}
                                            className="mt-1"
                                        />
                                        <div className="flex-1">
                                            <div className="flex items-center gap-2">
                                                <span className="font-semibold text-black">{ROLE_LABELS[role]}</span>
                                                {formData.role === role && <CheckCircle className="w-5 h-5 text-black" />}
                                            </div>
                                            <p className="text-sm text-gray-600 mt-1">
                                                {ROLE_DESCRIPTIONS[role]}
                                            </p>
                                        </div>
                                    </div>
                                </label>
                            ))}
                        </div>

                        {/* Buttons */}
                        <div className="flex gap-3">
                            <button
                                type="button"
                                onClick={() => setStep(1)}
                                disabled={isLoading}
                                className="flex-1 bg-white border border-gray-300 text-black py-3 rounded-lg font-semibold hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Back
                            </button>
                            <button
                                type="submit"
                                disabled={isLoading}
                                className="flex-1 bg-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                            >
                                {isLoading ? 'Creating Account...' : 'Create Account'}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}