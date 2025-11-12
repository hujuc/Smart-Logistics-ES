/**
 * Health check endpoint for monitoring and observability
 * Returns HTTP 200 if the service is healthy
 */
export async function GET() {
  return Response.json(
    {
      status: 'healthy',
      service: 'driver-pwa',
      timestamp: new Date().toISOString(),
    },
    { status: 200 }
  );
}
