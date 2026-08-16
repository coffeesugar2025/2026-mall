package com.customer.chat_server_boot.mapper;



import com.customer.chat_server_boot.entity.OfflineMsgDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OfflineMsgMapper {

    @Insert("INSERT INTO chat_offline_msg(msg_id,sender_uid,receiver_uid,content,msg_type,cmd,create_time) " +
            "VALUES(#{msgId},#{senderUid},#{receiverUid},#{content},#{msgType},#{cmd},NOW())")
    void insertOffline(OfflineMsgDTO dto);

    @Select("SELECT msg_id msgId,sender_uid senderUid,receiver_uid receiverUid,content,msg_type msgType,cmd,create_time createTime " +
            "FROM chat_offline_msg WHERE receiver_uid = #{receiverUid}")
    List<OfflineMsgDTO> listOfflineMsg(String receiverUid);

    @Delete("DELETE FROM chat_offline_msg WHERE receiver_uid = #{receiverUid}")
    void cleanOfflineMsg(String receiverUid);
}