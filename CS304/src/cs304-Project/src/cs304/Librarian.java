package cs304;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Librarian {
	
	private Connection con = null;

	public Librarian() {
		con = OracleConnection.getInstance().getConnection();
	}

	public void AddBook(String copyCallNumber, String isbn, String title, String mainAuthor, 
						String publisher, Integer year, List<String> authors,  List<String> subjects) throws SQLException {
            String[] strArr = copyCallNumber.split(" ");
            String callNumber = strArr[0] + " " + strArr[1];
            String copyNo = strArr[2];
            
			PreparedStatement ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
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
				ps.setString(2, isbn);
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
	
	private void AddAuthors(String callNumber, List<String> authors) throws SQLException {
		for (String author: authors) {
			PreparedStatement ps = con.prepareStatement("INSERT INTO hasauthor VALUES (?,?)");
			
			ps.setString(1, callNumber);
			ps.setString(2, author);
			
			ps.executeUpdate();

			ps.close();
		}
	}

	private void AddSubjects(String callNumber, List<String> subjects) throws SQLException {
			for (String subject : subjects) {
				PreparedStatement ps = con.prepareStatement("INSERT INTO hassubject VALUES (?,?)");

				ps.setString(1, callNumber);
				ps.setString(2, subject);

				ps.executeUpdate();

				ps.close();
			}
	}
	
	public List<SixTuple<String, String, String, Date, Date, String>> displayCheckedOut() throws SQLException {
            
                        List<SixTuple<String, String, String, Date, Date, String>> temp = new ArrayList<>();
            
			PreparedStatement ps = con.prepareStatement("SELECT * FROM borrowing WHERE inDate is null ORDER BY callNumber DESC");
			ResultSet rs = ps.executeQuery();

			while(rs.next()) {
				ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
                                String callNumber = rs.getString("callNumber");
				ps.setString(1, callNumber);
				ResultSet res = ps.executeQuery();
                                
                                Date outDate = rs.getDate("outDate");
				Integer bid = rs.getInt("bid");
                                String copyNo = rs.getString("copyNo");
                                String title = res.getString("title");
				Date dueDate = ComputeDueDate(bid, outDate);
                                
                                ps = con.prepareStatement("SELECT sysdate FROM dual WHERE sysdate >= ?");
                                ps.setDate(1, dueDate);
                                ResultSet reset = ps.executeQuery();
                                
                                SixTuple<String, String, String, Date, Date, String> sixtuple = null;
                                
                                if (reset.next()) {
                                    sixtuple = new SixTuple<String, String, String, Date, Date, String>(title, callNumber, copyNo, outDate, dueDate,"yes");
                                }
                                else {
                                    sixtuple = new SixTuple<String, String, String, Date, Date, String>(title, callNumber, copyNo, outDate, dueDate,"no");
                                }
				
				temp.add(sixtuple);
			}
			
			return temp;
	}
        
        public List<SixTuple<String, String, String, Date, Date, String>> displayCheckedOut(String subject) throws SQLException {
            
                        List<SixTuple<String, String, String, Date, Date, String>> temp = new ArrayList<>();
                        
			PreparedStatement ps = con.prepareStatement("SELECT * FROM borrowing NATURAL JOIN hassubject WHERE inDate is null AND subject = ? ORDER BY callNumber DESC");
                        ps.setString(1, subject);
			ResultSet rs = ps.executeQuery();

			while(rs.next()) {
                                ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");
                                String callNumber = rs.getString("callNumber");
                                ps.setString(1, callNumber);
                                ResultSet res = ps.executeQuery();
                                
				Date outDate = rs.getDate("outDate");
				Integer bid = rs.getInt("bid");
                                String copyNo = rs.getString("copyNo");
                                String title = res.getString("title");
				Date dueDate = ComputeDueDate(bid, outDate);
                                
                                ps = con.prepareStatement("SELECT sysdate FROM dual where sysdate >= ?");
                                ps.setDate(1, dueDate);
                                ResultSet reset = ps.executeQuery();
                
                                SixTuple<String, String, String, Date, Date, String> sixtuple = null;
                                
                                if (reset.next()) {
                                    sixtuple = new SixTuple<String, String, String, Date, Date, String>(title, callNumber, copyNo, outDate, dueDate,"yes");
                                }
                                else {
                                    sixtuple = new SixTuple<String, String, String, Date, Date, String>(title, callNumber, copyNo, outDate, dueDate,"no");
                                }
				
				temp.add(sixtuple);
			}
			
			return temp;
	}

	private Date ComputeDueDate(Integer bid, Date outDate) throws SQLException {

			PreparedStatement ps = con.prepareStatement("SELECT bookTimeLimit FROM borrower NATURAL JOIN borrowertype WHERE bid = ?");
			ps.setInt(1, bid);
			ResultSet rs = ps.executeQuery();

			Integer booktimelimit = rs.getInt("bookTimeLimit");
			
			ps = con.prepareStatement("SELECT (? + ?) as dueDate FROM dual");
                        ps.setDate(1, outDate);
                        ps.setInt(2, booktimelimit);
                        
                        rs = ps.executeQuery();
                        
                        Date dueDate = rs.getDate("dueDate");
			
			return dueDate;
	}

	public List<FourTuple<String,String,String,Integer>> MostPopular(Integer year, Integer n) throws SQLException {
            
                        List<FourTuple<String,String,String,Integer>> temp = new ArrayList<>();
                        
			PreparedStatement ps = con.prepareStatement("SELECT callNumber, COUNT(callnumber) AS Count FROM borrowing WHERE extract(year from outDate) = ? GROUP BY callNumber ORDER BY Count DESC");
			ps.setInt(1, year);
			ResultSet rs = ps.executeQuery();
                        
			for (int i = 0; i < n; i++) {
				if (rs.next()) {
                                        String callNumber = rs.getString("callnumber");
                                        Integer count = rs.getInt("Count");
                                        
					ps = con.prepareStatement("SELECT title, isbn FROM book WHERE callNumber = ?");
					ps.setString(1, callNumber);
                                        ResultSet res = ps.executeQuery();
                                        
                                        String title = res.getString("title");
                                        String isbn = res.getString("isbn");
					
					FourTuple<String,String,String,Integer> fourtuple = new FourTuple<String,String,String,Integer>(title, isbn, callNumber, count);
					
					temp.add(fourtuple);
				} else {
					break;
				}
			}
			
			return temp;
			
	}
}
