import { useState, useEffect, useRef } from 'react';

/**
 * Hook to trigger an animation when a value (like notification count) increases.
 * 
 * @param value The value to monitor for increases
 * @param duration Duration of the animation in milliseconds (default: 2000)
 * @returns boolean indicating if the animation should be active
 */
export function useNotificationAnimation(value: number, duration: number = 2000) {
    const [shouldAnimate, setShouldAnimate] = useState(false);
    const prevValueRef = useRef(value);

    useEffect(() => {
        // Only animate if the value has increased
        if (value > prevValueRef.current) {
            setShouldAnimate(true);
            const timer = setTimeout(() => setShouldAnimate(false), duration);
            return () => clearTimeout(timer);
        }
        prevValueRef.current = value;
    }, [value, duration]);

    return shouldAnimate;
}
