package library;

import java.sql.Date;

public class DueItem {

	Integer bid;
	String callNumber;
	String outDate;
	String dueDate;
	
	
	public DueItem (Integer bid, String callNumber, String outDate, String dueDate) {
		this.bid = bid;
		this.callNumber = callNumber;
		this.outDate = outDate;
		this.dueDate = dueDate;
	}
}
