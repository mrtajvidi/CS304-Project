package library;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Calendar;

import javax.swing.text.html.HTMLDocument.Iterator;

public class BorrowerModel {
	
	private Integer user;
	
	public void setUser(Integer newUser){
		this.user = newUser;
	}
	
	public Integer getUser(){
		return this.user;
	}
	
	public BorrowerModel() {
		
	}
	
	/**
	 * Search for books using keyword search on titles, authors and subjects. 
	 * The result is a list of books that match the search together with the number of copies that are in and out.
	 * @param keyword string entered by user to search authors, subjects and titles
	 * @return List<Match> which contains a list of all the books and relevant information which are associated with the keyword
	 */
	private List<Match> findKeyword(String keyword, Connection con) {
		//System.out.println("findKeyword");
		String callNumber;
		String title; 
		String isbn;
		String mainAuthor;
		String publisher;
		String subject;
		Integer year;
		Integer numOfCopiesIn;
		Integer numOfCopiesOut; 
		boolean bookInArray = false;
		
		ResultSet  rs;
		Statement  stmt;
		
		try {
		stmt = con.createStatement();
		rs = stmt.executeQuery("SELECT book.callNumber, book.title, book.isbn, book.mainauthor, hassubject.subject "
				+ "FROM book, hassubject WHERE book.callNumber = hassubject.callNumber and ( "
				+ "title LIKE '%" + keyword + "%' OR title LIKE '"+ keyword + "%' OR title LIKE '%" + keyword + "' OR title LIKE '" + keyword + "'"
				+ "OR mainAuthor LIKE '%" + keyword + "%' OR mainAuthor LIKE '"+ keyword + "%' OR mainAuthor LIKE '%" + keyword + "' OR mainAuthor LIKE '" + keyword + "'"
				+ "OR subject LIKE '%" + keyword + "%' OR subject LIKE '"+ keyword + "%' OR subject LIKE '%" + keyword + "' OR subject LIKE '" + keyword + "'"
				+ ")");
		
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int numCols = rsmd.getColumnCount();
	    
	    ArrayList<Match> books = new ArrayList<Match>();
	    
	    // display column names;
		for (int i = 0; i < numCols; i++)
		{
		    // get column name and print it
		    System.out.printf("%-15s", rsmd.getColumnName(i+1));    
		}

		System.out.println(" ");
		
	    while (rs.next()) {
	    	bookInArray = false;
	    	callNumber = rs.getString("callNumber");
	    	System.out.printf("%-15.15s", callNumber);
	    	
	    	title = rs.getString("title");
	    	System.out.printf("%-15.15s", title);
	    	
	    	isbn = rs.getString("isbn");
	    	System.out.printf("%-15.15s", isbn);
	    	
	    	mainAuthor = rs.getString("mainAuthor");
	    	System.out.printf("%-15.15s", mainAuthor);
	    	
	    	subject = rs.getString("subject");
	    	System.out.printf("%-15.15s", subject);
	    	
	    	numOfCopiesIn = 0;
	    	numOfCopiesOut = 0;
	    	
	    	Match bookTuple = new Match(callNumber, title, mainAuthor, isbn, numOfCopiesIn, numOfCopiesOut);
	    	
	    	for (int i = 0; i < books.size(); i++){
	    		Match temp = books.get(i);
	    		if (temp.title.equals(title)) {
	    			//System.out.printf("Book Already In Array");
	    			bookInArray = true;
	    		}
	    	}
	    	
	    	if (bookInArray == false){
	    		books.add(bookTuple);
	    	}
	    	
	    	System.out.println(" ");
	    }
	    
	    stmt.close();
	    
	    for (int i = 0; i < books.size(); i++){
    		Match temp = books.get(i);
    		numOfCopiesIn = FindNumOfCopies(temp.callNumber, "in", con);
        	numOfCopiesOut = FindNumOfCopies(temp.callNumber, "out", con);
        	
        	temp.numOfCopiesIn = numOfCopiesIn;
        	temp.numOfCopiesOut = numOfCopiesOut;
    	}
	    
	    
	    return books;

	    }
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		   
			return null;
		}
		
	}
	
	
	/**Check his/her account. 
	 * The system will display the items the borrower has currently borrowed and not yet returned,
	 * any outstanding fines and the hold requests that have been placed by the borrower
	 * 
	 * DISPLAY TIME LEFT TO BORROW
	 */
	private List<Triple<String,String,String>> CheckAccountBorrows(Integer bid, Connection con) {
		//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
		//Book (callNumber, isbn, title, mainAuthor, publisher, year )
		//Fine (fid, amount, issuedDate, paidDate, borid)
		//HoldRequest(hid, bid, callNumber, issuedDate) 

		//System.out.println("Check Account Borrows");
		
		Statement stmt;
		ResultSet rs;
		String callNumber;
		String outDate;
		String title;
		String mainAuthor;
		
		System.out.println("CheckAccountBorrows");
		
		try {
			stmt = con.createStatement();
			
			rs = stmt.executeQuery("SELECT borrowing.callnumber, borrowing.outDate, book.title, book.mainAuthor"
					+ " FROM borrowing, book WHERE bid = " + bid + " and borrowing.callNumber = book.callnumber");
			
			//rs = stmt.executeQuery("SELECT * from borrowing");
			
			Triple<String,String,String> triple;
			List<Triple<String,String,String>> borrows = new ArrayList<Triple<String,String,String>>();

			while(rs.next()) {
				//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 

				callNumber = rs.getString("callNumber");
		    	//System.out.printf("%-15.15s", callNumber);
		    	
		    	outDate = rs.getString("outDate");
		    	//System.out.printf("%-15.15s", outDate);
		    	
		    	title= rs.getString("title");
		    	//System.out.printf("%-15.15s", title);
		    	
		    	mainAuthor= rs.getString("mainAuthor");
		    	//System.out.printf("%-15.15s", mainAuthor);
		    	
				
				triple = new Triple<String,String,String>(title, mainAuthor, outDate);
				borrows.add(triple);
				
			}

			return borrows;

		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}
	
	/*
	 * 	private List<Triple<String,String,String>> CheckAccountBorrows(Integer bid) {
		try {
			ps = con.prepareStatement("SELECT callNumber FROM borrowing WHERE inDate = null, bid = ?");

			ps.setInt(1, bid);

			ResultSet rs = ps.executeQuery();

			Triple<String,String,String> triple;
			List<Triple<String,String,String>> borrows = new ArrayList<Triple<String,String,String>>();

			while(rs.next()) {
				ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");

				ps.setString(1, rs.getString("callNumber"));

				ResultSet res = ps.executeQuery();

				triple = new Triple<String,String,String>(res.getString("title"), res.getString("isbn"), rs.getString("outDate"));
				borrows.add(triple);
			}

			return borrows;

		}
		catch(SQLException e) {
			return null;
		}
	}
*/

	private List<Fine> CheckAccountFines(Integer bid, Connection con) {
		//Book (callNumber, isbn, title, mainAuthor, publisher, year )
		//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
		//Fine (fid, amount, issuedDate, paidDate, borid)
		Statement stmt;
		ResultSet rs;
		
		String callNumber;
		double amount;
		String title;
		String mainAuthor;
		Integer fid;
		String date;
		String paidDate;
		
		System.out.println("CheckAccountFines");

		
		try {
			stmt = con.createStatement();
			
			rs = stmt.executeQuery("SELECT borrowing.callnumber, book.title, fine.amount, fine.fid, fine.issuedDate "
					+ " FROM borrowing, book, fine WHERE borrowing.bid = " + bid + " and "
					+ "borrowing.borid = fine.borid and borrowing.callnumber = book.callnumber and paidDate is null");

			//rs = stmt.executeQuery("SELECT * FROM fine WHERE paidDate is null");
			
			List<Fine> fines = new ArrayList<Fine>();

			while(rs.next()) {
				//Fine (fid, amount, issuedDate, paidDate, borid)
				fid = rs.getInt("fid");
		    	System.out.printf("%-15.15s", fid);
				
				callNumber = rs.getString("callnumber");
		    	System.out.printf("%-15.15s", callNumber);
		    	
		    	amount = rs.getFloat("amount");
		    	System.out.printf("%15s", amount);
		    	
		    	title= rs.getString("title");
		    	System.out.printf("%-15.15s", title);
		    	 	
		    	date = rs.getString("issuedDate");
		    	System.out.printf("%-15.15s", date);

		    	System.out.println(" ");
		    	
				Fine fine = new Fine(fid, amount, date, title, callNumber);
				fines.add(fine);
			}

			return fines;

		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}

	
	private List<HoldRequest> CheckAccountHoldRequests(Integer bid, Connection con) {
		//Book (callNumber, isbn, title, mainAuthor, publisher, year )
		//HoldRequest(hid, bid, callNumber, issuedDate) 
		
		System.out.println("CheckAccountHoldRequests");
		
		Statement stmt;
		ResultSet rs;
		
		try
		{
		//ps = con.prepareStatement("SELECT * FROM holdrequest WHERE bid = ?");
		
		stmt = con.createStatement();
		
		rs = stmt.executeQuery("SELECT book.callnumber, book.title, holdrequest.issuedDate "
				+ " FROM holdrequest, book WHERE holdrequest.callNumber = book.callNumber "
				+ "and holdrequest.bid = " + bid);
		
	    
	    HoldRequest holdrequest;
	    List<HoldRequest> holdrequests = new ArrayList<HoldRequest>();
	    
	    while(rs.next()) {    	
	    	holdrequest = new HoldRequest(rs.getString("callNumber"), rs.getString("title"), rs.getString("issuedDate"));
	    	holdrequests.add(holdrequest);
	    }
	    
	    return holdrequests;
	    
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}
	
	

	/*
	private List<HoldRequest> CheckAccountHoldRequests(Integer bid) {
		try
		{
		ps = con.prepareStatement("SELECT DISTINCT callNumber FROM holdrequest WHERE bid = ?");

		ps.setInt(1, bid);

	    ResultSet rs = ps.executeQuery();

	    HoldRequest holdrequest;
	    List<HoldRequest> holdrequests = new ArrayList<HoldRequest>();

	    while(rs.next()) {
	    	ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");

			ps.setString(1, rs.getString("callNumber"));

		    ResultSet res = ps.executeQuery();

	    	holdrequest = new HoldRequest(res.getString("title"), res.getString("isbn"), rs.getDate("issuedDate"));
	    	holdrequests.add(holdrequest);
	    }

	    return holdrequests;

		}
		catch(SQLException e) {
			return null;
		}
	}
	 */
	
	private int FindNumOfCopies(String callNumber, String status, Connection con) {
		ResultSet rs;
		Statement  stmt;		
		String callNum;
		String copyNo; 
		String stat;
		
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM bookcopy WHERE callNumber ='" + callNumber + "' AND status = '" + status + "'");
			//rs = stmt.executeQuery("SELECT * FROM bookcopy");

			// get info on ResultSet
			ResultSetMetaData rsmd = rs.getMetaData();

			// get number of columns
			int numCols = rsmd.getColumnCount();
	    
			System.out.println(" ");
			
			 // display column names;
			for (int i = 0; i < numCols; i++)
			{
			    // get column name and print it
			    System.out.printf("%-15s", rsmd.getColumnName(i+1));    
			}
			
			System.out.println(" ");

			
			int copycount = 0;
	    
			while (rs.next()) 
			{	
				callNum = rs.getString("callNumber");
		    	System.out.printf("%-15.15s", callNum);
		    	
		    	copyNo = rs.getString("copyNo");
		    	System.out.printf("%-15.15s", copyNo);
				
		    	stat = rs.getString("status");
		    	System.out.printf("%-15.15s", stat);
		    	
				copycount++;
				System.out.println(" ");
			}
			
			stmt.close();
			System.out.println(copycount);
		    return copycount;
			}
			catch (SQLException e) {
				System.out.println("Message: " + e.getMessage());
				return 0;
		}
	}
	
	
	
	 private void showBookCopyTable(Connection con)
	    {
		String     callNumber;
		String     copyNo;
		String     status;

		Statement  stmt;
		ResultSet  rs;
		   
		try
		{
		  stmt = con.createStatement();

		  rs = stmt.executeQuery("SELECT * FROM bookcopy");

		  // get info on ResultSet
		  ResultSetMetaData rsmd = rs.getMetaData();

		  // get number of columns
		  int numCols = rsmd.getColumnCount();

		  System.out.println(" ");
		  
		  // display column names;
		  for (int i = 0; i < numCols; i++)
		  {
		      // get column name and print it

		      System.out.printf("%-15s", rsmd.getColumnName(i+1));    
		  }

		  System.out.println(" ");

		  while(rs.next())
		  {
		      // for display purposes get everything from Oracle 
		      // as a string

		      // simplified output formatting; truncation may occur

		      callNumber = rs.getString("callnumber");
		      System.out.printf("%-10.10s", callNumber);

		      copyNo = rs.getString("copyno");
		      System.out.printf("%-20.20s", copyNo);

		      status = rs.getString("status");
		      System.out.printf("%-15.15s", status);
		      
		      System.out.println(" ");
     
		  }
		  // close the statement; 
		  // the ResultSet will also be closed
		  stmt.close();
		}
		catch (SQLException ex)
		{
		    System.out.println("Message: " + ex.getMessage());
		}	
}

		
	/**
	 * Place a hold request for a book that is out. When the item is returned, the system sends an 
email to the borrower and informs the library clerk to keep the book out of the shelves. 
	 * @param bid
	 * @param callNumber
	 * @param con
	 */
	private void PlaceHoldRequest(Integer bid, String callNumber, Connection con) {
		
		//HoldRequest(hid, bid, callNumber, issuedDate) 
		PreparedStatement  ps;
		ResultSet  rs;
		Statement  stmt;
		int hid = 0;
		
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		java.util.Date today = Calendar.getInstance().getTime();        

		String issuedDate = df.format(today);
		
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM holdrequest");
			
			while(rs.next())
			  {
				hid = rs.getInt("hid");			
			  }
			
			hid ++;
		    System.out.println("HID: " + hid);

			stmt.close();
			
		}catch(SQLException ex){
		    System.out.println("Message: " + ex.getMessage());
		}

		
		if (FindNumOfCopies(callNumber, "in", con) > 0) {
		    System.out.println("There is a copy of this book in the library currently.");
		}else{
			try 
			{
				ps = con.prepareStatement("INSERT INTO holdrequest VALUES (?,?,?,?)");
				
				ps.setInt(1, hid);
				ps.setInt(2, bid);
				ps.setString(3, callNumber);
				ps.setString(4, issuedDate);

				ps.executeUpdate();

				con.commit();

				ps.close();
			}
			catch (SQLException e) {
				System.out.println("Message: " + e.getMessage());
			}
		}
	}

	
	
	private void payFine(Integer bid, Connection con) {
		//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
		//Fine (fid, amount, issuedDate, paidDate, borid) 

		Integer fid = 0; 
		
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		java.util.Date today = Calendar.getInstance().getTime();        

		String paidDate = df.format(today);
		
		PreparedStatement  ps;
		ResultSet  rs;
		Statement  stmt;
			
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT fine.fid FROM borrowing, fine WHERE borrowing.borid = fine.borid AND borrowing.bid = " + bid);
			
			while(rs.next())
			  {
				fid = rs.getInt("fid");			
			  }
			
		    System.out.println("FID: " + fid);

			stmt.close();
			
		}catch(SQLException ex){
		    System.out.println("Message: " + ex.getMessage());
		}
		
		try
		{	
			ps = con.prepareStatement("UPDATE fine SET paidDate = ? WHERE fid = ?");
			
			ps.setString(1, paidDate);
			ps.setInt(2, fid);
			
			ps.executeUpdate();

			con.commit();
			System.out.println("Fine Paid");
			ps.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
	}

}
