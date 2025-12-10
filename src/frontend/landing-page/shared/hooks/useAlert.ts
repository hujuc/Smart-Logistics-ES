import { useState } from 'react';
import { AlertType } from '../components/Alert';

interface AlertState {
  isOpen: boolean;
  title: string;
  message: string;
  type: AlertType;
}

export function useAlert() {
  const [alert, setAlert] = useState<AlertState>({
    isOpen: false,
    title: '',
    message: '',
    type: 'info',
  });

  const showAlert = (title: string, message: string, type: AlertType = 'info') => {
    setAlert({
      isOpen: true,
      title,
      message,
      type,
    });
  };

  const showSuccess = (title: string, message: string) => {
    showAlert(title, message, 'success');
  };

  const showError = (title: string, message: string) => {
    showAlert(title, message, 'error');
  };

  const showWarning = (title: string, message: string) => {
    showAlert(title, message, 'warning');
  };

  const showInfo = (title: string, message: string) => {
    showAlert(title, message, 'info');
  };

  const closeAlert = () => {
    setAlert((prev) => ({ ...prev, isOpen: false }));
  };

  return {
    alert,
    showAlert,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    closeAlert,
  };
}
