package cpsc304proj;

import java.sql.Date;

public class DueItem {

	Integer bid;
	String title;
	Integer isbn;
	Date outDate;
	
	public DueItem (Integer bid, String a, Integer b, Date c) {
		this.bid = bid;
		title = a;
		isbn = b;
		outDate = c;
	}
}
