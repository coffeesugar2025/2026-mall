package com.policy.web.groovy.bean;

import lombok.Data;

import java.util.Date;

/**
 * script DTO
 */
@Data
public class StrategyScriptEntity {
  /**
   * 策略id
   */
  private Integer strategyId;

  /**
   * 脚本内容
   */
  private String script;

  /**
   * 变量名称，类型json
   */
  private String variables;

  /**
   * 作者
   */
  private String author;

  /**
   * 创建时间
   */
  private Date createTime;

  /**
   * 更新时间
   */
  private Date updateTime;

}