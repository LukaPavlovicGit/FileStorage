package exception;

import java.io.IOException;

@SuppressWarnings("serial")
public class StorageSizeException extends IOException {

	public StorageSizeException(String msg) {
		super(msg);
	}

}
