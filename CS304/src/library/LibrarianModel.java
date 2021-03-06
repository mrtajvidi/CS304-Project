package library;

import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.security.auth.Subject;

public class LibrarianModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	private ResultSet  rs = null;
	private Statement  stmt = null;
	
	public LibrarianModel() {
		
	}
	
	
	private void addBookStandard(String callNumber, String isbn, String title, String mainAuthor, 
			String publisher, String yearTemp, String subject,String copyNo, String status, Connection con){

		int year;
		
		PreparedStatement psBook;
		PreparedStatement psAuthor;
		PreparedStatement psSubject;
		PreparedStatement psCopy;

		boolean moreCopies = true;
		String copyVal; 
		
		try
		{
		  psBook = con.prepareStatement("INSERT INTO Book VALUES (?,?,?,?,?,?)");
		  psAuthor = con.prepareStatement("INSERT INTO HasAuthor VALUES (?,?)");
		  psSubject = con.prepareStatement("INSERT INTO HasSubject VALUES (?,?)");
		  psCopy = con.prepareStatement("INSERT INTO BookCopy VALUES (?,?,?)");
		  
		  psBook.setString(1, callNumber);
		  psAuthor.setString(1, callNumber);
		  psSubject.setString(1, callNumber);
		  psCopy.setString(1, callNumber);

		  psBook.setString(2, isbn);

		  psBook.setString(3, title);

		  psBook.setString(4, mainAuthor);
		  psAuthor.setString(2, mainAuthor);

		  if (yearTemp.length() == 0)
		  {
		      psBook.setNull(6, java.sql.Types.INTEGER);
		  }
		  else
		  {
		      year = Integer.parseInt(yearTemp);
		      psBook.setInt(6, year);
		  }
		  	  
		  if (publisher.length() == 0)
	          {
		      psBook.setString(5, null);
		  }
		  else
		  {
		      psBook.setString(5, publisher);
		  }

		  psSubject.setString(2, subject);

		  psCopy.setString(2, copyNo);

		  psCopy.setString(3, status);
			 
		  psBook.executeUpdate();
		  psAuthor.executeUpdate();
		  psSubject.executeUpdate();
		  psCopy.executeUpdate();
		  
		  // commit work 
		  con.commit();

		  psBook.close();
		  psAuthor.close();
		  psSubject.close();
		  psCopy.close();
		  
		}
		catch (SQLException ex)
		{
			System.out.println("Message: " + ex.getMessage());
		}
	}
	
	private void addMoreAuthor(String callNumber, String name,Connection con)
	{
		PreparedStatement psAuthor;
		
		try{
			psAuthor = con.prepareStatement("INSERT INTO HasAuthor VALUES (?,?)");
			
			psAuthor.setString(1, callNumber);
			psAuthor.setString(2, name);
			psAuthor.executeUpdate();
			
			con.commit();
			psAuthor.close();
			
		}
		catch (SQLException ex)
		{
			System.out.println("Message: " + ex.getMessage());
		}
	}
	
	private void addMoreSubject(String callNumber, String subject, Connection con)
	{
		PreparedStatement psSubject;
		
		try{
			psSubject = con.prepareStatement("INSERT INTO HasSubject VALUES (?,?)");
			
			psSubject.setString(1, callNumber);
			psSubject.setString(2, subject);
			psSubject.executeUpdate();
			
			con.commit();
			psSubject.close();
		}
		catch (SQLException ex)
		{
			System.out.println("Message: " + ex.getMessage());
		}
	}
	

	private void addMoreCopies(String callNumber, String copyNo, String status, Connection con)
	{
		PreparedStatement psCopy;
		
		try
		{
			psCopy = con.prepareStatement("INSERT INTO BookCopy VALUES (?,?,?)");
			psCopy.setString(1, callNumber);
			psCopy.setString(2, copyNo);
			psCopy.setString(3, status);
			psCopy.executeUpdate();
			
			con.commit();
			psCopy.close();
		}
		catch (SQLException ex)
		{
			System.out.println("Message: " + ex.getMessage());
		}
		
	}
	
	private List<CheckedOut> generateReport_CheckedOut(String subject, Connection con){
		//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
		//BookCopy (callNumber, copyNo, status) 
		
		String callNumber;
		String copyNo;
		String status;
		String borid;
		int bid;
		String outdate;
		String indate;
		
		String overDue_column_name = "Overdue";
		int overdue_flag = 0;
		
		Statement  stmt;
		ResultSet  rsStatus;
		ResultSet  rsDate;
		
		try
		{
		  stmt = con.createStatement();
		  
		  if(subject.equals(""))
		  {
			  rsStatus = stmt.executeQuery("SELECT Borrowing.bid, BookCopy.callNumber, BookCopy.copyNo, Status, Borid, Outdate, Indate "
				  		+ "FROM BookCopy, Borrowing "
				  		+ "WHERE BookCopy.callNumber = Borrowing.callNumber and BookCopy.copyNo = Borrowing.copyNo and status = 'out'"
				  		+ "ORDER BY callNumber");
		  }
		  else //Edit
		  {
			  rsStatus = stmt.executeQuery("SELECT Borrowing.bid, BookCopy.callNumber, BookCopy.copyNo, Status, Borid, Outdate, Indate "
				  		+ "FROM BookCopy, Borrowing, HasSubject "
				  		+ "WHERE BookCopy.callNumber = Borrowing.callNumber and BookCopy.copyNo = Borrowing.copyNo and status = 'out' "
				  		+ "AND HasSubject.subject = '" + subject + "' AND Borrowing.callNumber = HasSubject.callNumber "
				  		+ " ORDER BY callNumber");
		  }
		  
		  // get info on ResultSet
		  ResultSetMetaData rsmd = rsStatus.getMetaData();

		  // get number of columns
		  int numCols = rsmd.getColumnCount();

		  ArrayList<CheckedOut> outStatus = new ArrayList<CheckedOut>();
		  //System.out.println(" ");
		  
		  // display column names;
		  for (int i = 0; i < numCols; i++)
		  {
		      // get column name and print it

		      System.out.printf("%-15s", rsmd.getColumnName(i+1)); 
		      if(i == numCols - 1)
		    	  System.out.printf("%-15s", overDue_column_name); 
		  }

		  System.out.println(" ");

		  while(rsStatus.next())
		  {
			  //BookCopy.callNumber, BookCopy.copyNo, Status, Borid, Outdate, Indate
			  bid = rsStatus.getInt("bid");
		      System.out.printf("%-10.10s", bid);
			  
		      callNumber = rsStatus.getString("callNumber");
		      System.out.printf("%-20.20s", callNumber);

		      copyNo = rsStatus.getString("copyNo");
		      System.out.printf("%-10.10s", copyNo);

		      status = rsStatus.getString("status");
		      System.out.printf("%-15.15s", status);
		      
		      borid = rsStatus.getString("borid");
		      System.out.printf("%-15.15s", borid);
    
		      outdate = rsStatus.getString("outdate");
		      System.out.printf("%-15.15s", outdate);
		     
		      indate = rsStatus.getString("indate");
		      System.out.printf("%-15.15s", indate);
		      
		      String dueDate = ComputeDueDate(bid, outdate, con);
				
		      Date dueDate_Date = stringToDate(dueDate);
		      Date outDate_Date = stringToDate(outdate);
		      Date today_Date = getTodayDate();
		      
		      if (outDate_Date != null && indate == null && today_Date.compareTo(dueDate_Date) > 0) {
		    	  overdue_flag = 1;
		    	  System.out.printf("%-15.15s", overdue_flag);
		      }
		      else 
		      {
		    	  overdue_flag = 0;
		    	  System.out.printf("%-15.15s", overdue_flag);
		      }
		      
		      System.out.println(" ");
		      
			  //CheckedOut(String callNumber, String copyNumber, String status, String borid, String outDate, String inDate) 

		      outStatus.add(new CheckedOut(callNumber, copyNo, status, borid, outdate, indate, overdue_flag));
		  }
	 
		  // close the statement; 
		  // the ResultSet will also be closed
		  stmt.close();
		  
		  return outStatus;
		}
		catch (SQLException ex)
		{
		    System.out.println("Message: " + ex.getMessage());
		    return null;
		}	
	}
	
	private List<Popular> find_Popular(String year, String n, Connection con)
	{
		int n_convert = Integer.parseInt(n);
		String title;
		String callNumber;
		int count;
		
		Statement  stmt;
		ResultSet  rsPopular;
		
		try
		{
		  stmt = con.createStatement();

		  rsPopular = stmt.executeQuery("SELECT Book.title, Borrowing.callNumber, count(*) AS borrowed_count "
		  		+ "FROM Borrowing INNER JOIN Book ON (Borrowing.callNumber=Book.callNumber) "
		  		+ "WHERE Borrowing.outDate Like '%" + year + "' "
		  		+ "GROUP BY Borrowing.callNumber "
		  		+ "ORDER BY borrowed_count desc limit " + n + ";");		  

		  // get info on ResultSet
		  ResultSetMetaData rsmd = rsPopular.getMetaData();

		  // get number of columns
		  int numCols = rsmd.getColumnCount();

		  ArrayList<Popular> borrowed_popular = new ArrayList<Popular>();
		  //System.out.println(" ");
		  
		  // display column names;
		  for (int i = 0; i < numCols; i++)
		  {
		      // get column name and print it

		      System.out.printf("%-15s", rsmd.getColumnName(i+1));    
		  }

		  System.out.println(" ");

		  while(rsPopular.next())
		  {
		      // for display purposes get everything from Oracle 
		      // as a string

		      // simplified output formatting; truncation may occur
			  
			  title = rsPopular.getString("Book.title");
		      System.out.printf("%-10.10s", title);

		      callNumber = rsPopular.getString("Borrowing.callNumber");
		      System.out.printf("%-20.20s", callNumber);

		      count = rsPopular.getInt("borrowed_count");
		      System.out.printf("%-20.20s", count);
		      
		      System.out.println(" ");
		      
		      borrowed_popular.add(new Popular(title, callNumber, count));
		  }
	 
		  // close the statement; 
		  // the ResultSet will also be closed
		  stmt.close();
		  
		  return borrowed_popular;
		}
		catch (SQLException ex)
		{
		    System.out.println("Message: " + ex.getMessage());
		    return null;
		}	
	}
	
	private String ComputeDueDate(Integer bid, String outDate, Connection con) {
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = con.createStatement();
			
			rs = stmt.executeQuery("SELECT type FROM borrower WHERE bid = " + bid);

			String type = null;
			
			while(rs.next()){

				type = rs.getString("type");
//				System.out.println("type: " + type);
			
			}
				
			rs = stmt.executeQuery("SELECT bookTimeLimit FROM borrowertype WHERE type = '" + type +"'");

			Integer timelimit = 0;

			while (rs.next()){
				timelimit = rs.getInt("bookTimeLimit");
//				System.out.println("Time Limit: " + timelimit);
			}
			
			
			
			String date = AdjustDate(outDate, timelimit);
			
			return date;
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}
	
	private String AdjustDate(String outDate, int length) {
		Date temp, result;
		String output;

		temp = stringToDate(outDate);
		result = addDate(temp, length*7 );
		output = dateToString(result);

		return output;
	}
	
	private Date getTodayDate(){
		
		Date todayDate = new Date();
		
		return todayDate;
	}

	private Date stringToDate(String string_input)
	{
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date date_output = null;

		try
		{
			date_output = df.parse(string_input);
		}
		catch (ParseException e) 
		{
			e.printStackTrace();
		}

		return date_output;
	}

	private String dateToString(Date date_input) {

		String string_output = null;

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		string_output = df.format(date_input);

		return string_output;
	}

	private Date addDate(Date inputDate, int amount_add) {

		Date result;

		Calendar c = Calendar.getInstance(); 
		c.setTime(inputDate);

		c.add(Calendar.DATE, amount_add);
		result = c.getTime();

		return result;
	}
}