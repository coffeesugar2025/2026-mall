package com.policy.client;

import com.policy.client.fegin.GeneralRuleInterface;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2020-12-29 16:15
 * @since 1.0.0
 */
public class GeneralRule extends Executor {

    public GeneralRule(PolicyProperties policyProperties, GeneralRuleInterface generalRuleInterface) {
        super(policyProperties, generalRuleInterface);
    }

}
