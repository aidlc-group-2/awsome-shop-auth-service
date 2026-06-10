-- 为用户表新增企业邮箱字段（FR-A1/FR-A2 注册与邮箱白名单）
ALTER TABLE `user`
    ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '企业邮箱' AFTER `username`;

-- 邮箱唯一索引（与逻辑删除联合，允许删除后同邮箱重新注册）
ALTER TABLE `user`
    ADD UNIQUE INDEX `uk_email` (`email`, `deleted`);

-- 为已有种子账户补充邮箱
UPDATE `user` SET `email` = 'admin@amazon.com' WHERE `username` = 'admin' AND `email` IS NULL;
UPDATE `user` SET `email` = 'employee@amazon.com' WHERE `username` = 'employee' AND `email` IS NULL;
