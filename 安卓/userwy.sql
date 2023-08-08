-- --------------------------------------------------------
-- 主机:                           39.100.133.144
-- 服务器版本:                        8.0.21 - Source distribution
-- 服务器操作系统:                      Linux
-- HeidiSQL 版本:                  11.3.0.6295
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 user144 的数据库结构
CREATE DATABASE IF NOT EXISTS `user144` /*!40100 DEFAULT CHARACTER SET latin1 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `user144`;

-- 导出  表 user144.t_dapp 结构
CREATE TABLE IF NOT EXISTS `t_dapp` (
                                        `id` int NOT NULL AUTO_INCREMENT,
                                        `dapp_id` varchar(200) NOT NULL DEFAULT '0',
    `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0',
    `dapp_info` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `state` tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `dapp_id` (`dapp_id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

-- 数据导出被取消选择。

-- 导出  表 user144.t_server 结构
CREATE TABLE IF NOT EXISTS `t_server` (
                                          `id` int NOT NULL AUTO_INCREMENT,
                                          `icon_url` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `service_name` char(255) CHARACTER SET gbk COLLATE gbk_chinese_ci DEFAULT NULL,
    `url` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `service_remark` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `app_remark` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `app_introduction` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

-- 数据导出被取消选择。

-- 导出  表 user144.t_user 结构
CREATE TABLE IF NOT EXISTS `t_user` (
                                        `id` int NOT NULL AUTO_INCREMENT,
                                        `username` varchar(255) NOT NULL,
    `password` varchar(255) DEFAULT NULL,
    `im_username` varchar(255) DEFAULT NULL,
    `im_pwd` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
    `im_id` varchar(255) DEFAULT NULL,
    `im_node` varchar(255) DEFAULT NULL,
    `nick_name` varchar(255) DEFAULT NULL,
    `state` tinyint DEFAULT NULL,
    `type` tinyint DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`)
    ) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=latin1;

-- 数据导出被取消选择。

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
