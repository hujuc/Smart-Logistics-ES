/**
 * Health check endpoint for monitoring and observability
 * Returns HTTP 200 if the service is healthy
 */
export async function GET() {
  return Response.json(
    {
      status: 'healthy',
      service: 'admin-console',
      timestamp: new Date().toISOString(),
    },
    { status: 200 }
  );
}
