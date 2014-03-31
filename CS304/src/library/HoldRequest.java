package library;

import java.sql.Date;

public class HoldRequest {

	String title;
	String callNumber;
	String issuedDate;

	public HoldRequest(String title, String callNum, String issDate) {
		this.title = title;
		this.callNumber = callNum;
		this.issuedDate = issDate;
	}
}