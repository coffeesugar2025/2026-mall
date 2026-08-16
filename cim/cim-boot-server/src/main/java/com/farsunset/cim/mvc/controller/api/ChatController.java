package com.farsunset.cim.mvc.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private CimSender cimSender;
    @Autowired
    private ChatSessionMapper sessionMapper;
    @Autowired
    private ChatMessageMapper messageMapper;

    /**
     * 会员发起咨询，分配客服，创建会话
     */
    @PostMapping("/createSession")
    public R<ChatSession> createSession(@RequestParam String memberUid){
        //简单逻辑：这里可以做客服负载均衡，这里写死kefu_001
        String kefuUid = "kefu_001";
        ChatSession session = sessionMapper.getByMember(memberUid);
        if(session == null){
            session = new ChatSession();
            session.setMemberUid(memberUid);
            session.setKefuUid(kefuUid);
            session.setStatus(1);
            sessionMapper.insert(session);
        }
        return R.success(session);
    }

    /**
     * 获取会员自己的会话（会员端调用）
     */
    @GetMapping("/member/session")
    public R<ChatSession> getMemberSession(@RequestParam String memberUid){
        return R.success(sessionMapper.getByMember(memberUid));
    }

    /**
     * 客服获取全部会话列表（客服工作台调用）
     */
    @GetMapping("/kefu/sessionList")
    public R<List<ChatSession>> getKefuSessionList(@RequestParam String kefuUid){
        return R.success(sessionMapper.listByKefu(kefuUid));
    }

    /**
     * 获取会话历史消息
     */
    @GetMapping("/history")
    public R<List<ChatMessage>> getHistory(@RequestParam Long sessionId){
        return R.success(messageMapper.listBySessionId(sessionId));
    }

    /**
     * 查询用户是否在线
     */
    @GetMapping("/online/{uid}")
    public R<Boolean> isOnline(@PathVariable String uid){
        return R.success(CimSessionManager.getInstance().isOnline(uid));
    }

    /**
     * 保存消息，CIM拦截器也可以做，这里提供http兜底
     */
    @PostMapping("/saveMsg")
    public R<?> saveMsg(@RequestBody ChatMessage msg){
        messageMapper.insert(msg);
        return R.success();
    }

    /**
     * 推送离线系统提示
     */
    @PostMapping("/sendOfflineTip")
    public R<?> sendOfflineTip(@RequestParam String receiverUid){
        Message cimMsg = new Message();
        cimMsg.setSender("system");
        cimMsg.setReceiver(receiverUid);
        cimMsg.setContent("很抱歉，客服目前不在线");
        cimMsg.setType(1);
        cimSender.send(cimMsg);
        return R.success();
    }
}
