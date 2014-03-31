package cs304;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Clerk {
	
	private Connection con = null;
	
	public Clerk() {
		con = OracleConnection.getInstance().getConnection();
	}
	
	public void AddBorrower(String password, String name, String address, String phone,
							String emailAddress, String sinOrStNo, java.sql.Date expiryDate, String type) throws SQLException {
			PreparedStatement ps = con.prepareStatement("INSERT INTO borrower VALUES (bid_counter.nextval,?,?,?,?,?,?,?,?)");
			ps.setString(1, password);
			ps.setString(2, name);
			ps.setString(3, address);
			ps.setString(4, phone);
			ps.setString(5, emailAddress);
			ps.setString(6, sinOrStNo);
			ps.setDate(7, expiryDate);
			ps.setString(8, type);
			ps.executeUpdate();
			ps.close();
			con.commit();
	}

	public TwoTuple<String, Date> CheckOut(Integer bid, String copyCallNumber) throws SQLException {
			if (IsAvailable(copyCallNumber) && !IsBlocked(bid)) {
                                
                                String[] strArr = copyCallNumber.split(" ");
				String callNumber = strArr[0] + strArr[1];
				String copyNo = strArr[2];

				PreparedStatement ps = con.prepareStatement("INSERT INTO borrowing VALUES (borid_counter.nextval,?,?,?,sysdate,?)");
				ps.setInt(1, bid);
				ps.setString(2, callNumber);
				ps.setString(3, copyNo);
				ps.setNull(4, Types.DATE);
				ps.executeUpdate();
				ps.close();
				
				ps = con.prepareStatement("UPDATE bookcopy SET status = 'out' WHERE callNumber = ?, copyNo = ?");
				ps.setString(1, callNumber);
				ps.setString(2, copyNo);
				ps.executeUpdate();
                                ps.close();

				con.commit();
                                
                                ps = con.prepareStatement("SELECT sysdate FROM dual");
				ResultSet res = ps.executeQuery();
                                Date outDate = res.getDate("sysdate");

				Date dueDate = ComputeDueDate(bid, outDate);

				ps = con.prepareStatement("SELECT title FROM book WHERE callNumber = ?");
				ps.setString(1, callNumber);
				ResultSet rs = ps.executeQuery();

				String title = "";

				if (rs.next()) {
					title = rs.getString("title");
				} else {

				}

				TwoTuple<String, Date> tuple = new TwoTuple<String, Date>(title, dueDate);

				return tuple;
                                
			} else {
				ExceptionFrame exception = new ExceptionFrame();
                                exception.setVisible(true);
                                return null;
			}
	}
	
	private boolean IsBlocked(Integer bid) throws SQLException {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM borrowing NATURAL JOIN fine WHERE bid = ? AND paidDate is null");
			ps.setInt(1, bid);
			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return true;
			else
				return false;
	}

	private boolean IsAvailable(String copyCallNumber) throws SQLException {
            
                        String[] strArr = copyCallNumber.split(" ");
			String callNumber = strArr[0] + strArr[1];
			String copyNo = strArr[2];

			PreparedStatement ps = con.prepareStatement("SELECT status FROM bookcopy WHERE callNumber = ?, copyNo = ?");
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				if (rs.getString("status").equals("in"))
					return true;
				else
					return false;
			} else {
				return false;
                        }
	}

	public void ProcessReturn(String copyCallNumber) throws SQLException {
            
                        String[] strArr = copyCallNumber.split(" ");
			String callNumber = strArr[0] + strArr[1];
			String copyNo = strArr[2];
                        
			PreparedStatement ps = con.prepareStatement("SELECT * FROM borrowing WHERE callnumber = ? AND copyNo =? AND inDate is null");
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			ResultSet rs = ps.executeQuery();
			
			Integer bid = 0;
			if (rs.next())
				bid =  rs.getInt("bid");
			else {

			}
			
			Date outDate = rs.getDate("outDate");
			Integer borid = rs.getInt("borid");
			
			ps = con.prepareStatement("UPDATE bookcopy SET status = 'in' WHERE callNumber = ? AND copyNo = ?");
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			ps.executeUpdate();
                        ps.close();
			
			ps = con.prepareStatement("UPDATE borrowing SET inDate = sysdate WHERE callNumber = ? AND copyNo = ? AND inDate is null");
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			ps.executeUpdate();
                        ps.close();

			if(IsOverdue(bid, outDate)) {
                                FineFrame fineframe = new FineFrame();
                                fineframe.borid = borid;
                                fineframe.callNumber = callNumber;
                                fineframe.copyNo = copyNo;
                                fineframe.bid = bid;
                                fineframe.setVisible(true);
                                //must enter fine at this point or exception
			} else {
                            
                        }

			CheckHoldRequests(callNumber, copyNo, bid);
			
			con.commit();
	}
	
	public void CheckHoldRequests(String callNumber, String copyNo, Integer bid) throws SQLException {
		PreparedStatement ps = con.prepareStatement("SELECT * FROM holdrequest WHERE callNumber = ? AND issuedDate = (SELECT MIN(issuedDate) FROM holdrequest WHERE callNumber = ?)");
	    ps.setString(1, callNumber);
	    ps.setString(2, callNumber);
	    ResultSet rs = ps.executeQuery();
	    
	    if (rs.next()) {
			ps = con.prepareStatement("UPDATE bookcopy SET status = 'on-hold' WHERE callNumber = ? AND copyNo = ?");
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			ps.executeUpdate();
			
			//"sent email to ...@hotmail.com"
	    }
            else {
	    	return;
            }
	}
	
	private boolean IsOverdue(Integer bid, Date outDate) throws SQLException {
            
		Date dueDate = ComputeDueDate(bid, outDate);

                PreparedStatement ps = con.prepareStatement("SELECT sysdate FROM dual where sysdate >= ?");
                ps.setDate(1, dueDate);
                ResultSet rs = ps.executeQuery();
                
		if (rs.next())
			return true;
		else
			return false;
	}

	public void AssessFine(double amount, Integer borid) throws SQLException {
			PreparedStatement ps = con.prepareStatement("INSERT INTO fine VALUES (fid_counter.nextval,?, sysdate,?,?)");

			ps.setDouble(1, amount);
			//ps.setDate(2, issuedDate);
			ps.setNull(2, Types.DATE);
			ps.setInt(3, borid);

			ps.executeUpdate();
	}
	
	public List<FiveTuple<String,String,String,Integer,String>> DisplayOverdue() throws SQLException {
                        
                        List<FiveTuple<String, String, String,Integer,String>> overduelist = new ArrayList<>();
            
			PreparedStatement ps = con.prepareStatement("SELECT * FROM borrowing WHERE inDate is null");
			ResultSet rs = ps.executeQuery();

			while(rs.next()) {
				Integer bid = rs.getInt("bid");
				Date outDate = rs.getDate("outDate");

				if (IsOverdue(bid, outDate)) {
                                        ps = con.prepareStatement("SELECT * FROM borrower WHERE bid = ?");
                                        ps.setInt(1, bid);
                                        ResultSet res = ps.executeQuery();
                                        
					ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
					ps.setString(1, rs.getString("callNumber"));
					ResultSet reset = ps.executeQuery();

					overduelist.add(new FiveTuple<String,String,String,Integer,String>(reset.getString("title"), rs.getString("callNumber"),rs.getString("copyNo"), bid, res.getString("emailAddress")));
					
				}
			}

			return overduelist;
	}
	
	private Date ComputeDueDate(Integer bid, Date outDate) throws SQLException {

			PreparedStatement ps = con.prepareStatement("SELECT bookTimeLimit FROM borrower NATURAL JOIN borrowertype WHERE bid = ?");
			ps.setInt(1, bid);
			ResultSet rs = ps.executeQuery();

			Integer booktimelimit = rs.getInt("bookTimeLimit");
			
			ps = con.prepareStatement("SELECT (? + ?) AS dueDate FROM dual");
                        ps.setDate(1, outDate);
                        ps.setInt(2, booktimelimit);
                        
                        rs = ps.executeQuery();
                        
                        Date dueDate = rs.getDate("dueDate");
			
			return dueDate;
	}
}
