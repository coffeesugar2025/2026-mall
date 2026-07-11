/**
 * Copyright (c) 2020  (761945125@qq.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.policy.web.listener;

import com.policy.web.store.entity.PolicySystemLog;
import com.policy.web.store.entity.PolicySystemLog;
import com.policy.web.store.manager.PolicySystemLogManager;
import com.policy.web.store.manager.PolicySystemLogManager;
import com.policy.common.config.rabbit.RabbitQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2019/11/1
 * @since 1.0.0
 */
@Slf4j
@Component
public class SystemLogMessageListener {

    @Resource
    private PolicySystemLogManager policySystemLogManager;

    /**
     * 接收日志消息
     *
     * @param policySystemLog 日志内容
     */
    @RabbitListener(queues = RabbitQueueConfig.SYSTEM_LOG_QUEUE)
    public void message(PolicySystemLog policySystemLog) {
        log.info("接收到日志消息,准备存入数据库!");
        this.policySystemLogManager.save(policySystemLog);
    }

}
