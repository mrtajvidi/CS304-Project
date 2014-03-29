package cpsc304proj;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class LibrarianModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;

	public LibrarianModel() {
		
	}

	private void AddBook(String copyCallNumber, Integer isbn, String title, String mainAuthor, 
						String publisher, Integer year, List<String> authors,  List<String> subjects) {
		try {
			ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
			
			String[] strArr = copyCallNumber.split(" ");
			String callNumber = strArr[0] + strArr[1];
			String copyNo = strArr[2];

			ps.setString(1, callNumber);

			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				ps = con.prepareStatement("INSERT INTO bookcopy VALUES (?,?,'in')");
				
				ps.setString(1, callNumber);
				ps.setString(2, copyNo);
				
				ps.executeUpdate();

				ps.close();
				
				con.commit();
			} else {
				ps = con.prepareStatement("INSERT INTO book VALUES (?,?,?,?,?,?)");
				
				ps.setString(1, callNumber);
				ps.setInt(2, isbn);
				ps.setString(3, title);
				ps.setString(4, mainAuthor);
				ps.setString(5, publisher);
				ps.setInt(6, year);
				
				ps.executeUpdate();

				ps.close();
				
				ps = con.prepareStatement("INSERT INTO bookcopy VALUES (?,?,'in')");
				
				ps.setString(1, callNumber);
				ps.setString(2, copyNo);
				
				ps.executeUpdate();

				ps.close();
				
				AddAuthors(callNumber, authors);
				AddSubjects(callNumber, subjects);
				
				con.commit();
			}
		}
		catch(SQLException e) {

		}
	}
	
	private void AddAuthors(String callNumber, List<String> authors) {
		try {
		for (String author: authors) {
			ps = con.prepareStatement("INSERT INTO hasauthor VALUES (?,?)");
			
			ps.setString(1, callNumber);
			ps.setString(2, author);
			
			ps.executeUpdate();

			ps.close();
		}
		}
		catch(SQLException e) {
			
		}
		
	}

	private void AddSubjects(String callNumber, List<String> subjects) {
		try {
			for (String subject : subjects) {
				ps = con.prepareStatement("INSERT INTO hassubject VALUES (?,?)");

				ps.setString(1, callNumber);
				ps.setString(2, subject);

				ps.executeUpdate();

				ps.close();
			}
		}
		catch(SQLException e) {

		}
	}
	
	private List<Triple<String, Date, Date>> displayCheckedOut() {
		try {
			ps = con.prepareStatement("SELECT * FROM borrowing WHERE inDate = null ORDER BY callNumber DESC");

			ResultSet rs = ps.executeQuery();
			
			List<Triple<String, Date, Date>> temp = new ArrayList<Triple<String, Date, Date>>();

			while(rs.next()) {
				Date outDate = rs.getDate("outDate");
				Integer bid = rs.getInt("bid");
				Date dueDate = ComputeDueDate(bid, outDate);
				
				ps = con.prepareStatement("SELECT * FROM borrowing WHERE callNumber = ?");
				
				ps.setString(1, rs.getString("callNumber"));

				ResultSet res = ps.executeQuery();

				GregorianCalendar gregCalendar = new GregorianCalendar();
				Date curDate = new Date(gregCalendar.getTime().getTime());

				if (curDate.compareTo(dueDate) > 0) {
					//flag overdue
				} else {

				}
				
				Triple<String, Date, Date> triple = new Triple<String, Date, Date>(res.getString("title"), outDate, dueDate);
				temp.add(triple);
			}
			
			return temp;
		}
		catch(SQLException e) {
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

/*	private List<Triple> MostPopular(Integer year, Integer n) {
		try {
		ps = con.prepareStatement("SELECT * FROM borrowing WHERE outDate = ? ORDER BY callNumber DESC");
		
		ps.setInt(year);

		ResultSet rs = ps.executeQuery();
		}
		catch(SQLException e) {
			
		}
	}*/
}
