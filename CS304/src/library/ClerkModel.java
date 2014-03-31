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
	
	private void AddBorrower(String bid_temp, String password, String name, String address, String phone_temp, 
			String emailAddress, String sinOrStNo_temp, Date expiryDate, String type, Connection con) {
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
	
	//CHECK OUT Function HERE!
	
	private boolean CheckAvailable(String callNumber) {
		try {
		rs = stmt.executeQuery("SELECT status FROM bookcopy WHERE callNumber =" + callNumber);
		
		//String copyNo = callNumber.split(" ")[2];
		
		//ps.setString(1, callNumber);
		//ps.setString(2, copyNo);
		
		//ResultSet rs = ps.executeQuery();
		
		if (rs.next()) {
			if (rs.getString("status").equals("in"))
				return true;
			else
				return false;
		} else
			return false;
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return false;
		}
	}
	
	private void ProcessReturn(String callNumber, String copyNo, String inDate, float amount) {
		int borid = 0;
		String issuedDate = null;
		String hold_callNumber = null;
		
		try {
			rs = stmt.executeQuery("SELECT borid FROM borrowing WHERE callnumber = " + callNumber + ", copyNo = " + copyNo + ";");

			if (rs.next())
				borid =  rs.getInt("bid");
			
			stmt.close();
			
			if(borid != 0)
			{
				UpdateStatusIn(callNumber);
				UpdateInDate(inDate, callNumber);

				//Check overdue
				rs = stmt.executeQuery("SELECT issuedDate FROM Fine WHERE borid = " + borid + ";");
				
				if(rs.next())
					issuedDate = rs.getString("issuedDate");
				
				stmt.close();
				
				if(inDate.equals(issuedDate))
				{
					ps = con.prepareStatement("UPDATE Fine SET amount = ?, paidDate = ? WHERE borid = " + borid + ";");
					
					ps.setFloat(1, amount);
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
					UpdateStatusHold(callNumber);
				}
			
			}
			else
				System.out.println("Record Not found");
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
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
			System.out.println("Message: " + e.getMessage());
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
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	private void UpdateInDate(String inDate, String callNumber) {
		try {
			ps = con.prepareStatement("UPDATE borrowing SET inDate = ? WHERE callNumber = ?, inDate= null");
			
			ps.setString(1, inDate);
			ps.setString(2, callNumber);
			
			ps.executeUpdate();
			
			ps.close();
			}
			catch(SQLException e) {
				System.out.println("Message: " + e.getMessage());
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
			      
			      overdue_items.add(new DueItem(bid, callNumber, outDate, dueDate));
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
	}
	
	/*private Date ComputeDueDate(Integer bid, Date outDate) {
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
			System.out.println("Message: " + e.getMessage());
		}
	}
}*/
