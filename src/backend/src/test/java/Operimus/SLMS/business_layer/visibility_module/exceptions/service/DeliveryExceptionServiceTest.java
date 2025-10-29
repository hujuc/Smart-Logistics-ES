package Operimus.SLMS.business_layer.visibility_module.exceptions.service;

import Operimus.SLMS.business_layer.visibility_module.exceptions.dto.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException;
import Operimus.SLMS.business_layer.visibility_module.exceptions.entities.DeliveryException.*;
import Operimus.SLMS.business_layer.visibility_module.exceptions.repository.DeliveryExceptionRepository;
import Operimus.SLMS.business_layer.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryExceptionServiceTest {

    @Mock
    private DeliveryExceptionRepository exceptionRepository;

    @InjectMocks
    private DeliveryExceptionService exceptionService;

    private DeliveryException testException;
    private UUID testExceptionId;
    private UUID testCustomerId;
    private UUID testAssignedTo;

    @BeforeEach
    void setUp() {
        testExceptionId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();
        testAssignedTo = UUID.randomUUID();

        testException = DeliveryException.builder()
                .id(testExceptionId)
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .severity(Severity.HIGH)
                .description("Package arrived damaged")
                .source(Source.CUSTOMER)
                .reportedBy(testCustomerId)
                .customerReported(true)
                .customerId(testCustomerId)
                .status(ExceptionStatus.OPEN)
                .priority(Priority.HIGH)
                .slaDeadline(LocalDateTime.now().plusHours(24))
                .slaBreached(false)
                .build();
    }

    // ========================================
    // Create Exception Tests
    // ========================================

    @Test
    void whenCreateException_thenExceptionIsSaved() {
        DeliveryExceptionRequest request = DeliveryExceptionRequest.builder()
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .source(Source.CUSTOMER)
                .severity(Severity.HIGH)
                .description("Package arrived damaged")
                .reportedBy(testCustomerId)
                .customerReported(true)
                .customerId(testCustomerId)
                .priority(Priority.HIGH)
                .build();

        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(testException);

        DeliveryExceptionResponse response = exceptionService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testExceptionId);
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo(ExceptionType.DAMAGED);
        assertThat(response.getStatus()).isEqualTo(ExceptionStatus.OPEN);
        assertThat(response.getSeverity()).isEqualTo(Severity.HIGH);

        verify(exceptionRepository).save(any(DeliveryException.class));
    }

    @Test
    void whenCreateExceptionWithoutSeverity_thenDefaultToMedium() {
        DeliveryExceptionRequest request = DeliveryExceptionRequest.builder()
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .source(Source.CUSTOMER)
                .description("Package arrived damaged")
                .reportedBy(testCustomerId)
                .build();

        DeliveryException savedException = DeliveryException.builder()
                .id(testExceptionId)
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .severity(Severity.MEDIUM)
                .source(Source.CUSTOMER)
                .status(ExceptionStatus.OPEN)
                .priority(Priority.MEDIUM)
                .build();

        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(savedException);

        DeliveryExceptionResponse response = exceptionService.create(request);

        assertThat(response.getSeverity()).isEqualTo(Severity.MEDIUM);
        assertThat(response.getPriority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void whenCreateHighPriorityException_thenSlaIs24Hours() {
        DeliveryExceptionRequest request = DeliveryExceptionRequest.builder()
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .source(Source.CUSTOMER)
                .description("Package arrived damaged")
                .priority(Priority.HIGH)
                .build();

        when(exceptionRepository.save(any(DeliveryException.class))).thenAnswer(invocation -> {
            DeliveryException saved = invocation.getArgument(0);
            assertThat(saved.getSlaDeadline()).isBetween(
                    LocalDateTime.now().plusHours(23),
                    LocalDateTime.now().plusHours(25)
            );
            return saved;
        });

        exceptionService.create(request);

        verify(exceptionRepository).save(any(DeliveryException.class));
    }

    @Test
    void whenCreateMediumPriorityException_thenSlaIs48Hours() {
        DeliveryExceptionRequest request = DeliveryExceptionRequest.builder()
                .orderId(1L)
                .type(ExceptionType.DAMAGED)
                .category("PACKAGE_DAMAGE")
                .source(Source.CUSTOMER)
                .description("Package arrived damaged")
                .priority(Priority.MEDIUM)
                .build();

        when(exceptionRepository.save(any(DeliveryException.class))).thenAnswer(invocation -> {
            DeliveryException saved = invocation.getArgument(0);
            assertThat(saved.getSlaDeadline()).isBetween(
                    LocalDateTime.now().plusHours(47),
                    LocalDateTime.now().plusHours(49)
            );
            return saved;
        });

        exceptionService.create(request);

        verify(exceptionRepository).save(any(DeliveryException.class));
    }

    // ========================================
    // Get Exception by ID Tests
    // ========================================

    @Test
    void whenGetById_thenReturnException() {
        when(exceptionRepository.findById(testExceptionId)).thenReturn(Optional.of(testException));

        DeliveryExceptionResponse response = exceptionService.getById(testExceptionId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testExceptionId);
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo(ExceptionType.DAMAGED);

        verify(exceptionRepository).findById(testExceptionId);
    }

    @Test
    void whenGetByIdNotFound_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        when(exceptionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exceptionService.getById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Delivery exception");

        verify(exceptionRepository).findById(nonExistentId);
    }

    // ========================================
    // Get All Exceptions Tests
    // ========================================

    @Test
    void whenGetAllWithoutFilters_thenReturnAllExceptions() {
        List<DeliveryException> exceptions = Arrays.asList(testException);
        when(exceptionRepository.findWithFilters(null, null, null, null))
                .thenReturn(exceptions);

        List<DeliveryExceptionResponse> responses = exceptionService.getAll(null, null, null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(testExceptionId);

        verify(exceptionRepository).findWithFilters(null, null, null, null);
    }

    @Test
    void whenGetAllWithFilters_thenReturnFilteredExceptions() {
        List<DeliveryException> exceptions = Arrays.asList(testException);
        when(exceptionRepository.findWithFilters(
                ExceptionStatus.OPEN, Priority.HIGH, testAssignedTo, Severity.HIGH))
                .thenReturn(exceptions);

        List<DeliveryExceptionResponse> responses = exceptionService.getAll(
                ExceptionStatus.OPEN, Priority.HIGH, testAssignedTo, Severity.HIGH);

        assertThat(responses).hasSize(1);

        verify(exceptionRepository).findWithFilters(
                ExceptionStatus.OPEN, Priority.HIGH, testAssignedTo, Severity.HIGH);
    }

    @Test
    void whenGetAllEmpty_thenReturnEmptyList() {
        when(exceptionRepository.findWithFilters(null, null, null, null))
                .thenReturn(Arrays.asList());

        List<DeliveryExceptionResponse> responses = exceptionService.getAll(null, null, null, null);

        assertThat(responses).isEmpty();

        verify(exceptionRepository).findWithFilters(null, null, null, null);
    }

    // ========================================
    // Update Exception Tests
    // ========================================

    @Test
    void whenUpdate_thenExceptionIsUpdated() {
        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.IN_PROGRESS)
                .assignedTo(testAssignedTo)
                .priority(Priority.CRITICAL)
                .resolutionNotes("Investigating with carrier")
                .build();

        when(exceptionRepository.findById(testExceptionId)).thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(testException);

        DeliveryExceptionResponse response = exceptionService.update(testExceptionId, updateRequest);

        assertThat(response).isNotNull();
        assertThat(testException.getStatus()).isEqualTo(ExceptionStatus.IN_PROGRESS);
        assertThat(testException.getAssignedTo()).isEqualTo(testAssignedTo);
        assertThat(testException.getPriority()).isEqualTo(Priority.CRITICAL);
        assertThat(testException.getResolutionNotes()).isEqualTo("Investigating with carrier");

        verify(exceptionRepository).findById(testExceptionId);
        verify(exceptionRepository).save(testException);
    }

    @Test
    void whenUpdateToResolved_thenSetResolvedAtAndCheckSla() {
        testException.setSlaDeadline(LocalDateTime.now().minusHours(1)); // SLA breached

        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.RESOLVED)
                .build();

        when(exceptionRepository.findById(testExceptionId)).thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(testException);

        exceptionService.update(testExceptionId, updateRequest);

        assertThat(testException.getResolvedAt()).isNotNull();
        assertThat(testException.getSlaBreached()).isTrue();

        verify(exceptionRepository).save(testException);
    }

    @Test
    void whenUpdateToClosed_thenSetResolvedAtAndCheckSla() {
        testException.setSlaDeadline(LocalDateTime.now().plusHours(1)); // SLA not breached

        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.CLOSED)
                .build();

        when(exceptionRepository.findById(testExceptionId)).thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(testException);

        exceptionService.update(testExceptionId, updateRequest);

        assertThat(testException.getResolvedAt()).isNotNull();
        assertThat(testException.getSlaBreached()).isFalse();

        verify(exceptionRepository).save(testException);
    }

    @Test
    void whenUpdateNotFound_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        DeliveryExceptionUpdateRequest updateRequest = DeliveryExceptionUpdateRequest.builder()
                .status(ExceptionStatus.IN_PROGRESS)
                .build();

        when(exceptionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exceptionService.update(nonExistentId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Delivery exception");

        verify(exceptionRepository).findById(nonExistentId);
        verify(exceptionRepository, never()).save(any());
    }

    // ========================================
    // Close Exception Tests
    // ========================================

    @Test
    void whenClose_thenExceptionIsClosed() {
        String resolutionNotes = "Issue resolved successfully";

        when(exceptionRepository.findById(testExceptionId)).thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(testException);

        DeliveryExceptionResponse response = exceptionService.close(testExceptionId, resolutionNotes);

        assertThat(response).isNotNull();
        assertThat(testException.getStatus()).isEqualTo(ExceptionStatus.CLOSED);
        assertThat(testException.getResolutionNotes()).isEqualTo(resolutionNotes);
        assertThat(testException.getResolvedAt()).isNotNull();

        verify(exceptionRepository).findById(testExceptionId);
        verify(exceptionRepository).save(testException);
    }

    @Test
    void whenCloseAfterSlaDeadline_thenMarkAsBreach() {
        testException.setSlaDeadline(LocalDateTime.now().minusHours(2)); // Past deadline
        String resolutionNotes = "Resolved";

        when(exceptionRepository.findById(testExceptionId)).thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(testException);

        exceptionService.close(testExceptionId, resolutionNotes);

        assertThat(testException.getSlaBreached()).isTrue();

        verify(exceptionRepository).save(testException);
    }

    @Test
    void whenCloseNotFound_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        String resolutionNotes = "Resolved";

        when(exceptionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exceptionService.close(nonExistentId, resolutionNotes))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Delivery exception");

        verify(exceptionRepository).findById(nonExistentId);
        verify(exceptionRepository, never()).save(any());
    }

    // ========================================
    // Get My Cases Tests
    // ========================================

    @Test
    void whenGetMyCases_thenReturnAssignedExceptions() {
        List<DeliveryException> myCases = Arrays.asList(testException);
        when(exceptionRepository.findByAssignedTo(testAssignedTo)).thenReturn(myCases);

        List<DeliveryExceptionResponse> responses = exceptionService.getMyCases(testAssignedTo);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(testExceptionId);

        verify(exceptionRepository).findByAssignedTo(testAssignedTo);
    }

    // ========================================
    // Dashboard Tests
    // ========================================

    @Test
    void whenGetDashboardSummary_thenReturnSummary() {
        when(exceptionRepository.count()).thenReturn(10L);
        when(exceptionRepository.countByStatus(ExceptionStatus.OPEN)).thenReturn(3L);
        when(exceptionRepository.countByStatus(ExceptionStatus.IN_PROGRESS)).thenReturn(4L);
        when(exceptionRepository.countResolvedSince(any())).thenReturn(2L);
        when(exceptionRepository.countByPriority(Priority.HIGH)).thenReturn(1L);
        when(exceptionRepository.countBySlaBreached(true)).thenReturn(0L);
        when(exceptionRepository.getAverageResolutionTimeHours()).thenReturn(24.5);
        when(exceptionRepository.getAverageCustomerSatisfaction()).thenReturn(4.5);

        ExceptionSummaryResponse summary = exceptionService.getDashboardSummary();

        assertThat(summary.getTotalExceptions()).isEqualTo(10L);
        assertThat(summary.getOpenExceptions()).isEqualTo(3L);
        assertThat(summary.getInProgressExceptions()).isEqualTo(4L);
        assertThat(summary.getResolvedToday()).isEqualTo(2L);
        assertThat(summary.getHighPriorityExceptions()).isEqualTo(1L);
        assertThat(summary.getSlaBreachedExceptions()).isEqualTo(0L);
        assertThat(summary.getAverageResolutionTimeHours()).isEqualTo(24.5);
        assertThat(summary.getCustomerSatisfactionRate()).isEqualTo(4.5);

        verify(exceptionRepository).count();
        verify(exceptionRepository).countByStatus(ExceptionStatus.OPEN);
        verify(exceptionRepository).countByStatus(ExceptionStatus.IN_PROGRESS);
        verify(exceptionRepository).countResolvedSince(any());
    }

    // ========================================
    // Alerts Tests
    // ========================================

    @Test
    void whenGetAlerts_thenReturnHighPriorityAndOverdueExceptions() {
        DeliveryException highPriority = testException;
        DeliveryException overdue = DeliveryException.builder()
                .id(UUID.randomUUID())
                .orderId(2L)
                .type(ExceptionType.LOST)
                .status(ExceptionStatus.OPEN)
                .slaDeadline(LocalDateTime.now().minusHours(1))
                .build();

        when(exceptionRepository.findHighPriorityOpen()).thenReturn(Arrays.asList(highPriority));
        when(exceptionRepository.findOverdueSla()).thenReturn(Arrays.asList(overdue));

        List<DeliveryExceptionResponse> alerts = exceptionService.getAlerts();

        assertThat(alerts).hasSize(2);

        verify(exceptionRepository).findHighPriorityOpen();
        verify(exceptionRepository).findOverdueSla();
    }

    @Test
    void whenGetAlertsEmpty_thenReturnEmptyList() {
        when(exceptionRepository.findHighPriorityOpen()).thenReturn(Arrays.asList());
        when(exceptionRepository.findOverdueSla()).thenReturn(Arrays.asList());

        List<DeliveryExceptionResponse> alerts = exceptionService.getAlerts();

        assertThat(alerts).isEmpty();
    }

    // ========================================
    // Get By Order ID Tests
    // ========================================

    @Test
    void whenGetByOrderId_thenReturnExceptions() {
        List<DeliveryException> exceptions = Arrays.asList(testException);
        when(exceptionRepository.findByOrderId(1L)).thenReturn(exceptions);

        List<DeliveryExceptionResponse> responses = exceptionService.getByOrderId(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getOrderId()).isEqualTo(1L);

        verify(exceptionRepository).findByOrderId(1L);
    }

    @Test
    void whenGetByOrderIdNotFound_thenReturnEmptyList() {
        when(exceptionRepository.findByOrderId(999L)).thenReturn(Arrays.asList());

        List<DeliveryExceptionResponse> responses = exceptionService.getByOrderId(999L);

        assertThat(responses).isEmpty();

        verify(exceptionRepository).findByOrderId(999L);
    }

    // ========================================
    // Submit Feedback Tests
    // ========================================

    @Test
    void whenSubmitFeedback_thenExceptionIsUpdated() {
        Integer rating = 5;
        String comment = "Excellent service";

        when(exceptionRepository.findById(testExceptionId)).thenReturn(Optional.of(testException));
        when(exceptionRepository.save(any(DeliveryException.class))).thenReturn(testException);

        DeliveryExceptionResponse response = exceptionService.submitFeedback(testExceptionId, rating, comment);

        assertThat(response).isNotNull();
        assertThat(testException.getCustomerFeedback()).isEqualTo(5);
        assertThat(testException.getFeedbackComment()).isEqualTo("Excellent service");

        verify(exceptionRepository).findById(testExceptionId);
        verify(exceptionRepository).save(testException);
    }

    @Test
    void whenSubmitFeedbackNotFound_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        Integer rating = 5;
        String comment = "Good";

        when(exceptionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exceptionService.submitFeedback(nonExistentId, rating, comment))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Delivery exception");

        verify(exceptionRepository).findById(nonExistentId);
        verify(exceptionRepository, never()).save(any());
    }
}
