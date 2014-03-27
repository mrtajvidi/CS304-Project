package library;

// We need to import the java.sql package to use JDBC
import java.sql.*;

// for reading from the command line
import java.io.*;

// for the login window
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*
 * This class implements a graphical login window and a simple text
 * interface for interacting with the branch table 
 */ 
public class library implements ActionListener
{
    // command line reader 
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private Connection con;

    // user is allowed 3 login attempts
    private int loginAttempts = 0;

    // components of the login window
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JFrame mainFrame;
    
    private String user;


    /*
     * constructs login window and loads JDBC driver
     */ 
    public library()
    {
      mainFrame = new JFrame("User Login");

      JLabel usernameLabel = new JLabel("Enter username: ");
      JLabel passwordLabel = new JLabel("Enter password: ");

      usernameField = new JTextField(10);
      passwordField = new JPasswordField(10);
      passwordField.setEchoChar('*');

      JButton loginButton = new JButton("Log In");

      JPanel contentPane = new JPanel();
      mainFrame.setContentPane(contentPane);


      // layout components using the GridBag layout manager

      GridBagLayout gb = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();

      contentPane.setLayout(gb);
      contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      // place the username label 
      c.gridwidth = GridBagConstraints.RELATIVE;
      c.insets = new Insets(10, 10, 5, 0);
      gb.setConstraints(usernameLabel, c);
      contentPane.add(usernameLabel);

      // place the text field for the username 
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.insets = new Insets(10, 0, 5, 10);
      gb.setConstraints(usernameField, c);
      contentPane.add(usernameField);

      // place password label
      c.gridwidth = GridBagConstraints.RELATIVE;
      c.insets = new Insets(0, 10, 10, 0);
      gb.setConstraints(passwordLabel, c);
      contentPane.add(passwordLabel);

      // place the password field 
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.insets = new Insets(0, 0, 10, 10);
      gb.setConstraints(passwordField, c);
      contentPane.add(passwordField);

      // place the login button
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.insets = new Insets(5, 10, 10, 10);
      c.anchor = GridBagConstraints.CENTER;
      gb.setConstraints(loginButton, c);
      contentPane.add(loginButton);

      // register password field and OK button with action event handler
      passwordField.addActionListener(this);
      loginButton.addActionListener(this);

      // anonymous inner class for closing the window
      mainFrame.addWindowListener(new WindowAdapter() 
      {
	public void windowClosing(WindowEvent e) 
	{ 
	  System.exit(0); 
	}
      });

      // size the window to obtain a best fit for the components
      mainFrame.pack();

      // center the frame
      Dimension d = mainFrame.getToolkit().getScreenSize();
      Rectangle r = mainFrame.getBounds();
      mainFrame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );

      // make the window visible
      mainFrame.setVisible(true);

      // place the cursor in the text field for the username
      usernameField.requestFocus();

