import { Package } from 'lucide-react';

interface LogoProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
  iconClassName?: string;
  textClassName?: string;
}

const sizeConfig = {
  sm: {
    icon: 'h-4 w-4',
    text: 'text-base',
  },
  md: {
    icon: 'h-6 w-6',
    text: 'text-lg',
  },
  lg: {
    icon: 'h-8 w-8',
    text: 'text-2xl',
  },
  xl: {
    icon: 'h-16 w-16',
    text: 'text-6xl',
  },
};

/**
 * SLMS Logo component
 * 
 * Official logo of the SLMS (Smart Logistics Management System).
 * Consists of a Package icon from lucide-react and the text "SLMS".
 */
export function Logo({ 
  size = 'lg', 
  className = '', 
  iconClassName = '',
  textClassName = '' 
}: LogoProps) {
  const config = sizeConfig[size];
  
  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <Package className={`${config.icon} text-black ${iconClassName}`} />
      <span className={`${config.text} font-bold text-black ${textClassName}`}>
        SLMS
      </span>
    </div>
  );
}

export default Logo;
