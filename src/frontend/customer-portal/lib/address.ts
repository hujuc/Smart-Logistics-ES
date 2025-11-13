import type { Order } from "@/types/common"

type ShippingAddressFields = Pick<
  Order,
  | "shippingAddress"
  | "shippingAddressLine1"
  | "shippingAddressLine2"
  | "shippingCity"
  | "shippingStateOrProvince"
  | "shippingPostalCode"
  | "shippingCountry"
  | "shippingPhone"
  | "shippingEmail"
>

const clean = (value?: string | null) => {
  const trimmed = value?.trim()
  return trimmed && trimmed.length > 0 ? trimmed : undefined
}

export type AddressFieldName =
  | "shippingAddressLine1"
  | "shippingAddressLine2"
  | "shippingCity"
  | "shippingStateOrProvince"
  | "shippingPostalCode"
  | "shippingCountry"
  | "shippingPhone"
  | "shippingEmail"

export const hasStructuredAddress = (
  fields: Partial<ShippingAddressFields>,
): boolean =>
  [
    fields.shippingAddressLine1,
    fields.shippingAddressLine2,
    fields.shippingCity,
    fields.shippingStateOrProvince,
    fields.shippingPostalCode,
    fields.shippingCountry,
    fields.shippingPhone,
    fields.shippingEmail,
  ].some((value) => clean(value))

const buildLocality = (fields: Partial<ShippingAddressFields>) => {
  const cityState = [clean(fields.shippingCity), clean(fields.shippingStateOrProvince)]
    .filter(Boolean)
    .join(", ")

  const postal = clean(fields.shippingPostalCode)

  if (!cityState && !postal) {
    return undefined
  }

  return [cityState || undefined, postal].filter(Boolean).join(" ")
}

export const formatOrderShippingAddress = (
  fields: Partial<ShippingAddressFields>,
  fallback = "—",
): string => {
  if (!hasStructuredAddress(fields)) {
    return clean(fields.shippingAddress) ?? fallback
  }

  const parts = [
    clean(fields.shippingAddressLine1),
    clean(fields.shippingAddressLine2),
    buildLocality(fields),
    clean(fields.shippingCountry),
  ].filter(Boolean) as string[]

  if (parts.length === 0) {
    return clean(fields.shippingAddress) ?? fallback
  }

  return parts.join(", ")
}

export const buildShippingAddressString = (
  fields: Partial<ShippingAddressFields>,
): string => formatOrderShippingAddress(fields, "").trim()
