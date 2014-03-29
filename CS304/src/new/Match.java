package cpsc304proj;

public class Match {
	
	String title;
	Integer isbn;
	int numOfCopiesIn;
	int numOfCopiesOut;

	public Match(String title, int a, int b, int c) {
		this.title = title;
		isbn = a;
		numOfCopiesIn = b;
		numOfCopiesOut = c;		
	}
}
