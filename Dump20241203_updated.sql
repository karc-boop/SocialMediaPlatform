CREATE DATABASE  IF NOT EXISTS `social_media` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `social_media`;
-- MySQL dump 10.13  Distrib 8.0.38, for macos14 (arm64)
--
-- Host: localhost    Database: social_media
-- ------------------------------------------------------
-- Server version	9.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Comments`
--

DROP TABLE IF EXISTS `Comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Comments` (
  `CommentID` int NOT NULL AUTO_INCREMENT,
  `PostID` int NOT NULL,
  `UserID` int NOT NULL,
  `Content` text NOT NULL,
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `like_count` int DEFAULT '0',
  PRIMARY KEY (`CommentID`),
  KEY `idx_comments_postid` (`PostID`),
  KEY `idx_comments_userid` (`UserID`),
  CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`PostID`) REFERENCES `posts` (`PostID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Comments`
--

LOCK TABLES `Comments` WRITE;
/*!40000 ALTER TABLE `Comments` DISABLE KEYS */;
INSERT INTO `Comments` VALUES (1,1,2,'Update the first comment','2024-11-30 06:01:41',0),(2,2,3,'Great job on the workout!','2024-11-30 06:01:41',0),(3,3,4,'Can’t wait to read your post.','2024-11-30 06:01:41',0),(4,4,5,'Love this recipe, thanks for sharing!','2024-11-30 06:01:41',0),(6,6,7,'Which book are you reading?','2024-11-30 06:01:41',0),(7,7,8,'Congrats on your first day!','2024-11-30 06:01:41',0),(8,8,9,'Looking forward to your AI model.','2024-11-30 06:01:41',0),(9,9,10,'Paris is beautiful, enjoy!','2024-11-30 06:01:41',0),(10,10,1,'Best of luck with your pitch!','2024-11-30 06:01:41',0);
/*!40000 ALTER TABLE `Comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_requests`
--

DROP TABLE IF EXISTS `friend_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend_requests` (
  `RequestID` int NOT NULL AUTO_INCREMENT,
  `SenderID` int NOT NULL,
  `ReceiverID` int NOT NULL,
  `Status` enum('PENDING','ACCEPTED','DECLINED') NOT NULL,
  `Timestamp` datetime NOT NULL,
  PRIMARY KEY (`RequestID`),
  KEY `SenderID` (`SenderID`),
  KEY `ReceiverID` (`ReceiverID`),
  CONSTRAINT `friend_requests_ibfk_1` FOREIGN KEY (`SenderID`) REFERENCES `users` (`UserID`),
  CONSTRAINT `friend_requests_ibfk_2` FOREIGN KEY (`ReceiverID`) REFERENCES `users` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_requests`
--

