package org.example.aispingboot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    public ChatMemory chatMemory() {  //ai的对话记忆管理，用于记录当前会话的上下文
        return MessageWindowChatMemory.builder()
                .maxMessages(30) // 保留最新30条消息
                .build();//创建MessageWindowChatMemory对象，用于记录当前会话的上下文,放入IOC容器
    }

    @Bean("open-ai")
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {//OpenAiChatModel是 Spring AI 提供的模型封装类,是AI模型的配置类，用于配置OpenAI API的调用
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory()).build())
                .defaultSystem("你是一个专业的心理疏导师，温和耐心，善于倾听，能够提供专业的心理支持和建议").build();
    }//基础配置，用于创建ChatClient对象，用于与OpenAI API交互
}
