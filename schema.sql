-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema push
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `push` ;

-- -----------------------------------------------------
-- Schema push
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `push` DEFAULT CHARACTER SET utf8 ;
USE `push` ;

-- -----------------------------------------------------
-- Table `push`.`User`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `push`.`User` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `key` VARCHAR(1000) NULL,
  `name` VARCHAR(1000) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `push`.`File`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `push`.`File` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(1000) NOT NULL,
  `hash` VARCHAR(1000) NOT NULL,
  `path` VARCHAR(1000) NOT NULL,
  `is_image` TINYINT(1) NOT NULL,
  `user_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_File_User_idx` (`user_id` ASC),
  CONSTRAINT `fk_File_User`
    FOREIGN KEY (`user_id`)
    REFERENCES `push`.`User` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
