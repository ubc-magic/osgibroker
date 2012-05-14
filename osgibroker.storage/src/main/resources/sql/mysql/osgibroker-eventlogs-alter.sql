CREATE DATABASE IF NOT EXISTS OSGiBroker;
USE OSGiBroker;

ALTER TABLE eventlogs
    CHANGE COLUMN time server_time BIGINT NOT NULL COMMENT 'Java System.currentTimeMillis() when event received', 
    ADD COLUMN `client_time` BIGINT COMMENT 'Client timestamp on the time the event is generated',
    ADD  INDEX (`server_time`),
    ADD  INDEX (`client_time`);
    
ALTER TABLE `OSGiBroker`.`clients`
	DROP PRIMARY KEY,
	ADD  PRIMARY KEY (`topic_id`, `name`),
	ADD  KEY (`id`);