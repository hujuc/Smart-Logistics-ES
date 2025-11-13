package Operimus.SLMS.business_layer.rate_carrier_module.service;

import Operimus.SLMS.business_layer.rate_carrier_module.dto.AddressValidationRequest;
import Operimus.SLMS.business_layer.rate_carrier_module.dto.AddressValidationResponse;
import Operimus.SLMS.business_layer.rate_carrier_module.integration.ShippoApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressValidationServiceTest {

    @Mock
    private ShippoApiClient shippoApiClient;

    @InjectMocks
    private AddressValidationService addressValidationService;

    private AddressValidationRequest validRequest;
    private AddressValidationResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = AddressValidationRequest.builder()
                .addressLine1("123 Main Street")
                .city("Aveiro")
                .stateOrProvince("Aveiro")
                .postalCode("3810-123")
                .country("PT")
                .build();

        validResponse = AddressValidationResponse.builder()
                .valid(true)
                .build();
    }

    // ========================================
    // Valid Address Tests
    // ========================================

    @Test
    void whenValidateValidAddress_thenReturnValidResponse() {
        when(shippoApiClient.validateAddress(any(AddressValidationRequest.class)))
                .thenReturn(validResponse);

        AddressValidationResponse result = addressValidationService.validate(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        verify(shippoApiClient).validateAddress(any(AddressValidationRequest.class));
    }

    // ========================================
    // Invalid Address Tests
    // ========================================

    @Test
    void whenValidateInvalidAddress_thenReturnInvalidResponse() {
        AddressValidationRequest invalidRequest = AddressValidationRequest.builder()
                .addressLine1("Invalid Address")
                .city("Unknown")
                .postalCode("00000")
                .country("XX")
                .build();

        AddressValidationResponse invalidResponse = AddressValidationResponse.builder()
                .valid(false)
                .build();

        when(shippoApiClient.validateAddress(any(AddressValidationRequest.class)))
                .thenReturn(invalidResponse);

        AddressValidationResponse result = addressValidationService.validate(invalidRequest);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        verify(shippoApiClient).validateAddress(any(AddressValidationRequest.class));
    }

    // ========================================
    // International Address Tests
    // ========================================

    @Test
    void whenValidateInternationalAddress_thenReturnValidResponse() {
        AddressValidationRequest internationalRequest = AddressValidationRequest.builder()
                .addressLine1("1600 Amphitheatre Parkway")
                .city("Mountain View")
                .stateOrProvince("CA")
                .postalCode("94043")
                .country("US")
                .build();

        AddressValidationResponse internationalResponse = AddressValidationResponse.builder()
                .valid(true)
                .build();

        when(shippoApiClient.validateAddress(any(AddressValidationRequest.class)))
                .thenReturn(internationalResponse);

        AddressValidationResponse result = addressValidationService.validate(internationalRequest);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        verify(shippoApiClient).validateAddress(any(AddressValidationRequest.class));
    }

    // ========================================
    // Edge Cases Tests
    // ========================================

    @Test
    void whenValidateAddressWithOptionalFields_thenReturnValidResponse() {
        AddressValidationRequest requestWithOptional = AddressValidationRequest.builder()
                .addressLine1("456 Oak Avenue")
                .addressLine2("Apt 5B")
                .city("Porto")
                .stateOrProvince("Porto")
                .postalCode("4000-001")
                .country("PT")
                .build();

        AddressValidationResponse responseWithOptional = AddressValidationResponse.builder()
                .valid(true)
                .build();

        when(shippoApiClient.validateAddress(any(AddressValidationRequest.class)))
                .thenReturn(responseWithOptional);

        AddressValidationResponse result = addressValidationService.validate(requestWithOptional);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        verify(shippoApiClient).validateAddress(any(AddressValidationRequest.class));
    }

    @Test
    void whenValidateAddressWithWarnings_thenReturnResponseWithMessages() {
        AddressValidationResponse responseWithWarnings = AddressValidationResponse.builder()
                .valid(true)
                .build();

        when(shippoApiClient.validateAddress(any(AddressValidationRequest.class)))
                .thenReturn(responseWithWarnings);

        AddressValidationResponse result = addressValidationService.validate(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessages()).isNotNull();
        verify(shippoApiClient).validateAddress(any(AddressValidationRequest.class));
    }
}
