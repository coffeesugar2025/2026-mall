package com.customer.chat_server_boot.entity;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSession {
    private Long id;
    private String memberUid;
    private String kefuUid;
    private Integer status;
    private String lastMsg;
    private LocalDateTime lastTime;
    private LocalDateTime createTime;
}
