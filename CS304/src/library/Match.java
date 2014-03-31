package library;

public class Match {
	
	String title;
	String isbn;
	int numOfCopiesIn;
	int numOfCopiesOut;
	String mainAuthor;
	String callNumber;
	//Match(callNumber, title, mainAuthor, isbn, numOfCopiesIn, numOfCopiesOut)

	public Match(String callNumber, String title, String mainAuthor, String isbn, int in, int out) {
		this.title = title;
		this.isbn = isbn;
		numOfCopiesIn = in;
		numOfCopiesOut = out;
		this.callNumber = callNumber;
		this.mainAuthor = mainAuthor;
	}
}
