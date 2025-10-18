CREATE DATABASE  IF NOT EXISTS `salesync` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `salesync`;
-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: salesync
-- ------------------------------------------------------
-- Server version	9.3.0

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
-- Table structure for table `app_acct_financial_accounts`
--

DROP TABLE IF EXISTS `app_acct_financial_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_acct_financial_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_paid` double DEFAULT NULL,
  `amount_received` double DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `current_balance` double NOT NULL,
  `fin_acc_id` varchar(255) NOT NULL,
  `fin_acc_name` varchar(100) NOT NULL,
  `financial_year` varchar(255) DEFAULT NULL,
  `opening_balance` double DEFAULT NULL,
  `payment_status` varchar(255) DEFAULT NULL,
  `transaction_type` varchar(20) DEFAULT NULL,
  `trn_date` datetime DEFAULT NULL,
  `trn_ref_no` varchar(255) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6bse8vfp0ymc717i10x7f7jox` (`fin_acc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_acct_financial_accounts`
--

LOCK TABLES `app_acct_financial_accounts` WRITE;
/*!40000 ALTER TABLE `app_acct_financial_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_acct_financial_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint DEFAULT NULL,
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `table_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_id` bigint DEFAULT NULL,
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idx_audit_log_created_at` (`created_at`),
  KEY `idx_audit_log_user_id` (`user_id`),
  KEY `idx_audit_log_table_record` (`table_name`,`record_id`),
  KEY `idx_audit_log_action_type` (`action_type`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_log`
--

