package library;

import java.sql.Date;

public class Fine {

	Integer fine;
	Double amount;
	String issuedDate;
	String callNumber;
	String title;

	public Fine(Integer fine, Double amount, String issdate, String title, String callNum) {
		this.fine = fine;
		this.amount = amount;
		this.issuedDate = issdate;
		this.title = title;
		this.callNumber = callNum;
	}
}