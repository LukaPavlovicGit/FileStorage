package exception;

import exception.storageConfigurationException.StorageConfigurationException;

public class DirectoryException extends StorageConfigurationException{

	public DirectoryException(String msg) {
		super(msg);
	}

}
