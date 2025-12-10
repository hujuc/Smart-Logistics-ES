# Reusable Alert Component

A modular, customizable alert component for the driver PWA application.

## Components

### Alert Component (`Alert.tsx`)

A styled modal alert that displays messages with different types (success, error, warning, info).

### useAlert Hook (`hooks/useAlert.ts`)

A custom React hook for managing alert state throughout the application.

## Usage

### 1. Import the hook in your component

```tsx
import { useAlert } from '@/hooks/useAlert';
import Alert from '@/components/Alert';
```

### 2. Initialize the hook

```tsx
export default function MyComponent() {
  const { alert, showSuccess, showError, showWarning, showInfo, closeAlert } = useAlert();

  // ... your component logic
}
```

### 3. Add the Alert component to your JSX

```tsx
return (
  <div>
    {/* Alert Component - should be placed at the top level */}
    <Alert
      isOpen={alert.isOpen}
      onClose={closeAlert}
      title={alert.title}
      message={alert.message}
      type={alert.type}
    />

    {/* Your other components */}
  </div>
);
```

### 4. Trigger alerts in your code

```tsx
// Success alert (green checkmark icon)
showSuccess('Success Title', 'Your operation completed successfully!');

// Error alert (red X icon)
showError('Error Title', 'Something went wrong. Please try again.');

// Warning alert (orange warning triangle icon)
showWarning('Warning Title', 'Please review this important information.');

// Info alert (blue info icon)
showInfo('Info Title', 'Here is some useful information.');
```

## Alert Types

| Type      | Icon           | Color  | Use Case                                      |
|-----------|----------------|--------|-----------------------------------------------|
| `success` | CheckCircle    | Green  | Operation completed successfully              |
| `error`   | XCircle        | Red    | Operation failed or error occurred            |
| `warning` | AlertTriangle  | Orange | Important information or caution              |
| `info`    | Info           | Blue   | General information or helpful tips           |

## Examples

### Example 1: Delivery Completion

```tsx
const handleDelivery = async () => {
  try {
    await completeDelivery(orderId);
    showSuccess('Delivery Completed', 'Order has been marked as DELIVERED.');
  } catch (error) {
    showError('Delivery Failed', 'Unable to complete delivery. Please try again.');
  }
};
```

### Example 2: Form Validation

```tsx
const handleSubmit = () => {
  if (!formData.name) {
    showWarning('Missing Information', 'Please provide your name.');
    return;
  }

  if (!formData.email) {
    showWarning('Missing Information', 'Please provide your email address.');
    return;
  }

  // Submit form...
  showSuccess('Form Submitted', 'Your information has been saved.');
};
```

### Example 3: No Data Available

```tsx
const handleStartNavigation = async () => {
  const deliveries = await getDeliveries();

  if (deliveries.length === 0) {
    showWarning('No Packages to Deliver', 'You have no packages to deliver at this time.');
    return;
  }

  router.push('/navigation');
};
```

## Customization

### Custom Button Text

```tsx
<Alert
  isOpen={alert.isOpen}
  onClose={closeAlert}
  title={alert.title}
  message={alert.message}
  type={alert.type}
  confirmText="Got it!" // Default is "OK"
/>
```

### Alert Configuration

The alert styles are configured in `Alert.tsx`:

```tsx
const alertConfig = {
  success: {
    icon: CheckCircle,
    bgColor: 'bg-green-100',
    iconColor: 'text-green-600',
  },
  // ... other types
};
```

## Best Practices

1. **Use appropriate alert types**: Match the alert type to the context (success for completions, error for failures, etc.)

2. **Keep titles concise**: Use 2-4 words for titles (e.g., "Delivery Completed", "Error Occurred")

3. **Provide clear messages**: Explain what happened and what the user should do next

4. **Replace all `alert()` calls**: Use the modular alert system instead of browser's native `alert()`

5. **Avoid multiple alerts**: Only show one alert at a time for better UX

## Migration Guide

### Before (using browser alert):

```tsx
alert('Shipment accepted successfully!');
alert('Failed to accept shipment: ' + error.message);
```

### After (using modular alert):

```tsx
showSuccess('Shipment Accepted', 'Shipment accepted successfully!');
showError('Accept Failed', 'Failed to accept shipment: ' + error.message);
```

## Files Updated

The following files have been updated to use the modular alert system:

- ✅ `app/page.tsx` (Driver Home)
- ✅ `app/interfaces/navigation-optimized/page.tsx` (Navigation & POD)
- 📝 Add more pages as needed

## Next Steps

To add alerts to other pages:

1. Import the hook and component
2. Initialize `useAlert()` in your component
3. Add the `<Alert />` component to your JSX
4. Replace all `alert()` calls with `showSuccess()`, `showError()`, etc.
