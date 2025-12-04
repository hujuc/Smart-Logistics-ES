package Operimus.SLMS.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Examples of using OpenTelemetry for distributed tracing and observability
 *
 * DISABLED: This class has been disabled because we switched from OpenTelemetry
 * to Elastic APM's native instrumentation. Elastic APM automatically captures
 * all traces, spans, and metrics without manual instrumentation.
 *
 * Key differences:
 * - OpenTelemetry: Manual span creation and instrumentation
 * - Elastic APM: Automatic instrumentation of HTTP requests, database queries, etc.
 *
 * Following ES_08_Monitoring_Observability course material:
 * - Slide 26: Event-based observability (Logs, Metrics, Traces)
 * - Slide 27-29: Spans and trace structure
 * - Slide 30-31: Latency tracking across distributed systems
 *
 * With Elastic APM, you get all of this automatically:
 * 1. HTTP request traces (with status codes, duration, URLs)
 * 2. Database query spans (automatically instrumented)
 * 3. External API call spans (HTTP clients)
 * 4. Exception tracking and error rates
 * 5. JVM metrics (heap, GC, threads)
 * 6. Distributed tracing across services
 *
 * To view this data:
 * - Go to Kibana → Observability → APM → Services → SLMS
 * - Click on "Transactions" to see HTTP requests
 * - Click on "Errors" to see exceptions
 * - Click on individual transactions to see detailed span breakdowns
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "observability.examples.enabled", havingValue = "true", matchIfMissing = false)
public class ObservabilityExample {

    // This class is disabled by default.
    // Elastic APM provides automatic instrumentation, so manual tracing is not needed.

    public ObservabilityExample() {
        log.info("ObservabilityExample bean created (but functionality is disabled - using Elastic APM instead)");
    }
}
