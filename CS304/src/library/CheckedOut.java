package library;

public class CheckedOut {

	String callNumber, copyNumber, status, borid, outDate, inDate;
	int overdue_flag;

	public CheckedOut(String callNumber, String copyNumber, String status, String borid, String outDate, String inDate, int overdue_flag) {
		this.callNumber	= callNumber;
		this.copyNumber = copyNumber;
		this.status = status;
		this.borid = borid;
		this.outDate = outDate;
		this.inDate = inDate;
		this.overdue_flag = overdue_flag;
	}
}
