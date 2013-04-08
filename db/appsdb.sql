SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `appsdb` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `appsdb` ;

-- -----------------------------------------------------
-- Table `appsdb`.`apps`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `appsdb`.`apps` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(128) NOT NULL ,
  `feature_vector` BLOB NULL ,
  `md5` VARCHAR(32) NULL ,
  `x` DOUBLE NULL ,
  `y` DOUBLE NULL ,
  `z` DOUBLE NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
COMMENT = 'Table that stores information about each android app.';



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
