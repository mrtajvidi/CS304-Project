package cpsc304proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	
	public BorrowerModel() {
		
	}
	
	private List<Match> findKeywordTitle(String keyword) {
		try {
		ps = con.prepareStatement("SELECT * FROM book WHERE title LIKE '%?%'");
		
	    ps.setString(1, keyword);

	    ResultSet rs = ps.executeQuery();
	    
	    ArrayList<Match> books = new ArrayList<Match>();
	    
	    while (rs.next()) {
	    	String title = rs.getString("title");
	    	Integer isbn = rs.getInt("isbn");
	    	Integer numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	Integer numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	books.add(new Match(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
	    return books;
	    
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	private List<Match> findKeywordAuthor(String keyword) {
		try {
		ps = con.prepareStatement("SELECT DISTINCT callNumber FROM book NATURAL JOIN hasauthor WHERE mainAuthor LIKE '%?%' OR name LIKE '%?%'");
		
	    ps.setString(1, keyword);

	    ResultSet rs = ps.executeQuery();
	    
	    ArrayList<Match> books = new ArrayList<Match>();
	    
	    while (rs.next()) {
	    	ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");

			ps.setString(1, rs.getString("callNumber"));

			ResultSet res = ps.executeQuery();
	    	
	    	String title = res.getString("title");
	    	Integer isbn = res.getInt("isbn");
	    	Integer numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	Integer numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	books.add(new Match(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
	    return books;
	    
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	private List<Match> findKeywordSubject(String keyword) {
		try {
		ps = con.prepareStatement("SELECT DISTINCT callNumber FROM book NATURAL JOIN hassubject WHERE subject LIKE '%?%'");
		
	    ps.setString(1, keyword);

	    ResultSet rs = ps.executeQuery();
	    
	    ArrayList<Match> books = new ArrayList<Match>();
	    
	    while (rs.next()) {
	    	ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");

			ps.setString(1, rs.getString("callNumber"));

			ResultSet res = ps.executeQuery();
	    	
	    	String title = res.getString("title");
	    	Integer isbn = res.getInt("isbn");
	    	Integer numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	Integer numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	books.add(new Match(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
	    return books;
	    
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	private List<Triple<String,String,String>> CheckAccountBorrows(Integer bid) {
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

				while(res.next()) {
					Fine fine = new Fine(res.getInt("fid"), res.getDouble("amount"), res.getDate("issuedDate"));
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
	
	private int FindNumOfCopies(String callNumber, String status) {
		try {
		ps = con.prepareStatement("SELECT * FROM bookcopy WHERE callNumber = ? AND status = ?");
		
	    ps.setString(1, callNumber);
	    ps.setString(2, status);

	    ResultSet rs = ps.executeQuery();
	    
	    int copycount = 0;
	    
	    while (rs.next()) {
	    	copycount++;
	    }
	    
	    return copycount;
		}
		catch (SQLException e) {
			return 0;
		}
	}
	
	private void PlaceHoldRequest(Integer bid, String callNumber, Date issuedDate) {
		if (FindNumOfCopies(callNumber, "in") > 0) {
			try 
			{
				ps = con.prepareStatement("INSERT INTO holdrequest VALUES (hid_counter.nextval,?,?,?)");
				
				ps.setInt(1, bid);
				ps.setString(2, callNumber);
				ps.setDate(3, issuedDate);

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
			ps = con.prepareStatement("UPDATE fine SET paidDate = ? WHERE fid = ?");
			
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
