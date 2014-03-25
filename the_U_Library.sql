SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `the_U_Library` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `the_U_Library` ;

-- -----------------------------------------------------
-- Table `the_U_Library`.`BorrowerType`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`BorrowerType` (
  `type` CHAR NOT NULL,
  `bookTimeLimit` DATE NULL,
  PRIMARY KEY (`type`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`Borrower`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`Borrower` (
  `bid` INT NOT NULL,
  `password` VARCHAR(45) NULL,
  `name` CHAR NULL,
  `address` VARCHAR(45) NULL,
  `phone` INT NULL,
  `emailAddress` VARCHAR(45) NULL,
  `sinOrStNo` INT NULL,
  `expiryDate` DATE NULL,
  `type` CHAR NOT NULL,
  PRIMARY KEY (`bid`),
  INDEX `type_idx` (`type` ASC),
  CONSTRAINT `type`
    FOREIGN KEY (`type`)
    REFERENCES `the_U_Library`.`BorrowerType` (`type`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`Book`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`Book` (
  `callNumber` VARCHAR(10) NOT NULL,
  `isbn` INT NULL,
  `title` VARCHAR(20) NULL,
  `mainAuthor` CHAR NULL,
  `publisher` CHAR NULL,
  `year` YEAR NULL,
  PRIMARY KEY (`callNumber`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`HasAuthor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`HasAuthor` (
  `callNumber` VARCHAR(10) NOT NULL,
  `name` CHAR NOT NULL,
  PRIMARY KEY (`callNumber`, `name`),
  CONSTRAINT `callNumber`
    FOREIGN KEY (`callNumber`)
    REFERENCES `the_U_Library`.`Book` (`callNumber`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`BookCopy`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`BookCopy` (
  `callNumber` VARCHAR(10) NOT NULL,
  `copyNo` VARCHAR(3) NOT NULL,
  `status` CHAR NULL,
  PRIMARY KEY (`callNumber`, `copyNo`),
  CONSTRAINT `callNumber`
    FOREIGN KEY (`callNumber`)
    REFERENCES `the_U_Library`.`Book` (`callNumber`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`Borrowing`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`Borrowing` (
  `borid` INT NOT NULL,
  `bid` INT NOT NULL,
  `callNumber` VARCHAR(10) NOT NULL,
  `copyNo` VARCHAR(3) NULL,
  `outDate` DATE NULL,
  `inDate` DATE NULL,
  PRIMARY KEY (`borid`),
  INDEX `bid_idx` (`bid` ASC),
  INDEX `callNumber_idx` (`callNumber` ASC),
  CONSTRAINT `bid`
    FOREIGN KEY (`bid`)
    REFERENCES `the_U_Library`.`Borrower` (`bid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `callNumber`
    FOREIGN KEY (`callNumber`)
    REFERENCES `the_U_Library`.`BookCopy` (`callNumber`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`Fine`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`Fine` (
  `fid` INT NOT NULL,
  `amount` DOUBLE NULL,
  `issuedDate` DATE NULL,
  `paidDate` DATE NULL,
  `borid` INT NOT NULL,
  PRIMARY KEY (`fid`),
  INDEX `borid_idx` (`borid` ASC),
  CONSTRAINT `borid`
    FOREIGN KEY (`borid`)
    REFERENCES `the_U_Library`.`Borrowing` (`borid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`HasSubject`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`HasSubject` (
  `callNumber` VARCHAR(10) NOT NULL,
  `subject` CHAR NOT NULL,
  PRIMARY KEY (`callNumber`, `subject`),
  CONSTRAINT `callNumber`
    FOREIGN KEY (`callNumber`)
    REFERENCES `the_U_Library`.`Book` (`callNumber`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `the_U_Library`.`HoldRequest`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `the_U_Library`.`HoldRequest` (
  `hid` INT NOT NULL,
  `bid` INT NOT NULL,
  `callNumber` VARCHAR(10) NOT NULL,
  `issuedDate` DATE NULL,
  PRIMARY KEY (`hid`),
  INDEX `bid_idx` (`bid` ASC),
  INDEX `callNumber_idx` (`callNumber` ASC),
  CONSTRAINT `bid`
    FOREIGN KEY (`bid`)
    REFERENCES `the_U_Library`.`Borrower` (`bid`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `callNumber`
    FOREIGN KEY (`callNumber`)
    REFERENCES `the_U_Library`.`Book` (`callNumber`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
