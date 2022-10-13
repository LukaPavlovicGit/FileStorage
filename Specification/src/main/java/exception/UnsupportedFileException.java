package exception;

import java.io.IOException;

public class UnsupportedFileException extends IOException{

	public UnsupportedFileException(String msg) {
		super(msg);
	}

}
