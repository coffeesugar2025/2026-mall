package com.policy.client;

import com.policy.client.fegin.BaseInterface;
import com.policy.client.result.Output;
import org.springframework.lang.NonNull;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020-12-29 15:51
 * @since 1.0.0
 */
public class DecisionTable extends Executor {


    public DecisionTable(PolicyProperties policyProperties, BaseInterface decisionTableInterface) {
        super(policyProperties, decisionTableInterface);
    }

    @Override
    public Output execute(@NonNull Object model) {
        return super.execute(model);
    }

}