LOCK TABLES `audit_log` WRITE;
/*!40000 ALTER TABLE `audit_log` DISABLE KEYS */;
INSERT INTO `audit_log` VALUES (1,'Order inserted: 42','2025-10-03 16:35:10',NULL,NULL,NULL,NULL,NULL,NULL),(2,'Order inserted: 43','2025-10-03 17:55:42',NULL,NULL,NULL,NULL,NULL,NULL),(3,'Order inserted: 44','2025-10-11 17:48:36',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `brands`
--

DROP TABLE IF EXISTS `brands`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brands` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brands`
--

LOCK TABLES `brands` WRITE;
/*!40000 ALTER TABLE `brands` DISABLE KEYS */;
INSERT INTO `brands` VALUES (1,'Bangladeshi electronics and home appliances brand','Walton'),(2,'Famous Bangladeshi food and beverage brand','Pran'),(3,'Popular Bangladeshi footwear and bicycle brand','Runner'),(4,'Bangladeshi clothing and handicraft brand','Aarong'),(5,'Leading telecom service provider in Bangladesh','Grameenphone'),(6,'Bangladeshi conglomerate with products in textiles, pharmaceuticals, and more','Beximco'),(7,'Electronics and small appliances brand under Pran-RFL Group','Vision'),(8,'Bangladeshi pharmaceutical and consumer goods company','ACI Limited'),(9,'Electronics and mobile brand in Bangladesh','Samsung BD'),(10,'Mobile and communication devices brand in Bangladesh','Nokia BD'),(11,'Bangladeshi electronics and home appliances brand','Walton'),(12,'Famous Bangladeshi food and beverage brand','Pran'),(13,'Popular Bangladeshi footwear and bicycle brand','Runner'),(14,'Popular brand for switches, sockets, and lighting accessories','Super Star'),(15,'Leading telecom service provider in Bangladesh','Grameenphone'),(16,'Unbranded or local shop-manufactured products','Local Generic'),(17,'Bangladeshi electronics and electrical appliance brand','Rangs'),(18,'Bangladeshi pharmaceutical and consumer goods company','ACI Limited'),(19,'Electronics and mobile brand in Bangladesh','Samsung BD'),(20,'Mobile and communication devices brand in Bangladesh','Nokia BD');
/*!40000 ALTER TABLE `brands` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Devices and gadgets including phones, laptops, and accessories','Electronics'),(2,'Daily grocery items including food and beverages','Groceries'),(3,'Men, women, and children clothing items','Clothing'),(4,'Home and office furniture products','Furniture'),(5,'Books, magazines, and educational materials','Books'),(6,'Office and school supplies like pens, notebooks, etc.','Stationery'),(7,'Sports equipment and activewear','Sports'),(8,'Personal care, cosmetics, and health products','Health & Beauty'),(9,'Toys, board games, and kids entertainment items','Toys & Games'),(10,'Car accessories, parts, and automotive tools','Automotive'),(11,'Products used in electrical installation and wiring','Electrical Items'),(12,'Clothing and fashion items','Apparel'),(13,'General-purpose hardware accessories and tools','Hardware'),(14,'Home and office furniture products.','Furniture'),(15,'Devices and gadgets including phones, laptops, and accessories','Electronics1');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'123 Main Street, Dhaka','Walk-in Customer1','01710000001'),(2,'45 Green Avenue, Dhaka','Jane Smith','01710000002'),(3,'12 Blue Street, Chittagong','Michael Johnson','01710000003'),(4,'78 Red Road, Dhaka','Emily Davis','01710000004'),(5,'56 Lake View, Sylhet','David Wilson','01710000005'),(6,'34 Park Lane, Dhaka','Sarah Brown','01710000006'),(7,'90 Hill Street, Rajshahi','Daniel Taylor','01710000007'),(8,'67 River Road, Khulna','Laura Anderson','01710000008'),(9,'23 Garden Street, Barishal','James Thomas','01710000009'),(10,'89 Sunset Boulevard, Dhaka','Olivia Martinez','01710000010'),(11,'11 Ocean Drive, Chittagong','William Lee','01710000011'),(12,'45 Mountain View, Dhaka','Sophia Harris','01710000012'),(13,'22 Broadway, Sylhet','Benjamin Clark','01710000013'),(14,'77 Elm Street, Dhaka','Ava Lewis','01710000014'),(15,'33 Maple Avenue, Rajshahi','Alexander Robinson','01710000015'),(16,'56 Pine Street, Khulna','Mia Walker','01710000016'),(17,'12 Cedar Road, Barishal','Ethan Hall','01710000017'),(18,'89 Birch Lane, Dhaka','Isabella Young','01710000018'),(19,'44 Willow Street, Chittagong','Lucas King','01710000019'),(20,'78 Oak Avenue, Dhaka','Charlotte Scott','01710000020'),(21,'44 Willow Street, Chittagong','Maikel','01710000020'),(22,'asdfkl, askdfj, daskfj','Habib','01719000000');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `error_log`
--

DROP TABLE IF EXISTS `error_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `error_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'e.g., TRIGGER, APPLICATION, API',
  `severity` enum('LOW','MEDIUM','HIGH','CRITICAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'MEDIUM',
  PRIMARY KEY (`id`),
  KEY `idx_error_log_created_at` (`created_at`),
  KEY `idx_error_log_severity` (`severity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `error_log`
--

LOCK TABLES `error_log` WRITE;
/*!40000 ALTER TABLE `error_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `error_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `expenses`
--

DROP TABLE IF EXISTS `expenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `expenses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `category` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `expense_date` date NOT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `reference_no` varchar(50) DEFAULT NULL,
  `vendor_name` varchar(100) DEFAULT NULL,
  `payment_status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `expenses`
--

LOCK TABLES `expenses` WRITE;
/*!40000 ALTER TABLE `expenses` DISABLE KEYS */;
/*!40000 ALTER TABLE `expenses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_details`
--

DROP TABLE IF EXISTS `order_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `invoice_number` varchar(255) DEFAULT NULL,
  `total_price` decimal(10,2) DEFAULT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `vat` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjyu2qbqt8gnvno9oe9j2s2ldk` (`order_id`),
  KEY `FK4q98utpd73imf4yhttm3w0eax` (`product_id`),
  CONSTRAINT `FK4q98utpd73imf4yhttm3w0eax` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKjyu2qbqt8gnvno9oe9j2s2ldk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_details`
--

LOCK TABLES `order_details` WRITE;
/*!40000 ALTER TABLE `order_details` DISABLE KEYS */;
INSERT INTO `order_details` VALUES (1,3,35,50,'INV-20251003-6270',173880.00,50400.00,22680.00),(2,5,41,3,'INV-20251003-8836',321.94,55.99,41.99),(3,5,41,53,'INV-20251003-8836',16100.00,2800.00,2100.00),(4,5,42,3,'INV-20251003-6983',321.94,55.99,41.99),(5,10,42,54,'INV-20251003-6983',2576.00,224.00,336.00),(6,10,43,53,'INV-20251003-2325',32200.00,2800.00,4200.00),(7,10,43,54,'INV-20251003-2325',2576.00,224.00,336.00),(8,1,44,1,'INV-20251011-9516',1159.19,1007.99,151.20),(9,10,44,51,'INV-20251011-9516',1545.60,134.40,201.60),(10,10,48,51,'INV-20251018-3102',1545.60,134.40,201.60),(11,10,49,51,'INV-20251018-6221',1545.60,134.40,201.60),(12,10,50,51,'INV-20251018-3694',1545.60,134.40,201.60),(13,10,51,53,'INV-20251018-8995',32200.00,2800.00,4200.00);
/*!40000 ALTER TABLE `order_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date_ordered` date NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `customer_id` bigint NOT NULL,
  `amount_due` decimal(10,2) DEFAULT '0.00',
  `amount_paid` decimal(10,2) DEFAULT '0.00',
  `invoice_number` varchar(255) NOT NULL,
  `payment_status` varchar(20) DEFAULT NULL,
  `deleted` tinyint(1) DEFAULT '0',
  `discount` decimal(10,2) DEFAULT '0.00',
  `grand_total` decimal(10,2) DEFAULT '0.00',
  `insert_date` datetime DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_vat` decimal(10,2) DEFAULT NULL,
  `update_date` datetime DEFAULT NULL,
  `insertDate` datetime(6) DEFAULT NULL,
  `updateDate` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpxtb8awmi0dk6smoh2vp1litg` (`customer_id`),
  CONSTRAINT `FKpxtb8awmi0dk6smoh2vp1litg` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'2025-09-26',4649.70,8,0.00,4649.70,'INV-20250929-8828',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,'2025-09-28',416000.00,11,0.00,416000.00,'INV-20250929-5528',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,'2025-09-29',26499.50,1,0.00,26499.50,'INV-20250929-5428',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,'2025-09-29',4999.85,1,0.00,4999.85,'INV-20250929-6144',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,'2025-09-29',6794.98,1,0.00,6794.98,'INV-20250929-1187',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,'2025-09-29',1499.50,1,499.50,1000.00,'INV-20250929-5197',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,'2025-09-29',1499.50,1,499.50,1000.00,'INV-20250929-7186',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,'2025-09-30',54900.00,1,4900.00,50000.00,'INV-20250930-1372',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,'2025-09-30',54900.00,1,4900.00,50000.00,'INV-20250930-1372',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(10,'2025-09-30',4396.49,1,396.49,4000.00,'INV-20250930-6481',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(11,'2025-09-30',91200.00,1,11200.00,80000.00,'INV-20250930-5745',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(12,'2025-09-30',2729.95,2,729.95,2000.00,'INV-20250930-7421',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(13,'2025-09-30',17700.00,3,7700.00,10000.00,'INV-20250930-2503',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(14,'2025-09-30',1200.00,2,0.00,1200.00,'INV-20250930-5885',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(15,'2025-09-30',1800.00,4,300.00,1500.00,'INV-20250930-6301',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(17,'2025-09-28',1000.00,1,800.00,200.00,'INV-1001',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(23,'2025-10-03',100.00,1,100.00,0.00,'TEST-001',NULL,1,0.00,100.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(29,'2025-10-03',200.00,1,0.00,50.00,'TEST-002',NULL,1,0.00,200.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(32,'2025-10-03',1344.00,4,0.00,1344.00,'INV-20251003-2945','PAID',1,0.00,1344.00,'2025-10-03 05:23:50',NULL,NULL,0.00,'2025-10-03 05:23:50',NULL,NULL),(33,'2025-10-03',5594.40,2,0.00,5594.40,'INV-20251003-1612','PAID',1,0.00,5594.40,'2025-10-03 05:26:16',NULL,NULL,0.00,'2025-10-03 05:26:16',NULL,NULL),(34,'2025-10-03',5594.40,2,0.00,5594.40,'INV-20251003-1612','PAID',1,0.00,5594.40,'2025-10-03 05:27:00',NULL,NULL,0.00,'2025-10-03 05:27:00',NULL,NULL),(35,'2025-10-03',151200.00,4,136200.00,15000.00,'INV-20251003-6270','PARTIALLY_PAID',0,0.00,151200.00,'2025-10-03 05:50:10',NULL,NULL,0.00,'2025-10-03 05:50:10',NULL,NULL),(41,'2025-10-03',14279.95,22,4279.95,10000.00,'INV-20251003-8836','PARTIALLY_PAID',0,0.00,14279.95,'2025-10-03 15:31:44',NULL,NULL,0.00,'2025-10-03 15:31:44',NULL,NULL),(42,'2025-10-03',2519.95,19,519.95,2000.00,'INV-20251003-6983','PARTIALLY_PAID',0,0.00,2519.95,'2025-10-03 16:35:10',NULL,NULL,0.00,'2025-10-03 16:35:10',NULL,NULL),(43,'2025-10-03',30240.00,21,240.00,30000.00,'INV-20251003-2325','PARTIALLY_PAID',0,0.00,30240.00,'2025-10-03 17:55:43',NULL,NULL,0.00,'2025-10-03 17:55:43',NULL,NULL),(44,'2025-10-11',2351.99,1,351.99,2000.00,'INV-20251011-9516','PARTIALLY_PAID',0,0.00,2351.99,'2025-10-11 17:48:36',NULL,NULL,0.00,'2025-10-11 17:48:36',NULL,NULL),(48,'2025-10-18',1300.00,1,300.00,1000.00,'INV-20251018-3102','PARTIALLY_PAID',0,0.00,1300.00,NULL,NULL,NULL,0.00,NULL,'2025-10-18 07:16:51.187304','2025-10-18 07:16:51.187304'),(49,'2025-10-18',1300.00,2,300.00,1000.00,'INV-20251018-6221','PARTIALLY_PAID',0,0.00,1300.00,NULL,NULL,NULL,0.00,NULL,'2025-10-18 07:24:22.853308','2025-10-18 07:24:22.853308'),(50,'2025-10-18',1300.00,1,256.00,1000.00,'INV-20251018-3694','PARTIALLY_PAID',0,44.00,1256.00,NULL,NULL,NULL,0.00,NULL,'2025-10-18 07:34:00.061955','2025-10-18 07:34:00.061955'),(51,'2025-10-18',28000.00,1,7700.00,20000.00,'INV-20251018-8995','PARTIALLY_PAID',0,300.00,27700.00,NULL,NULL,NULL,0.00,NULL,'2025-10-18 08:11:53.324388','2025-10-18 08:11:53.324388');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `from_account` varchar(100) DEFAULT NULL,
  `instrument_no` varchar(100) DEFAULT NULL,
  `method` varchar(50) DEFAULT NULL,
  `payment_date` datetime NOT NULL,
  `ref_id` bigint NOT NULL,
  `ref_type` enum('EXPENSE','PURCHASE_ORDER','SALE_ORDER') NOT NULL,
  `amount_due` decimal(12,2) NOT NULL,
  `amount_paid` decimal(12,2) NOT NULL,
  `discount` decimal(12,2) NOT NULL,
  `grand_total` decimal(12,2) NOT NULL,
  `payment_status` varchar(20) DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `to_account` varchar(100) DEFAULT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `total_vat` decimal(12,2) NOT NULL,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `paid_amount` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,'2025-10-02 20:14:44',NULL,NULL,'Cash','2025-10-02 20:14:44',17,'',800.00,200.00,0.00,1000.00,'PARTIALLY_PAID','Auto payment entry for Order #INV-1001 - Soft deleted with Order #INV-1001','Cash',1000.00,0.00,1,0.00),(4,'2025-10-03 00:19:13',NULL,NULL,'Cash','2025-10-03 00:19:13',29,'',0.00,50.00,0.00,200.00,'PARTIALLY_PAID','Auto payment entry for Order #TEST-002 - Soft deleted with Order #TEST-002','Walk-in Customer',200.00,0.00,1,0.00),(6,'2025-10-03 11:23:49',NULL,NULL,'Cash','2025-10-03 11:23:49',32,'',0.00,1344.00,0.00,1344.00,'PAID','Auto payment entry for Order #INV-20251003-2945 - Soft deleted with Order #INV-20251003-2945','Emily Davis',1344.00,0.00,1,0.00),(7,'2025-10-03 11:26:15',NULL,NULL,'Cash','2025-10-03 11:26:15',33,'',0.00,5594.40,0.00,5594.40,'PAID','Auto payment entry for Order #INV-20251003-1612 - Soft deleted with Order #INV-20251003-1612','Jane Smith',5594.40,0.00,1,0.00),(8,'2025-10-03 11:26:59',NULL,NULL,'Cash','2025-10-03 11:26:59',34,'',0.00,5594.40,0.00,5594.40,'PAID','Auto payment entry for Order #INV-20251003-1612 - Soft deleted with Order #INV-20251003-1612','Jane Smith',5594.40,0.00,1,0.00),(9,'2025-10-03 11:50:09',NULL,NULL,'Cash','2025-10-03 11:50:09',35,'',136200.00,15000.00,0.00,151200.00,'PARTIALLY_PAID','Auto payment entry for Order #INV-20251003-6270','Emily Davis',151200.00,0.00,0,0.00),(10,'2025-10-03 21:31:43',NULL,NULL,'Cash','2025-10-03 21:31:43',41,'',4279.95,10000.00,0.00,14279.95,'PARTIALLY_PAID','Auto payment entry for Order #INV-20251003-8836','Habib',14279.95,0.00,0,0.00),(11,'2025-10-03 22:35:10',NULL,NULL,'Cash','2025-10-03 22:35:10',42,'',519.95,2000.00,0.00,2519.95,'PARTIALLY_PAID','Auto payment for Order #INV-20251003-6983','Lucas King',2519.95,0.00,0,0.00),(12,'2025-10-03 23:55:42',NULL,NULL,'Cash','2025-10-03 23:55:42',43,'',240.00,30000.00,0.00,30240.00,'PARTIALLY_PAID','Auto payment for Order #INV-20251003-2325','Maikel',30240.00,0.00,0,0.00),(13,'2025-10-11 23:48:36',NULL,NULL,'Cash','2025-10-11 23:48:36',44,'',351.99,2000.00,0.00,2351.99,'PARTIALLY_PAID','Auto payment for Order #INV-20251011-9516','Walk-in Customer',2351.99,0.00,0,0.00),(14,'2025-10-18 07:16:51','POS',NULL,'Cash','2025-10-18 07:16:51',48,'SALE_ORDER',300.00,1000.00,0.00,1300.00,'PARTIALLY_PAID','Auto payment for Order #INV-20251018-3102','Walk-in Customer1',1300.00,0.00,0,1000.00),(15,'2025-10-18 07:24:23','POS',NULL,'Cash','2025-10-18 07:24:23',49,'SALE_ORDER',300.00,1000.00,0.00,1300.00,'PARTIALLY_PAID','Auto payment for Order #INV-20251018-6221','Jane Smith',1300.00,0.00,0,1000.00),(16,'2025-10-18 07:34:00','POS',NULL,'Cash','2025-10-18 07:34:00',50,'SALE_ORDER',256.00,1000.00,44.00,1256.00,'PARTIALLY_PAID','Auto payment for Order #INV-20251018-3694','Walk-in Customer1',1300.00,0.00,0,1000.00),(17,'2025-10-18 08:11:53','POS',NULL,'Cash','2025-10-18 08:11:53',51,'SALE_ORDER',7700.00,20000.00,300.00,27700.00,'PARTIALLY_PAID','Auto payment for Order #INV-20251018-8995','Walk-in Customer1',28000.00,0.00,0,20000.00);
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `quantity` int NOT NULL,
  `barcode` varchar(50) DEFAULT NULL,
  `batch_no` varchar(100) DEFAULT NULL,
  `color` varchar(50) DEFAULT NULL,
  `description` text,
  `flavor` varchar(50) DEFAULT NULL,
  `model` varchar(100) DEFAULT NULL,
  `size` varchar(50) DEFAULT NULL,
  `sku` varchar(50) NOT NULL,
  `unit_of_measure` varchar(50) DEFAULT NULL,
  `brand_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `supplier_id` bigint DEFAULT NULL,
  `cost_price` decimal(10,2) NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `manufacture_date` date DEFAULT NULL,
  `min_stock_level` int NOT NULL,
  `reorder_level` int DEFAULT NULL,
  `selling_price` decimal(10,2) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `vat_percent` decimal(5,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `warranty_period` int DEFAULT NULL,
  `warranty_description` varchar(255) DEFAULT NULL,
  `guarantee_period` int DEFAULT NULL,
  `guarantee_description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfhmd06dsmj6k0n90swsh8ie9g` (`sku`),
  UNIQUE KEY `UK_qfr8vf85k3q1xinifvsl1eynf` (`barcode`),
  UNIQUE KEY `UKqfr8vf85k3q1xinifvsl1eynf` (`barcode`),
  KEY `FKa3a4mpsfdf4d2y6r8ra3sc8mv` (`brand_id`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  KEY `FK6i174ixi9087gcvvut45em7fd` (`supplier_id`),
  CONSTRAINT `FK6i174ixi9087gcvvut45em7fd` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `FKa3a4mpsfdf4d2y6r8ra3sc8mv` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Laptop',12,'890100000046','BATCH-1759051756','Silver',NULL,NULL,'WR300LS',NULL,'PRD-00046','pcs',1,1,1,34542.85,NULL,NULL,0,NULL,55000.00,'2025-10-18 06:31:28.184670',NULL,'2025-09-30 00:00:00.000000',0,NULL,0,NULL),(2,'Smartphone',5,'890100000047','BATCH-1759051756','Silver',NULL,NULL,'WR300LD',NULL,'PRD-00047','pcs',1,1,1,499.50,NULL,NULL,0,NULL,559.44,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(3,'Headphones',15,'890100000048','BATCH-1759051756','Silver',NULL,NULL,'WR300LF',NULL,'PRD-00048','pcs',1,1,1,49.99,NULL,NULL,0,NULL,55.99,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(4,'Keyboard',15,'890100000049','BATCH-1759051756','Silver',NULL,NULL,'WR300LG',NULL,'PRD-00049','pcs',1,1,1,29.95,NULL,NULL,0,NULL,33.54,'2025-10-18 07:00:55.550942',NULL,'2025-09-30 00:00:00.000000',0,NULL,0,NULL),(50,'Walton Refrigerator',7,'890100000050','BATCH-1759051757','Silver','Double door refrigerator, energy efficient',NULL,'WR300LH','300L','PRD-00050','pcs',1,1,1,45000.00,NULL,NULL,0,NULL,50400.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(51,'Pran Mango Juice',130,'890100000051','BATCH-1759051757',NULL,'Natural mango juice 1L','Mango',NULL,'1L','PRD-00051','bottle',2,2,2,120.00,NULL,NULL,0,NULL,134.40,'2025-10-18 07:34:00.037154',NULL,'2025-09-30 00:00:00.000000',0,NULL,0,NULL),(52,'Runner Bicycle',25,'890100000052','BATCH-1759051757','Black','Durable mountain bicycle',NULL,'RB26M','26 inch','PRD-00052','pcs',3,7,3,15000.00,NULL,NULL,0,NULL,16800.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(53,'Aarong Saree',8,'890100000053','BATCH-1759051757','Red','Handloom cotton saree',NULL,NULL,'5.5m','PRD-00053','pcs',4,3,4,2500.00,NULL,NULL,0,NULL,2800.00,'2025-10-18 08:11:53.288288',NULL,'2025-09-30 00:00:00.000000',0,NULL,0,NULL),(54,'Grameenphone SIM',990,'890100000054','BATCH-1759051757','White-Blue','4G SIM card with welcome package',NULL,'GP4G',NULL,'PRD-00054','pcs',5,1,5,334.34,NULL,NULL,0,NULL,1400.00,'2025-10-16 17:11:32.052348',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(55,'Beximco Paracetamol',500,'890100000055','BATCH-1759051757',NULL,'500mg tablet, 10 pcs per strip',NULL,NULL,NULL,'PRD-00055','strip',6,8,6,30.00,NULL,NULL,0,NULL,33.60,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(56,'Gazi TV LED',15,'890100000056','BATCH-1759051757','Black','32 inch LED Television',NULL,'GTV32','32 inch','PRD-00056','pcs',7,1,7,18000.00,NULL,NULL,0,NULL,20160.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(57,'ACI Savlon',300,'890100000057','BATCH-1759051757',NULL,'Antiseptic liquid 500ml',NULL,NULL,'500ml','PRD-00057','bottle',8,8,8,90.00,NULL,NULL,0,NULL,100.80,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(58,'Samsung Galaxy A20',25,'890100000058','BATCH-1759051757','Blue','Smartphone with 4GB RAM, 64GB storage',NULL,'A20','6.4 inch','PRD-00058','pcs',9,1,9,16000.00,NULL,NULL,0,NULL,17920.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(59,'Nokia Headphones',40,'890100000059','BATCH-1759051757','Black','Wireless Bluetooth headphones',NULL,'NH200',NULL,'PRD-00059','pcs',10,1,10,3500.00,NULL,NULL,0,NULL,3920.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(60,'Walton Smart TV',20,'890100000060','BATCH-1759051920','Black','43 inch Full HD Smart LED TV',NULL,'WTV43FHD','43 inch','PRD-00060','pcs',1,1,1,38000.00,NULL,NULL,0,NULL,42560.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(61,'Walton Air Conditioner',9,'890100000061','BATCH-1759051920','White','1.5 Ton Split AC, energy efficient',NULL,'WAC15S','1.5 Ton','PRD-00061','pcs',1,1,2,56000.00,NULL,NULL,0,NULL,62720.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(62,'Pran Blender',40,'890100000062','BATCH-1759051920','White','500W kitchen blender with grinder',NULL,'PBL500','2L','PRD-00062','pcs',2,1,2,3500.00,NULL,NULL,0,NULL,3920.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(63,'Runner Electric Bicycle',12,'890100000063','BATCH-1759051920','Blue','Battery-powered e-bicycle, 25km range',NULL,'REB26','26 inch','PRD-00063','pcs',3,1,3,45000.00,NULL,NULL,0,NULL,50400.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(64,'Samsung Smartphone A52',30,'890100000064','BATCH-1759051920','Black','6GB RAM, 128GB storage, 64MP Camera',NULL,'A52','6.5 inch','PRD-00064','pcs',9,1,5,32000.00,NULL,NULL,0,NULL,35840.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(65,'Samsung Washing Machine',18,'890100000065','BATCH-1759051920','Silver','7kg front load automatic washer',NULL,'SWM7','7kg','PRD-00065','pcs',9,1,4,48000.00,NULL,NULL,0,NULL,53760.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(66,'Nokia Tablet T20',25,'890100000066','BATCH-1759051920','Gray','10.4 inch tablet, 4GB RAM, 64GB storage',NULL,'NT20','10.4 inch','PRD-00066','pcs',10,1,6,22000.00,NULL,NULL,0,NULL,24640.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(67,'Gazi Microwave Oven',22,'890100000067','BATCH-1759051920','Black','30L convection microwave oven',NULL,'GMO30C','30L','PRD-00067','pcs',7,1,7,10500.00,NULL,NULL,0,NULL,11760.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(68,'ACI Power Bank',90,'890100000068','BATCH-1759051920','White','10000mAh fast charging power bank',NULL,'APB10K',NULL,'PRD-00068','pcs',8,1,8,1800.00,NULL,NULL,0,NULL,2016.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(69,'Walton Laptop',10,'890100000069','BATCH-1759051920','Silver','Core i5, 8GB RAM, 512GB SSD, 14 inch',NULL,'WL14I5','14 inch','PRD-00069','pcs',1,1,9,52000.00,NULL,NULL,0,NULL,58240.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(70,'Walton Smartwatch',40,'890100000070','BATCH-1759052020','Black','Smartwatch with heart rate and fitness tracking',NULL,'WSW01',NULL,'PRD-00070','pcs',1,1,1,5200.00,NULL,NULL,0,NULL,5824.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(71,'Walton Bluetooth Speaker',60,'890100000071','BATCH-1759052020','Blue','Portable Bluetooth speaker with 12h battery',NULL,'WBS12',NULL,'PRD-00071','pcs',1,1,2,2800.00,NULL,NULL,0,NULL,3136.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(72,'Samsung Galaxy S21',15,'890100000072','BATCH-1759052020','Phantom Gray','Flagship smartphone, 8GB RAM, 128GB storage',NULL,'S21','6.2 inch','PRD-00072','pcs',9,1,5,78000.00,NULL,NULL,0,NULL,87360.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(73,'Samsung Galaxy Buds',35,'890100000073','BATCH-1759052020','White','True wireless earbuds with noise cancellation',NULL,'BUDS2',NULL,'PRD-00073','pcs',9,1,9,9500.00,NULL,NULL,0,NULL,10640.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(74,'Nokia Charger',100,'890100000074','BATCH-1759052020','White','Fast charging 25W mobile charger',NULL,'NC25W',NULL,'PRD-00074','pcs',10,1,6,1200.00,NULL,NULL,0,NULL,1344.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(75,'Nokia Earphones',200,'890100000075','BATCH-1759052020','Black','Wired earphones with mic',NULL,'NE100',NULL,'PRD-00075','pcs',10,1,10,700.00,NULL,NULL,0,NULL,784.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(76,'Gazi Rice Cooker',25,'890100000076','BATCH-1759052020','Red','1.8L electric rice cooker with keep warm function',NULL,'GRC18','1.8L','PRD-00076','pcs',7,1,7,3800.00,NULL,NULL,0,NULL,4256.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(77,'Gazi Induction Cooker',30,'890100000077','BATCH-1759052020','Black','2000W induction cooker with touch control',NULL,'GIC2000',NULL,'PRD-00077','pcs',7,1,8,4800.00,NULL,NULL,0,NULL,5376.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(78,'ACI LED Bulb 12W',500,'890100000078','BATCH-1759052020','White','Energy saving LED bulb, 12W',NULL,'ALB12',NULL,'PRD-00078','pcs',8,1,8,250.00,NULL,NULL,0,NULL,280.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(79,'ACI Electric Kettle',70,'890100000079','BATCH-1759052020','Silver','1.5L stainless steel electric kettle',NULL,'AEK15','1.5L','PRD-00079','pcs',8,1,3,1600.00,NULL,NULL,0,NULL,1792.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(80,'Pran Electric Iron',55,'890100000080','BATCH-1759052020','Blue','Dry electric iron, lightweight',NULL,'PEI12',NULL,'PRD-00080','pcs',2,1,2,1200.00,NULL,NULL,0,NULL,1344.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(81,'Pran Water Purifier',58,'890100000081','BATCH-1759052020','White','UV water purifier 10L',NULL,'PWP10','10L','PRD-00081','pcs',2,1,2,12500.00,NULL,NULL,0,NULL,14000.00,'2025-10-16 13:07:21.565370',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(82,'Runner E-Scooter',8,'890100000082','BATCH-1759052020','Red','Electric scooter, 40km range',NULL,'RES40',NULL,'PRD-00082','pcs',3,1,3,68000.00,NULL,NULL,0,NULL,76160.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(83,'Runner UPS 650VA',22,'890100000083','BATCH-1759052020','Black','Uninterrupted Power Supply 650VA',NULL,'RUPS650',NULL,'PRD-00083','pcs',3,1,4,5200.00,NULL,NULL,0,NULL,5824.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(84,'Walton Tablet WTab10',28,'890100000084','BATCH-1759052020','Gray','10.1 inch tablet, 4GB RAM, 64GB storage',NULL,'WTAB10','10.1 inch','PRD-00084','pcs',1,1,1,21000.00,NULL,NULL,0,NULL,23520.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(85,'Walton Gaming Laptop',6,'890100000085','BATCH-1759052020','Black','Core i7, 16GB RAM, 1TB SSD, GTX 1650 GPU',NULL,'WGL1650','15.6 inch','PRD-00085','pcs',1,1,9,105000.00,NULL,NULL,0,NULL,117600.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(86,'Samsung Monitor 24',20,'890100000086','BATCH-1759052020','Black','24 inch Full HD LED monitor',NULL,'SM24','24 inch','PRD-00086','pcs',9,1,5,17500.00,NULL,NULL,0,NULL,19600.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(87,'Samsung Tablet S6 Lite',12,'890100000087','BATCH-1759052020','Blue','10.4 inch tablet, S-Pen supported',NULL,'S6LITE','10.4 inch','PRD-00087','pcs',9,1,5,32000.00,NULL,NULL,0,NULL,35840.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(88,'Nokia Smart TV 55',9,'890100000088','BATCH-1759052020','Black','55 inch 4K UHD Android Smart TV',NULL,'NSTV55','55 inch','PRD-00088','pcs',10,1,10,68000.00,NULL,NULL,0,NULL,76160.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(89,'Nokia Power Bank',65,'890100000089','BATCH-1759052020','Black','20000mAh fast charging power bank',NULL,'NPB20K',NULL,'PRD-00089','pcs',10,1,6,2800.00,NULL,NULL,0,NULL,3136.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(90,'Wireless Bluetooth Speaker',40,'890100000090','BATCH-1759052069','Black','Portable waterproof Bluetooth speaker with deep bass',NULL,'JBL GO 4','Medium','PRD-00090','pcs',1,1,2,3200.00,NULL,NULL,0,NULL,3584.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(91,'Smartwatch Pro',25,'890100000091','BATCH-1759052069','Silver','Fitness tracking smartwatch with heart rate monitor',NULL,'Amazfit GTR 3','42mm','PRD-00091','pcs',2,1,3,4500.00,NULL,NULL,0,NULL,5040.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(92,'Laptop Backpack',60,'890100000092','BATCH-1759052069','Grey','Anti-theft laptop backpack with USB charging port',NULL,'Tigernu Pro','17 inch','PRD-00092','pcs',3,1,4,2200.00,NULL,NULL,0,NULL,2464.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(93,'4K LED TV 43\"',155,'890100000093','BATCH-1759052069','Black','Smart Android TV with 4K resolution and HDR10',NULL,'Sony Bravia 43X','43 inch','PRD-00093','pcs',4,1,5,19758.06,NULL,NULL,0,NULL,14000.00,'2025-10-18 06:21:06.538745',NULL,'2025-09-30 00:00:00.000000',0,NULL,0,NULL),(94,'Wireless Keyboard & Mouse',45,'890100000094','BATCH-1759052069','Black','Combo pack wireless keyboard and mouse with long battery life',NULL,'Logitech MK270','Standard','PRD-00094','set',5,1,6,1700.00,NULL,NULL,0,NULL,1904.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(95,'Power Bank 20000mAh',75,'890100000095','BATCH-1759052069','White','High capacity fast charging power bank with dual USB output',NULL,'Xiaomi PB200','20000mAh','PRD-00095','pcs',6,1,7,2600.00,NULL,NULL,0,NULL,2912.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(96,'DSLR Camera',10,'890100000096','BATCH-1759052069','Black','Professional DSLR camera with 24.1MP CMOS sensor',NULL,'Canon EOS 200D','Standard','PRD-00096','pcs',7,1,8,54000.00,NULL,NULL,0,NULL,60480.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(97,'Ring Light Kit',55,'890100000097','BATCH-1759052069','White','LED ring light with tripod stand for photography and vlogging',NULL,'Neewer RL18','18 inch','PRD-00097','pcs',8,1,9,2800.00,NULL,NULL,0,NULL,3136.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(98,'Portable SSD 1TB',30,'890100000098','BATCH-1759052069','Black','High speed external SSD drive for data storage',NULL,'Samsung T7','1TB','PRD-00098','pcs',9,1,10,12000.00,NULL,NULL,0,NULL,13440.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(99,'Smart Home Camera',40,'890100000099','BATCH-1759052069','White','WiFi indoor camera with night vision and motion detection',NULL,'Mi Home Cam 360','Compact','PRD-00099','pcs',10,1,1,3500.00,NULL,NULL,0,NULL,3920.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(100,'Noise Cancelling Earbuds',35,'890100000100','BATCH-1759052069','Blue','Wireless earbuds with active noise cancellation',NULL,'Sony WF-1000XM4','Small','PRD-00100','pcs',1,1,2,4900.00,NULL,NULL,0,NULL,5488.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(101,'HDMI Cable 5m',100,'890100000101','BATCH-1759052069','Black','High speed HDMI 2.0 cable for 4K video output',NULL,'Ugreen HDMI5','5 meter','PRD-00101','pcs',2,1,3,700.00,NULL,NULL,0,NULL,784.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(102,'Car Phone Holder',80,'890100000102','BATCH-1759052069','Black','Magnetic mount for smartphones in car dashboard',NULL,'Baseus CarMount','Universal','PRD-00102','pcs',3,1,4,650.00,NULL,NULL,0,NULL,728.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(103,'VR Headset',20,'890100000103','BATCH-1759052069','Black','Virtual reality headset compatible with Android & iOS',NULL,'Oculus Go Lite','Standard','PRD-00103','pcs',4,1,5,3800.00,NULL,NULL,0,NULL,4256.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(104,'Smart LED Bulb',120,'890100000104','BATCH-1759052069','RGB','WiFi enabled smart bulb with color changing feature',NULL,'Mi SmartBulb','9W','PRD-00104','pcs',5,1,6,900.00,NULL,NULL,0,NULL,1008.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(105,'Portable Projector',12,'890100000105','BATCH-1759052069','White','Mini projector with HDMI & USB for home cinema',NULL,'Anker Nebula','Compact','PRD-00105','pcs',6,1,7,11000.00,NULL,NULL,0,NULL,12320.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(106,'USB Hub 7-Port',65,'890100000106','BATCH-1759052069','Black','High speed USB 3.0 hub with 7 ports',NULL,'Orico USB307','Standard','PRD-00106','pcs',7,1,8,1500.00,NULL,NULL,0,NULL,1680.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(107,'Digital Drawing Tablet',25,'890100000107','BATCH-1759052069','Black','Graphic drawing tablet with pressure sensitive pen',NULL,'XP-Pen Deco 01','10 inch','PRD-00107','pcs',8,1,9,5200.00,NULL,NULL,0,NULL,5824.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(108,'Smart Door Lock',8,'890100000108','BATCH-1759052069','Silver','Fingerprint and password based smart door lock',NULL,'Yale SmartLock','Standard','PRD-00108','pcs',9,1,10,14500.00,NULL,NULL,0,NULL,16240.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(109,'Portable Rechargeable Fan',70,'890100000109','BATCH-1759052069','White','Mini USB rechargeable portable desk fan',NULL,'Baseus Breeze','Small','PRD-00109','pcs',10,1,1,1200.00,NULL,NULL,0,NULL,1344.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(110,'LED Monitor 24\"',20,'890100000110','BATCH-1759052110','Black','Full HD LED monitor with HDMI & VGA ports',NULL,'Dell SE2419H','24 inch','PRD-00110','pcs',1,1,2,14500.00,NULL,NULL,0,NULL,16240.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(111,'LED Monitor 27\"',15,'890100000111','BATCH-1759052110','Black','QHD LED monitor with slim bezels',NULL,'LG 27QN600','27 inch','PRD-00111','pcs',2,1,3,22000.00,NULL,NULL,0,NULL,24640.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(112,'Laser Printer',12,'890100000112','BATCH-1759052110','White','Monochrome laser printer with WiFi & Duplex printing',NULL,'HP LaserJet 107w','Standard','PRD-00112','pcs',3,1,4,18500.00,NULL,NULL,0,NULL,20720.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(113,'Ink Tank Printer',10,'890100000113','BATCH-1759052110','Black','All-in-One ink tank printer with scanner & copier',NULL,'Epson L3250','Standard','PRD-00113','pcs',4,1,5,17500.00,NULL,NULL,0,NULL,19600.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(114,'Gaming Laptop',8,'890100000114','BATCH-1759052110','Black','High performance gaming laptop with RTX graphics',NULL,'Asus TUF A15','15.6 inch','PRD-00114','pcs',5,1,6,115000.00,NULL,NULL,0,NULL,128800.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(115,'Ultrabook Laptop',10,'890100000115','BATCH-1759052110','Silver','Lightweight business ultrabook with SSD & fingerprint reader',NULL,'HP EliteBook 840','14 inch','PRD-00115','pcs',6,1,7,89000.00,NULL,NULL,0,NULL,99680.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(116,'Desktop PC Tower',5,'890100000116','BATCH-1759052110','Black','Intel Core i7 12th Gen desktop with 16GB RAM & SSD',NULL,'Lenovo ThinkCentre','ATX Tower','PRD-00116','pcs',7,1,8,75000.00,NULL,NULL,0,NULL,84000.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(117,'External Hard Drive 2TB',30,'890100000117','BATCH-1759052110','Black','USB 3.0 external HDD for data backup',NULL,'WD Elements','2TB','PRD-00117','pcs',8,1,9,8500.00,NULL,NULL,0,NULL,9520.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(118,'RAM DDR4 16GB',40,'890100000118','BATCH-1759052110','Green','High performance DDR4 RAM module 3200MHz',NULL,'Corsair Vengeance LPX','16GB','PRD-00118','pcs',9,1,10,5200.00,NULL,NULL,0,NULL,5824.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(119,'Graphics Card RTX 3060',10,'890100000119','BATCH-1759052110','Black','NVIDIA GeForce RTX 3060 12GB GDDR6 graphics card',NULL,'MSI Ventus 2X','Standard','PRD-00119','pcs',10,1,1,38000.00,NULL,NULL,0,NULL,42560.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(120,'Wireless Router',25,'890100000120','BATCH-1759052110','White','Dual-band WiFi router with MU-MIMO technology',NULL,'TP-Link Archer AX20','Standard','PRD-00120','pcs',1,1,2,4500.00,NULL,NULL,0,NULL,5040.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(121,'Network Switch 16-Port',15,'890100000121','BATCH-1759052110','Black','Gigabit Ethernet switch with plug & play',NULL,'Netgear GS316','16-Port','PRD-00121','pcs',2,1,3,7200.00,NULL,NULL,0,NULL,8064.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(122,'Projector Full HD',8,'890100000122','BATCH-1759052110','White','1080p projector with HDMI & USB input',NULL,'Epson EB-S41','Compact','PRD-00122','pcs',3,1,4,24500.00,NULL,NULL,0,NULL,27440.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(123,'Home Theater System',12,'890100000123','BATCH-1759052110','Black','5.1 channel home theater with Bluetooth & USB',NULL,'Sony HT-S20R','Standard','PRD-00123','set',4,1,5,28500.00,NULL,NULL,0,NULL,31920.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(124,'Soundbar 2.1',18,'890100000124','BATCH-1759052110','Black','Wireless soundbar with subwoofer and Dolby Audio',NULL,'Samsung HW-T450','Standard','PRD-00124','pcs',5,1,6,16500.00,NULL,NULL,0,NULL,18480.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(125,'Smart TV 55\"',10,'890100000125','BATCH-1759052110','Black','Ultra HD 4K Smart LED TV with Netflix & YouTube support',NULL,'LG 55UP7750','55 inch','PRD-00125','pcs',6,1,7,58000.00,NULL,NULL,0,NULL,64960.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(126,'Smart TV 65\"',7,'890100000126','BATCH-1759052110','Black','Ultra HD 4K Android TV with Google Assistant',NULL,'Sony KD-65X80J','65 inch','PRD-00126','pcs',7,1,8,87000.00,NULL,NULL,0,NULL,97440.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(127,'Blu-ray Player',12,'890100000127','BATCH-1759052110','Black','Full HD Blu-ray Disc player with USB playback',NULL,'Sony BDP-S3700','Standard','PRD-00127','pcs',8,1,9,12500.00,NULL,NULL,0,NULL,14000.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(128,'Desktop Speaker Set',35,'890100000128','BATCH-1759052110','Black','2.1 multimedia speakers with deep bass',NULL,'Logitech Z313','Standard','PRD-00128','set',9,1,10,3800.00,NULL,NULL,0,NULL,4256.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(129,'Professional Microphone',20,'890100000129','BATCH-1759052110','Black','USB condenser microphone for streaming & podcasting',NULL,'Blue Yeti Nano','Standard','PRD-00129','pcs',10,1,1,7800.00,NULL,NULL,0,NULL,8736.00,'2025-10-03 00:00:00.000000',NULL,'2025-09-30 00:00:00.000000',NULL,NULL,NULL,NULL),(130,'LED Bulb',200,'10001','BULB2025','White','9W energy-saving LED bulb',NULL,NULL,NULL,'SKU1001','pcs',1,1,1,80.00,NULL,NULL,20,50,120.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(131,'Light Holder',150,'10002','LH2025','White','Plastic bulb holder for LED lights',NULL,NULL,NULL,'SKU1002','pcs',3,1,1,25.00,NULL,NULL,15,30,40.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(132,'Electric Cable',500,'10003','CABLE2025','Red','1.5mm flexible electric cable roll',NULL,NULL,NULL,'SKU1003','roll',2,1,1,300.00,NULL,NULL,30,100,450.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(133,'Fan',80,'10004','FAN2025','White','Ceiling fan 56 inch',NULL,NULL,NULL,'SKU1004','pcs',2,1,2,1200.00,NULL,NULL,10,20,1600.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(134,'Fan Regulator',120,'10005','REG2025','White','Electric fan speed regulator',NULL,NULL,NULL,'SKU1005','pcs',3,1,1,150.00,NULL,NULL,15,25,250.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(135,'Tester',100,'10006','TEST2025','Yellow','Electric line tester screwdriver',NULL,NULL,NULL,'SKU1006','pcs',3,1,1,50.00,NULL,NULL,20,50,80.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(136,'Gang Switch',60,'10007','GS2025','White','3-gang electric wall switch',NULL,NULL,NULL,'SKU1007','pcs',3,1,1,200.00,NULL,NULL,10,25,320.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(137,'Switchboard',40,'10008','SB2025','White','Multi-switch electrical board',NULL,NULL,NULL,'SKU1008','pcs',3,1,1,350.00,NULL,NULL,5,15,500.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(138,'Cut Out',75,'10009','CO2025','White','Fuse cut out for electric line',NULL,NULL,NULL,'SKU1009','pcs',3,1,1,60.00,NULL,NULL,10,20,100.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(139,'Capacitor',120,'10010','CAP2025','Silver','Fan capacitor 2.5uF/3.15uF',NULL,NULL,NULL,'SKU1010','pcs',2,1,1,80.00,NULL,NULL,15,30,130.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(140,'Ceiling Rose',140,'10011','CR2025','White','Ceiling rose for wiring connection',NULL,NULL,NULL,'SKU1011','pcs',3,1,1,30.00,NULL,NULL,10,20,50.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(141,'Electric Cable Clip',250,'10012','ECC2025','White','Cable clip with nail, pack of 100',NULL,NULL,NULL,'SKU1012','pack',3,1,2,40.00,NULL,NULL,20,40,60.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(142,'Cable Tie (Plastic Jip)',200,'10013','CT2025','Black','Plastic cable tie 100 pcs pack',NULL,NULL,NULL,'SKU1013','pack',5,1,2,50.00,NULL,NULL,25,50,90.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(143,'Mobile Charger',180,'10014','MC2025','White','5V mobile charger (fast charging)',NULL,NULL,NULL,'SKU1014','pcs',4,2,3,150.00,NULL,NULL,20,50,250.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(144,'Head Phone',160,'10015','HP2025','Black','Standard wired headphone',NULL,NULL,NULL,'SKU1015','pcs',4,2,3,250.00,NULL,NULL,20,50,400.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(145,'Charging Cable',200,'10016','CC2025','White','USB data cable (Type-C)',NULL,NULL,NULL,'SKU1016','pcs',4,2,3,100.00,NULL,NULL,30,60,180.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(146,'Bluetooth Headphone',100,'10017','BHP2025','Black','Wireless Bluetooth headphone',NULL,NULL,NULL,'SKU1017','pcs',4,2,3,600.00,NULL,NULL,10,25,900.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(147,'Data Cable',220,'10018','DC2025','White','USB data cable for smartphones',NULL,NULL,NULL,'SKU1018','pcs',4,2,3,80.00,NULL,NULL,25,50,150.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(148,'Charging Light',120,'10019','CL2025','White','Rechargeable LED emergency light',NULL,NULL,NULL,'SKU1019','pcs',1,2,3,450.00,NULL,NULL,10,20,650.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(149,'Charger Fan',80,'10020','CF2025','Blue','Rechargeable mini charger fan',NULL,NULL,NULL,'SKU1020','pcs',2,2,3,900.00,NULL,NULL,10,15,1200.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(150,'Gang Suits',50,'10021','GSU2025','Navy Blue','Men’s sports gang suit (L size)',NULL,NULL,NULL,'SKU1021','set',5,3,4,1000.00,NULL,NULL,10,20,1500.00,NULL,NULL,'2025-10-11 00:00:00.000000',NULL,NULL,NULL,NULL),(151,'A4Tech Mini Keyboard',10,'890100000151','BATCH-1760636751','Black','Information Information Information Information Information Information Information Information Information Information Information Information',NULL,'KB001','12\"','PRD-00151','pcs',7,1,6,450.00,'2027-10-31','2025-10-01',2,NULL,550.00,'2025-10-16 17:45:51.083011',0.00,'2025-10-16 17:45:51.083011',12,'Warranty & Guarantee Information',12,'Warranty & Guarantee Information');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order`
--

DROP TABLE IF EXISTS `purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_due` double NOT NULL,
  `amount_paid` double NOT NULL,
  `discount` double NOT NULL,
  `grand_total` double NOT NULL,
  `insert_date` date DEFAULT NULL,
  `purchase_order_no` varchar(255) NOT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_amount` double NOT NULL,
  `total_vat` double NOT NULL,
  `update_date` date DEFAULT NULL,
  `vat_amount` decimal(12,2) DEFAULT NULL,
  `supplier_id` bigint NOT NULL,
  `payment_status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_305j4lwtmyupicsrgqqe0s3rd` (`purchase_order_no`),
  KEY `FK5c1ljr3j297ec1ea0apppger` (`supplier_id`),
  CONSTRAINT `FK5c1ljr3j297ec1ea0apppger` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order`
--

LOCK TABLES `purchase_order` WRITE;
/*!40000 ALTER TABLE `purchase_order` DISABLE KEYS */;
INSERT INTO `purchase_order` VALUES (39,480000,0,0,480000,'2025-10-16','PO-20251016-001','VAT: 0.0','RECEIVED',480000,0,'2025-10-16',0.00,1,'PENDING'),(40,1250000,0,0,1250000,'2025-10-18','PO-20251018-001','VAT: 0.0','RECEIVED',1250000,0,'2025-10-18',0.00,3,'PENDING');
/*!40000 ALTER TABLE `purchase_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order_item`
--

DROP TABLE IF EXISTS `purchase_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `purchase_price` double NOT NULL,
  `quantity` int NOT NULL,
  `selling_price` double NOT NULL,
  `subtotal` double NOT NULL,
  `vat_amount` double NOT NULL,
  `vat_percent` double NOT NULL,
  `product_id` bigint NOT NULL,
  `purchase_order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKppexs9b56y3dqjemtescfc3od` (`product_id`),
  KEY `FKmj122necubadvuquvjoq967y7` (`purchase_order_id`),
  CONSTRAINT `FKmj122necubadvuquvjoq967y7` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_order` (`id`),
  CONSTRAINT `FKppexs9b56y3dqjemtescfc3od` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order_item`
--

LOCK TABLES `purchase_order_item` WRITE;
/*!40000 ALTER TABLE `purchase_order_item` DISABLE KEYS */;
INSERT INTO `purchase_order_item` VALUES (1,48000,10,55000,480000,0,0,1,39),(2,125000,10,14000,1250000,0,0,93,40);
/*!40000 ALTER TABLE `purchase_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `contact_person` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `supplier_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,'Dhaka, Bangladesh','Abdul Rahman','abdul.rahman@example.com','+8801712345671','Rahman Traders'),(2,'Chittagong, Bangladesh','Salma Akter','salma.akter@example.com','+8801712345672','Bangla Electronics'),(3,'Khulna, Bangladesh','Jamal Uddin','jamal.uddin@example.com','+8801712345673','Sundarban Suppliers'),(4,'Rajshahi, Bangladesh','Fatima Begum','fatima.begum@example.com','+8801712345674','Padma Traders'),(5,'Barishal, Bangladesh','Rashed Hossain','rashed.hossain@example.com','+8801712345675','Delta Enterprises'),(6,'Sylhet, Bangladesh','Nusrat Jahan','nusrat.jahan@example.com','+8801712345676','Ganges Suppliers'),(7,'Dhaka, Bangladesh','Hasan Mahmud','hasan.mahmud@example.com','+8801712345677','Bengal Traders'),(8,'Chittagong, Bangladesh','Ayesha Siddique','ayesha.siddique@example.com','+8801712345678','Meghna Electronics'),(9,'Khulna, Bangladesh','Tanvir Alam','tanvir.alam@example.com','+8801712345679','Jamuna Traders'),(10,'Rajshahi, Bangladesh','Farhana Rahman','farhana.rahman@example.com','+8801712345680','Himaloy Suppliers');
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `contact_person` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `supplier_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suppliers`
--

LOCK TABLES `suppliers` WRITE;
/*!40000 ALTER TABLE `suppliers` DISABLE KEYS */;
INSERT INTO `suppliers` VALUES (1,'Dhaka, Bangladesh','Abdul Rahman','abdul.rahman@example.com','+8801712345671','Rahman Traders'),(2,'Chittagong, Bangladesh','Salma Akter','salma.akter@example.com','+8801712345672','Bangla Electronics'),(3,'Khulna, Bangladesh','Jamal Uddin','jamal.uddin@example.com','+8801712345673','Sundarban Suppliers'),(4,'Rajshahi, Bangladesh','Fatima Begum','fatima.begum@example.com','+8801712345674','Padma Traders'),(5,'Barishal, Bangladesh','Rashed Hossain','rashed.hossain@example.com','+8801712345675','Delta Enterprises'),(6,'Sylhet, Bangladesh','Nusrat Jahan','nusrat.jahan@example.com','+8801712345676','Ganges Suppliers'),(7,'Dhaka, Bangladesh','Hasan Mahmud','hasan.mahmud@example.com','+8801712345677','Bengal Traders'),(8,'Chittagong, Bangladesh','Ayesha Siddique','ayesha.siddique@example.com','+8801712345678','Meghna Electronics'),(9,'Khulna, Bangladesh','Tanvir Alam','tanvir.alam@example.com','+8801712345679','Jamuna Traders'),(10,'Rajshahi, Bangladesh','Farhana Rahman','farhana.rahman@example.com','+8801712345680','Himaloy Suppliers');
/*!40000 ALTER TABLE `suppliers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` int NOT NULL,
  `username` varchar(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin@test.gr','Admin','Admin','$2a$12$hq7NkqWrWjc7Y5MyOqsJTeCkSd0ezJy.nfiJDJbsWtxLqwWQtz9Ay',0,'admin');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'salesync'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-18 19:10:51
