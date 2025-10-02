package Operimus.SLMS.presentation_layer.admin_console_bff;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    //apenas para testar se o spring boot ta a funcionar
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
}
