package org.example.aispingboot.AiService;

import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.service.ConsultationMessageService;
import org.example.aispingboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
public class PsychologicalSupportService {
    @Autowired
    @Qualifier("open-ai")//注入IOC容器中的open-ai ChatClient对象
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;//注入IOC容器中的chatMemory对象,用于记录当前会话的上下文

    @Autowired
    private ConsultationSessionService consultationSessionService;  // 注入咨询会话服务,交给AiService层处理

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 创建数据库会话记录，consultationSessionService.createSession(userId, createDTO)得到一个entity层的实体类ConsultationSession对象session
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, createDTO);

        // 将初始用户消息保存到Message表
        //consultationMessageService.saveUserMessage(consultationSession.getId(), createDTO.getInitialMessage(), null);

        // 创建会话信息
        String sessionId = "session_" + consultationSession.getId();//创建数据库会话，获得会话ID
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),  // 初始用户发送的第一条消息
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L, // 24小时
                1,
                "ACTIVE"
        );//创建会话后的返回信息给前端，前端保存会话id，后续请求时需要传递会话id，包含会话ID、用户ID、初始消息、开始时间、过期时间、消息数量、状态等
    }//创建一次咨询，并生成 sessionId 作为会话ID
    //Flux<String>代表一条持续的数据流，用于发布对话中的消息，包括用户消息和AI回复
    public Flux<String> streamPsychologicalChat(Long userId, String sessionId, String userMessage) {
        //Flux.create() 方法是 Reactor 的响应式编程,用于处理异步事件流，如对话中的用户消息和AI回复
        // 创建响应流
        return Flux.create(sink -> { //sink理解为一个数据发送通道
            // sink.next("数据1") // 发布数据
            // sink.complete(); // 完成流
            // sink.error(exception); // 发布错误
            Long dbSessionId = extractSessionId(sessionId);  // 从会话ID中提取数据库会话ID
            ConsultationSession session =
                    consultationSessionService.getSessionById(dbSessionId);
            if (session == null) {
                sink.error(new RuntimeException("会话ID格式错误"));
                return;
            }
            if (!session.getUserId().equals(userId)) {
                sink.error(new RuntimeException("会话ID与用户ID不匹配"));
                return;
            }
            // 是否为初始消息
            boolean isInitialMessage = false;  //防止重复保存初始消息
            // 检查是否为初始消息，避免重复保存
            //Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbSessionId);
            //if (messageCount == 1) {
            ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbSessionId);
            if (lastMessage != null && lastMessage.getSenderType() == 1 && userMessage.equals(lastMessage.getContent())) {
                    isInitialMessage = true;
                }
            //}
            if (!isInitialMessage) {
                // 保存用户消息到数据库
                consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
            }

            // 进行流式对话
            // 生成对话记忆管理
            String conversationId =
                    "user_" + userId + "_conversation_" + sessionId;  //不是给数据库用的，而是给AI用的，用于记录当前会话的上下文
            Prompt prompt = new Prompt(List.of(//new Prompt创建Prompt，是对象消息集合，发送给模型的提示结构
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
            ));//new SystemMessage是在设定AI人格时，用于指定AI的行为和特征，这里设定为心理健康助手，用于提供心理健康支持

            //用于存储AI完成的响应
            StringBuilder fullResponse = new StringBuilder();//累计完整ai回复，用于存储AI返回的完整内容,包括用户消息和AI回复

            // 使用chatClient发送消息到Open AI
            chatClient.prompt(prompt)//开始构造一次AI请求，并先加入系统提示词
                    .user(userMessage)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))//ChatMemory.CONVERSATION_ID是Spring AI的常量，用于指定会话ID的参数名,conversationId是实际的会话ID，用于记录当前会话的上下文
                    .stream()  //开始流式对话
                    .content()//Spring AI Stream返回的对象里面可能包含多个部分，这里只关注content部分
                    .doOnNext(Fragment -> {//Fragment是AI返回的内容片段
                        fullResponse.append(Fragment);//将AI返回的内容片段添加到fullResponse中
                        sink.next(Fragment);//将AI返回的内容片段发布到响应流中,使得前端可以实时收到AI回复的内容
                    })//组织一次 AI 请求，将用户消息和AI回复的内容片段发布到响应流中,使得前端可以实时收到AI回复的内容
                    .doOnComplete(() -> {
                        String completeRes = fullResponse.toString();
                        // 将AI返回的完整内容保存到数据库
                        consultationMessageService.saveAimessage(dbSessionId, completeRes, "openai");


                        sink.complete();  //完成流，通知订阅者数据发送完成,告诉前端数据发送完成
                    })
                    .doOnError(error -> {
                        sink.error(error);
                    })
                    .subscribe(); // 订阅并启动流
        });//返回一个Flux对象，用于发布AI回复的内容片段Flux<String>
    }

    // 获取参数中的sessionId
    public Long extractSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            String idStr = sessionId.substring("session_".length());
            return Long.parseLong(idStr);  //转换为Long类型
        }
        return null;
    }
}
