package Operimus.SLMS.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Examples of using OpenTelemetry for distributed tracing and observability
 *
 * Following ES_08_Monitoring_Observability course material:
 * - Slide 26: Event-based observability (Logs, Metrics, Traces)
 * - Slide 27-29: Spans and trace structure
 * - Slide 30-31: Latency tracking across distributed systems
 *
 * This class demonstrates:
 * 1. Creating custom spans for business operations
 * 2. Adding attributes to spans for context
 * 3. Creating child spans for sub-operations
 * 4. Error handling and span status
 * 5. Correlation with structured logging (trace_id, span_id)
 */
@Slf4j
@Component
public class ObservabilityExample {

    private final Tracer tracer;

    public ObservabilityExample(OpenTelemetry openTelemetry) {
        // Initialize tracer for this application component
        // Slide 38: Injecting OpenTelemetry via Spring Boot Starter
        this.tracer = openTelemetry.getTracer("SLMS-application");
    }

    /**
     * Example 1: Basic custom span for a business operation
     *
     * Use case: Track a high-level business operation like "process shipment"
     * Following Slide 27: Spans contain operation name, timestamps, and attributes
     */
    public void exampleBasicSpan(String shipmentId) {
        // Create a new span
        Span span = tracer.spanBuilder("process_shipment")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Add attributes to provide context
            // Slide 27: Attributes are key-value pairs
            span.setAttribute("shipment.id", shipmentId);
            span.setAttribute("operation.type", "processing");

            // Structured logging with trace correlation
            // Slide 26: Logs are events with trace_id/span_id for correlation
            log.info("Processing shipment: {}", shipmentId);

            // Simulate business logic
            Thread.sleep(100);

            // Mark span as successful
            span.setStatus(StatusCode.OK);

        } catch (Exception e) {
            // Record exception and mark span as error
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to process shipment");
            log.error("Error processing shipment: {}", shipmentId, e);
            throw new RuntimeException(e);

        } finally {
            // Always end the span
            span.end();
        }
    }

    /**
     * Example 2: Nested spans for detailed operation breakdown
     *
     * Use case: Track sub-operations within a larger transaction
     * Following Slide 28-29: Traces are directed acyclic graphs of spans
     *
     * This creates a parent-child relationship:
     * [Span A] (ship_order)
     *    ├── [Span B] (validate_order)
     *    ├── [Span C] (select_carrier)
     *    └── [Span D] (generate_label)
     */
    public void exampleNestedSpans(String orderId) {
        // Parent span: Overall operation
        Span parentSpan = tracer.spanBuilder("ship_order")
                .startSpan();

        try (Scope parentScope = parentSpan.makeCurrent()) {
            parentSpan.setAttribute("order.id", orderId);
            log.info("Starting order shipment process for order: {}", orderId);

            // Child Span 1: Validate order
            validateOrder(orderId);

            // Child Span 2: Select carrier
            String carrier = selectCarrier(orderId);

            // Child Span 3: Generate label
            generateLabel(orderId, carrier);

            parentSpan.setStatus(StatusCode.OK);
            log.info("Successfully shipped order: {}", orderId);

        } catch (Exception e) {
            parentSpan.recordException(e);
            parentSpan.setStatus(StatusCode.ERROR, "Order shipment failed");
            log.error("Failed to ship order: {}", orderId, e);
            throw new RuntimeException(e);

        } finally {
            parentSpan.end();
        }
    }

    /**
     * Child operation 1: Validate order
     * Slide 27: Each span has parent_span identifier
     */
    private void validateOrder(String orderId) {
        Span span = tracer.spanBuilder("validate_order")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("order.id", orderId);
            log.debug("Validating order: {}", orderId);

            // Simulate validation logic
            Thread.sleep(50);

            span.setStatus(StatusCode.OK);

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }

    /**
     * Child operation 2: Select carrier
     * Adds business-specific attributes
     */
    private String selectCarrier(String orderId) {
        Span span = tracer.spanBuilder("select_carrier")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("order.id", orderId);

            // Simulate carrier selection logic
            String carrier = "DHL";
            Thread.sleep(75);

            // Add result as attribute
            span.setAttribute("carrier.selected", carrier);
            span.setStatus(StatusCode.OK);

            log.info("Selected carrier {} for order {}", carrier, orderId);
            return carrier;

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }

    /**
     * Child operation 3: Generate shipping label
     * Demonstrates adding events within a span
     */
    private void generateLabel(String orderId, String carrier) {
        Span span = tracer.spanBuilder("generate_label")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("order.id", orderId);
            span.setAttribute("carrier.name", carrier);

            // Add events within the span (Slide 27: Spans can contain Events)
            span.addEvent("label_generation_started");

            log.debug("Generating label for order {} with carrier {}", orderId, carrier);

            // Simulate label generation
            Thread.sleep(100);

            span.addEvent("label_generation_completed");
            span.setStatus(StatusCode.OK);

        } catch (Exception e) {
            span.addEvent("label_generation_failed");
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }

    /**
     * Example 3: Measuring latency across operations
     *
     * Following Slide 30-31: Use traces to compare latency
     * - Logs show individual event times
     * - Metrics show aggregate response times
     * - Traces show the complete breakdown of where time is spent
     */
    public void exampleLatencyTracking(String clientId) {
        Span span = tracer.spanBuilder("sync_client_manager")
                .startSpan();

        long startTime = System.currentTimeMillis();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("client.id", clientId);

            log.info("Starting client sync for client: {}", clientId);

            // Simulate database query (42ms)
            Span dbSpan = tracer.spanBuilder("database_query").startSpan();
            try (Scope dbScope = dbSpan.makeCurrent()) {
                dbSpan.setAttribute("db.operation", "SELECT");
                dbSpan.setAttribute("db.table", "clients");
                Thread.sleep(42);
                dbSpan.setStatus(StatusCode.OK);
            } finally {
                dbSpan.end();
            }

            // Simulate external API call (50ms)
            Span apiSpan = tracer.spanBuilder("external_api_call").startSpan();
            try (Scope apiScope = apiSpan.makeCurrent()) {
                apiSpan.setAttribute("http.method", "POST");
                apiSpan.setAttribute("http.url", "/api/v1/clients");
                Thread.sleep(50);
                apiSpan.setStatus(StatusCode.OK);
            } finally {
                apiSpan.end();
            }

            long duration = System.currentTimeMillis() - startTime;
            span.setAttribute("operation.duration_ms", duration);
            span.setStatus(StatusCode.OK);

            // Slide 30: Log shows total response time (92ms in this example)
            log.info("Client sync completed in {}ms for client: {}", duration, clientId);

            // Slide 31: Trace shows detailed breakdown:
            // - Total: 92ms
            // - Database query: 42ms
            // - External API: 50ms
            // This answers: "What caused the delay of 92ms?"

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            log.error("Client sync failed for client: {}", clientId, e);
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }

    /**
     * Example 4: Adding custom metrics within spans
     *
     * Demonstrates how traces, metrics, and logs work together:
     * - Log: "Processed 150 items" (event)
     * - Metric: batch_size=150 (value)
     * - Trace: Span shows how long it took (tree structure)
     */
    public void exampleMetricsAndTracing(int batchSize) {
        Span span = tracer.spanBuilder("process_batch")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Add business metrics as span attributes
            span.setAttribute("batch.size", batchSize);
            span.setAttribute("batch.type", "shipment_updates");

            log.info("Processing batch of {} shipment updates", batchSize);

            // Simulate batch processing
            for (int i = 0; i < batchSize; i++) {
                Thread.sleep(10);
            }

            span.setAttribute("items.processed", batchSize);
            span.setStatus(StatusCode.OK);

            // Slide 26: All three pillars unified:
            // - Log: Event recording
            // - Metric: Value (batch size)
            // - Trace: Operation tree with timing
            log.info("Completed processing batch of {} items", batchSize);

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            log.error("Batch processing failed", e);
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }
}
