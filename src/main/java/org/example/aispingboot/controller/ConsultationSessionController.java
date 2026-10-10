package org.example.aispingboot.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.example.aispingboot.AiService.PsychologicalSupportService;
import org.example.aispingboot.AiService.StructOutPut;
import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.aispingboot.DTO.response.ConsultationSessionResponseDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.exception.BusinessException;
import org.example.aispingboot.repository.RedisChatMemoryRepository;
import org.example.aispingboot.service.ConsultationMessageService;
import org.example.aispingboot.service.ConsultationSessionService;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/psychological-chat")
public class ConsultationSessionController {

    @Autowired
    private PsychologicalSupportService psychologicalSupportService;  // 注入心理支持服务,交给AiService层处理

    @Autowired
    private ConsultationSessionService consultationSessionService;  // 注入会话服务,交给service层处理

    @Autowired
    private ConsultationMessageService consultationMessageService;  // 注入消息服务,交给service层处理

    @Autowired
    private RedisChatMemoryRepository redisChatMemoryRepository;//注入IOC容器中的chatMemory对象,用于记录当前会话的上下文

    @PostMapping("/session/start")//负责创建一次心理健康对话会话
    public Result<StructOutPut.StreamChatSession> startSession(@Valid @RequestBody ConsultationSessionCreateDTO createDTO) {
        // 获取当前用户
        String token = JwtTokenUtil.getCurrentToken();  // 当前请求的JWT令牌
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);  // 验证JWT令牌,获取解码后的JWT对象
        Long userId = jwt.getClaim("userId").asLong();  // 从JWT中获取用户ID，因为需要根据用户ID创建会话记录
        StructOutPut.StreamChatSession session = psychologicalSupportService.startSession(userId, createDTO);
        return Result.ok(session);
    }

    @GetMapping("/sessions")
    public Result<List<ConsultationSessionResponseDTO>> getSessions(){

        String token = JwtTokenUtil.getCurrentToken();

        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);

        Long userId = jwt.getClaim("userId").asLong();

        return Result.ok(consultationSessionService.getUserSessions(userId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageResponseDTO>> getMessages(
            @PathVariable String sessionId
    ){

        String token = JwtTokenUtil.getCurrentToken();

        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);

        Long userId = jwt.getClaim("userId").asLong();

        Long dbSessionId = psychologicalSupportService.extractSessionId(sessionId);


        ConsultationSession session = consultationSessionService.getSessionById(dbSessionId);


        if(session == null){

            throw new BusinessException("会话ID格式错误");
        }


        if(!session.getUserId().equals(userId)){

            throw new BusinessException(ResultCode.AUTHORIZED_ERROR.getCode(), "无权访问该会话");

        }


        return Result.ok(consultationMessageService.getMessagesBySessionId(dbSessionId));

    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<?> deleteSession(
            @PathVariable String sessionId
    ){

        // 获取当前用户
        String token = JwtTokenUtil.getCurrentToken();

        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);

        Long userId = jwt.getClaim("userId").asLong();

        Long dbSessionId = psychologicalSupportService.extractSessionId(sessionId);

        // 查询会话
        ConsultationSession session = consultationSessionService.getSessionById(dbSessionId);


        if(session == null){

            throw new BusinessException("会话不存在");

        }

        // 用户隔离校验
        if(!session.getUserId().equals(userId)){

            throw new BusinessException(ResultCode.AUTHORIZED_ERROR.getCode(), "无权删除该会话");

        }

        // 1.删除MySQL聊天记录
        consultationMessageService.deleteBySessionId(dbSessionId);

        // 2.删除MySQL会话
        consultationSessionService.deleteSession(dbSessionId);


        // 3.删除Redis AI记忆
        String conversationId = "user_" + userId + "_conversation_" + sessionId;

        redisChatMemoryRepository.deleteByConversationId(conversationId);


        return Result.ok();

    }



}