      try 
      {
	// Load the Oracle JDBC driver
	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
      }
      catch (SQLException ex)
      {
	System.out.println("Message: " + ex.getMessage());
	System.exit(-1);
      }
    }


    /*
     * connects to Oracle database named ug using user supplied username and password
     */ 
    private boolean connect(String username, String password)
    {
      String connectURL = "jdbc:oracle:thin:@dbhost.ugrad.cs.ubc.ca:1522:ug"; 

      try 
      {
	con = DriverManager.getConnection(connectURL,username,password);

	System.out.println("\nConnected to Oracle!");
	return true;
      }
      catch (SQLException ex)
      {
	System.out.println("Message: " + ex.getMessage());
	return false;
      }
    }


    /*
     * event handler for login window
     */ 
    public void actionPerformed(ActionEvent e) 
    {
	if ( connect(usernameField.getText(), String.valueOf(passwordField.getPassword())) )
	{
	  // if the username and password are valid, 
	  // remove the login window and display a text menu 
	  mainFrame.dispose();
          showMenu();     
	}
	else
	{
	  loginAttempts++;
	  
	  if (loginAttempts >= 3)
	  {
	      mainFrame.dispose();
	      System.exit(-1);
	  }
	  else
	  {
	      // clear the password
	      passwordField.setText("");
	  }
	}             
                    
    }


    /*
     * displays simple text interface
     */ 
    private void showMenu()
    {
	int choice;
	boolean quit;

	quit = false;
	
	try 
	{
	    // disable auto commit mode
	    con.setAutoCommit(false);

	    while (!quit)
	    {
		System.out.print("\n\nPlease choose one of the following: \n");
		System.out.print("1.  Borrower\n");
		System.out.print("2.  Clerk\n");
		System.out.print("3.  Librarian\n");
		System.out.print("4.  Quit\n>> ");

		choice = Integer.parseInt(in.readLine());
		
		System.out.println(" ");

		switch(choice)
		{
		   case 1:  selectBorrower(); break;
		   case 2:  selectClerk(); break;
		   case 3:  selectLibrarian(); break;
		   case 4:  quit = true;
		}
	    }

	    con.close();
            in.close();
	    System.out.println("\nGood Bye!\n\n");
	    System.exit(0);
	}
	catch (IOException e)
	{
	    System.out.println("IOException!");

	    try
	    {
		con.close();
		System.exit(-1);
	    }
	    catch (SQLException ex)
	    {
		 System.out.println("Message: " + ex.getMessage());
	    }
	}
	catch (SQLException ex)
	{
	    System.out.println("Message: " + ex.getMessage());
	}
    }
    
    
    private void selectBorrower(){
        	int choice;
        	boolean quit;

        	quit = false;
        	
        	try 
        	{
        	    // disable auto commit mode
        	    con.setAutoCommit(false);

        	    while (!quit)
        	    {
        		System.out.print("\n\nPlease choose one of the following: \n");
        		System.out.print("1.  Search\n");
        		System.out.print("2.  Check Account\n");
        		System.out.print("3.  Place a Hold Request\n");
        		System.out.print("4.  Pay Fine\n");
        		System.out.print("5.  Switch User\n");
        		System.out.print("6.  Quit\n>> ");

        		choice = Integer.parseInt(in.readLine());
        		
        		System.out.println(" ");

        		switch(choice)
        		{
        		   case 1:  borrowerSearch(); break;
        		   case 2:  checkAccount(); break;
        		   case 3:  placeHold(); break;
        		   case 4:  payFine(); break;
        		   case 5: 	showMenu(); break;
        		   case 6:  quit = true;
        		}
        	    }

        	    con.close();
                    in.close();
        	    System.out.println("\nGood Bye!\n\n");
        	    System.exit(0);
        	}
        	catch (IOException e)
        	{
        	    System.out.println("IOException!");

        	    try
        	    {
        		con.close();
        		System.exit(-1);
        	    }
        	    catch (SQLException ex)
        	    {
        		 System.out.println("Message: " + ex.getMessage());
        	    }
        	}
        	catch (SQLException ex)
        	{
        	    System.out.println("Message: " + ex.getMessage());
        	}
    }

    private void selectClerk(){
    	int choice;
    	boolean quit;

    	quit = false;
    	
    	try 
    	{
    	    // disable auto commit mode
    	    con.setAutoCommit(false);

    	    while (!quit)
    	    {
    		System.out.print("\n\nPlease choose one of the following: \n");
    		System.out.print("1.  Add Borrower\n");
    		System.out.print("2.  Check Out Item\n");
    		System.out.print("3.  Process Return\n");
    		System.out.print("4.  Check Overdue Items\n");
    		System.out.print("5.  Switch User\n");
    		System.out.print("6.  Quit\n>> ");

    		choice = Integer.parseInt(in.readLine());
    		
    		System.out.println(" ");

    		switch(choice)
    		{
    		   case 1:  addBorrower(); break;
    		   case 2:  checkOutItem(); break;
    		   case 3:  processReturn(); break;
    		   case 4:  checkOverdue(); break;
    		   case 5:	showMenu(); break;
    		   case 6:  quit = true;
    		}
    	    }

    	    con.close();
                in.close();
    	    System.out.println("\nGood Bye!\n\n");
    	    System.exit(0);
    	}
    	catch (IOException e)
    	{
    	    System.out.println("IOException!");

    	    try
    	    {
    		con.close();
    		System.exit(-1);
    	    }
    	    catch (SQLException ex)
    	    {
    		 System.out.println("Message: " + ex.getMessage());
    	    }
    	}
    	catch (SQLException ex)
    	{
    	    System.out.println("Message: " + ex.getMessage());
    	}
}   
    
    
    private void selectLibrarian(){
    	int choice;
    	boolean quit;

    	quit = false;
    	
    	try 
    	{
    	    // disable auto commit mode
    	    con.setAutoCommit(false);

    	    while (!quit)
    	    {
    		System.out.print("\n\nPlease choose one of the following: \n");
    		System.out.print("1.  Add Book\n");
    		System.out.print("2.  Generate Report - Checked Out Books\n");
    		System.out.print("3.  Generate Report - Popular Books\n");
    		System.out.print("4.  Switch User\n");
    		System.out.print("5.  Quit\n>> ");

    		choice = Integer.parseInt(in.readLine());
    		
    		System.out.println(" ");

    		switch(choice)
    		{
    		   case 1:  addNewBook(); break;
    		   case 2:  generateReportCheckedOut(); break;
    		   case 3:  generateReportPopular(); break;
    		   case 4:  showMenu(); break;
    		   case 5:  quit = true;
    		}
    	    }

    	    con.close();
                in.close();
    	    System.out.println("\nGood Bye!\n\n");
    	    System.exit(0);
    	}
    	catch (IOException e)
    	{
    	    System.out.println("IOException!");

    	    try
    	    {
    		con.close();
    		System.exit(-1);
    	    }
    	    catch (SQLException ex)
    	    {
    		 System.out.println("Message: " + ex.getMessage());
    	    }
    	}
    	catch (SQLException ex)
    	{
    	    System.out.println("Message: " + ex.getMessage());
    	}
}
    
    
    /*Search for books using keyword search on titles, authors and subjects. The result is a list 
     *of books that match the search together with the number of copies that are in and out. 
     *Book (callNumber, isbn, title, mainAuthor, publisher, year )
 	 *HasAuthor (callNumber, name)
 	 *HasSubject (callNumber, subject) 
	 */
    private void borrowerSearch(){
    	boolean quit;
    	String keyword;
		ResultSet  rs;
		Statement  stmt;
		String title; 
		String name;
		String subject;
    	
    	quit = false;
    	
    	try{
	    	while (!quit){
	    		System.out.println("Enter Search Keyword (or type quit to leave search): ");
	
	    		keyword = in.readLine();

	    		if(keyword.equals("quit")){
	    			quit = true;
	    		}else{
	    			
		    		stmt = con.createStatement();
		    		
		    		rs = stmt.executeQuery("SELECT * FROM branch");
		
		    		// get info on ResultSet
		    		ResultSetMetaData rsmd = rs.getMetaData();
		
		    		// get number of columns
		    		int numCols = rsmd.getColumnCount();
		
		    		// close the statement; 
		    		// the ResultSet will also be closed
		    		stmt.close();
	    		}
    		}
	    	
    	    con.close();
    	    	in.close();
    	    System.out.println("\nGood Bye!\n\n");
    	    System.exit(0);
    		}
    		catch (SQLException ex)
    		{
    		    System.out.println("Message: " + ex.getMessage());
    		}	catch (IOException e)
    		{
    		    System.out.println("IOException!");
    		}
    	
    }
    
    
    private void checkAccount(){
    	
    }
    
    
    private void placeHold(){
	 
    }
    
    
 	private void payFine(){
 		
 	}

 	private void addBorrower(){
	
 	}


 	private void checkOutItem(){
 
 	}


	private void processReturn(){
		
	}
	
	private void checkOverdue(){
		
	}
	

	private void addNewBook(){
		/* UPDATES:
		 * Book (callNumber, isbn, title, mainAuthor, publisher, year )
		 * HasAuthor (callNumber, name)
		 * HasSubject (callNumber, subject) 
		 * BookCopy (callNumber, copyNo, status) */

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
		    System.out.println("Message: " + ex.getMessage());
		    try 
		    {
			// undo the insert
			con.rollback();	
		    }
		    catch (SQLException ex2)
		    {
			System.out.println("Message: " + ex2.getMessage());
			System.exit(-1);
		    }
		}
	}

	
	
 /* Generate a report with all the books that have been checked out. For each book the report 
  * shows the date it was checked out and the due date. 
  * 
  * The system flags the items that are overdue. 
  * The items are ordered by the book call number. 
  * If a subject is provided the report lists only books related to that subject, 
  * otherwise all the books that are out are listed by the report. 
  */
	private void generateReportCheckedOut(){
		//Borrowing(borid, bid, callNumber, copyNo, outDate, inDate) 
		//BookCopy (callNumber, copyNo, status) 
		
		String     callNumber;
		String     copyNo;
		String     status;
		Statement  stmt;
		ResultSet  rsStatus;
		ResultSet  rsDate;
		
		
		try
		{
		  stmt = con.createStatement();

		  rsStatus = stmt.executeQuery("SELECT * FROM BookCopy WHERE status = 'out'");

		  // get info on ResultSet
		  ResultSetMetaData rsmd = rsStatus.getMetaData();

		  // get number of columns
		  int numCols = rsmd.getColumnCount();

		  System.out.println(" ");
		  
		  // display column names;
		  for (int i = 0; i < numCols; i++)
		  {
		      // get column name and print it

		      System.out.printf("%-15s", rsmd.getColumnName(i+1));    
		  }

		  System.out.println(" ");

		  while(rsStatus.next())
		  {
		      // for display purposes get everything from Oracle 
		      // as a string

		      // simplified output formatting; truncation may occur

		      callNumber = rsStatus.getString("callNumber");
		      System.out.printf("%-20.20s", callNumber);

		      copyNo = rsStatus.getString("copyNo");
		      System.out.printf("%-10.10s", copyNo);

		      status = rsStatus.getString("status");
		      System.out.printf("%-15.15s", status);
    
		  }
	 
		  // close the statement; 
		  // the ResultSet will also be closed
		  stmt.close();
		}
		catch (SQLException ex)
		{
		    System.out.println("Message: " + ex.getMessage());
		}
		
	}

	private void generateReportPopular(){
	
	}

    //values must be of the form (?,?,?,?,...,?)
    private void insertTuple(String table, String values){
    	PreparedStatement ps;
    	try{
    		ps = con.prepareStatement("INSERT INTO"+table +"VALUES" + values);
    	    ps.executeUpdate();

    		// commit work 
    	    con.commit();

    		ps.close();
    	}
    	catch (SQLException ex)
    	{
    	    System.out.println("Message: " + ex.getMessage());
    	    try 
    	    {
    		// undo the insert
    		con.rollback();	
    	    }
    	    catch (SQLException ex2)
    	    {
    		System.out.println("Message: " + ex2.getMessage());
    		System.exit(-1);
    	    }
    	}
    }

    
 
    public static void main(String args[])
    {
      library l = new library();
    }
}

