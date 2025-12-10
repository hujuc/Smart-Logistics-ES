package Operimus.SLMS.business_layer.chatbot_module.tools.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to use a tool they don't have permission for.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedToolAccessException extends RuntimeException {

    public UnauthorizedToolAccessException(String message) {
        super(message);
    }

    public UnauthorizedToolAccessException(String toolName, String role) {
        super("User with role '%s' is not authorized to use tool: %s".formatted(role, toolName));
    }

    public UnauthorizedToolAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
