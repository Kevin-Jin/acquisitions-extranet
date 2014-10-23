CREATE TABLE `organizations` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`id`)
);
INSERT INTO `organizations` (`id`, `name`) VALUES
	('-1', 'INDEPENDENT_BUYER'),
	('-2', 'INDEPENDENT_SELLER')
;

CREATE TABLE `accounts` (
	`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
	`organization` INT(11) NOT NULL,
	`email` VARCHAR(255) NOT NULL,
	`password` CHAR(40) NOT NULL,
	PRIMARY KEY (`id`),
	KEY (`organization`),
	KEY (`email`),
	CONSTRAINT FOREIGN KEY (`organization`) REFERENCES `organizations` (`id`) ON DELETE RESTRICT
);

CREATE TABLE `persistentlogin` (
  `uniqueid` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `accountid` INT(10) UNSIGNED NOT NULL,
  `tokenhash` CHAR(40) NOT NULL,
  `expiretime` BIGINT(20),
  PRIMARY KEY (`uniqueid`),
  KEY (`accountid`),
  CONSTRAINT FOREIGN KEY (`accountid`) REFERENCES `accounts` (`id`) ON DELETE CASCADE
);

CREATE TABLE `listings` (
	`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
	`owner` INT(10) UNSIGNED NOT NULL,
	`title` VARCHAR(255) NOT NULL,
	`summary` TEXT,
	PRIMARY KEY (`id`),
	KEY (`owner`),
	CONSTRAINT FOREIGN KEY (`owner`) REFERENCES `accounts` (`id`) ON DELETE RESTRICT
);

CREATE TABLE `listinginfo` (
	`id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
	`listing` INT(10) UNSIGNED NOT NULL,
	`privacylevel` TINYINT(4) NOT NULL,
	`field` VARCHAR(255) NOT NULL,
	`info` VARCHAR(255),
	PRIMARY KEY (`id`),
	KEY (`listing`),
	CONSTRAINT FOREIGN KEY (`listing`) REFERENCES `listings` (`id`) ON DELETE RESTRICT
);