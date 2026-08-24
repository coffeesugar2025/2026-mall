# 运维排查指南

## DB 连接超时
当服务出现大量 DB 连接超时错误时，排查步骤：
1. 检查数据库实例 CPU/内存/连接数是否打满
2. 检查应用连接池（HikariCP）配置：maximumPoolSize 是否合理
3. 检查是否存在慢 SQL 导致连接长时间占用
4. 检查网络连通性与安全组策略

## 连接池耗尽
连接池耗尽的典型表现：应用日志出现 `HikariPool-1 - Connection is not available`
处置建议：
- 临时扩容 maximumPoolSize
- 优化慢 SQL，缩短连接占用时间
- 增加连接获取超时告警
