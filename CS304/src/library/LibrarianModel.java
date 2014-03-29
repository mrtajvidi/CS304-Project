package library;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibrarianModel {
	
	private PreparedStatement ps = null;
	private Connection con = null;
	
	public LibrarianModel() {
		
	}
	
/*	
	private void addNewBook(){
		/* UPDATES:
		 * Book (callNumber, isbn, title, mainAuthor, publisher, year )
		 * HasAuthor (callNumber, name)
		 * HasSubject (callNumber, subject) 
		 * BookCopy (callNumber, copyNo, status) 

		//Book Values
		String callNumber;
		String isbn;
		String title;
		String mainAuthor;
		String publisher;
		int year;
		
		//HasAuthor Values
		String name; 
		
		//HasSubject Values
		String subject;
		
		//BookCopy Values
		String copyNo;
		String status;
		
		PreparedStatement psBook;
		PreparedStatement psAuthor;
		PreparedStatement psSubject;
		PreparedStatement psCopy;
		
		boolean moreAuthors = true;
		String authorVal;
		
		boolean moreSubjects = true;
		String subjectVal;
		
		boolean moreCopies = true;
		String copyVal; 
		
		try
		{
		  psBook = con.prepareStatement("INSERT INTO Book VALUES (?,?,?,?,?,?)");
		  psAuthor = con.prepareStatement("INSERT INTO HasAuthor VALUES (?,?)");
		  psSubject = con.prepareStatement("INSERT INTO HasSubject VALUES (?,?)");
		  psCopy = con.prepareStatement("INSERT INTO BookCopy VALUES (?,?,?)");
		  
		  System.out.print("\nCall Number: ");
		  callNumber = in.readLine();
		  psBook.setString(1, callNumber);
		  psAuthor.setString(1, callNumber);
		  psSubject.setString(1, callNumber);
		  psCopy.setString(1, callNumber);

		  System.out.print("\nISBN: ");
		  isbn = in.readLine();
		  psBook.setString(2, isbn);
		  
		  System.out.print("\nTitle: ");
		  title = in.readLine();
		  psBook.setString(3, title);
		  
		  System.out.print("\nMain Author: ");
		  mainAuthor = in.readLine();
		  psBook.setString(4, mainAuthor);
		  psAuthor.setString(2, mainAuthor);
		  
		  System.out.print("\nYear: ");
		  String yearTemp = in.readLine();
		  if (yearTemp.length() == 0)
		  {
		      psBook.setNull(6, java.sql.Types.INTEGER);
		  }
		  else
		  {
		      year = Integer.parseInt(yearTemp);
		      psBook.setInt(6, year);
		  }

		  System.out.print("\nPublisher: ");
		  publisher = in.readLine();
		  
		  if (publisher.length() == 0)
	          {
		      psBook.setString(5, null);
		  }
		  else
		  {
		      psBook.setString(5, publisher);
		  }
		  
		  System.out.print("\nSubject: ");
		  subject = in.readLine();
		  psSubject.setString(2, subject);
		  
		  System.out.print("\nCopy Number: ");
		  copyNo = in.readLine();
		  psCopy.setString(2, copyNo);
		  
		  System.out.print("\nStatus: ");
		  status = in.readLine();
		  psCopy.setString(3, status);
			 
		  psBook.executeUpdate();
		  psAuthor.executeUpdate();
		  psSubject.executeUpdate();
		  psCopy.executeUpdate();
		  
		  while (moreAuthors){
			  System.out.print("\nOther Authors? (if yes enter y, or n otherwise): ");
			  authorVal = in.readLine();
			  
			  if(authorVal.equals("y")){
				  System.out.print("\nAuthor Name: ");
				  name = in.readLine();
				  psAuthor.setString(2, name);
				  psAuthor.executeUpdate();
				  moreAuthors = true;
			  }else{
				  moreAuthors = false;
			  }
		  }	
		  
		  while (moreSubjects){
			  System.out.print("\nOther Subjects? (if yes enter y, or n otherwise): ");
			  subjectVal = in.readLine();
			  
			  if(subjectVal.equals("y")){
				  System.out.print("\nAdditional Subject: ");
				  subject = in.readLine();
				  psSubject.setString(2, subject);
				  psSubject.executeUpdate();
				  moreSubjects = true;
			  }else{
				  moreSubjects = false;
			  }
		  }	
		  
		  while (moreCopies){
			  System.out.print("\nOther Copies? (if yes enter y, or n otherwise): ");
			  copyVal = in.readLine();
			  
			  if(copyVal.equals("y")){
				  System.out.print("\nAdditional Copy Number: ");
				  copyNo = in.readLine();
				  psCopy.setString(2, copyNo);
				  psCopy.executeUpdate();
				  moreCopies = true;
			  }else{
				  moreCopies = false;
			  }
		  }	

		  // commit work 
		  con.commit();

		  psBook.close();
		  psAuthor.close();
		  psSubject.close();
		  psCopy.close();
		  
		}
		catch (IOException e)
		{
		    System.out.println("IOException!");
		}
		catch (SQLException ex)
		{
		    
		}
	}*/
	
	
	
}