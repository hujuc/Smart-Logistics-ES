package Operimus.SLMS.presentation_layer.groq;

import Operimus.SLMS.presentation_layer.groq.dto.GroqPromptRequest;
import Operimus.SLMS.presentation_layer.groq.dto.GroqResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groq")
public class GroqController {

    private final GroqService groqService;

    public GroqController(GroqService groqService) {
        this.groqService = groqService;
    }

    @PostMapping("/test")
    public GroqResponseDto generateResponse(@Valid @RequestBody GroqPromptRequest request) {
        return groqService.generateCompletion(request.prompt());
    }
}
