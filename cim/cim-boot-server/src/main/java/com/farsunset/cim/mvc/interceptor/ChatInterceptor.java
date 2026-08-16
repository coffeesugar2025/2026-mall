package com.farsunset.cim.mvc.interceptor;

import com.farsunset.cim.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatInterceptor implements MessageInterceptor {
    @Autowired
    private ChatMessageMapper messageMapper;
    @Autowired
    private ChatSessionMapper sessionMapper;

    @Override
    public void intercept(Message message) {
        //过滤system系统消息
        if("system".equals(message.getSender())) return;

        //查询sessionId，根据收发双方uid查会话
        ChatSession session = sessionMapper.getByTwoUid(message.getSender(), message.getReceiver());
        if(session == null) return;

        ChatMessage record = new ChatMessage();
        record.setSessionId(session.getId());
        record.setSenderUid(message.getSender());
        record.setReceiverUid(message.getReceiver());
        record.setContent(message.getContent());
        record.setMsgType(message.getType());
        messageMapper.insert(record);

        //更新会话最后消息和时间
        sessionMapper.updateLastMsg(session.getId(),message.getContent(),LocalDateTime.now());
    }
}
