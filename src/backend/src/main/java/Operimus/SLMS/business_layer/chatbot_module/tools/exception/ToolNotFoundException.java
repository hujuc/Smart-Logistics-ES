package Operimus.SLMS.business_layer.chatbot_module.tools.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested tool cannot be found in the registry.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ToolNotFoundException extends RuntimeException {

    public ToolNotFoundException(String toolName) {
        super("Tool not found: " + toolName);
    }

    public ToolNotFoundException(String toolName, Throwable cause) {
        super("Tool not found: " + toolName, cause);
    }
}
