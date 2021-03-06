-- MySQL dump 10.11
--
-- Host: localhost    Database: afunms
-- ------------------------------------------------------
-- Server version	5.0.41-community-nt

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `allutilhdxday101014`
--

DROP TABLE IF EXISTS `allutilhdxday101014`;
CREATE TABLE `allutilhdxday101014` (
  `ID` bigint(20) NOT NULL auto_increment,
  `IPADDRESS` varchar(30) default NULL,
  `RESTYPE` varchar(20) default NULL,
  `CATEGORY` varchar(50) default NULL,
  `ENTITY` varchar(100) default NULL,
  `SUBENTITY` varchar(60) default NULL,
  `THEVALUE` varchar(255) default NULL,
  `COLLECTTIME` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `UNIT` varchar(30) default NULL,
  `COUNT` bigint(20) default NULL,
  `BAK` varchar(100) default NULL,
  `CHNAME` varchar(100) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=gb2312;

--
-- Dumping data for table `allutilhdxday101014`
--

LOCK TABLES `allutilhdxday101014` WRITE;
/*!40000 ALTER TABLE `allutilhdxday101014` DISABLE KEYS */;
/*!40000 ALTER TABLE `allutilhdxday101014` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-05-05  6:02:52
