package Operimus.SLMS.business_layer.order_module.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class MultipartFileUtilsTest {

    @Test
    void createMultipartFile_shouldWrapBytesInMockMultipartFile() throws Exception {
        byte[] content = "hello".getBytes();

        MultipartFile multipartFile = MultipartFileUtils.createMultipartFile(content, "test.png");

        assertThat(multipartFile.getOriginalFilename()).isEqualTo("test.png");
        assertThat(multipartFile.getName()).isEqualTo("file");
        assertThat(multipartFile.getContentType()).isEqualTo("image/png");
        assertThat(multipartFile.isEmpty()).isFalse();
        assertThat(multipartFile.getBytes()).isEqualTo(content);
    }
}
