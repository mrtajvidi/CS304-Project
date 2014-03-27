package cpsc304proj;

import java.sql.Date;

public class Fine {

	Double amount;
	Date issuedDate;
	
	public Fine(Double a, Date b) {
		amount = a;
		issuedDate = b;
	}
}
