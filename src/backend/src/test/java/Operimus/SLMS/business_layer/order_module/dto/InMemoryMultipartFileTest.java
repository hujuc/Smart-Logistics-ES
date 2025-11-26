package Operimus.SLMS.business_layer.order_module.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class InMemoryMultipartFileTest {

    @Test
    void shouldExposeContentMetadataAndStream() throws IOException {
        byte[] bytes = "sample".getBytes(StandardCharsets.UTF_8);
        InMemoryMultipartFile file = new InMemoryMultipartFile("sample.png", bytes);

        assertThat(file.getName()).isEqualTo("sample.png");
        assertThat(file.getOriginalFilename()).isEqualTo("sample.png");
        assertThat(file.getContentType()).isEqualTo("image/png");
        assertThat(file.isEmpty()).isFalse();
        assertThat(file.getSize()).isEqualTo(bytes.length);
        assertThat(file.getBytes()).isEqualTo(bytes);
        assertThat(file.getInputStream().readAllBytes()).isEqualTo(bytes);
    }

    @Test
    void transferToShouldThrowUnsupportedOperation() {
        InMemoryMultipartFile file = new InMemoryMultipartFile("file.txt", new byte[0]);

        assertThrows(UnsupportedOperationException.class, () -> file.transferTo(new java.io.File("dest")));
    }
}
