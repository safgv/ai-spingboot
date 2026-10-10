package org.example.aispingboot.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.aispingboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.aispingboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.aispingboot.DTO.response.ConsultationSessionResponseDTO;
import org.example.aispingboot.entity.ConsultationSession;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.mapper.ConsultationSessionMapper;
import org.example.aispingboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 验证用户是否存在
        User user =userMapper.selectById(userId);
        if (user != null) {
            // 创建会话记录，对应数据库中的ConsultationSession表，是entity层的实体类
             ConsultationSession session = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(createDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            // 如果未提供标题
            if (StrUtil.isBlank(createDTO.getSessionTitle())) {
                session.setSessionTitle(String.format("AI助手 - " + DateUtil.format(LocalDateTime.now(), "MM-dd HH:mm")));
            }

            // 插入记录
            consultationSessionMapper.insert(session);
            return session;
        }

        return null;
    }

    /**
     * 根据会话ID查询会话
     */
    public ConsultationSession getSessionById(Long sessionId){

        return consultationSessionMapper.selectById(sessionId);

    }

    /**
     * 删除会话
     */
    public void deleteSession(Long sessionId){

        consultationSessionMapper.deleteById(sessionId);

    }

    /**
     * 查询用户所有会话
     */
    public List<ConsultationSessionResponseDTO> getUserSessions(Long userId){


        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(ConsultationSession::getUserId, userId);

        wrapper.orderByDesc(ConsultationSession::getStartedAt);


        List<ConsultationSession> sessions = consultationSessionMapper.selectList(wrapper);

        List<ConsultationSessionResponseDTO> result = new ArrayList<>();

        for(ConsultationSession session : sessions){

            ConsultationSessionResponseDTO dto = new ConsultationSessionResponseDTO();

            dto.setSessionId("session_" + session.getId());

            dto.setSessionTitle(session.getSessionTitle());

            dto.setStartedAt(session.getStartedAt());

            dto.setMessageCount(
                    consultationMessageService
                            .getMessageCountBySessionId(session.getId())
            );

            ConsultationMessageResponseDTO last =
                    consultationMessageService
                            .getLastMessageBySessionId(session.getId());


            if(last != null){
                dto.setLastMessage(last.getContent());

            }

            result.add(dto);
        }

        return result;
    }
}
