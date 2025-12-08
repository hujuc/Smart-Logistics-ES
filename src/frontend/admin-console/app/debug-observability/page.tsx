"use client";

/**
 * Debug page to verify observability configuration
 * Visit: http://localhost/admin/debug-observability
 */

export default function DebugObservabilityPage() {
  const envVars = {
    NEXT_PUBLIC_ELASTIC_APM_SERVER_URL:
      process.env.NEXT_PUBLIC_ELASTIC_APM_SERVER_URL,
    NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN: process.env
      .NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN
      ? "***SET***"
      : "NOT SET",
    NEXT_PUBLIC_DEPLOYMENT_ENV: process.env.NEXT_PUBLIC_DEPLOYMENT_ENV,
    NEXT_PUBLIC_PROJECT_VERSION: process.env.NEXT_PUBLIC_PROJECT_VERSION,
    NEXT_PUBLIC_TRACING_SAMPLING_RATE:
      process.env.NEXT_PUBLIC_TRACING_SAMPLING_RATE,
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
  };

  return (
    <div style={{ padding: "2rem", fontFamily: "monospace" }}>
      <h1>Observability Debug Page</h1>

      <h2>Environment Variables</h2>
      <pre
        style={{ background: "#f5f5f5", padding: "1rem", borderRadius: "4px" }}
      >
        {JSON.stringify(envVars, null, 2)}
      </pre>

      <h2>Check Browser Console</h2>
      <p>Open your browser&apos;s developer console (F12) and look for:</p>
      <ul>
        <li>[Observability] Initializing messages</li>
        <li>[APM] Initialized messages</li>
        <li>[Web Vitals] Tracking initialized</li>
      </ul>

      <h2>Test APM</h2>
      <button
        onClick={() => {
          console.log("Testing APM error capture...");
          try {
            throw new Error("Test error from debug page");
          } catch (err) {
            console.error("Caught test error:", err);
          }
        }}
        style={{
          padding: "0.5rem 1rem",
          background: "#007bff",
          color: "white",
          border: "none",
          borderRadius: "4px",
          cursor: "pointer",
        }}
      >
        Trigger Test Error
      </button>

      <button
        onClick={async () => {
          console.log("Testing API call...");
          try {
            await fetch("/api/customer-service/health");
            console.log("API call completed");
          } catch (err) {
            console.error("API call failed:", err);
          }
        }}
        style={{
          padding: "0.5rem 1rem",
          background: "#28a745",
          color: "white",
          border: "none",
          borderRadius: "4px",
          cursor: "pointer",
          marginLeft: "1rem",
        }}
      >
        Test API Call
      </button>
    </div>
  );
}
