package com.customer.chat_server_boot.manager;


import com.customer.chat_server_boot.mapper.ChatMsgMapper;
import com.customer.chat_server_boot.mapper.OfflineMsgMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketBeanInject {

    @Resource
    private ChatMsgMapper chatMsgMapper;
    @Resource
    private OfflineMsgMapper offlineMsgMapper;

    @PostConstruct
    public void setStaticMapper() {
        ChatWebSocketEndpoint.setChatMsgMapper(chatMsgMapper);
        ChatWebSocketEndpoint.setOfflineMsgMapper(offlineMsgMapper);
    }


    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}