package exception;

@SuppressWarnings("serial")
public class NotAllowedOperation extends Exception {
	
	public NotAllowedOperation(String msg) {
		super(msg);
	}
}
