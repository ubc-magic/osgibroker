---- The database is now created and updated from within the code
---- CREATE DATABASE IF NOT EXISTS OSGiBroker;
---- USE OSGiBroker;

---
--- The table storing the set of topics for OSGiBroker
---
DROP TABLE IF EXISTS `topics`;
CREATE TABLE  `topics` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT 'Channel Name',
  PRIMARY KEY (`name`),
  KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

---- The following line adds a fake topic to the table in order to enable validationQuery to
---- act normally when it comes to validating communication with the DataBase
INSERT INTO `topics` (`name`) VALUES ('test');


---
--- The table for storing received events in OSGiBroker
---
DROP TABLE IF EXISTS `eventlogs`;
CREATE TABLE  `eventlogs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic_id` int(11) DEFAULT NULL COMMENT 'topic the events are sent under',
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `server_time` BIGINT NOT NULL COMMENT 'Java System.currentTimeMillis() when event received',
  `client_time` BIGINT COMMENT 'Client timestamp on the time the event is generated',
  PRIMARY KEY (`id`),
  KEY `topics` (`topic_id`),
  KEY (`server_time`),
  KEY (`client_time`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

---
--- The table for storing received events in OSGiBroker
---
DROP TABLE IF EXISTS `events`;
CREATE TABLE `events` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`event_id` int(11) NOT NULL,
	`name` varchar(50) NOT NULL,
	`value` varchar(500) NOT NULL,
	PRIMARY KEY (`id`, `event_id`),
	KEY (`event_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

---
--- The table for subscribers 
---
DROP TABLE IF EXISTS `subscriptions`;
CREATE TABLE  `subscriptions` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(256) NOT NULL COMMENT 'name of subscriber for event delivery',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 COMMENT='subscriptions are links from topics to subscribers';

---
--- The table for clients 
---
DROP TABLE IF EXISTS `clients`;
CREATE TABLE  `clients` (
  `id` int(11) NOT NULL auto_increment,
  `topic_id` int(11) NOT NULL,
  `subscription_id` int (11) DEFAULT NULL COMMENT 'the subscription sending these',
  `type` varchar(256) NOT NULL COMMENT 'type of subscriber: topic, direct or polling',
  `name` varchar(256) NOT NULL COMMENT 'name of topic used to reference it (globally unique)',
  `access` varchar(256) DEFAULT NULL COMMENT 'topic or URL (depending on type) used to access subscriber',
  `expire_time` time DEFAULT NULL COMMENT 'time that subscriber can be removed if not used (lease)',
  PRIMARY KEY  (`topic_id`, `name`),
  KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 COMMENT='endpoints for events';

---
--- The table for cleint properties
---
DROP TABLE IF EXISTS `clientprops`;
CREATE TABLE `clientprops`(
	`id` int(11) NOT NULL auto_increment,
	`client_id` int(11) NOT NULL COMMENT 'the id for the client with these properties',
	`prop_name` varchar(256) NOT NULL COMMENT 'the name for the property the client holds',
	`prop_value` varchar(256) NOT NULL COMMENT 'the value for the property the client holds',
	PRIMARY KEY (`id`)
)ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 COMMENT='properties for the clients';

---
--- The table for state attributes
---

DROP TABLE IF EXISTS `state_attributes`;
CREATE TABLE  `state_attributes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attr_index` int(11) DEFAULT 0 COMMENT 'index of attribute if multiple with same name',
  `name` varchar(255) NOT NULL,
  `type` varchar(20) DEFAULT NULL,
  `value` varchar(1024) DEFAULT NULL,
  `topic_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `topics` (`topic_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

---
--- The table for content attributes
---

DROP TABLE IF EXISTS `content_attributes`;
CREATE TABLE  `content_attributes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content_id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `value` varchar(1000) DEFAULT NULL,
  `topic_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE (`content_id`, `name`),
  KEY `topics` (`topic_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
