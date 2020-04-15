-- MySQL dump 10.13  Distrib 5.7.17, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dwara_v2_latest
-- ------------------------------------------------------
-- Server version	5.7.11

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
-- Temporary view structure for view `extension_taskfiletype_view`
--

DROP TABLE IF EXISTS `extension_taskfiletype_view`;
/*!50001 DROP VIEW IF EXISTS `extension_taskfiletype_view`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `extension_taskfiletype_view` AS SELECT 
 1 AS `taskfiletype_name`,
 1 AS `extension_name`,
 1 AS `sidecar`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_restore_file`
--

DROP TABLE IF EXISTS `v_restore_file`;
/*!50001 DROP VIEW IF EXISTS `v_restore_file`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `v_restore_file` AS SELECT 
 1 AS `tape_id`,
 1 AS `tapeset_id`,
 1 AS `file_id`,
 1 AS `library_id`,
 1 AS `targetvolume_id`,
 1 AS `action_id`,
 1 AS `user_id`,
 1 AS `libraryclass_id`,
 1 AS `libraryclass_name`,
 1 AS `file_pathname`,
 1 AS `file_size`,
 1 AS `file_crc`,
 1 AS `tape_barcode`,
 1 AS `tape_blocksize`,
 1 AS `tape_finalized`,
 1 AS `tapeset_copy_number`,
 1 AS `storageformat_id`,
 1 AS `storageformat_name`,
 1 AS `file_tape_block`,
 1 AS `file_tape_offset`,
 1 AS `file_tape_encrypted`,
 1 AS `file_tape_deleted`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `extension_taskfiletype_view`
--

/*!50001 DROP VIEW IF EXISTS `extension_taskfiletype_view`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `extension_taskfiletype_view` AS select `taskfiletype`.`name` AS `taskfiletype_name`,`extension`.`name` AS `extension_name`,`extension_taskfiletype`.`sidecar` AS `sidecar` from ((`extension_taskfiletype` join `taskfiletype` on((`taskfiletype`.`id` = `extension_taskfiletype`.`taskfiletype_id`))) join `extension` on((`extension`.`id` = `extension_taskfiletype`.`extension_id`))) order by `taskfiletype`.`id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_restore_file`
--

/*!50001 DROP VIEW IF EXISTS `v_restore_file`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_restore_file` AS select `tape`.`id` AS `tape_id`,`tapeset`.`id` AS `tapeset_id`,`file`.`id` AS `file_id`,`library`.`id` AS `library_id`,`libraryclass_targetvolume`.`targetvolume_id` AS `targetvolume_id`,`libraryclass_action_user`.`action_id` AS `action_id`,`libraryclass_action_user`.`user_id` AS `user_id`,`libraryclass`.`id` AS `libraryclass_id`,`libraryclass`.`name` AS `libraryclass_name`,`file`.`pathname` AS `file_pathname`,`file`.`size` AS `file_size`,`file`.`crc` AS `file_crc`,`tape`.`barcode` AS `tape_barcode`,`tape`.`blocksize` AS `tape_blocksize`,`tape`.`finalized` AS `tape_finalized`,`tapeset`.`copy_number` AS `tapeset_copy_number`,`storageformat`.`id` AS `storageformat_id`,`storageformat`.`name` AS `storageformat_name`,`file_tape`.`block` AS `file_tape_block`,`file_tape`.`offset` AS `file_tape_offset`,`file_tape`.`encrypted` AS `file_tape_encrypted`,`file_tape`.`deleted` AS `file_tape_deleted` from ((((((((`file_tape` join `tape` on((`file_tape`.`tape_id` = `tape`.`id`))) join `tapeset` on((`tape`.`tapeset_id` = `tapeset`.`id`))) join `file` on((`file`.`id` = `file_tape`.`file_id`))) join `library` on((`file`.`library_id` = `library`.`id`))) join `storageformat` on((`storageformat`.`id` = `tapeset`.`storageformat_id`))) join `libraryclass` on((`libraryclass`.`id` = `library`.`libraryclass_id`))) join `libraryclass_targetvolume` on((`libraryclass_targetvolume`.`libraryclass_id` = `library`.`libraryclass_id`))) join `libraryclass_action_user` on((`libraryclass_action_user`.`libraryclass_id` = `library`.`libraryclass_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-04-15 12:01:07
