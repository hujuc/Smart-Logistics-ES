"use client"

import { useState, useEffect, Suspense } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  ArrowLeft,
  AlertCircle,
  Package,
  FileText,
  MessageSquare,
  PackageX,
  MapPin,
  Clock,
  XCircle,
  AlertTriangle
} from "lucide-react"
import { ExceptionType, Severity, Priority, Source } from "@/types/exception"
import { exceptionService } from "@/lib/services/exception.service"
import { EXCEPTION_TYPE_LABELS } from "@/types/exception"
import { apiFetch, API_ENDPOINTS } from "@/lib/api"

// Order interface for type safety
interface Order {
  id: number
  reference: string
  [key: string]: unknown
}

const categories = [
  {
    value: ExceptionType.DAMAGED,
    label: EXCEPTION_TYPE_LABELS[ExceptionType.DAMAGED],
    icon: PackageX,
    description: "Package arrived damaged or contents broken"
  },
  {
    value: ExceptionType.LOST,
    label: EXCEPTION_TYPE_LABELS[ExceptionType.LOST],
    icon: AlertTriangle,
    description: "Package never arrived or items missing from box"
  },
  {
    value: ExceptionType.WRONG_ITEM,
    label: EXCEPTION_TYPE_LABELS[ExceptionType.WRONG_ITEM],
    icon: XCircle,
    description: "Received wrong item or incorrect product"
  },
  {
    value: ExceptionType.DELAYED,
    label: EXCEPTION_TYPE_LABELS[ExceptionType.DELAYED],
    icon: Clock,
    description: "Package delayed or past estimated delivery date"
  },
  {
    value: ExceptionType.WRONG_ADDRESS,
    label: EXCEPTION_TYPE_LABELS[ExceptionType.WRONG_ADDRESS],
    icon: MapPin,
    description: "Delivered to wrong address or address issues"
  },
  {
    value: ExceptionType.NOT_DELIVERED,
    label: EXCEPTION_TYPE_LABELS[ExceptionType.NOT_DELIVERED],
    icon: Package,
    description: "Package not delivered or unable to deliver"
  },
  {
    value: ExceptionType.OTHER,
    label: EXCEPTION_TYPE_LABELS[ExceptionType.OTHER],
    icon: MessageSquare,
    description: "Other delivery issues not listed above"
  }
]

const priorities = [
  {
    value: Priority.LOW,
    label: "Low",
    description: "Non-urgent inquiry or question"
  },
  {
    value: Priority.MEDIUM,
    label: "Medium",
    description: "Important but not critical issue"
  },
  {
    value: Priority.HIGH,
    label: "High",
    description: "Urgent issue requiring immediate attention"
  },
  {
    value: Priority.CRITICAL,
    label: "Critical",
    description: "Critical issue requiring immediate attention"
  }
]

const severities = [
  {
    value: Severity.LOW,
    label: "Minor Issue",
    description: "Minimal impact on delivery"
  },
  {
    value: Severity.MEDIUM,
    label: "Moderate Issue",
    description: "Noticeable impact on delivery"
  },
  {
    value: Severity.HIGH,
    label: "Major Issue",
    description: "Significant impact on delivery"
  },
  {
    value: Severity.CRITICAL,
    label: "Critical Issue",
    description: "Severe impact, requires immediate action"
  }
]

function NewTicketForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  
  const [formData, setFormData] = useState({
    type: "" as ExceptionType | "",
    severity: Severity.MEDIUM,
    priority: Priority.MEDIUM,
    category: "",
    description: "",
    orderReference: "",
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [submitSuccess, setSubmitSuccess] = useState(false)

  // Load data from URL parameters
  useEffect(() => {
    const type = searchParams.get("type") as ExceptionType
    const priority = searchParams.get("priority") as Priority
    const severity = searchParams.get("severity") as Severity
    const category = searchParams.get("category")
    const description = searchParams.get("description")
    const orderReference = searchParams.get("orderReference")

    setFormData(prev => ({
      ...prev,
      ...(type && { type }),
      ...(priority && { priority }),
      ...(severity && { severity }),
      ...(category && { category }),
      ...(description && { description }),
      ...(orderReference && { orderReference })
    }))
  }, [searchParams])

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.type) {
      newErrors.type = "Please select an issue type"
    }
    if (!formData.category.trim()) {
      newErrors.category = "Category is required"
    }
    if (!formData.description.trim()) {
      newErrors.description = "Description is required"
    }
    if (formData.description.trim().length < 20) {
      newErrors.description = "Description must be at least 20 characters"
    }
    if (!formData.orderReference.trim()) {
      newErrors.orderReference = "Order Reference is required"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    setIsSubmitting(true)
    setSubmitError(null)

    try {
      // First, we need to get the order ID from the order reference
      // We'll fetch customer orders and find the matching one
      const orders = await apiFetch<Order[]>(API_ENDPOINTS.orders, {
        method: 'GET',
      });

      const matchingOrder = orders.find((order) => 
        order.reference === formData.orderReference ||
        order.id.toString() === formData.orderReference
      );

      if (!matchingOrder) {
        throw new Error(`Order with reference "${formData.orderReference}" not found. Please check the order number and try again.`);
      }

      // Create the exception via API
      await exceptionService.create({
        orderId: matchingOrder.id,
        type: formData.type as ExceptionType,
        category: formData.category,
        severity: formData.severity,
        description: formData.description,
        priority: formData.priority,
        source: Source.CUSTOMER, // Required: tickets from customer portal are always customer-reported
      })

      setSubmitSuccess(true)
      
      // Redirect after a short delay to show success message
      setTimeout(() => {
        router.push("/interfaces/support/tickets")
      }, 1500)
    } catch (error: unknown) {
      console.error("Failed to create ticket:", error)
      if (error instanceof Error) {
        setSubmitError(error.message)
      } else {
        setSubmitError("Failed to create ticket. Please try again.")
      }
      setIsSubmitting(false)
    }
  }

  const handleInputChange = (field: string, value: string | ExceptionType | Priority | Severity) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev }
        delete newErrors[field]
        return newErrors
      })
    }
    // Clear submit error when user makes changes
    if (submitError) {
      setSubmitError(null)
    }
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Tickets
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-black">Create New Support Ticket</h1>
          <p className="text-gray-600 mt-1">Report a delivery issue or problem with your order</p>
        </div>
      </div>

      {/* Success Message */}
      {submitSuccess && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="flex items-start gap-3">
            <AlertCircle className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="text-sm text-green-900 font-medium">
                Ticket created successfully!
              </p>
              <p className="text-sm text-green-700 mt-1">
                Redirecting to your tickets...
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Error Message */}
      {submitError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-start gap-3">
            <AlertCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="text-sm text-red-900 font-medium">
                Failed to create ticket
              </p>
              <p className="text-sm text-red-700 mt-1">
                {submitError}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Pre-filled Info Banner */}
      {formData.orderReference && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-start gap-3">
            <AlertCircle className="h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5" />
            <div>
              <p className="text-sm text-blue-900 font-medium">
                Order information loaded
              </p>
              <p className="text-sm text-blue-700 mt-1">
                We&apos;ve pre-filled this form with your order reference. Please add details about your issue.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Info Banner */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <div className="flex items-start gap-3">
          <AlertCircle className="h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5" />
          <div>
            <p className="text-sm text-blue-900 font-medium">
              Before creating a ticket
            </p>
            <p className="text-sm text-blue-700 mt-1">
              Please provide as much detail as possible to help us resolve your issue quickly. 
              All fields marked with * are required.
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Issue Type Selection */}
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <Label className="text-lg font-semibold text-black mb-4 block">
            What type of issue are you experiencing? *
          </Label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {categories.map((category) => {
              const Icon = category.icon
              return (
                <button
                  key={category.value}
                  type="button"
                  onClick={() => handleInputChange("type", category.value)}
                  className={`flex items-start gap-3 p-4 rounded-lg border-2 transition-all text-left ${
                    formData.type === category.value
                      ? "border-blue-500 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300"
                  }`}
                >
                  <Icon className={`h-5 w-5 mt-0.5 ${
                    formData.type === category.value ? "text-blue-600" : "text-gray-400"
                  }`} />
                  <div>
                    <p className={`font-medium ${
                      formData.type === category.value ? "text-blue-900" : "text-black"
                    }`}>
                      {category.label}
                    </p>
                    <p className="text-sm text-gray-600 mt-0.5">
                      {category.description}
                    </p>
                  </div>
                </button>
              )
            })}
          </div>
          {errors.type && (
            <p className="text-sm text-red-600 mt-2">{errors.type}</p>
          )}
        </div>

        {/* Severity Selection */}
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <Label className="text-lg font-semibold text-black mb-4 block">
            How severe is the issue? *
          </Label>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {severities.map((severity) => (
              <button
                key={severity.value}
                type="button"
                onClick={() => handleInputChange("severity", severity.value)}
                className={`flex flex-col items-start p-4 rounded-lg border-2 transition-all text-left ${
                  formData.severity === severity.value
                    ? severity.value === Severity.CRITICAL
                      ? "border-red-600 bg-red-50"
                      : severity.value === Severity.HIGH
                      ? "border-orange-500 bg-orange-50"
                      : severity.value === Severity.MEDIUM
                      ? "border-yellow-500 bg-yellow-50"
                      : "border-blue-500 bg-blue-50"
                    : "border-gray-200 hover:border-gray-300"
                }`}
              >
                <p className={`font-medium ${
                  formData.severity === severity.value
                    ? severity.value === Severity.CRITICAL
                      ? "text-red-900"
                      : severity.value === Severity.HIGH
                      ? "text-orange-900"
                      : severity.value === Severity.MEDIUM
                      ? "text-yellow-900"
                      : "text-blue-900"
                    : "text-black"
                }`}>
                  {severity.label}
                </p>
                <p className="text-sm text-gray-600 mt-1">
                  {severity.description}
                </p>
              </button>
            ))}
          </div>
        </div>

        {/* Priority Selection */}
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <Label className="text-lg font-semibold text-black mb-4 block">
            Priority Level *
          </Label>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            {priorities.map((priority) => (
              <button
                key={priority.value}
                type="button"
                onClick={() => handleInputChange("priority", priority.value)}
                className={`flex flex-col items-start p-4 rounded-lg border-2 transition-all text-left ${
                  formData.priority === priority.value
                    ? priority.value === Priority.CRITICAL
                      ? "border-red-600 bg-red-50"
                      : priority.value === Priority.HIGH
                      ? "border-red-500 bg-red-50"
                      : priority.value === Priority.MEDIUM
                      ? "border-yellow-500 bg-yellow-50"
                      : "border-blue-500 bg-blue-50"
                    : "border-gray-200 hover:border-gray-300"
                }`}
              >
                <p className={`font-medium ${
                  formData.priority === priority.value
                    ? priority.value === Priority.CRITICAL
                      ? "text-red-900"
                      : priority.value === Priority.HIGH
                      ? "text-red-900"
                      : priority.value === Priority.MEDIUM
                      ? "text-yellow-900"
                      : "text-blue-900"
                    : "text-black"
                }`}>
                  {priority.label}
                </p>
                <p className="text-sm text-gray-600 mt-1">
                  {priority.description}
                </p>
              </button>
            ))}
          </div>
        </div>

        {/* Ticket Details */}
        <div className="bg-white border rounded-lg p-6 shadow-sm space-y-4">
          <h2 className="text-lg font-semibold text-black">Issue Details</h2>
          
          {/* Order Reference */}
          <div>
            <Label htmlFor="orderReference">Order Reference *</Label>
            <Input
              id="orderReference"
              type="text"
              placeholder="e.g., ORD-2025-001 or 1"
              value={formData.orderReference}
              onChange={(e) => handleInputChange("orderReference", e.target.value)}
              className={`mt-2 ${errors.orderReference ? "border-red-500" : ""}`}
            />
            {errors.orderReference && (
              <p className="text-sm text-red-600 mt-1">{errors.orderReference}</p>
            )}
            <p className="text-xs text-gray-500 mt-1">
              The order reference or number related to this issue (you can find this in your order history)
            </p>
          </div>

          {/* Category */}
          <div>
            <Label htmlFor="category">Category / Short Summary *</Label>
            <Input
              id="category"
              type="text"
              placeholder="Brief category or title for this issue"
              value={formData.category}
              onChange={(e) => handleInputChange("category", e.target.value)}
              className={`mt-2 ${errors.category ? "border-red-500" : ""}`}
            />
            {errors.category && (
              <p className="text-sm text-red-600 mt-1">{errors.category}</p>
            )}
          </div>

          {/* Description */}
          <div>
            <Label htmlFor="description">Detailed Description *</Label>
            <textarea
              id="description"
              className={`mt-2 w-full min-h-[150px] rounded-md border bg-white px-3 py-2 text-sm text-black shadow-sm transition-colors placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 ${
                errors.description ? "border-red-500" : ""
              }`}
              placeholder="Please provide detailed information about your issue. Include any relevant dates, tracking numbers, photos descriptions, or other details that might help us assist you better."
              value={formData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
            />
            <div className="flex items-center justify-between mt-1">
              {errors.description ? (
                <p className="text-sm text-red-600">{errors.description}</p>
              ) : (
                <p className="text-xs text-gray-500">
                  Minimum 20 characters ({formData.description.length}/20)
                </p>
              )}
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-4">
          <Button
            type="button"
            variant="outline"
            onClick={() => router.back()}
            disabled={isSubmitting}
            className="flex-1"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            disabled={isSubmitting || submitSuccess}
            className="flex-1"
          >
            {isSubmitting ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2" />
                Creating Ticket...
              </>
            ) : submitSuccess ? (
              <>
                <AlertCircle className="mr-2 h-4 w-4" />
                Ticket Created!
              </>
            ) : (
              <>
                <FileText className="mr-2 h-4 w-4" />
                Create Ticket
              </>
            )}
          </Button>
        </div>
      </form>
    </div>
  )
}

export default function NewTicketPage() {
  return (
    <Suspense fallback={
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/3 mb-4"></div>
          <div className="h-64 bg-gray-200 rounded"></div>
        </div>
      </div>
    }>
      <NewTicketForm />
    </Suspense>
  )
}
