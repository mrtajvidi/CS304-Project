package cpsc304proj;

import java.sql.Date;

public class Fine {

	Integer fine;
	Double amount;
	Date issuedDate;
	
	public Fine(Integer fine, Double a, Date b) {
		this.fine = fine;
		amount = a;
		issuedDate = b;
	}
}
