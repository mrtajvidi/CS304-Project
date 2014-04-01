package cs304;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Borrower {
    
	private Connection con = null;
	
	public Borrower() {
		con = OracleConnection.getInstance().getConnection();
	}
	
	public List<FourTuple<String,String,Integer,Integer>> findKeywordTitle(String keyword) throws SQLException {
            
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM book WHERE title LIKE '%" + keyword + "%'");
	    
	    ArrayList<FourTuple<String,String,Integer,Integer>> books = new ArrayList<FourTuple<String,String,Integer,Integer>>();
	    
	    while (rs.next()) {
	    	String title = rs.getString("title");
	    	String isbn = rs.getString("isbn");
	    	Integer numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	Integer numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	books.add(new FourTuple<String,String,Integer,Integer>(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
	    return books;
	}
	
	public List<FourTuple<String,String,Integer,Integer>> findKeywordAuthor(String keyword) throws SQLException {
		
	    Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT callNumber FROM book NATURAL JOIN hasauthor WHERE mainAuthor LIKE '%" + keyword + "%' OR name LIKE '%" + keyword +"%'");
	    
	    ArrayList<FourTuple<String,String,Integer,Integer>> books = new ArrayList<FourTuple<String,String,Integer,Integer>>();
	    
	    while (rs.next()) {
	    	PreparedStatement ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
                ps.setString(1, rs.getString("callNumber"));
                ResultSet res = ps.executeQuery();
	    	
	    	String title = res.getString("title");
	    	String isbn = res.getString("isbn");
	    	Integer numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	Integer numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	books.add(new FourTuple<String,String,Integer,Integer>(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
	    return books;
	}
	
	public List<FourTuple<String,String,Integer,Integer>> findKeywordSubject(String keyword) throws SQLException {
            
	    Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT callNumber FROM book NATURAL JOIN hassubject WHERE subject LIKE '%" + keyword + "%'");
	    
	    ArrayList<FourTuple<String,String,Integer,Integer>> books = new ArrayList<FourTuple<String,String,Integer,Integer>>();
	    
	    while (rs.next()) {
	    	PreparedStatement ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
                ps.setString(1, rs.getString("callNumber"));
                ResultSet res = ps.executeQuery();
	    	
	    	String title = res.getString("title");
	    	String isbn = res.getString("isbn");
	    	Integer numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	Integer numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	books.add(new FourTuple<String,String,Integer,Integer>(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
	    return books;
            
	}
	
	public List<ThreeTuple<String,String,String>> CheckAccountBorrows(Integer bid) throws SQLException {
			//PreparedStatement ps = con.prepareStatement("SELECT callNumber FROM borrowing WHERE inDate is null AND bid = ?");

			//ps.setInt(1, bid);
                        System.out.println(String.valueOf(bid));
			//ResultSet rs = ps.executeQuery();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT callNumber FROM borrowing WHERE inDate is null AND bid = " + bid);
            //ResultSet rs = stmt.executeQuery("SELECT callNumber FROM borrowing WHERE inDate is null AND bid = 215");

			ThreeTuple<String,String,String> triple;
			List<ThreeTuple<String,String,String>> borrows = new ArrayList<ThreeTuple<String,String,String>>();

			while(rs.next()) {
                                System.out.println("made it here!");
				PreparedStatement ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");

				ps.setString(1, rs.getString("callNumber"));

				ResultSet res = ps.executeQuery();

				triple = new ThreeTuple<String,String,String>(res.getString("title"), res.getString("isbn"), rs.getString("outDate"));
				borrows.add(triple);
			}

			return borrows;
	}
        
        /*public List<ThreeTuple<String,String,String>> CheckAccountBorrows(Integer bid) {
		
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
					+ " FROM borrowing, book WHERE bid = " + bid + " and borrowing.callNumber = book.callnumber and borrowing.indate is null");
			
			//rs = stmt.executeQuery("SELECT * from borrowing");
			
			ThreeTuple<String,String,String> triple;
			List<ThreeTuple<String,String,String>> borrows = new ArrayList<ThreeTuple<String,String,String>>();

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
		    	
				
				triple = new ThreeTuple<String,String,String>(title, mainAuthor, outDate);
				borrows.add(triple);
				
			}

			return borrows;

		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}*/
        
	public List<ThreeTuple<Integer,Double,Date>> CheckAccountFines(Integer bid) throws SQLException {
			PreparedStatement ps = con.prepareStatement("SELECT borid FROM borrowing WHERE bid = ?");

			ps.setInt(1, bid);

			ResultSet rs = ps.executeQuery();	

			List<ThreeTuple<Integer,Double,Date>> fines = new ArrayList<ThreeTuple<Integer,Double,Date>>();

			while(rs.next()) {
				ps = con.prepareStatement("SELECT * FROM fine WHERE borid = ? AND paidDate is null");

				ps.setString(1, rs.getString("borid"));

				ResultSet res = ps.executeQuery();

				while(res.next()) {
					ThreeTuple<Integer,Double,Date> fine = new ThreeTuple<Integer,Double,Date>(res.getInt("fid"), res.getDouble("amount"), res.getDate("issuedDate"));
					fines.add(fine);
				}
			}

			return fines;
	}
	
	public List<ThreeTuple<String,String,Date>> CheckAccountHoldRequests(Integer bid) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT DISTINCT callNumber FROM holdrequest WHERE bid = ?");
		
		ps.setInt(1, bid);
	    
	    ResultSet rs = ps.executeQuery();
	    
	    ThreeTuple<String,String,Date>holdrequest;
	    List<ThreeTuple<String,String,Date>> holdrequests = new ArrayList<ThreeTuple<String,String,Date>>();
	    
	    while(rs.next()) {
	    	ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
			
			ps.setString(1, rs.getString("callNumber"));
		    
		    ResultSet res = ps.executeQuery();
	    	
	    	holdrequest = new ThreeTuple<String,String,Date>(res.getString("title"), res.getString("isbn"), rs.getDate("issuedDate"));
	    	holdrequests.add(holdrequest);
	    }
	    
	    return holdrequests;
	}
	
	private int FindNumOfCopies(String callNumber, String status) throws SQLException {
            
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bookcopy WHERE callNumber = ? AND status = ?");
	    ps.setString(1, callNumber);
	    ps.setString(2, status);
	    ResultSet rs = ps.executeQuery();
	    
	    int copycount = 0;
            
	    while (rs.next()) {
	    	copycount++;
	    }
	    
	    return copycount;
	}
	
	private void PlaceHoldRequest(Integer bid, String callNumber, Date issuedDate) throws SQLException {
		if (FindNumOfCopies(callNumber, "in") > 0) {
				PreparedStatement ps = con.prepareStatement("INSERT INTO holdrequest VALUES (hid_counter.nextval,?,?,?)");
				
				ps.setInt(1, bid);
				ps.setString(2, callNumber);
				ps.setDate(3, issuedDate);

				ps.executeUpdate();

				con.commit();

				ps.close();
		} else {
			//
		}
	}
	
	public void payFine(Integer fid) throws SQLException {
			PreparedStatement ps = con.prepareStatement("UPDATE fine SET paidDate = sysdate WHERE fid = ?");
			
			ps.setInt(1, fid);
			
			ps.executeUpdate();

			con.commit();

			ps.close();
	}
        
}
