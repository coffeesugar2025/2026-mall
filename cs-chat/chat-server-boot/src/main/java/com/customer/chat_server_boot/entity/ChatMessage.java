package com.customer.chat_server_boot.entity;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    /** 全局唯一消息id */
    private String msgId;
    /** 发送人uid */
    private String senderUid;
    /** 接收人uid */
    private String receiverUid;
    /** 消息内容 */
    private String content;
    /** 1文本 2图片 */
    private Integer msgType;
    /** 指令：99 ping，100 pong，1普通聊天 */
    private Integer cmd;
    /** 时间戳 */
    private Long timestamp;
}