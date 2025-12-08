package Operimus.SLMS.business_layer.chatbot_module.tools.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when tool execution fails unexpectedly.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ToolExecutionException extends RuntimeException {

    public ToolExecutionException(String message) {
        super(message);
    }

    public ToolExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