LOCK TABLES `friend_requests` WRITE;
/*!40000 ALTER TABLE `friend_requests` DISABLE KEYS */;
INSERT INTO `friend_requests` VALUES (1,12,13,'ACCEPTED','2024-12-02 10:23:13'),(2,15,4,'PENDING','2024-12-03 10:16:00'),(3,15,12,'ACCEPTED','2024-12-03 10:16:49');
/*!40000 ALTER TABLE `friend_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friendships`
--

DROP TABLE IF EXISTS `friendships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friendships` (
  `UserID1` int NOT NULL,
  `UserID2` int NOT NULL,
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserID1`,`UserID2`),
  KEY `UserID2` (`UserID2`),
  CONSTRAINT `friendships_ibfk_1` FOREIGN KEY (`UserID1`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `friendships_ibfk_2` FOREIGN KEY (`UserID2`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friendships`
--

LOCK TABLES `friendships` WRITE;
/*!40000 ALTER TABLE `friendships` DISABLE KEYS */;
INSERT INTO `friendships` VALUES (1,2,'2024-11-30 06:01:41'),(1,10,'2024-11-30 06:01:41'),(1,12,'2024-12-02 01:16:25'),(2,3,'2024-11-30 06:01:41'),(3,4,'2024-11-30 06:01:41'),(4,5,'2024-11-30 06:01:41'),(4,12,'2024-12-02 14:43:48'),(5,6,'2024-11-30 06:01:41'),(6,7,'2024-11-30 06:01:41'),(7,8,'2024-11-30 06:01:41'),(8,9,'2024-11-30 06:01:41'),(9,10,'2024-11-30 06:01:41'),(12,13,'2024-12-02 15:23:27'),(12,15,'2024-12-03 15:17:04');
/*!40000 ALTER TABLE `friendships` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hashtags`
--

DROP TABLE IF EXISTS `hashtags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hashtags` (
  `HashtagID` int NOT NULL AUTO_INCREMENT,
  `TagName` varchar(50) NOT NULL,
  PRIMARY KEY (`HashtagID`),
  UNIQUE KEY `TagName_Unique` (`TagName`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hashtags`
--

LOCK TABLES `hashtags` WRITE;
/*!40000 ALTER TABLE `hashtags` DISABLE KEYS */;
INSERT INTO `hashtags` VALUES (8,'#AI'),(6,'#Books'),(2,'#Fitness'),(4,'#Foodie'),(7,'#Job'),(5,'#Motivation'),(1,'#Nature'),(10,'#Startup'),(3,'#Technology'),(9,'#Travel'),(11,'weather');
/*!40000 ALTER TABLE `hashtags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `MessageID` int NOT NULL AUTO_INCREMENT,
  `SenderID` int NOT NULL,
  `ReceiverID` int NOT NULL,
  `Content` text NOT NULL,
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`MessageID`),
  KEY `idx_messages_senderid` (`SenderID`),
  KEY `idx_messages_receiverid` (`ReceiverID`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`SenderID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`ReceiverID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (1,1,2,'Hey, how are you?','2024-11-30 06:01:41'),(2,2,3,'Did you see my post?','2024-11-30 06:01:41'),(3,3,4,'Let’s meet up this weekend.','2024-11-30 06:01:41'),(4,4,5,'Great job on your project!','2024-11-30 06:01:41'),(5,5,6,'What’s your favorite book?','2024-11-30 06:01:41'),(6,6,7,'Congrats on your new job!','2024-11-30 06:01:41'),(7,7,8,'Excited to see your AI project.','2024-11-30 06:01:41'),(8,8,9,'Paris sounds amazing, enjoy!','2024-11-30 06:01:41'),(9,9,10,'Let me know about the startup pitch.','2024-11-30 06:01:41'),(10,10,1,'Hope you’re doing well!','2024-11-30 06:01:41'),(14,12,1,'test msg','2024-12-02 15:04:30');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `NotificationID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `Message` text NOT NULL,
  `Type` varchar(50) NOT NULL,
  `IsRead` tinyint(1) DEFAULT '0',
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`NotificationID`),
  KEY `UserID` (`UserID`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post_likes`
--

DROP TABLE IF EXISTS `post_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_likes` (
  `LikeID` int NOT NULL AUTO_INCREMENT,
  `PostID` int NOT NULL,
  `UserID` int NOT NULL,
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`LikeID`),
  KEY `post_likes_ibfk_1` (`PostID`),
  KEY `post_likes_ibfk_2` (`UserID`),
  CONSTRAINT `post_likes_ibfk_1` FOREIGN KEY (`PostID`) REFERENCES `posts` (`PostID`) ON DELETE CASCADE,
  CONSTRAINT `post_likes_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post_likes`
--

LOCK TABLES `post_likes` WRITE;
/*!40000 ALTER TABLE `post_likes` DISABLE KEYS */;
/*!40000 ALTER TABLE `post_likes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `posthashtags`
--

DROP TABLE IF EXISTS `posthashtags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posthashtags` (
  `PostID` int NOT NULL,
  `HashtagID` int NOT NULL,
  PRIMARY KEY (`PostID`,`HashtagID`),
  KEY `idx_posthashtags_postid` (`PostID`),
  KEY `idx_posthashtags_hashtagid` (`HashtagID`),
  CONSTRAINT `posthashtags_ibfk_1` FOREIGN KEY (`PostID`) REFERENCES `posts` (`PostID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `posthashtags_ibfk_2` FOREIGN KEY (`HashtagID`) REFERENCES `hashtags` (`HashtagID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `posthashtags`
--

LOCK TABLES `posthashtags` WRITE;
/*!40000 ALTER TABLE `posthashtags` DISABLE KEYS */;
INSERT INTO `posthashtags` VALUES (1,1),(1,2),(2,2),(2,3),(3,3),(4,4),(6,6),(7,7),(8,8),(9,9),(10,10),(15,11);
/*!40000 ALTER TABLE `posthashtags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Posts`
--

DROP TABLE IF EXISTS `Posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Posts` (
  `PostID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `Content` text NOT NULL,
  `MediaType` enum('text','image','video') DEFAULT 'text',
  `MediaURL` varchar(255) DEFAULT NULL,
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `like_count` int DEFAULT '0',
  PRIMARY KEY (`PostID`),
  KEY `idx_posts_userid` (`UserID`),
  CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Posts`
--

LOCK TABLES `Posts` WRITE;
/*!40000 ALTER TABLE `Posts` DISABLE KEYS */;
INSERT INTO `Posts` VALUES (1,1,'Check out this amazing view!','image','view.jpg','2024-11-30 06:01:41',0),(2,2,'Had a great workout today!','text',NULL,'2024-11-30 06:01:41',0),(3,3,'New blog post is up!','text',NULL,'2024-11-30 06:01:41',0),(4,4,'Delicious cake recipe shared here.','image','cake.jpg','2024-11-30 06:01:41',0),(6,6,'Found a great book to read!','text',NULL,'2024-11-30 06:01:41',0),(7,7,'First day at the new job!','text',NULL,'2024-11-30 06:01:41',0),(8,8,'Building a new AI model.','text',NULL,'2024-11-30 06:01:41',0),(9,9,'Traveling to Paris soon.','text',NULL,'2024-11-30 06:01:41',0),(10,10,'Startup pitch deck ready.','text',NULL,'2024-11-30 06:01:41',0),(11,1,'john_doe update a post','text',NULL,'2024-11-30 06:42:36',0),(12,12,'new post test','text',NULL,'2024-12-02 01:33:03',0),(13,12,'test dec.2','text',NULL,'2024-12-02 14:42:13',0),(14,12,'test thrid','text',NULL,'2024-12-02 14:49:28',0),(15,12,'#weather test','text',NULL,'2024-12-03 14:29:18',0);
/*!40000 ALTER TABLE `Posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shares`
--

DROP TABLE IF EXISTS `shares`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shares` (
  `ShareID` int NOT NULL AUTO_INCREMENT,
  `PostID` int NOT NULL,
  `UserID` int NOT NULL,
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ShareID`),
  KEY `idx_shares_postid` (`PostID`),
  KEY `idx_shares_userid` (`UserID`),
  CONSTRAINT `shares_ibfk_1` FOREIGN KEY (`PostID`) REFERENCES `posts` (`PostID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `shares_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shares`
--

LOCK TABLES `shares` WRITE;
/*!40000 ALTER TABLE `shares` DISABLE KEYS */;
INSERT INTO `shares` VALUES (1,1,2,'2024-11-30 06:01:41'),(2,2,3,'2024-11-30 06:01:41'),(3,3,4,'2024-11-30 06:01:41'),(4,4,5,'2024-11-30 06:01:41'),(6,6,7,'2024-11-30 06:01:41'),(7,7,8,'2024-11-30 06:01:41'),(8,8,9,'2024-11-30 06:01:41'),(9,9,10,'2024-11-30 06:01:41'),(10,10,1,'2024-11-30 06:01:41'),(11,11,12,'2024-12-03 02:45:27');
/*!40000 ALTER TABLE `shares` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_settings`
--

DROP TABLE IF EXISTS `user_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_settings` (
  `SettingID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `NotificationsEnabled` tinyint(1) DEFAULT '1',
  `PrivacyLevel` enum('PUBLIC','FRIENDS','PRIVATE') DEFAULT 'PUBLIC',
  PRIMARY KEY (`SettingID`),
  UNIQUE KEY `UserID` (`UserID`),
  CONSTRAINT `user_settings_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_settings`
--

LOCK TABLES `user_settings` WRITE;
/*!40000 ALTER TABLE `user_settings` DISABLE KEYS */;
INSERT INTO `user_settings` VALUES (1,14,1,'PUBLIC'),(2,15,1,'PUBLIC');
/*!40000 ALTER TABLE `user_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `Username` varchar(50) NOT NULL,
  `Email` varchar(100) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `Name` varchar(100) DEFAULT NULL,
  `Bio` text,
  `ProfilePicture` varchar(255) DEFAULT NULL,
  `CreatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Username_Unique` (`Username`),
  UNIQUE KEY `Email_Unique` (`Email`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'john_doe','john@example.com','ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f','John Doe','Tech enthusiast.','profile1.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(2,'mike_jones','mike@example.com','89e01536ac207279409d4de1e5253e01f4a1769e696db0d6062ca9b8f56767c8','Mike Jones','Loves photography.','profile2.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(3,'jane_smith','jane@example.com','47625ed74cab8fbc0a8348f3df1feb07f87601e34d62bd12eb0d51616566fab5','Jane Smith','Adventure seeker.','profile3.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(4,'lucy_lu','lucy@example.com','bd94dcda26fccb4e68d6a31f9b5aac0b571ae266d822620e901ef7ebe3a11d4f','Lucy Lu','Food blogger.','profile4.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(5,'tom_hardy','tom@example.com','e47b162b403327f02ad3481eebaa36ed8b56d7fedf52159c0a7461dc40f5d810','Tom Hardy','Fitness coach.','profile5.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(6,'emma_watson','emma@example.com','52293754fdbea92ab6c69cd64e644deed1552f40ccd3c1cef9d4d63c754d13e3','Emma Watson','Book lover.','profile6.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(7,'james_bond','james@example.com','0a5d59d1e826a9fec485bfdd01186c9ed93b8b2fcaf898ec6c826a44bcbe6ff2','James Bond','Secret agent.','profile7.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(8,'tony_stark','tony@example.com','4f278cdddf52263fe21c64c94932f2b2ec316acecd39a7adcc01eb2e6592a678','Tony Stark','Inventor.','profile8.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(9,'natasha_romanoff','natasha@example.com','af4976f2ca0b78460afd7f2e3fc2287e0d9ca759524a854ad7d59dcead6867cb','Natasha Romanoff','Spy.','profile9.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(10,'bruce_wayne','bruce@example.com','1532e76dbe9d43d0dea98c331ca5ae8a65c5e8e8b99d3e2a42ae989356f6242a','Bruce Wayne','Businessman.','profile10.jpg','2024-11-30 06:01:41','2024-11-30 06:01:41'),(12,'new2test','new2test@example.com','d772c08e624227dbe897d1ca202f46d6de6d42d837fc9638f2497fd60f30cbf8','new2test','test update dec.2',NULL,'2024-12-02 00:43:48','2024-12-02 14:40:51'),(13,'newf','newf@example.com','b234b05c975f2a68f7ecf4c0826a3e009bfa932c6d7e8d62310719535dd12c18','newf','new frind test','','2024-12-02 15:22:44','2024-12-02 15:22:44'),(14,'test','test@example.com','d772c08e624227dbe897d1ca202f46d6de6d42d837fc9638f2497fd60f30cbf8','karmen','test.','','2024-12-03 15:04:22','2024-12-03 15:04:22'),(15,'test1','test@exm.com','9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08','test1','test','','2024-12-03 15:04:53','2024-12-03 15:04:53');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usersettings`
--

DROP TABLE IF EXISTS `usersettings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usersettings` (
  `SettingID` int NOT NULL AUTO_INCREMENT,
  `UserID` int NOT NULL,
  `NotificationsEnabled` tinyint(1) DEFAULT '1',
  `PrivacyLevel` enum('public','private','friends-only') DEFAULT 'public',
  PRIMARY KEY (`SettingID`),
  UNIQUE KEY `UniqueUserSettings` (`UserID`),
  KEY `idx_usersettings_userid` (`UserID`),
  CONSTRAINT `usersettings_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usersettings`
--

LOCK TABLES `usersettings` WRITE;
/*!40000 ALTER TABLE `usersettings` DISABLE KEYS */;
INSERT INTO `usersettings` VALUES (1,1,1,'public'),(2,2,1,'friends-only'),(3,3,0,'private'),(4,4,1,'public'),(5,5,0,'friends-only'),(6,6,1,'private'),(7,7,1,'public'),(8,8,0,'friends-only'),(9,9,1,'private'),(10,10,1,'public');
/*!40000 ALTER TABLE `usersettings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `views`
--

DROP TABLE IF EXISTS `views`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `views` (
  `ViewID` int NOT NULL AUTO_INCREMENT,
  `PostID` int NOT NULL,
  `UserID` int NOT NULL,
  `Timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ViewID`),
  KEY `idx_views_postid` (`PostID`),
  KEY `idx_views_userid` (`UserID`),
  CONSTRAINT `views_ibfk_1` FOREIGN KEY (`PostID`) REFERENCES `posts` (`PostID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `views_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `views`
--

LOCK TABLES `views` WRITE;
/*!40000 ALTER TABLE `views` DISABLE KEYS */;
INSERT INTO `views` VALUES (1,1,2,'2024-11-30 06:01:41'),(2,2,3,'2024-11-30 06:01:41'),(3,3,4,'2024-11-30 06:01:41'),(4,4,5,'2024-11-30 06:01:41'),(6,6,7,'2024-11-30 06:01:41'),(7,7,8,'2024-11-30 06:01:41'),(8,8,9,'2024-11-30 06:01:41'),(9,9,10,'2024-11-30 06:01:41'),(10,10,1,'2024-11-30 06:01:41');
/*!40000 ALTER TABLE `views` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-12-03 14:41:31
DROP TABLE IF EXISTS Views;
DROP TABLE IF EXISTS Shares;

CREATE TABLE Views (
    ViewID INT AUTO_INCREMENT PRIMARY KEY,
    PostID INT NOT NULL,
    UserID INT NOT NULL,
    Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (PostID) REFERENCES Posts(PostID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Shares (
    ShareID INT AUTO_INCREMENT PRIMARY KEY,
    PostID INT NOT NULL,
    UserID INT NOT NULL,
    Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (PostID) REFERENCES Posts(PostID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
