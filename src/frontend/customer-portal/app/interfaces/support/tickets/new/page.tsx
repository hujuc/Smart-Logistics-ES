"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  ArrowLeft,
  AlertCircle,
  Package,
  HelpCircle,
  FileText,
  CreditCard,
  MessageSquare
} from "lucide-react"

const categories = [
  {
    value: "Delivery Issue",
    label: "Delivery Issue",
    icon: Package,
    description: "Problems with package delivery or missing items"
  },
  {
    value: "Tracking",
    label: "Tracking",
    icon: Package,
    description: "Unable to track package or tracking issues"
  },
  {
    value: "Refund",
    label: "Refund Request",
    icon: CreditCard,
    description: "Request refund for damaged or incorrect items"
  },
  {
    value: "General Inquiry",
    label: "General Inquiry",
    icon: HelpCircle,
    description: "Questions about services or general support"
  },
  {
    value: "Other",
    label: "Other",
    icon: MessageSquare,
    description: "Other issues not listed above"
  }
]

const priorities = [
  {
    value: "Low",
    label: "Low",
    description: "Non-urgent inquiry or question"
  },
  {
    value: "Medium",
    label: "Medium",
    description: "Important but not critical issue"
  },
  {
    value: "High",
    label: "High",
    description: "Urgent issue requiring immediate attention"
  }
]

export default function NewTicketPage() {
  const router = useRouter()
  
  const [formData, setFormData] = useState({
    category: "",
    priority: "Medium",
    subject: "",
    description: "",
    orderNumber: "",
    trackingNumber: ""
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.category) {
      newErrors.category = "Please select a category"
    }
    if (!formData.subject.trim()) {
      newErrors.subject = "Subject is required"
    }
    if (!formData.description.trim()) {
      newErrors.description = "Description is required"
    }
    if (formData.description.trim().length < 20) {
      newErrors.description = "Description must be at least 20 characters"
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

    // Simulate API call
    setTimeout(() => {
      alert("Ticket created successfully! (This is a mock implementation)")
      router.push("/interfaces/support/tickets")
    }, 1500)
  }

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev }
        delete newErrors[field]
        return newErrors
      })
    }
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Tickets
        </Button>
        <div>
          <h1 className="text-3xl font-bold text-black">Create New Ticket</h1>
          <p className="text-gray-600 mt-1">Get help with your order or inquiry</p>
        </div>
      </div>

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
              Include order numbers, tracking numbers, and any relevant information.
            </p>
          </div>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Category Selection */}
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <Label className="text-lg font-semibold text-black mb-4 block">
            Select Category *
          </Label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {categories.map((category) => {
              const Icon = category.icon
              return (
                <button
                  key={category.value}
                  type="button"
                  onClick={() => handleInputChange("category", category.value)}
                  className={`flex items-start gap-3 p-4 rounded-lg border-2 transition-all text-left ${
                    formData.category === category.value
                      ? "border-blue-500 bg-blue-50"
                      : "border-gray-200 hover:border-gray-300"
                  }`}
                >
                  <Icon className={`h-5 w-5 mt-0.5 ${
                    formData.category === category.value ? "text-blue-600" : "text-gray-400"
                  }`} />
                  <div>
                    <p className={`font-medium ${
                      formData.category === category.value ? "text-blue-900" : "text-black"
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
          {errors.category && (
            <p className="text-sm text-red-600 mt-2">{errors.category}</p>
          )}
        </div>

        {/* Priority Selection */}
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <Label className="text-lg font-semibold text-black mb-4 block">
            Priority Level *
          </Label>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {priorities.map((priority) => (
              <button
                key={priority.value}
                type="button"
                onClick={() => handleInputChange("priority", priority.value)}
                className={`flex flex-col items-start p-4 rounded-lg border-2 transition-all text-left ${
                  formData.priority === priority.value
                    ? priority.value === "High"
                      ? "border-red-500 bg-red-50"
                      : priority.value === "Medium"
                      ? "border-yellow-500 bg-yellow-50"
                      : "border-blue-500 bg-blue-50"
                    : "border-gray-200 hover:border-gray-300"
                }`}
              >
                <p className={`font-medium ${
                  formData.priority === priority.value
                    ? priority.value === "High"
                      ? "text-red-900"
                      : priority.value === "Medium"
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
          <h2 className="text-lg font-semibold text-black">Ticket Details</h2>
          
          {/* Subject */}
          <div>
            <Label htmlFor="subject">Subject *</Label>
            <Input
              id="subject"
              type="text"
              placeholder="Brief description of your issue"
              value={formData.subject}
              onChange={(e) => handleInputChange("subject", e.target.value)}
              className={`mt-2 ${errors.subject ? "border-red-500" : ""}`}
            />
            {errors.subject && (
              <p className="text-sm text-red-600 mt-1">{errors.subject}</p>
            )}
          </div>

          {/* Description */}
          <div>
            <Label htmlFor="description">Description *</Label>
            <textarea
              id="description"
              className={`mt-2 w-full min-h-[150px] rounded-md border bg-white px-3 py-2 text-sm text-black shadow-sm transition-colors placeholder:text-gray-500 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 ${
                errors.description ? "border-red-500" : ""
              }`}
              placeholder="Please provide detailed information about your issue. Include any relevant order numbers, tracking numbers, dates, or other details that might help us assist you better."
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

        {/* Optional Information */}
        <div className="bg-white border rounded-lg p-6 shadow-sm space-y-4">
          <h2 className="text-lg font-semibold text-black">
            Additional Information <span className="text-gray-500 text-sm font-normal">(Optional)</span>
          </h2>
          
          {/* Order Number */}
          <div>
            <Label htmlFor="orderNumber">Order Number</Label>
            <Input
              id="orderNumber"
              type="text"
              placeholder="e.g., ORD-2025-001"
              value={formData.orderNumber}
              onChange={(e) => handleInputChange("orderNumber", e.target.value)}
              className="mt-2"
            />
            <p className="text-xs text-gray-500 mt-1">
              If your issue is related to a specific order
            </p>
          </div>

          {/* Tracking Number */}
          <div>
            <Label htmlFor="trackingNumber">Tracking Number</Label>
            <Input
              id="trackingNumber"
              type="text"
              placeholder="e.g., TRK123456789"
              value={formData.trackingNumber}
              onChange={(e) => handleInputChange("trackingNumber", e.target.value)}
              className="mt-2"
            />
            <p className="text-xs text-gray-500 mt-1">
              If your issue is related to a specific shipment
            </p>
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
            disabled={isSubmitting}
            className="flex-1"
          >
            {isSubmitting ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2" />
                Creating Ticket...
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
