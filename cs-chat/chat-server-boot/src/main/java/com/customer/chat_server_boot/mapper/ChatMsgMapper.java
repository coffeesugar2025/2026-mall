package com.customer.chat_server_boot.mapper;



import com.customer.chat_server_boot.entity.ChatMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMsgMapper {

    @Insert("INSERT INTO chat_msg(msg_id,sender_uid,receiver_uid,content,msg_type,cmd,create_time) " +
            "VALUES(#{msgId},#{senderUid},#{receiverUid},#{content},#{msgType},#{cmd},NOW())")
    int insertChatMsg(ChatMessage message);
}