package exception;

import java.io.IOException;

@SuppressWarnings("serial")
public class DirectoryException extends IOException {

	public DirectoryException(String msg) {
		super(msg);
	}
}
