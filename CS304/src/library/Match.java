package library;

public class Match {
	
	String title;
	String isbn;
	int numOfCopiesIn;
	int numOfCopiesOut;

	public Match(String title, String a, int b, int c) {
		this.title = title;
		isbn = a;
		numOfCopiesIn = b;
		numOfCopiesOut = c;		
	}
}
