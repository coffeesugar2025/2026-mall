package com.policy.web.listener.event;

import com.policy.web.listener.body.GeneralRuleMessageBody;
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
public class GeneralRuleEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1628296277627810450L;

    @Getter
    private final GeneralRuleMessageBody ruleMessageBody;

    public GeneralRuleEvent(GeneralRuleMessageBody ruleMessageBody) {
        super(ruleMessageBody);
        this.ruleMessageBody = ruleMessageBody;
    }

}
