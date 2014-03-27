package cpsc304proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	
	public BorrowerModel() {
		
	}
	
	private List<Match> findKeyword(String keyword) {
		try {
		ps = con.prepareStatement("SELECT * FROM book b, hassubject h WHERE b.callNumber = h.callNumber, title = ? OR mainAuthor = ? OR subject = ?");
		
	    ps.setString(1, keyword);
	    ps.setString(2, keyword);
	    ps.setString(3, keyword);

	    ResultSet rs = ps.executeQuery();
	    
	    ArrayList<Match> books = new ArrayList<Match>();
	    
	    while (rs.next()) {
	    	String title = rs.getString("title");
	    	Integer isbn = rs.getInt("isbn");
	    	Integer numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	Integer numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	books.add(new Match(title, isbn, numOfCopiesIn, numOfCopiesOut));
	    }
	    
/*	    ps = con.prepareStatement("SELECT callNumber FROM hassubject WHERE subject = ?");
		
	    ps.setString(1, keyword);
	    
	    ResultSet rset = ps.executeQuery();
	    
	    while (rset.next()) {
	    	String title = rset.getString("title");
	    	int numOfCopiesIn = FindNumOfCopies(rs.getString("callNumber"), "in");
	    	int numOfCopiesOut = FindNumOfCopies(rs.getString("callNumber"), "out");
	    	temp.add(new Triple(title, numOfCopiesIn, numOfCopiesOut));
	    }*/
	    
	    return books;
	    
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	private List<Triple> CheckAccountBorrows(Integer bid) {
		try {
			ps = con.prepareStatement("SELECT * FROM borrowing WHERE inDate = null, bid = ?");

			ps.setInt(1, bid);

			ResultSet rs = ps.executeQuery();

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
