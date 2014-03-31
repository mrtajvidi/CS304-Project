insert into BorrowerType values ( 'student', 2 );

insert into BorrowerType values ( 'staff', 6 );

insert into BorrowerType values ( 'faculty', 12 );

insert into Borrower values ( 10 , 'pass01', 'Sean', '123 xyz avenue', 7787778888, 'sean@abc.com', 11223344, '2014/12/12' , 'student');

insert into Borrower values ( 20 , 'pass02', 'Erin', '456 wvu avenue', 7788887777, 'erin@abc.com', 22334455, '2014/11/22' , 'staff');

insert into Borrower values( 30 , 'pass03', 'Collin', '789 tgwe avenue', 6046040000, 'collin@abc.com', 66778899, '2014/01/12' , 'faculty');

insert into Borrower values ( 40 , 'pass04', 'Homayoun', '3444 gggggg avenue', 7777777777, 'homayoun@abc.com', 88554455, '2013/08/22' , 'faculty');


insert into Book values ( '101' , 'isbnaaaabbbb', 'The Book of ELi', 'Homayoun', 'UBC', 2020);

insert into Book values ( '102' , 'isbnCCCCDDDD', 'ABC Book', 'Sean', 'MIT', 2012);

insert into Book values ( '103' , 'isbneeeeffff', 'Design Patterns', 'Collin', 'Springer', 2010);

insert into HasAuthor values ( '102' , 'Sean');
insert into HasAuthor values( '101' , 'Homayoun');
insert into HasAuthor values( '103' , 'Collin');


insert into HasSubject values('101', 'A nice movie'); 
insert into HasSubject values('102', 'About ABC'); 
insert into HasSubject values('103', 'About Patterns');


insert into HoldRequest values(1, 10, '103', '2014/02/01'); 
insert into HoldRequest values(2, 20, '101', '2014/03/01');  


insert into BookCopy values ( '101' , '3', 'on-hold');
insert into BookCopy values ( '101' , '1', 'in');
insert into BookCopy values ( '101' , '5', 'out');


insert into BookCopy values ( '102' , '2', 'on-hold');
insert into BookCopy values ( '102' , '6', 'in');
insert into BookCopy values ( '102' , '3', 'out');


insert into BookCopy values ( '103' , '20', 'on-hold');
insert into BookCopy values ( '103' , '0', 'in');
insert into BookCopy values ( '103' , '15', 'out');


insert into Borrowing values (1, 10, '101', '5', '2013/10/14', null);

insert into Borrowing values (2, 10, '101', '1', '2013/11/16', '2013/10/15');


insert into Borrowing values (3, 20, '102', '2', '2013/08/15', null);

insert into Borrowing values (4, 20, '102', '6', '2013/05/13', '2013/05/15'); --TODO: CHECK IN DATE IS GREATER THAN OUT DATE


insert into Fine values (11, 23.98, '2013/10/15', NULL , 1);
insert into Fine values (12, 2222.13, '2013/08/16', '2013/08/18', 3);




