package com.customer.chat_server_boot.manager;


import com.alibaba.fastjson2.JSON;
import com.customer.chat_server_boot.entity.ChatMessage;
import com.customer.chat_server_boot.entity.OfflineMsgDTO;
import com.customer.chat_server_boot.mapper.ChatMsgMapper;
import com.customer.chat_server_boot.mapper.OfflineMsgMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/ws")
@Component
public class ChatWebSocketEndpoint {

    private static final Map<String, Session> ONLINE_SESSION_MAP = new ConcurrentHashMap<>();

    private static ChatMsgMapper chatMsgMapper;
    private static OfflineMsgMapper offlineMsgMapper;

    // set注入，解决@ServerEndpoint无法直接@Autowired
    public static void setChatMsgMapper(ChatMsgMapper mapper) {
        chatMsgMapper = mapper;
    }

    public static void setOfflineMsgMapper(OfflineMsgMapper mapper) {
        offlineMsgMapper = mapper;
    }

    private String currentUid;

    @OnOpen
    public void onOpen(Session session) {
        Map<String, List<String>> paramMap = session.getRequestParameterMap();
        List<String> uidArr = paramMap.get("uid");
        if (uidArr == null || uidArr.size() == 0) {
            try {
                session.close();
            } catch (IOException e) {
            }
            log.warn("ws连接拒绝：缺少uid参数");
            return;
        }
        currentUid = uidArr.get(0);

        // 重复登录：踢掉旧连接
        Session oldSession = ONLINE_SESSION_MAP.get(currentUid);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                //oldSession.close();
                oldSession = session;
                log.info("uid={} 旧连接被踢下线", currentUid);
            } catch (Exception e) {
                log.error("关闭旧连接异常", e);
            }
        }else {
            ONLINE_SESSION_MAP.put(currentUid, session);
        }
        log.info("ws连接建立 uid={},在线数={}", currentUid, ONLINE_SESSION_MAP.size());

        // 上线推送离线消息
        pushOfflineMsg(session, currentUid);
    }

    /**
     * 上线推送离线消息
     */
    private void pushOfflineMsg(Session session, String uid) {
        var offlineList = offlineMsgMapper.listOfflineMsg(uid);
        if (offlineList == null || offlineList.isEmpty()) {
            return;
        }
        for (OfflineMsgDTO dto : offlineList) {
            ChatMessage msg = new ChatMessage();
            msg.setMsgId(dto.getMsgId());
            msg.setSenderUid(dto.getSenderUid());
            msg.setReceiverUid(dto.getReceiverUid());
            msg.setContent(dto.getContent());
            msg.setMsgType(dto.getMsgType());
            msg.setCmd(dto.getCmd());
            msg.setTimestamp(System.currentTimeMillis());
            sendAsync(session, JSON.toJSONString(msg));
        }
        offlineMsgMapper.cleanOfflineMsg(uid);
        log.info("uid={} 推送离线消息 {}条", uid, offlineList.size());
    }

    @OnMessage
    public void onMessage(String rawJson, Session session) {
        log.info("收到消息 raw={}", rawJson);
        ChatMessage msg;
        try {
            msg = JSON.parseObject(rawJson, ChatMessage.class);
        } catch (Exception e) {
            log.error("消息json解析失败", e);
            return;
        }

        // 心跳处理 ping=99 返回pong=100
        if (msg.getCmd() != null) {
            if (msg.getCmd() == 99) {
                ChatMessage pong = new ChatMessage();
                pong.setCmd(100);
                pong.setTimestamp(System.currentTimeMillis());
                sendAsync(session, JSON.toJSONString(pong));
                return;
            }
            if (msg.getCmd() == 100) {
                return;
            }
        }

        // 参数校验
        if (msg.getSenderUid() == null || msg.getSenderUid().isBlank()) {
            log.warn("senderUid为空，丢弃消息");
            return;
        }
        if (msg.getReceiverUid() == null || msg.getReceiverUid().isBlank()) {
            log.warn("receiverUid为空，丢弃消息");
            return;
        }
        if (msg.getContent() != null && msg.getContent().length() > 2000) {
            log.warn("消息内容超长，丢弃消息");
            return;
        }

        // 填充消息元数据
        String msgId = UUID.randomUUID().toString().replace("-", "");
        msg.setMsgId(msgId);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setCmd(1);

        // 消息入库
        chatMsgMapper.insertChatMsg(msg);

        Session receiverSession = ONLINE_SESSION_MAP.get(msg.getReceiverUid());
        String outJson = JSON.toJSONString(msg);
        if (receiverSession != null && receiverSession.isOpen()) {
            // 接收方在线：异步发送
            sendAsync(receiverSession, outJson);
            log.info("消息转发 {} → {} msgId={}", msg.getSenderUid(), msg.getReceiverUid(), msgId);
        } else {
            // 接收方不在线：存入离线消息
            OfflineMsgDTO offline = new OfflineMsgDTO();
            offline.setMsgId(msg.getMsgId());
            offline.setSenderUid(msg.getSenderUid());
            offline.setReceiverUid(msg.getReceiverUid());
            offline.setContent(msg.getContent());
            offline.setMsgType(msg.getMsgType());
            offline.setCmd(msg.getCmd());
            offlineMsgMapper.insertOffline(offline);
            log.info("接收方{}不在线，存入离线消息 msgId={}", msg.getReceiverUid(), msgId);
        }
    }

    /**
     * 异步发送，规避getBasicRemote线程不安全
     */
    private void sendAsync(Session session, String json) {
        if (!session.isOpen()) {
            return;
        }
        session.getAsyncRemote().sendText(json, result -> {
            if (!result.isOK()) {
                log.error("消息发送失败", result.getException());
            }
        });
    }

    @OnClose
    public void onClose() {
        if (currentUid != null) {
            ONLINE_SESSION_MAP.remove(currentUid);
        }
        log.info("ws断开 uid={},在线数={}", currentUid, ONLINE_SESSION_MAP.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("ws异常 uid={}", currentUid, throwable);
    }
}