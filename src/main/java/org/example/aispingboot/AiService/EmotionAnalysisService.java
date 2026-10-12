package org.example.aispingboot.AiService;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class EmotionAnalysisService {

    @Resource(name = "emotion-ai")
    private ChatClient chatClient;


    public EmotionAnalysisService(@Qualifier("emotion-ai")ChatClient chatClient){
        // 注入ChatClient,用于调用AI模型
        this.chatClient = chatClient;
    }



    public String analyze(String content){

        System.out.println(
                "==========开始情绪分析=========="
        );



        String result = chatClient
                        .prompt(PromptManage.EMOTION_ANALYSIS_PROMPT + content)
                        .call()
                        .content()
                        .trim();

        System.out.println(
                "情绪结果="
                        + result
        );

        return result;

    }

}