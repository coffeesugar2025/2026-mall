package com.policy.web.vo.decisiontable;

import lombok.Data;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2021/1/1
 * @since 1.0.0
 */
@Data
public class GetDecisionTableResponse {

    private Integer id;

    private String name;

    private String code;

    private String description;

    private Integer workspaceId;

    private String workspaceCode;

    private Integer strategyType;

    /**
     * 决策表数据
     */
    private TableData tableData;

}
