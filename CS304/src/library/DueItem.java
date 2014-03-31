package library;

import java.sql.Date;

public class DueItem {

	Integer bid;
	String callNumber;
	String copyNo;
	String outDate;
	String inDate;
	String dueDate;
	
	
	public DueItem (Integer bid, String callNumber, String copyNo,String outDate, String inDate,String dueDate) {
		this.bid = bid;
		this.callNumber = callNumber;
		this.copyNo = copyNo;
		this.outDate = outDate;
		this.inDate = inDate;
		this.dueDate = dueDate;
	}
}
