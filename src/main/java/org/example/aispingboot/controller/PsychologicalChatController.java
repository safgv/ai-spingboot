package org.example.aispingboot.controller;

import cn.hutool.json.JSONUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import org.example.aispingboot.AiService.PsychologicalSupportService;
import org.example.aispingboot.DTO.command.ConsultationStreamDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/psychological-chat")
public class PsychologicalChatController {
    @Autowired
    private PsychologicalSupportService psychologicalSupportService;  // 注入心理健康支持服务,交给AiService层处理


    //告诉前端不是返回的数据类型不是普通 JSON,而是SSE事件流式事件,需要前端特殊处理,接口会不断向客户端发送事件,包含用户消息和AI回复的消息
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ConsultationStreamDTO streamDTO) {
        // 获取当前用户
        String token = JwtTokenUtil.getCurrentToken();  // 当前请求的JWT令牌
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);  // 验证JWT令牌,获取解码后的JWT对象
               Long userId = jwt.getClaim("userId").asLong();  // 从JWT中获取用户ID，因为需要根据用户ID创建会话记录

        if (userId == null) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")//错误也必须包装成 SSE事件,通知前端用户未登录,不能进行心理健康对话
                    .data(JSONUtil.toJsonStr(Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), "用户未登录")))
                    .build());
        }//返回流式事件，连续数据,通知前端用户未登录,不能进行心理健康对话

        // 开始流式对话
        return psychologicalSupportService.streamPsychologicalChat(userId, streamDTO.getSessionId(), streamDTO.getUserMessage())//返回心理健康对话流式事件，Flux<String>
                .map(Fragment -> {
                    return ServerSentEvent.<String>builder()//springboot提供的SSE事件构建器
                            .event("message")//事件类型,用于标识这是用户消息还是AI回复,前端可以监听
                            .data(JSONUtil.toJsonStr(Result.ok(Map.of("content", Fragment, "type", "normal"))))//ai回复的消息,包含内容和类型,类型为normal，转换为JSON字符串
                            .build();//转换为SSE事件,包含用户消息和AI回复的消息
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()//ai结束,通知前端对话结束
                ))
                .onErrorResume(error ->
                        Flux.just(
                                ServerSentEvent.<String>builder()
                                        .event("error")
                                        .data(JSONUtil.toJsonStr(Result.error(ResultCode.SYSTEM_ERROR.getCode(), "AI服务异常", null)))
                                        .build()
                        )
                )
                .delayElements(Duration.ofMillis(50)); // 添加延迟确保流式数据的体验
    }//返回心理健康对话流式事件，Flux<ServerSentEvent<String>>

}
