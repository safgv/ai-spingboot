package org.example.aispingboot.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import org.example.aispingboot.DTO.cache.ChatMemoryMessageDTO;
import org.example.aispingboot.service.RedisService;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


@Repository
public class RedisChatMemoryRepository
        implements ChatMemoryRepository {


    private static final String KEY_PREFIX =
            "chat:memory:";


    @Resource
    private RedisService redisService;


    /**
     * 获取所有conversationId。
     *
     * 第一版暂时不使用这个方法，
     * 后面我们再用Redis Set优化。
     */
    @Override
    public List<String> findConversationIds() {

        return new ArrayList<>();
    }


    /**
     * 根据conversationId获取聊天记忆
     */
    @Override
    public List<Message> findByConversationId(
            String conversationId
    ){

        String key =
                KEY_PREFIX + conversationId;


        List<ChatMemoryMessageDTO> dtoList =
                redisService.get(
                        key,
                        new TypeReference<
                                List<ChatMemoryMessageDTO>
                                >() {}
                );


        if(dtoList == null){

            return new ArrayList<>();

        }


        List<Message> messages =
                new ArrayList<>();


        for(ChatMemoryMessageDTO dto : dtoList){

            switch(dto.getType()){

                case "USER":

                    messages.add(
                            new UserMessage(
                                    dto.getContent()
                            )
                    );

                    break;


                case "ASSISTANT":

                    messages.add(
                            new AssistantMessage(
                                    dto.getContent()
                            )
                    );

                    break;


                case "SYSTEM":

                    messages.add(
                            new SystemMessage(
                                    dto.getContent()
                            )
                    );

                    break;


                default:

                    break;
            }

        }


        return messages;
    }


    /**
     * 保存当前会话的全部ChatMemory
     */
    @Override
    public void saveAll(
            String conversationId,
            List<Message> messages
    ){

        String key =
                KEY_PREFIX + conversationId;


        List<ChatMemoryMessageDTO> dtoList =
                new ArrayList<>();


        for(Message message : messages){

            dtoList.add(
                    new ChatMemoryMessageDTO(
                            message.getMessageType().name(),
                            message.getText()
                    )
            );

        }


        // 24小时过期
        redisService.set(
                key,
                dtoList,
                24 * 60
        );
    }


    /**
     * 删除某个会话的AI记忆
     */
    @Override
    public void deleteByConversationId(
            String conversationId
    ){

        redisService.delete(
                KEY_PREFIX + conversationId
        );

    }

}