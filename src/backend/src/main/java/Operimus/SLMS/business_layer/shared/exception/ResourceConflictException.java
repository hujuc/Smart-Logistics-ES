package Operimus.SLMS.business_layer.shared.exception;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@StandardException
public class ResourceConflictException extends RuntimeException {}
