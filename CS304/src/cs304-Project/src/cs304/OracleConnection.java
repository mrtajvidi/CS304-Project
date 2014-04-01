/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cs304;

import java.sql.*; 

/**
 *
 * @author Collin
 */
public class OracleConnection {
    
    private static OracleConnection oracle = null;
    public Connection con = null;

    public OracleConnection() {
        
    }
     
    public static OracleConnection getInstance() {
	if (oracle == null) {
	    oracle = new OracleConnection(); 
	}

	return oracle;
    }
    
    public boolean connect(String username, String password) {
      
      String connectURL = "jdbc:oracle:thin:@dbhost.ugrad.cs.ubc.ca:1522:ug"; 

      try {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
          
	con = DriverManager.getConnection(connectURL, username, password);
        
        con.setAutoCommit(false);
        
	return true;
      }
      catch (SQLException e) {
	//System.out.println("Message: " + e.getMessage());
	return false;
      }
    }
    
    public Connection getConnection() {
	return con; 
    }
}
