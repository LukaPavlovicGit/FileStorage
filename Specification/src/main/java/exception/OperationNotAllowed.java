package exception;

@SuppressWarnings("serial")
public class OperationNotAllowed extends Exception {
	
	public OperationNotAllowed(String msg) {
		super(msg);
	}
}
