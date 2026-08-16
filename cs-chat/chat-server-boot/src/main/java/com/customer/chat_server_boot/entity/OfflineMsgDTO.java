package com.customer.chat_server_boot.entity;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OfflineMsgDTO {
    private String msgId;
    private String senderUid;
    private String receiverUid;
    private String content;
    private Integer msgType;
    private Integer cmd;
    private LocalDateTime createTime;
}