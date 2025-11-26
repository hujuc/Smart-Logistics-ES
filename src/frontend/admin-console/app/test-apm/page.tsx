'use client';

import { useEffect, useState } from 'react';
import { getApm, getApmConfig } from '@shared/lib/observability/apm';

export default function TestAPMPage() {
  const [status, setStatus] = useState<any>({});

  useEffect(() => {
    // Check APM status
    const apm = getApm();
    const config = getApmConfig();

    const statusInfo = {
      apmInitialized: apm !== null,
      serverUrl: process.env.NEXT_PUBLIC_ELASTIC_APM_SERVER_URL,
      hasSecretToken: !!process.env.NEXT_PUBLIC_ELASTIC_APM_SECRET_TOKEN,
      serviceName: config?.serviceName || 'Not available',
      environment: config?.environment || process.env.NEXT_PUBLIC_DEPLOYMENT_ENV || 'Not available',
      active: apm?.isActive() || false,
    };

    setStatus(statusInfo);

    console.log('[Test Page] APM Status:', statusInfo);
    console.log('[Test Page] APM Instance:', apm);

    if (apm) {
      console.log('[Test Page] ✓ APM is initialized');

      // Test capturing a custom transaction
      const transaction = apm.startTransaction('test-page-load', 'page-load');
      if (transaction) {
        console.log('[Test Page] Started test transaction');
        setTimeout(() => {
          transaction.end();
          console.log('[Test Page] Ended test transaction');
        }, 100);
      }
    } else {
      console.error('[Test Page] ✗ APM is NOT initialized');
    }
  }, []);

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: '800px', margin: '0 auto' }}>
      <h1>APM Test Page</h1>

      <div style={{ background: '#f0f0f0', padding: '1rem', borderRadius: '8px', marginBottom: '2rem' }}>
        <h2>APM Status</h2>
        <pre style={{ overflow: 'auto' }}>
          {JSON.stringify(status, null, 2)}
        </pre>
      </div>

      <div style={{ background: '#e3f2fd', padding: '1rem', borderRadius: '8px', marginBottom: '1rem' }}>
        <h3>✓ What to Check:</h3>
        <ol>
          <li>Open browser DevTools (F12)</li>
          <li>Go to <strong>Console</strong> tab - Look for [APM] and [Test Page] messages</li>
          <li>Go to <strong>Network</strong> tab</li>
          <li>Filter by "intake" or your Elastic APM URL</li>
          <li>Refresh this page</li>
          <li>You should see POST requests to: <code>/intake/v2/rum/events</code></li>
        </ol>
      </div>

      <div style={{ background: '#fff3e0', padding: '1rem', borderRadius: '8px' }}>
        <h3>⚠️ If No Requests Appear:</h3>
        <ul>
          <li>APM RUM might not be initialized (check Console for errors)</li>
          <li>Environment variables might not be set correctly</li>
          <li>RUM endpoint might not be enabled in your Elastic Cloud deployment</li>
        </ul>
      </div>

      <div style={{ marginTop: '2rem' }}>
        <button
          onClick={() => {
            const apm = getApm();
            if (apm) {
              apm.captureError(new Error('Test error from button click'));
              alert('Test error sent to Elastic APM! Check Network tab for request.');
            } else {
              alert('APM is not initialized!');
            }
          }}
          style={{
            padding: '0.75rem 1.5rem',
            background: '#d32f2f',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '1rem',
          }}
        >
          Send Test Error to APM
        </button>
      </div>
    </div>
  );
}
