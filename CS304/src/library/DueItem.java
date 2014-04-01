package library;

import java.sql.Date;

public class DueItem {

	Integer bid;
	String title;
	Integer isbn;
	Date outDate;
	Date inDate;
	
	public DueItem (Integer bid, String a, Integer b, Date c, Date d) {
		this.bid = bid;
		title = a;
		isbn = b;
		outDate = c;
		inDate = d;
	}
}
