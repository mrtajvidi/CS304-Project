
CREATE TABLE BorrowerType (
  type CHAR(20) NOT NULL PRIMARY KEY,
  bookTimeLimit DATE);


CREATE TABLE Borrower (
  bid integer NOT NULL primary key,
  password VARCHAR(45),
  name CHAR(30),
  address VARCHAR(45),
  phone integer,
  emailAddress VARCHAR(45),
  sinOrStNo integer,
  expiryDate DATE NULL,
  type CHAR(20) NOT NULL,
  FOREIGN KEY (type) REFERENCES BorrowerType);


CREATE TABLE Book (
  callNumber VARCHAR(10) NOT NULL primary key,
  isbn varchar(13),
  title VARCHAR(20),
  mainAuthor varchar(30),
  publisher varchar(30),
  year integer);


CREATE TABLE HasAuthor (
  callNumber VARCHAR(10) NOT NULL primary key,
  name varchar(30) NOT NULL,
  FOREIGN KEY (callNumber) REFERENCES Book ON DELETE CASCADE);


CREATE TABLE BookCopy (
  callNumber VARCHAR(10) NOT NULL primary key,
  copyNo VARCHAR(3) NOT NULL,
  status CHAR(10),
  FOREIGN KEY (callNumber) REFERENCES Book );


CREATE TABLE Borrowing (
  borid integer NOT NULL primary key,
  bid integer NOT NULL,
  callNumber VARCHAR(10) NOT NULL,
  copyNo VARCHAR(3),
  outDate DATE,
  inDate DATE,
  FOREIGN KEY (bid) REFERENCES Borrower,
  FOREIGN KEY (callNumber) REFERENCES BookCopy);    
  
  

CREATE TABLE Fine (
  fid integer NOT NULL primary key,
  amount float,
  issuedDate DATE,
  paidDate DATE ,
  borid integer NOT NULL,
  FOREIGN KEY (borid) REFERENCES Borrowing (borid));


CREATE TABLE HasSubject (
  callNumber varchar(10) NOT NULL primary key,
  subject char(20) NOT NULL,
  FOREIGN KEY (callNumber) REFERENCES Book ON DELETE CASCADE);


CREATE TABLE HoldRequest (
  hid integer NOT NULL primary key,
  bid integer NOT NULL,
  callNumber VARCHAR(10) NOT NULL,
  issuedDate DATE,
  FOREIGN KEY (bid) REFERENCES Borrower (bid),
  FOREIGN KEY (callNumber) REFERENCES Book);


