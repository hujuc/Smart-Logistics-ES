"use client";

import { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Package, Camera, Upload, ArrowLeft, CheckCircle, X } from 'lucide-react';
import { getAccessToken, getDailySchedule, DailyScheduleDelivery } from '@/lib/api';
import { featureFlagManager, isProofOfDeliveryEnabled } from '@/lib/flagsmith';

export default function ScanPackage() {
  const router = useRouter();
  const [orders, setOrders] = useState<DailyScheduleDelivery[]>([]);
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [featureEnabled, setFeatureEnabled] = useState(true);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    async function initializeAndFetchOrders() {
      try {
        // Initialize Flagsmith with driver ID
        // In production, this would come from auth context
        const driverId = 'driver-' + Math.random().toString(36).substring(7);
        await featureFlagManager.initialize(driverId);

        // Check if POD feature is enabled
        const enabled = isProofOfDeliveryEnabled();
        setFeatureEnabled(enabled);

        console.log('[ScanPackage] Feature flags initialized:', {
          podEnabled: enabled,
          driverId,
        });

        // Load orders if feature is enabled
        if (enabled) {
          const schedule = await getDailySchedule();
          const activeStatuses = [
            'IN_TRANSIT',
            'CREATED',
            'READY_FOR_PICKUP',
            'COLLECTED',
            'ASSIGNED_TO_SHIPMENT',
          ];
          const pendingOrders = schedule.deliveries.filter(delivery =>
            activeStatuses.includes(delivery.status)
          );
          setOrders(pendingOrders);
        }
      } catch (err) {
        console.error('Failed to fetch orders:', err);
        setError('Failed to load orders. Please try again.');
      } finally {
        setLoading(false);
      }
    }

    initializeAndFetchOrders();
  }, []);

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result as string);
      };
      reader.readAsDataURL(file);
      setError(null);
    }
  };

  const handleCameraCapture = (event: React.ChangeEvent<HTMLInputElement>) => {
    handleFileSelect(event);
  };

  const handleUpload = async () => {
    if (!selectedOrderId || !selectedFile) {
      setError('Please select an order and a photo');
      return;
    }

    setUploading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('photo', selectedFile);

      const token = getAccessToken();
      const response = await fetch(`http://localhost/api/driver/orders/${selectedOrderId}/deliver`, {
        method: 'POST',
        headers: {
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        credentials: 'include',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: 'Upload failed' }));
        throw new Error(errorData.error || `Upload failed: ${response.status}`);
      }

      const result = await response.json();
      console.log('Delivery completed:', result);

      setSuccess(true);

      // Redirect to home after 2 seconds
      setTimeout(() => {
        router.push('/');
      }, 2000);
    } catch (err) {
      console.error('Upload error:', err);
      setError(err instanceof Error ? err.message : 'Failed to upload proof of delivery');
    } finally {
      setUploading(false);
    }
  };

  const handleRemovePhoto = () => {
    setSelectedFile(null);
    setPreviewUrl(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
    if (cameraInputRef.current) cameraInputRef.current.value = '';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <Package className="h-12 w-12 text-black mx-auto mb-4 animate-pulse" />
          <p className="text-gray-600">Loading orders...</p>
        </div>
      </div>
    );
  }

  if (!featureEnabled) {
    return (
      <div className="min-h-screen bg-white">
        {/* Header */}
        <div className="bg-black text-white p-6">
          <div className="flex items-center gap-4 mb-6">
            <Link href="/">
              <button className="p-2 hover:bg-gray-800 rounded-lg transition-colors">
                <ArrowLeft className="h-6 w-6" />
              </button>
            </Link>
            <div className="flex items-center gap-2">
              <Package className="h-6 w-6" />
              <span className="text-lg font-bold">SLMS</span>
            </div>
          </div>
          <h1 className="text-3xl font-bold">Scan Package</h1>
          <p className="text-gray-300 text-sm mt-1">Complete delivery with proof of delivery</p>
        </div>

        {/* Feature Disabled Message */}
        <div className="p-6 flex items-center justify-center min-h-[60vh]">
          <div className="text-center max-w-md">
            <Package className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-black mb-2">Feature Coming Soon</h2>
            <p className="text-gray-600 mb-6">
              The Proof of Delivery feature is currently being rolled out and is not yet available for your account.
              Please check back later or contact your supervisor for more information.
            </p>
            <Link href="/">
              <button className="bg-black text-white px-6 py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors">
                Return to Home
              </button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center p-6">
        <div className="text-center">
          <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-black mb-2">Delivery Completed!</h2>
          <p className="text-gray-600">Redirecting to home...</p>
        </div>
      </div>
    );
  }

  const selectedOrder = orders.find(order => order.orderId === selectedOrderId);

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="bg-black text-white p-6">
        <div className="flex items-center gap-4 mb-6">
          <Link href="/">
            <button className="p-2 hover:bg-gray-800 rounded-lg transition-colors">
              <ArrowLeft className="h-6 w-6" />
            </button>
          </Link>
          <div className="flex items-center gap-2">
            <Package className="h-6 w-6" />
            <span className="text-lg font-bold">SLMS</span>
          </div>
        </div>
        <h1 className="text-3xl font-bold">Scan Package</h1>
        <p className="text-gray-300 text-sm mt-1">Complete delivery with proof of delivery</p>
      </div>

      {/* Content */}
      <div className="p-6 space-y-6">
        {/* Order Selection Dropdown */}
        <div>
          <label className="block text-sm font-semibold text-black mb-2">
            Select Order
          </label>
          {orders.length === 0 ? (
            <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded">
              <p className="text-sm text-yellow-700">
                No pending orders available for delivery.
              </p>
            </div>
          ) : (
            <select
              value={selectedOrderId || ''}
              onChange={(e) => setSelectedOrderId(Number(e.target.value))}
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:border-black focus:outline-none text-black bg-white"
            >
              <option value="">Select an order...</option>
              {orders.map((order) => (
                <option key={order.orderId} value={order.orderId}>
                  Stop #{order.stopNumber} - {order.customerName} ({order.orderReference})
                </option>
              ))}
            </select>
          )}
        </div>

        {/* Selected Order Details */}
        {selectedOrder && (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
            <h3 className="font-semibold text-black mb-2">Delivery Details</h3>
            <div className="space-y-1 text-sm">
              <p className="text-gray-700">
                <span className="font-medium">Customer:</span> {selectedOrder.customerName}
              </p>
              <p className="text-gray-700">
                <span className="font-medium">Address:</span> {selectedOrder.address}
              </p>
              <p className="text-gray-700">
                <span className="font-medium">Phone:</span> {selectedOrder.phone}
              </p>
              <p className="text-gray-700">
                <span className="font-medium">Reference:</span> {selectedOrder.orderReference}
              </p>
            </div>
          </div>
        )}

        {/* Photo Upload Section */}
        <div>
          <label className="block text-sm font-semibold text-black mb-2">
            Proof of Delivery Photo
          </label>

          {!previewUrl ? (
            <div className="space-y-3">
              {/* Camera Button */}
              <button
                onClick={() => cameraInputRef.current?.click()}
                className="w-full bg-black text-white py-4 rounded-lg font-semibold hover:bg-gray-800 transition-colors flex items-center justify-center gap-2"
              >
                <Camera className="w-5 h-5" />
                Take Photo
              </button>
              <input
                ref={cameraInputRef}
                type="file"
                accept="image/*"
                capture="environment"
                onChange={handleCameraCapture}
                className="hidden"
              />

              {/* Upload Button */}
              <button
                onClick={() => fileInputRef.current?.click()}
                className="w-full bg-white border-2 border-gray-300 text-black py-4 rounded-lg font-semibold hover:bg-gray-50 transition-colors flex items-center justify-center gap-2"
              >
                <Upload className="w-5 h-5" />
                Upload from Gallery
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFileSelect}
                className="hidden"
              />
            </div>
          ) : (
            <div className="space-y-4">
              {/* Photo Preview */}
              <div className="relative">
                <img
                  src={previewUrl}
                  alt="Proof of delivery preview"
                  className="w-full rounded-lg border-2 border-gray-300"
                />
                <button
                  onClick={handleRemovePhoto}
                  className="absolute top-2 right-2 bg-red-500 text-white p-2 rounded-full hover:bg-red-600 transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <p className="text-sm text-gray-600 text-center">
                {selectedFile?.name}
              </p>
            </div>
          )}
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 rounded">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        {/* Submit Button */}
        <button
          onClick={handleUpload}
          disabled={!selectedOrderId || !selectedFile || uploading || orders.length === 0}
          className={`w-full py-4 rounded-lg font-semibold transition-colors flex items-center justify-center gap-2 ${
            !selectedOrderId || !selectedFile || uploading || orders.length === 0
              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
              : 'bg-green-600 text-white hover:bg-green-700'
          }`}
        >
          {uploading ? (
            <>
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
              Uploading...
            </>
          ) : (
            <>
              <CheckCircle className="w-5 h-5" />
              Complete Delivery
            </>
          )}
        </button>
      </div>
    </div>
  );
}
