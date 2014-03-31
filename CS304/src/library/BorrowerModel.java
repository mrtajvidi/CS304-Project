package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	private ResultSet  rs = null;
	private Statement  stmt = null;
	
	public BorrowerModel() {
		
	}
	
	/**
	 * Search for books using keyword search on titles, authors and subjects. 
	 * The result is a list of books that match the search together with the number of copies that are in and out.
	 * @param keyword string entered by user to search authors, subjects and titles
	 * @return List<Match> which contains a list of all the books and relevant information which are associated with the keyword
	 */
	public List<Match> findKeyword(String keyword) {
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
		
		try {
		stmt = con.createStatement();
		rs = stmt.executeQuery("SELECT book.callNumber, book.title, book.isbn, book.mainauthor, hassubject.subject "
				+ "FROM book, hassubject WHERE book.callNumber = hassubject.callNumber, "
				+ "title LIKE '%" + keyword + "%' OR mainAuthor LIKE '%" + keyword + "%' OR subject LIKE '%" + keyword + "'%");
		
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
	    	callNumber = rs.getString("callNumber");
	    	System.out.printf("%-10.10s", callNumber);
	    	
	    	title = rs.getString("title");
	    	System.out.printf("%-10.10s", title);
	    	
	    	isbn = rs.getString("isbn");
	    	System.out.printf("%-10.10s", isbn);
	    	
	    	mainAuthor = rs.getString("mainAuthor");
	    	System.out.printf("%-10.10s", mainAuthor);
	    	
	    	//publisher = rs.getString("publisher");
	    	//System.out.printf("%-10.10s", publisher);
	    	
	    	//year = rs.getInt("year");
	    	//System.out.printf("%-10.10s", year);
	    	
	    	//callNumber = rs.getString("callNumber");
	    	//System.out.printf("%-10.10s", callNumber);
	    	
	    	subject = rs.getString("subject");
	    	System.out.printf("%-10.10s", subject);
	    	

	    	numOfCopiesIn = FindNumOfCopies(callNumber, "in");
	    	numOfCopiesOut = FindNumOfCopies(callNumber, "out");
	    	
	    	books.add(new Match(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
	    stmt.close();
	    
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
	 */
	private List<Triple> CheckAccountBorrows(Integer bid) {
		//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
		//Fine (fid, amount, issuedDate, paidDate, borid)
		//HoldRequest(hid, bid, callNumber, issuedDate) 
		//
		
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = con.createStatement();
			
			rs = stmt.executeQuery("SELECT * FROM borrowing WHERE inDate = null, bid =" + bid);
			
			ResultSetMetaData rsmd = rs.getMetaData();

			Triple triple;
			List<Triple> borrows = new ArrayList<Triple>();

			while(rs.next()) {
				ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");

				ps.setString(1, rs.getString("callNumber"));

				ResultSet res = ps.executeQuery();

				triple = new Triple(res.getString("title"), res.getString("isbn"), rs.getString("outDate"));
				borrows.add(triple);
			}

			return borrows;

		}
		catch(SQLException e) {
			return null;
		}
	}
	
	private List<Fine> CheckAccountFines(Integer bid) {
		try {
			ps = con.prepareStatement("SELECT borid FROM borrowing WHERE bid = ?");

			ps.setInt(1, bid);

			ResultSet rs = ps.executeQuery();	

			List<Fine> fines = new ArrayList<Fine>();

			while(rs.next()) {
				ps = con.prepareStatement("SELECT * FROM fine WHERE borid = ?, paidDate = null");

				ps.setString(1, rs.getString("borid"));

				ResultSet res = ps.executeQuery();

				Fine fine;

				while(res.next()) {
					fine = new Fine(res.getDouble("amount"), res.getDate("issuedDate"));
					fines.add(fine);
				}
			}

			return fines;

		}
		catch(SQLException e) {
			return null;
		}
	}
	
	private List<HoldRequest> CheckAccountHoldRequests(Integer bid) {
		try
		{
		ps = con.prepareStatement("SELECT * FROM holdrequest WHERE bid = ?");
		
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
	
	
	
	private int FindNumOfCopies(String callNumber, String status) {
		ResultSet rsCopies;
		Statement  stmt;
		
		try {
			stmt = con.createStatement();
			rsCopies = stmt.executeQuery("SELECT * FROM bookcopy WHERE callNumber =" + callNumber + "AND status =" + status);

			// get info on ResultSet
			ResultSetMetaData rsmd = rs.getMetaData();

			// get number of columns
			int numCols = rsmd.getColumnCount();
	    
			int copycount = 0;
	    
			while (rsCopies.next()) {
				copycount++;
			}
	    
		    return copycount;
			}
			catch (SQLException e) {
				return 0;
		}
	}
		
	private void PlaceHoldRequest(Integer hid, Integer bid, String callNumber, Date issuedDate) {
		if (FindNumOfCopies(callNumber, "in") > 0) {
			try 
			{
				ps = con.prepareStatement("INSERT INTO holdrequest VALUES (?,?,?,?)");
				
				ps.setInt(1, hid);
				ps.setInt(2, bid);
				ps.setString(3, callNumber);
				ps.setDate(4, issuedDate);

				ps.executeUpdate();

				con.commit();

				ps.close();
			}
			catch (SQLException e) {

			}
		} else {
			//
		}
	}
	
	private void payFine(Integer fid, Date paidDate) {
		try
		{	
			ps = con.prepareStatement("UPDATE fine SET paidDate = ?, fid = ?");
			
			ps.setDate(1, paidDate);
			ps.setInt(2, fid);
			
			ps.executeUpdate();

			con.commit();

			ps.close();
		}
		catch(SQLException e) {
			
		}
	}

}
