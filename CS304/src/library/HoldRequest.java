package library;

import java.sql.Date;

public class HoldRequest {
	
	String title;
	String isbn;
	Date issuedDate;
	
	public HoldRequest(String a, String b, Date c) {
		title = a;
		isbn = b;
		issuedDate = c;
	}
}
