package storageManager;

import specification.Storage;
import storageInformation.StorageInformation;

/**
 * 
 * @author Luka Pavlovic
 *
 */
public class StorageManager {
	
	private static StorageManager instance = null;
	private static Storage storage = null;
	private StorageInformation storageInformation;
	
	
    public static void registerStorage(Storage storageImplementation) {
    	storage = storageImplementation;
    }
    
	private StorageManager() {
		this.storageInformation = new StorageInformation();
	}
	
	public static StorageManager getInstance() {
		if(instance == null) {
			
			synchronized (StorageManager.class) {
				if(instance == null)
					instance = new StorageManager();
			}
		}
		
		return instance;
	}
	
	public static Storage getStorage() {
		return storage;
	}

	public static void setStorage(Storage storage) {
		StorageManager.storage = storage;
	}
	public StorageInformation getStorageInformation() {
		return storageInformation;
	}
	public void setStorageInformation(StorageInformation storageInformation) {
		this.storageInformation = storageInformation;
	}
}
