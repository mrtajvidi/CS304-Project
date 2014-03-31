
CREATE TABLE BorrowerType (
  type CHAR(20) NOT NULL PRIMARY KEY,
  bookTimeLimit Integer);


CREATE TABLE Borrower (
  bid integer NOT NULL primary key,
  password VARCHAR(45),
  name CHAR(30),
  address VARCHAR(45),
  phone integer,
  emailAddress VARCHAR(45),
  sinOrStNo integer,
  expiryDate VARCHAR(20),
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
  callNumber VARCHAR(10) NOT NULL,
  name varchar(30) NOT NULL,
  CONSTRAINT author PRIMARY KEY (callNumber, name),
  FOREIGN KEY (callNumber) REFERENCES Book ON DELETE CASCADE);


CREATE TABLE BookCopy (
  callNumber VARCHAR(10) NOT NULL,
  copyNo VARCHAR(3) NOT NULL,
  status CHAR(10),
  CONSTRAINT copy PRIMARY KEY (callNumber, copyNo),
  FOREIGN KEY (callNumber) REFERENCES Book (callNumber) );


CREATE TABLE Borrowing (
  borid integer NOT NULL primary key,
  bid integer NOT NULL,
  callNumber VARCHAR(10) NOT NULL,
  copyNo VARCHAR(3) not null,
  outDate VARCHAR(20),
  inDate varchar(20),
  FOREIGN KEY (bid) REFERENCES Borrower,
  FOREIGN KEY (callNumber, copyNo) REFERENCES BookCopy (callNumber, copyNo));    
  
CREATE TABLE Fine (
  fid integer NOT NULL primary key,
  amount float,
  issuedDate Varchar(20),
  paidDate varchar(20),
  borid integer NOT NULL,
  FOREIGN KEY (borid) REFERENCES Borrowing (borid));


CREATE TABLE HasSubject (
  callNumber varchar(10) NOT NULL,
  subject char(20) NOT NULL,
  CONSTRAINT sub PRIMARY KEY (callNumber, subject),
  FOREIGN KEY (callNumber) REFERENCES Book ON DELETE CASCADE);


CREATE TABLE HoldRequest (
  hid integer NOT NULL primary key,
  bid integer NOT NULL,
  callNumber VARCHAR(10) NOT NULL,
  issuedDate VarChar(20),
  FOREIGN KEY (bid) REFERENCES Borrower (bid),
  FOREIGN KEY (callNumber) REFERENCES Book);


