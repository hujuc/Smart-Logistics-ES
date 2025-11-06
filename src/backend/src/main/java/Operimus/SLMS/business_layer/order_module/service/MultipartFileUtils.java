package Operimus.SLMS.business_layer.order_module.service;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileUtils {
    public static MultipartFile createMultipartFile(byte[] content, String fileName) {
        return new MockMultipartFile("file", fileName, "image/png", content);
    }
}
