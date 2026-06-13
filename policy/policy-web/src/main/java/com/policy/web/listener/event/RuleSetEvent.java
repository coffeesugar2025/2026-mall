package com.policy.web.listener.event;

import com.policy.web.listener.body.RuleSetMessageBody;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020-12-23 17:55
 * @since 1.0.0
 */
public class RuleSetEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1628296277627810450L;

    @Getter
    private final RuleSetMessageBody ruleSetMessageBody;

    public RuleSetEvent(RuleSetMessageBody ruleSetMessageBody) {
        super(ruleSetMessageBody);
        this.ruleSetMessageBody = ruleSetMessageBody;
    }

}
