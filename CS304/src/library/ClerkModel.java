package library;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ClerkModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	private ResultSet rs = null;
	private Statement  stmt = null;
	
	public ClerkModel() {
		
	}
	private void AddBorrower(String bid_temp, String password, String name, String address, String phone_temp, String emailAddress, String sinOrStNo_temp,  String type, Connection con) {
		
		//SET EXPIRY DATE
		java.util.Date myDate;
	    Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.MONTH, 9);
	    cal.set(Calendar.DATE, 24);
	    cal.set(Calendar.YEAR, 2016);
	    cal.set(Calendar.HOUR, 0 );
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    
	    myDate = cal.getTime();
				
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		String expiryDate = df.format(myDate);
		
		//AUTOMATICALLY ASSIGN BORROWER ID
		
		try {
			
			int bid = Integer.parseInt(bid_temp);
			int phone = Integer.parseInt(phone_temp);
			int sinOrStNo = Integer.parseInt(sinOrStNo_temp);
			
			
			
			ps = con.prepareStatement("INSERT INTO borrower VALUES (?,?,?,?,?,?,?,?,?)");

			ps.setInt(1, bid);
			ps.setString(2, password);
			ps.setString(3, name);
			ps.setString(4, address);
			ps.setInt(5, phone);
			ps.setString(6, emailAddress);
			ps.setInt(7, sinOrStNo);
			ps.setString(8, expiryDate);
			ps.setString(9, type);

			ps.executeUpdate();

			con.commit();

			ps.close();
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}

	}
	
	
	private String CheckOut(Integer bid, String callNumber, Connection con) {

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		java.util.Date today = Calendar.getInstance().getTime();        

		String outDate = df.format(today);
		
		String checked;
		Integer borid = 0;
		
		//GET BORID
		PreparedStatement  ps;
		ResultSet  rs;
		Statement  stmt;
			
		try {
			//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT borid FROM borrowing");
			
			while(rs.next())
			  {
				borid = rs.getInt("borid");			
			  }
			
			borid ++;
			
		    System.out.println("borid: " + borid);

			stmt.close();
			
		}catch(SQLException ex){
		    System.out.println("Message: " + ex.getMessage());
		}
		
		try {
			//check if borrower account is valid
			String copyNo = CheckAvailable(callNumber, con);
			//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
			
			if (!copyNo.equals("none") && CheckAccount(bid, con)) {

				ps = con.prepareStatement("INSERT INTO borrowing VALUES (?,?,?,?,?,?)");

				ps.setInt(1, borid);
				ps.setInt(2, bid);
				ps.setString(3, callNumber);
				ps.setString(4, copyNo);
				ps.setString(5, outDate);
				ps.setString(6, null);

				ps.executeUpdate();

				con.commit();

				
				System.out.println("copyno: "+ copyNo);
				System.out.println("callnumber: "+ callNumber);
				
				ps = con.prepareStatement("UPDATE bookcopy SET status = 'out' WHERE callnumber = ? and copyno = ?");
				
				ps.setString(1, callNumber);
				ps.setString(2, copyNo);

				ps.executeUpdate();

				con.commit();
				
				String dueDate = ComputeDueDate(bid, outDate, con);
				
				//return title of item and dueDate
				checked = "Callnumber: " + callNumber + " Due Date: " + dueDate;
				
				ps.close();
				
				return checked;
				
			} else {
				System.out.println("Can't Check Out Book");
				return null;
			}
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}
	
	private boolean CheckAccount(Integer bid, Connection con){
		
		Statement stmt;
		ResultSet rs;
		
		try
		{		
		stmt = con.createStatement();
		
		rs = stmt.executeQuery("SELECT * FROM borrower WHERE bid = " + bid);
		 
	    if (rs.next() != false) {    	
	    	System.out.println("Valid Borrower");

	    	return true;
	    }else{
	    	
	    	System.out.println("NON Valid Borrower");

	    	return false;
	    	
	    }
	    
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return false;
		}
	}
	
	private String CheckAvailable(String callNumber, Connection con) {
		Statement stmt;
		ResultSet rs;
		
		//CHECK IF AVAILABLE FOR HOLD REQUESTS	
		
		try {
			
			stmt = con.createStatement();
			
			rs = stmt.executeQuery("SELECT copyNo FROM bookcopy WHERE callNumber = " + callNumber + " and status = 'in'");

			if (rs.next()) {
				System.out.println("Book is Available");
				return rs.getString("copyNo");
			} else
				System.out.println("Book not Available");
				return "none";
			}
			catch (SQLException e) {
				return "none";
			}
	}
	
	private void ProcessReturn(String callNumber, String copyNo, Connection con) {
		ResultSet rs;
		Statement stmt;

		String bid;
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		java.util.Date today = Calendar.getInstance().getTime();        

		String inDate = df.format(today);
		
		float amount;
		
		int borid = 0;
		try{
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT borid FROM borrowing WHERE callnumber = '" + callNumber + "' and copyNo = '" + copyNo +"'");
			
			borid = rs.getInt("borid");
			
		}catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
				
		String issuedDate = null;
		String hold_callNumber = null;
				
		try {
			UpdateStatusIn(callNumber, copyNo,con);
			UpdateInDate(inDate, callNumber, con);

			stmt = con.createStatement();
			
			//Check overdue
			rs = stmt.executeQuery("SELECT issuedDate FROM Fine WHERE borid = " + borid + ";");
				
				if(rs.next())
					issuedDate = rs.getString("issuedDate");
				
				stmt.close();
				
				if(inDate.equals(issuedDate))
				{
					ps = con.prepareStatement("UPDATE Fine SET amount = ?, paidDate = ? WHERE borid = " + borid + ";");
					
					//ps.setFloat(1, amount);
					ps.setString(2, inDate);
					
					ps.executeUpdate();
					
					ps.close();
				}
				
				rs = stmt.executeQuery("SELECT callNumber FROM holdRequest WHERE callNumber = " + callNumber + ";");
				
				if(rs.next())
					hold_callNumber = rs.getString("issuedDate");
				
				stmt.close();
				if(callNumber.equals(hold_callNumber))
				{
					UpdateStatusHold(callNumber, con);
				}
			else
				System.out.println("Record Not found");
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	private void UpdateStatusIn(String callNumber, String copyNo,Connection con) {
		PreparedStatement ps;
		
		try {
		
			ps = con.prepareStatement("UPDATE bookcopy SET status = 'in' WHERE callNumber = ?, copyNo = ?");
		
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			
			ps.executeUpdate();
		
			ps.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	private void UpdateStatusHold(String callNumber, Connection con) {
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
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	private void UpdateInDate(String inDate, String callNumber, Connection con) {
		PreparedStatement ps;
		try {
			ps = con.prepareStatement("UPDATE borrowing SET inDate = ? WHERE callNumber = ?");
			
			ps.setString(1, inDate);
			ps.setString(2, callNumber);
			
			ps.executeUpdate();
			
			ps.close();
			}
			catch(SQLException e) {
				System.out.println("Message: " + e.getMessage());
			}
	}
	
	private void CheckHoldRequests(String callNumber, Connection con) {
		try {
		ps = con.prepareStatement("SELECT callNumber FROM holdrequest WHERE callNumber = ?");

	    ps.setString(1, callNumber);

	    ResultSet rs = ps.executeQuery();
	    
	    if (rs.next()) {
	    	UpdateStatusHold(callNumber, con); 
	    }
	    else
	    	return;
		}
		catch (SQLException e) {
				
		}
	}
	
	private List<DueItem> CheckOverdue() {
		
		int bid;
		String callNumber, outDate, dueDate;
		
		Statement  stmt;
		ResultSet  rsOverdue;
		
		try {
			stmt = con.createStatement();

			rsOverdue = stmt.executeQuery("SELECT Borrowing.bid, Borrowing.callNumber, Borrowing.outDate, Fine.issuedDate "
					+ "FROM Fine INNER JOIN Borrowing ON (Fine.bid=Borrowing.bid) "
					+ "WHERE amount != 'null' AND paidDate = 'null'; ");
			
			// get info on ResultSet
			ResultSetMetaData rsmd = rsOverdue.getMetaData();

			// get number of columns
			int numCols = rsmd.getColumnCount();

			ArrayList<DueItem> overdue_items = new ArrayList<DueItem>();
			
			for (int i = 0; i < numCols; i++)
			  {
			      // get column name and print it

			      System.out.printf("%-15s", rsmd.getColumnName(i+1));    
			  }

			  System.out.println(" ");

			  while(rsOverdue.next())
			  {
			      // for display purposes get everything from Oracle 
			      // as a string

			      // simplified output formatting; truncation may occur
				  
				  bid = rsOverdue.getInt("Borrowing.bid");
			      System.out.printf("%-10.10s", bid);

			      callNumber = rsOverdue.getString("Borrowing.callNumber");
			      System.out.printf("%-20.20s", callNumber);

			      outDate = rsOverdue.getString("Borrowing.outDate");
			      System.out.printf("%-20.20s", outDate);
			      
			      dueDate = rsOverdue.getString("Fine.IssuedDate");
			      System.out.printf("%-20.20s", dueDate);
			      
			      //overdue_items.add(new DueItem(bid, callNumber, outDate, dueDate));
			  }
		 
			  // close the statement; 
			  // the ResultSet will also be closed
			  stmt.close();
			  
			  return overdue_items;
			}
			catch (SQLException ex)
			{
			    System.out.println("Message: " + ex.getMessage());
			    return null;
			}	
					
	}

	private void AssessFine(Integer bid, String callNumber, Integer fid, double amount, Date issuedDate, Connection con) {
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
	
	private List<DueItem> DisplayOverdue(Connection con) {
		try {
			ps = con.prepareStatement("SELECT * FROM borrowing");
			
			ResultSet rs = ps.executeQuery();
			
			List<DueItem> duelist = new ArrayList<DueItem>();
			
			while(rs.next()) {

				String inDate = rs.getString("inDate");
				
				String outDate = rs.getString("outDate");
				
				Integer bid = rs.getInt("bid");

				String dueDate = ComputeDueDate(bid, outDate, con);

				if (inDate != null && inDate.compareTo(dueDate) > 0) {
					ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
					
					ps.setString(1, rs.getString("callNumber"));
					
					ResultSet res = ps.executeQuery();
					
					//duelist.add(new DueItem(bid, res.getString("title"), res.getInt("isbn"), outDate, inDate));
				}
			}
			
			return duelist;
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	private String ComputeDueDate(Integer bid, String outDate, Connection con) {
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = con.createStatement();
			
			rs = stmt.executeQuery("SELECT type FROM borrower WHERE bid = " + bid);

			String type;

			type = rs.getString("type");
				
			rs = stmt.executeQuery("SELECT bookTimeLimit FROM borrowertype WHERE type = " + type);

			Integer timelimit = 0;

			timelimit = rs.getInt("bookTimeLimit");
			
			String date = AdjustDate(outDate, timelimit);
			return date;
		}
		catch (SQLException e) {
			return null;
		}
	}

	private String AdjustDate(String outDate, int length) {
		String date = outDate;
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
