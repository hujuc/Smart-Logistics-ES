package Operimus.SLMS.business_layer.chatbot_module.config;

import Operimus.SLMS.business_layer.chatbot_module.integration.gemini.GeminiProperties;
import Operimus.SLMS.business_layer.chatbot_module.integration.pinecone.PineconeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Chatbot Module.
 * Enables configuration properties for Gemini and Pinecone integrations.
 */
@Configuration
@EnableConfigurationProperties({
        GeminiProperties.class,
        PineconeProperties.class
})
public class ChatbotModuleConfig {
}
