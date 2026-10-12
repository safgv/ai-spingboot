package org.example.aispingboot.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aispingboot.AiService.PromptManage;
import org.example.aispingboot.entity.ConsultationMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmotionSummaryService {

    @Autowired
    private ConsultationMessageService consultationMessageService;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Resource(name="emotion-ai")
    private ChatClient chatClient;

    /**
     * 更新会话情绪总结，异步执行
     */
    @Async
    public void updateEmotionSummary(Long sessionId){

        try {

            log.info(
                    "开始异步更新会话情绪总结,sessionId={}",
                    sessionId
            );


            List<ConsultationMessage> messages = consultationMessageService.getUserMessagesBySessionId(sessionId);


            // 没有用户消息，不需要总结
            if(messages.isEmpty()){

                log.info(
                        "会话{}没有用户消息,无需更新情绪总结",
                        sessionId
                );

                return;

            }


            StringBuilder content = new StringBuilder();


            for(ConsultationMessage message : messages){

                content.append(message.getContent());
                content.append("\n");

            }


            String summary = chatClient
                            .prompt(PromptManage.EMOTION_SUMMARY_PROMPT + content.toString())
                            .call()
                            .content();


            consultationSessionService.updateEmotionSummary(sessionId, summary);


            log.info(
                    "会话情绪总结更新完成,sessionId={}",
                    sessionId
            );


        }catch(Exception e){


            log.error(
                    "情绪总结失败,sessionId={}",
                    sessionId,
                    e
            );


        }

    }

}
