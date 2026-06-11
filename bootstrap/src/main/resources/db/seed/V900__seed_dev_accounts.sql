-- 非生产环境种子账户（仅 local/dev/test/docker 通过 spring.flyway.locations 加载本目录）
-- admin/admin123、employee/employee123 为公开弱口令，严禁在生产使用。
INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`)
VALUES ('admin', '$2b$10$d9d1rsMTb5H08rbbOMB1xeYv4TyGqds/noBBHpgkAvV2AJNB2DzEW', '系统管理员', 'ADMIN', 'ACTIVE');

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`)
VALUES ('employee', '$2b$10$LICm.1cUM63/Fp/6elh8ZuG3vYRgnbjiGoTDRi944KnNVRuMuoX8m', '李明', 'EMPLOYEE', 'ACTIVE');
