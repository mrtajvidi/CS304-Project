package cpsc304proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.GregorianCalendar;

public class ClerkModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	
	public ClerkModel() {
		
	}
	
	private void AddBorrower(String password, String name, String address, Integer phone,
							String emailAddress, Integer sinOrStNo, Date expiryDate, String type) {
		try {
			ps = con.prepareStatement("INSERT INTO borrower VALUES (bid_counter.nextval,?,?,?,?,?,?,?,?)");

			ps.setString(1, password);
			ps.setString(2, name);
			ps.setString(3, address);
			ps.setInt(4, phone);
			ps.setString(5, emailAddress);
			ps.setInt(6, sinOrStNo);
			ps.setDate(7, expiryDate);
			ps.setString(8, type);

			ps.executeUpdate();
			
			con.commit();
		}
		catch (SQLException e) {

		}
	}

	private Tuple<String, Date> CheckOut(Integer bid, String copyCallNumber, Date outDate) {
		try {
			if (IsAvailable(copyCallNumber) && !IsBlocked(bid)) {

				ps = con.prepareStatement("INSERT INTO borrowing VALUES (borid_counter.nextval,?,?,?,?,?)");
				
				String[] strArr = copyCallNumber.split(" ");
				String callNumber = strArr[0] + strArr[1];
				String copyNo = strArr[2];

				ps.setInt(1, bid);
				ps.setString(2, callNumber);
				ps.setString(3, copyNo);
				ps.setDate(4, outDate);
				ps.setNull(5, Types.DATE);

				ps.executeUpdate();

				con.commit();

				Date dueDate = ComputeDueDate(bid, outDate);

				ps = con.prepareStatement("SELECT title FROM book WHERE callNumber = ?");

				ps.setString(1, callNumber);

				ResultSet rs = ps.executeQuery();

				String title = "";

				if (rs.next()) {
					title = rs.getString("title");
				} else {

				}

				Tuple<String, Date> tuple = new Tuple<String, Date>(title, dueDate);

				return tuple;
				
			} else {
				return null;
			}
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	private boolean IsBlocked(Integer bid) {
		try {
			ps = con.prepareStatement("SELECT * FROM borrowing NATURAL JOIN fine WHERE bid = ?, paidDate = null");

			ps.setInt(1, bid);

			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return true;
			else
				return false;
		}
		catch (SQLException e) {
			return false;
		}
	}

	private boolean IsAvailable(String copyCallNumber) {
		try {
			ps = con.prepareStatement("SELECT status FROM bookcopy WHERE callNumber = ?, copyNo = ?");

			String[] strArr = copyCallNumber.split(" ");
			String callNumber = strArr[0] + strArr[1];
			String copyNo = strArr[2];

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

	private void ProcessReturn(String copyCallNumber, Date inDate, double amount) {
		try {
			ps = con.prepareStatement("SELECT * FROM borrowing WHERE callnumber = ?, copyNo =?, inDate = null");
			
			String[] strArr = copyCallNumber.split(" ");
			String callNumber = strArr[0] + strArr[1];
			String copyNo = strArr[2];

			ps.setString(1, callNumber);
			ps.setString(2, copyNo);

			ResultSet rs = ps.executeQuery();
			
			Integer bid = 0;

			if (rs.next())
				bid =  rs.getInt("bid");
			else {

			}
			
			Date outDate =  rs.getDate("outDate");
			Integer borid = rs.getInt("borid");
			
			ps = con.prepareStatement("UPDATE bookcopy SET status = 'in' WHERE callNumber = ?, copyNo = ?");
			
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			
			ps.executeUpdate();
			
			ps = con.prepareStatement("UPDATE borrowing SET inDate = ? WHERE callNumber = ?, copyNo = ?, inDate= null");
			
			ps.setDate(1, inDate);
			ps.setString(2, callNumber);
			ps.setString(3, copyNo);
			
			ps.executeUpdate();

			if(IsOverdue(bid, outDate, inDate)) {
				AssessFine(amount, inDate, borid);
			}

			CheckHoldRequests(callNumber, copyNo);
			
			con.commit();
		}
		catch (SQLException e) {
			//rollback everything
		}
	}
	
	private void CheckHoldRequests(String callNumber, String copyNo) {
		try {
		ps = con.prepareStatement("SELECT * FROM holdrequest WHERE callNumber = ? AND issuedDate = (SELECT MAX(issuedDate) FROM holdrequest WHERE callNumber = ?)");
		
	    ps.setString(1, callNumber);
	    ps.setString(2, callNumber);

	    ResultSet rs = ps.executeQuery();
	    
	    if (rs.next()) {
	    	
			ps = con.prepareStatement("UPDATE bookcopy SET status = 'on-hold' WHERE callNumber = ?, copyNo = ?");
			
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			
			ps.executeUpdate();
			
			//send email?????
			/*Integer bid = rs.getInt("bid");
			
			ps = con.prepareStatement("SELECT * FROM borrower WHERE bid = ?");*/
	    }
	    else
	    	return;
		}
		catch (SQLException e) {
				
		}
	}
	
	private boolean IsOverdue(Integer bid, Date outDate, Date inDate) {
		Date dueDate = ComputeDueDate(bid, outDate);

		if (inDate.compareTo(dueDate) > 0)
			return true;
		else
			return false;
	}

	private void AssessFine(double amount, Date issuedDate, Integer borid) {
		try {
			ps = con.prepareStatement("INSERT INTO fine VALUES (fid_counter.nextval,?,?,?,?)");

			ps.setDouble(1, amount);
			ps.setDate(2, issuedDate);
			ps.setNull(3, Types.DATE);
			ps.setInt(4, borid);

			ps.executeUpdate();
		}
		catch (SQLException e) {

		}
	}
	
	private List<DueItem> DisplayOverdue() {
		try {
			ps = con.prepareStatement("SELECT * FROM borrowing WHERE inDate = null");

			ResultSet rs = ps.executeQuery();

			List<DueItem> duelist = new ArrayList<DueItem>();

			while(rs.next()) {

				Integer bid = rs.getInt("bid");
				Date outDate = rs.getDate("outDate");

				GregorianCalendar gregCalendar = new GregorianCalendar();
				Date curDate = new Date(gregCalendar.getTime().getTime());

				if (IsOverdue(bid, outDate, curDate)) {
					ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");

					ps.setString(1, rs.getString("callNumber"));

					ResultSet res = ps.executeQuery();

					duelist.add(new DueItem(bid, res.getString("title"), res.getInt("isbn"), outDate));
					
					//send emails?
					
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
				
			}

			ps = con.prepareStatement("SELECT bookTimeLimit FROM borrowertype WHERE type = ?");

			ps.setString(1, type);

			ResultSet res = ps.executeQuery();

			Integer booktimelimit = 0;

			if (rs.next()) {
				booktimelimit = res.getInt("bookTimeLimit");
			} else {

			}
			
			String date = outDate.toString();
			String[] strArr = date.split("-");
			Integer year = Integer.valueOf(strArr[0]);
			Integer month = Integer.valueOf(strArr[1]);
			Integer day = Integer.valueOf(strArr[2]);
			
			GregorianCalendar gregCalendar = new GregorianCalendar(year, month, day);
			gregCalendar.add(Calendar.DAY_OF_YEAR, booktimelimit);

			Date sqldate = new Date(gregCalendar.getTime().getTime());
			
			return sqldate;
		}
		catch (SQLException e) {
			return null;
		}
	}
}
