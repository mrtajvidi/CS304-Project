package library;

// We need to import the java.sql package to use JDBC
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// for reading from the command line
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
















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
    
    //private String user;
	BorrowerModel b = new BorrowerModel();


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
      String connectURL = "jdbc:oracle:thin:@localhost:1522:ug"; 

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
        		   //case 2: 	checkCopies(); break;
        		   case 3:  placeHold(); break;
        		   case 4:  payFine(); break;
        		   case 5: 	switchUser(); break;
        		   case 6:  showMenu(); break;
        		}
        	    }

        	    con.close();
                //in.close();
        	    //System.out.println("\nGood Bye!\n\n");
        	    //System.exit(0);
                 
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
            //in.close();
    	    //System.out.println("\nGood Bye!\n\n");
    	    //System.exit(0);
            showMenu();
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
           // in.close();
            showMenu();
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
    
    
    private void switchUser(){
    	int user;
    	try{
    		System.out.println("Enter BID: ");
    		user = Integer.parseInt(in.readLine());
    		
    		//BorrowerModel b = new BorrowerModel();
    		Class c = b.getClass();
    		
    		Method userSwitch= c.getDeclaredMethod("setUser", Integer.class);
	        userSwitch.setAccessible(true);
	        userSwitch.invoke(b, user);
	        
	        Method userRetrieve= c.getDeclaredMethod("getUser");
	        userRetrieve.setAccessible(true);
	        System.out.println("CURRENT USER: " + userRetrieve.invoke(b));	
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
    	} catch (NoSuchMethodException x) {
		    x.printStackTrace();
		} catch (InvocationTargetException x) {
		    x.printStackTrace();
		} catch (IllegalAccessException x) {
		    x.printStackTrace();
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
    	//BorrowerModel b = new BorrowerModel();
		Class c = b.getClass();
		ArrayList<Match> books = new ArrayList<Match>();
		Object tempBooks;
    	quit = false;

    	//RETURN TYPES
    	
    	try{
	    	while (!quit){
	    		System.out.println("Enter Search Keyword (or type quit to leave search): ");
	
	    		keyword = in.readLine();

	    		if(keyword.equals("quit")){
	    			quit = true;
	    		}else{
	    			
	    			try{    
	    		    	Method findKeyword= c.getDeclaredMethod("findKeyword", String.class, Connection.class);
	    		        findKeyword.setAccessible(true);
	    		        tempBooks = findKeyword.invoke(b, keyword, con);
	    		        books = (ArrayList<Match>) tempBooks;
	    		        
	    		        if (books.size() == 0){
	    		        	System.out.println("No Results Match Search");
	    		        }else{
		    		        //callNumber, title, mainAuthor, isbn, numOfCopiesIn, numOfCopiesOut
		    		        
		    		        System.out.printf("%-15s %-15s %-15s %-15s %-15s %-15s", "CallNumber", "Title", "Main Author","ISBN" ,"Num Copies In", "Num Copies Out");
		    		        System.out.println(" ");
		    		        
		    		        for (int i = 0; i< books.size(); i++){
		    		        	Match temp = books.get(i);
			    		        System.out.printf("%-15s %-15s %-15s %-15s %-15s %-15s", temp.callNumber, temp.title,temp.mainAuthor,temp.isbn ,temp.numOfCopiesIn, temp.numOfCopiesOut);
	
		    		        	System.out.println(" ");
		    		        }
	    		        }
	    		        
	    			}catch (NoSuchMethodException x) {
	    			    x.printStackTrace();
	    			} catch (InvocationTargetException x) {
	    			    x.printStackTrace();
	    			} catch (IllegalAccessException x) {
	    			    x.printStackTrace();
	    			}
	    			
	    		}
    		}
	    	
	    	//in.close();
	    	selectBorrower();
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
    	    	
    	
    }
    

	/**Check his/her account. 
	 * The system will display the items the borrower has currently borrowed and not yet returned,
	 * any outstanding fines and the hold requests that have been placed by the borrower
	 */
    private void checkAccount(){
      	boolean quit;
    	//String bid;
      	Integer bid;
      	
		Class c = b.getClass();
		ArrayList<Match> books = new ArrayList<Match>();
		
		List<Triple<String,String,String>> borrows = new ArrayList<Triple<String,String,String>>();
		List<Fine> fines = new ArrayList<Fine>();
		List<HoldRequest> holdrequests = new ArrayList<HoldRequest>();
		
		Object tempBorrows;
		Object tempFines;
		Object tempRequests;
    	quit = false;

    	//RETURN TYPES
    	
    	try{
	    	while (!quit){
	    		System.out.println("Type quit to leave account: ");
	
	    		//bid = in.readLine();

	    		if(in.readLine().equals("quit")){
	    			quit = true;
	    		}else{
	    			
	    			try{    
	    				Method userRetrieve= c.getDeclaredMethod("getUser");
	    		        userRetrieve.setAccessible(true);
	    		        bid = (Integer) userRetrieve.invoke(b);	
	    		        System.out.println("BID: " + bid);
	    		        
	    		    	Method checkBorrows= c.getDeclaredMethod("CheckAccountBorrows", Integer.class, Connection.class);
	    		        checkBorrows.setAccessible(true);
	    		        //tempBooks = checkAccount.invoke(b, Integer.parseInt( bid , con);
	    		        tempBorrows = checkBorrows.invoke(b, bid , con);
	    		        
	    		        Method checkFines= c.getDeclaredMethod("CheckAccountFines", Integer.class, Connection.class);
	    		        checkFines.setAccessible(true);
	    		        tempFines = checkFines.invoke(b, bid , con);
	    		        
	    		        Method checkRequests= c.getDeclaredMethod("CheckAccountHoldRequests", Integer.class, Connection.class);
	    		        checkRequests.setAccessible(true);
	    		        tempRequests = checkRequests.invoke(b, bid , con);
	    		        
	    		        System.out.println(" ");
	    		        
	    		        borrows = (List<Triple<String,String,String>>) tempBorrows;
    		        	System.out.println("BOOKS BORROWED:");
	    		        
	    		        if (borrows.size() == 0){
	    		        	System.out.println("No Books Currently Borrowed");
	    		        }else{
	    		        	//(rs.getString("title"), rs.getString("mainAuthor"), rs.getString("outDate"));
		    		        
		    		        System.out.printf("%-15s %-15s %-15s", "Title", "Main Author","Out Date");
		    		        System.out.println(" ");
		    		        
		    		        for (int i = 0; i< borrows.size(); i++){
		    		        	Triple temp = borrows.get(i);
			    		        System.out.printf("%-15s %-15s %-15s", temp.x, temp.y, temp.z );
	
		    		        	System.out.println(" ");
		    		        }
	    		        }
	    		        
	    		        
	    		        fines = (ArrayList<Fine>) tempFines;
	    		        
	    		        System.out.println(" ");
    		        	System.out.println("FINES:");
	    		        
	    		        if (fines.size() == 0){
	    		        	System.out.println("No Outstanding Fines");
	    		        }else{
	    		        	//Integer fine, Double amount, Date issdate, String title, String callNum
		    		        
		    		        System.out.printf("%-15s %-15s %-15s %-15s %-15s", "Fine ID", "Amount","Issue Date", "Book Title", "Book Call Number");
		    		        System.out.println(" ");
		    		        
		    		        for (int i = 0; i< fines.size(); i++){
		    		        	Fine temp = fines.get(i);
			    		        System.out.printf("%-15s %15s %-15s %-15s %-15s", temp.fine, temp.amount, temp.issuedDate, temp.title, temp.callNumber );
	
		    		        	System.out.println(" ");
		    		        }
	    		        }
	    		        
	    		        
	    		        holdrequests = (ArrayList<HoldRequest>) tempRequests;
	    		        
	    		        System.out.println(" ");
    		        	System.out.println("HOLD REQUESTS:");
	    		        
	    		        if (holdrequests.size() == 0){
	    		        	System.out.println("No Current Holds");
	    		        }else{
	    		        	//HoldRequest(String title, String callNum, String issDate)
		    		        
		    		        System.out.printf("%-15s %-15s %-15s", "Book Title", "Book Call Number","Issue Date");
		    		        System.out.println(" ");
		    		        
		    		        for (int i = 0; i< holdrequests.size(); i++){
		    		        	HoldRequest temp = holdrequests.get(i);
			    		        System.out.printf("%-15s %15s %-15s", temp.title, temp.callNumber, temp.issuedDate );
	
		    		        	System.out.println(" ");
		    		        }
	    		        }

	    			}catch (NoSuchMethodException x) {
	    			    x.printStackTrace();
	    			} catch (InvocationTargetException x) {
	    			    x.printStackTrace();
	    			} catch (IllegalAccessException x) {
	    			    x.printStackTrace();
	    			}
	    			
	    		}
    		}
	    	
	    	//in.close();
	    	selectBorrower();
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
    	    
    }
    
    
    private void checkCopies(){
      	boolean quit;
    	String bid;
    	//BorrowerModel b = new BorrowerModel();
		Class c = b.getClass();
    	quit = false;

    	//RETURN TYPES
    	
    	try{
	    	while (!quit){
	    		System.out.println("Enter Callnumber (or type quit to leave search): ");
	
	    		bid = in.readLine();

	    		if(bid.equals("quit")){
	    			quit = true;
	    		}else{
	    			
	    			try{    
	    		    	Method checkAccount= c.getDeclaredMethod("showBookCopyTable", Connection.class);
	    		        checkAccount.setAccessible(true);
	    		        checkAccount.invoke(b, con);
	    		        
	    			}catch (NoSuchMethodException x) {
	    			    x.printStackTrace();
	    			} catch (InvocationTargetException x) {
	    			    x.printStackTrace();
	    			} catch (IllegalAccessException x) {
	    			    x.printStackTrace();
	    			}
	    			
	    		}
    		}
	    	
	    	in.close();
	    	selectBorrower();
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
    	    
    }
    
    
    /**
     * Place a hold request for a book that is out. When the item is returned, the system sends an 
     * email to the borrower and informs the library clerk to keep the book out of the shelves. 
     * 
     * USER INPUTS TITLE OR CALLNUMBER
     * PROGRAM ENTERS INFORMATION INTO HOLD REQUEST TABLE 
     * PROGRAM THEN MUST CHECK WHEN A USER RETURNS A BOOK IF THERE IS A HOLD REQUEST ON IT --CLERK TRANSACTION
     */
    private void placeHold(){
    	//PlaceHoldRequest(Integer hid, Integer bid, String callNumber, Date issuedDate, Connection con) {
    	//TABLE: HoldRequest(hid, bid, callNumber, issuedDate) 
    	
    	boolean quit;
    	String hold;
    	String bid;
    	String searchMethod;
    	//BorrowerModel b = new BorrowerModel();
		Class c = b.getClass();
    	quit = false;

    	//RETURN TYPES
    	
    	try{
	    	while (!quit){
	    		System.out.println("Enter Borrower ID (or type quit to leave search): ");
	    		
	    		bid = in.readLine();
	    		
	    		System.out.println("Enter t to place hold by title or enter c to place hold by callnumber (or type quit to leave search): ");
	
	    		hold = in.readLine();

	    		if(hold.equals("quit")){
	    			quit = true;
	    		}else{
	    			
	    			if (hold.equals("t")){
	    				searchMethod = "title";
	    				System.out.println("Enter a Book Title: ");
	    				hold = in.readLine();
	    			}
	    			else if (hold.equals("c")){
	    				searchMethod = "call";
	    				System.out.println("Enter a Call Number: ");
	    				hold = in.readLine();
	    			}
	    			else{
	    				System.out.println("Please enter a valid search method");
	    			}
	    			
	    			try{    
	    		    	Method placeHold= c.getDeclaredMethod("PlaceHoldRequest", Integer.class, String.class, Connection.class);
	    		        placeHold.setAccessible(true);
	    		        placeHold.invoke(b, Integer.parseInt( bid ), hold, con);
	    			}catch (NoSuchMethodException x) {
	    			    x.printStackTrace();
	    			} catch (InvocationTargetException x) {
	    			    x.printStackTrace();
	    			} catch (IllegalAccessException x) {
	    			    x.printStackTrace();
	    			}
	    			
	    		}
    		}
	    	
	    	in.close();
    	    selectBorrower();
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
    	
	 
    }
    
    //Fine (fid, amount, issuedDate, paidDate, borid) 
    //ONLY PAY FINES IN ENTIRETY RIGHT NOW
 	private void payFine(){
    	boolean quit;
    	String bid;
    	//BorrowerModel b = new BorrowerModel();
		Class c = b.getClass();
    	quit = false;

    	//RETURN TYPES
    	
    	try{
	    	while (!quit){
	    		System.out.println("Enter bid (or type quit to leave search): ");
	
	    		bid = in.readLine();

	    		if(bid.equals("quit")){
	    			quit = true;
	    		}else{
	    			
	    			try{    
	    		    	Method payFines= c.getDeclaredMethod("payFine", Integer.class, Connection.class);
	    		        payFines.setAccessible(true);
	    		        payFines.invoke(b, Integer.parseInt( bid ) ,con);
	    		        
	    			}catch (NoSuchMethodException x) {
	    			    x.printStackTrace();
	    			} catch (InvocationTargetException x) {
	    			    x.printStackTrace();
	    			} catch (IllegalAccessException x) {
	    			    x.printStackTrace();
	    			}
	    			
	    		}
    		}
	    	
	    	in.close();
	    	selectBorrower();
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
		
	}


	private void generateReportCheckedOut(){
		
	}

	private void generateReportPopular(){
	
	}
    
 
    public static void main(String args[])
    {
      library l = new library();
    }
}

