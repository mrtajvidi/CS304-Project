package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClerkModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	private ResultSet rs = null;
	private Statement  stmt = null;
	
	public ClerkModel() {
		
	}
	
	private void AddBorrower(Integer bid, String password, String name, String address, Integer phone, 
			String emailAddress, Integer sinOrStNo, Date expiryDate, String type) {
		try {
			ps = con.prepareStatement("INSERT INTO borrower VALUES (?,?,?,?,?,?,?,?,?)");

			ps.setInt(1, bid);
			ps.setString(2, password);
			ps.setString(3, name);
			ps.setString(4, address);
			ps.setInt(5, phone);
			ps.setString(6, emailAddress);
			ps.setInt(7, sinOrStNo);
			ps.setDate(8, expiryDate);
			ps.setString(9, type);

			ps.executeUpdate();

			con.commit();

			ps.close();
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}

	}
	
	private void CheckOut(Integer borid, Integer bid, String callNumber, Date outDate) {
		try {
			//check if borrower account is valid
			
			if (CheckAvailable(callNumber)) {

				ps = con.prepareStatement("INSERT INTO borrowing VALUES (?,?,?,?,?,?)");

				String copyNo = callNumber.split(" ")[2];

				ps.setInt(1, borid);
				ps.setInt(2, bid);
				ps.setString(3, callNumber);
				ps.setString(4, copyNo);
				ps.setDate(5, outDate);
				ps.setNull(6, Types.DATE);

				ps.executeUpdate();

				con.commit();

				ps.close();
				
				Date dueDate = ComputeDueDate(bid, outDate);
				
				//return title of item and dueDate
				
			} else {
				// not available
			}
		}
		catch (SQLException e) {
			
		}
	}
	
	private boolean CheckAvailable(String callNumber) {
		try {
		ps = con.prepareStatement("SELECT status FROM bookcopy WHERE callNumber = ?, copyNo = ?");
		
		String copyNo = callNumber.split(" ")[2];
		
		ps.setString(1, callNumber);
		ps.setString(2, copyNo);
		
		ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
			if (rs.getString("status").equals("in"))
				return true;
			else
				return false;
		} else
			return false;
		}
		catch (SQLException e) {
			return false;
		}
	}
	
	private void ProcessReturn(String callNumber, Date inDate, Integer fid, double amount) {
		try {
			ps = con.prepareStatement("SELECT bid FROM borrowing WHERE callnumber = ?, inDate = null");

			ps.setString(1, callNumber);

			ResultSet rs = ps.executeQuery();
			
			Integer bid = 0;

			if (rs.next())
				bid =  rs.getInt("bid");
			else {

			}

			UpdateStatusIn(callNumber);
			UpdateInDate(inDate, callNumber);

			if(CheckOverdue(bid, callNumber)) {
				AssessFine(bid, callNumber, fid, amount, inDate);
			} else {
				//RemoveBorrowingRecord();
			}

			CheckHoldRequests(callNumber);
		}
		catch (SQLException e) {
			//rollback everything
		}
	}
	
	private void UpdateStatusIn(String callNumber) {
		try {
		String copyNo = callNumber.split(" ")[2];
		
		ps = con.prepareStatement("UPDATE bookcopy SET status = 'in' WHERE callNumber = ?, copyNo = ?");
		
		ps.setString(1, callNumber);
		ps.setString(2, copyNo);
		
		ps.executeUpdate();
		
		ps.close();
		}
		catch(SQLException e) {
			
		}
	}
	
	private void UpdateStatusHold(String callNumber) {
		try {
		String copyNo = callNumber.split(" ")[2];
		
		ps = con.prepareStatement("UPDATE bookcopy SET status = 'on-hold' WHERE callNumber = ?, copyNo = ?");
		
		ps.setString(1, callNumber);
		ps.setString(2, copyNo);
		
		ps.executeUpdate();
		
		con.commit();
		
		ps.close();
		}
		catch(SQLException e) {
			
		}
	}
	
	private void UpdateInDate(Date inDate, String callNumber) {
		try {
			ps = con.prepareStatement("UPDATE borrowing SET inDate = ? WHERE callNumber = ?, inDate= null");
			
			ps.setDate(1, inDate);
			ps.setString(2, callNumber);
			
			ps.executeUpdate();
			
			ps.close();
			}
			catch(SQLException e) {
				
			}
	}
	
	private void CheckHoldRequests(String callNumber) {
		try {
		ps = con.prepareStatement("SELECT callNumber FROM holdrequest WHERE callNumber = ?");

	    ps.setString(1, callNumber);

	    ResultSet rs = ps.executeQuery();
	    
	    if (rs.next()) {
	    	UpdateStatusHold(callNumber); 
	    }
	    else
	    	return;
		}
		catch (SQLException e) {
				
		}
	}
	
	private boolean CheckOverdue(Integer bid, String callNumber) {
		try {
			ps = con.prepareStatement("SELECT inDate FROM borrowing WHERE bid = ?, callnumber = ?");
			
			ps.setInt(1, bid);
			ps.setString(2, callNumber);

			ResultSet rs = ps.executeQuery();

			Date inDate = new Date(0);

			if (rs.next())
				inDate = rs.getDate("inDate");
			else
				return false;
			
			ps = con.prepareStatement("SELECT outDate FROM borrowing WHERE bid =?, callnumber = ?");

			ps.setInt(1, bid);
			ps.setString(2, callNumber);

			ResultSet res = ps.executeQuery();

			Date outDate = new Date(0);

			if (rs.next())
				outDate = res.getDate("outDate");
			else
				return false;
			
			Date dueDate = ComputeDueDate(bid, outDate);

			if (inDate.compareTo(dueDate) > 0)
				return true;
			else
				return false;
		}
		catch (SQLException e) {
			return false;
		}
	}

	private void AssessFine(Integer bid, String callNumber, Integer fid, double amount, Date issuedDate) {
		try {
			ps = con.prepareStatement("SELECT borid FROM borrowing WHERE bid = ?, callnumber = ?");
			
			ps.setInt(1, bid);
			ps.setString(2, callNumber);
			
			ResultSet rs = ps.executeQuery();
			
			Integer borid = 0;
			
			if (rs.next())
				borid = rs.getInt("borid");
			else {
				
			}
			
			ps = con.prepareStatement("INSERT INTO fine VALUES (?,?,?,?,?)");
			
			ps.setInt(1, fid);
			ps.setDouble(2, amount);
			ps.setDate(3, issuedDate);
			ps.setNull(4, Types.DATE);
			ps.setInt(5, borid);
			
			ps.executeUpdate();
			
			con.commit();
			
			ps.close();
			}
			catch (SQLException e) {
				
			}
	}
	
	private List<DueItem> DisplayOverdue() {
		try {
			ps = con.prepareStatement("SELECT * FROM borrowing");
			
			ResultSet rs = ps.executeQuery();
			
			List<DueItem> duelist = new ArrayList<DueItem>();
			
			while(rs.next()) {

				Date inDate = rs.getDate("inDate");
				
				Date outDate = rs.getDate("outDate");
				
				Integer bid = rs.getInt("bid");

				Date dueDate = ComputeDueDate(bid, outDate);

				if (inDate != null && inDate.compareTo(dueDate) > 0) {
					ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
					
					ps.setString(1, rs.getString("callNumber"));
					
					ResultSet res = ps.executeQuery();
					
					duelist.add(new DueItem(bid, res.getString("title"), res.getInt("isbn"), outDate, inDate));
				}
			}
			
			return duelist;
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	private Date ComputeDueDate(Integer bid, Date outDate) {
		try {
			ps = con.prepareStatement("SELECT type FROM borrower WHERE bid = ?");

			ps.setInt(1, bid);

			ResultSet rs = ps.executeQuery();

			String type = "";

			if (rs.next()) {
				type = rs.getString("type");
			} else {
				//exception thrown?
			}

			ps = con.prepareStatement("SELECT bookTimeLimit FROM borrowertype WHERE type = ?");

			ps.setString(1, type);

			ResultSet res = ps.executeQuery();

			Integer timelimit = 0;

			if (rs.next()) {
				timelimit = rs.getInt("bookTimeLimit");
			} else {

			}

			Date date = new Date(0);
			
			return date.valueOf(AdjustDate(outDate,timelimit));
		}
		catch (SQLException e) {
			return null;
		}
	}

	private String AdjustDate(Date outDate, int length) {
		String date = outDate.toString();
		String[] strArr = date.split("-");
		Integer year = Integer.valueOf(strArr[0]);
		Integer month = Integer.valueOf(strArr[1]);
		Integer day = Integer.valueOf(strArr[2]);
		
		switch(month) {
		case 1: if (day + length > 31) {
			length -= 31 - day;
		} else {
			day += length;
			return (year.toString() + "-" + "1" + "-" + day.toString());
		}
		case 2: if (day + length > 28) {
			length -= 31 - day;
		} else {
			day += length;
			return (year.toString() + "-" + "2" + "-" + day.toString());
		}
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
			default: return "";
		}
	}
}
