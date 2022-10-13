package exception;

import exception.storageConfigurationException.StorageConfigurationException;

public class StorageSizeException extends StorageConfigurationException{

	public StorageSizeException(String msg) {
		super(msg);
	}

}
