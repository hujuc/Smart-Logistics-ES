package Operimus.SLMS.business_layer.order_shipment_module.dto;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InMemoryMultipartFile implements MultipartFile {

    private final String name;
    private final byte[] content;

    public InMemoryMultipartFile(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return "image/png"; // Ou qualquer outro tipo de imagem que corresponder
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("Transfer to file not supported in InMemoryMultipartFile");
    }
}
