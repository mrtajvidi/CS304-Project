package library;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;


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

		Integer bid = 0;
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		java.util.Date today = Calendar.getInstance().getTime();        

		String inDate = df.format(today);
		String outDate = null;
		
		int borid = 0;
		try{
			stmt = con.createStatement();
			rs = stmt.executeQuery("SELECT bid, borid, outDate FROM borrowing WHERE callnumber = "+ callNumber + " and copyNo = " + copyNo + " and indate is null");
			
			while(rs.next()){

				bid = rs.getInt("bid");
				System.out.printf("bid: " + bid);
				
				borid = rs.getInt("borid");
				System.out.printf("  borid: " + borid);
				
				outDate = rs.getString("outDate");
				System.out.printf("  outdate: " + outDate);
				
				System.out.println(" ");
			}
			
			stmt.close();
			
		}catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
				
		String dueDate = ComputeDueDate(bid, outDate, con);
		System.out.println("Due Date : " + dueDate);
		
		UpdateStatusIn(callNumber, copyNo, con);
		UpdateInDate(inDate, callNumber, con);
		
		
		String hold_callNumber = null;
				
		try {
			stmt = con.createStatement();

			if (isOverdue(inDate, dueDate)){
				
				System.out.println("Book Overdue");
				
				//if it is overdue assign fine
				
				//get fid
				Integer fid = 0;
				
				try {
					//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
					stmt = con.createStatement();
					rs = stmt.executeQuery("SELECT fid FROM fine");
					
					while(rs.next())
					  {
						fid ++;			
					  }
					
					fid ++;
					
				    System.out.println("fid: " + fid);

					stmt.close();
					
				}catch(SQLException ex){
				    System.out.println("Message: " + ex.getMessage());
				}
				
				//ASSIGN FINE AMOUNT
				double amount = 2.50;
				amount = assignFineAmount(inDate, dueDate);
				
				ps = con.prepareStatement("INSERT INTO Fine VALUES (?,?,?,?,?)");
				
				ps.setInt(1, fid);
				ps.setFloat(2, (float) amount);
				ps.setString(3, inDate);
				ps.setString(4, null);
				ps.setInt(5, borid);
				
				ps.executeUpdate();
				
				ps.close();
				
			}
			
			//check hold requests
				
			CheckHoldRequests(callNumber, copyNo, con);
			
			
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	private double assignFineAmount(String inDate, String outDate){
		
		int time = daysBetween (inDate, outDate);

		return 0.5*time;
	}
	
	
	private void UpdateStatusIn(String callNumber, String copyNo, Connection con) {
		PreparedStatement ps;
		
		try {
		
			ps = con.prepareStatement("UPDATE bookcopy SET status = 'in' WHERE callNumber = ? and copyNo = ?");
		
			ps.setString(1, callNumber);
			ps.setString(2, copyNo);
			System.out.println("callnumber : "+ callNumber + " copy no: " + copyNo);
			
			ps.executeUpdate();
			con.commit();
			ps.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			System.out.println("UpdateStatusIn");
		}
	}
	
	private void UpdateStatusHold(String callNumber, String copyNo, Connection con) {
		PreparedStatement ps;
		
		try {
			
			ps = con.prepareStatement("UPDATE bookcopy SET status = 'on-hold' WHERE callNumber = ? and copyNo = ?");
			
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
			con.commit();
			ps.close();
			}
			catch(SQLException e) {
				System.out.println("Message: " + e.getMessage());
				System.out.println("UpdateInDate");
			}
	}
	
	private void CheckHoldRequests(String callNumber, String copyNo, Connection con) {
		ResultSet rs;
		Statement stmt;
		List<String> issuedDates = new ArrayList<String>();
		List<Integer> bid = new ArrayList<Integer>();
		
		try {
			stmt = con.createStatement();
			boolean isHold = false;
			rs = stmt.executeQuery("SELECT issuedDate, bid FROM holdrequest WHERE callNumber = '"+ callNumber + "'");
			
		    while (rs.next()) {
		    	issuedDates.add(rs.getString("issuedDate"));
		    	bid.add(rs.getInt("bid"));
		    	isHold = true;
		    }
		    
		    //CHECK WHO RESERVED THE BOOK FIRST AND EMAIL THEM
		    if (isHold){
		    	Integer firstBID = firstReserve(issuedDates, bid);
		    
		    	UpdateStatusHold(callNumber, copyNo, con); 
		    	//delete hold request
		    	
		    	ps = con.prepareStatement("DELETE FROM holdrequest WHERE callnumber = ? and bid = ?");
		    	
		    	ps.setString(1, callNumber);
		    	ps.setInt(2, firstBID);
		    	
		    	ps.executeUpdate();
				con.commit();
				ps.close();
		    	
		    	System.out.println("BOOK PLACED ON HOLD");
		    	System.out.println("EMAILING BID USER: "+ firstBID);
		    
		    }
		}
		catch (SQLException e) {
				
		}
	}
	
	private Integer firstReserve(List<String> issuedDates, List<Integer> bid){
		
		Integer minYear = 2015;
		Integer minMonth = 12;
		Integer minDay = 12;
		Integer minBID = 0;
		
		for (int i = 0; i<issuedDates.size() - 1 ; i++){
			String[] date = issuedDates.get(i).split("/");
			String yearString[] = date[2].split(" ");
			Integer year = Integer.valueOf(yearString[0]);
			Integer month = Integer.valueOf(date[0]);
			Integer day = Integer.valueOf(date[1]);
			
			if (year < minYear ) {
				minYear = year;
				minBID = bid.get(i);
			}
			if (month < minMonth ) minMonth = month;
			if (day < minDay ) minDay = day;

			
		}
		
		return minBID;
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

	private void AssessFine(Integer bid, String callNumber, Integer fid, double amount, String issuedDate, Connection con) {
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
			ps.setString(3, issuedDate);
			ps.setNull(4, Types.DATE);
			ps.setInt(5, borid);
			
			ps.executeUpdate();
			
			con.commit();
			
			ps.close();
			}
			catch (SQLException e) {
				System.out.println("Message: " + e.getMessage());
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
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}
	
	private String ComputeDueDate(Integer bid, String outDate, Connection con) {
		Statement stmt;
		ResultSet rs;
		
		try {
			stmt = con.createStatement();
			
			rs = stmt.executeQuery("SELECT type FROM borrower WHERE bid = " + bid);

			String type = null;
			
			while(rs.next()){

				type = rs.getString("type");
				System.out.println("type: " + type);
			
			}
				
			rs = stmt.executeQuery("SELECT bookTimeLimit FROM borrowertype WHERE type = '" + type +"'");

			Integer timelimit = 0;

			while (rs.next()){
				timelimit = rs.getInt("bookTimeLimit");
				System.out.println("Time Limit: " + timelimit);
			}
			
			
			
			String date = AdjustDate(outDate, timelimit);
			
			return date;
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			return null;
		}
	}
	private boolean isOverdue(String inDate, String dueDate){

		Date inDate_Date, dueDate_Date;

		inDate_Date = stringToDate(inDate);
		dueDate_Date = stringToDate(dueDate);

		//inDate_Date is after dueDate_Date
		if(inDate_Date.compareTo(dueDate_Date) > 0) 
			return true;
		else
			return false;
	}

	private String AdjustDate(String outDate, int length) {
		Date temp, result;
		String output;

		temp = stringToDate(outDate);
		result = addDate(temp, length*7 );
		output = dateToString(result);

		return output;
	}

	private Date stringToDate(String string_input)
	{
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date date_output = null;

		try
		{
			date_output = df.parse(string_input);
		}
		catch (ParseException e) 
		{
			e.printStackTrace();
		}

		return date_output;
	}

	private String dateToString(Date date_input) {

		String string_output = null;

		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		string_output = df.format(date_input);

		return string_output;
	}

	private Date addDate(Date inputDate, int amount_add) {

		Date result;

		Calendar c = Calendar.getInstance(); 
		c.setTime(inputDate);

		c.add(Calendar.DATE, amount_add);
		result = c.getTime();

		return result;
	}
	
	private int daysBetween( String date1, String date2){
		try{
			DateFormat formatter= new SimpleDateFormat("MM/dd/yyyy");
			String truncatedDateString1 = formatter.format(date1);
			Date truncatedDate1 = formatter.parse(truncatedDateString1);
	
			String truncatedDateString2 = formatter.format(date2);
			Date truncatedDate2 = formatter.parse(truncatedDateString2);
	
			long timeDifference = truncatedDate2.getTime()- truncatedDate1.getTime();
	
			int daysInBetween = (int) timeDifference / (24*60*60*1000);
			return daysInBetween;
		}
		catch (ParseException p){
			System.out.println("PARSE EXCEPTION");
			return 0;
		}
		
	}

}

