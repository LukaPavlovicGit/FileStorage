package storageManager;

import configuration.StorageConfiguration;
import specification.Storage;
import storageInformation.StorageInformation;

public class StorageManager {
	
	public static boolean storageIsConnected = false;
	private static StorageManager instance = null;
	private static Storage storage = null;
	private StorageConfiguration storageConfiguration;
	private StorageInformation storageInformation;
	
	
    public static void registerStorage(Storage storageImplementation) {
    	storage = storageImplementation;
    }
    
	private StorageManager() {
		this.storageConfiguration = new StorageConfiguration();
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

	public StorageConfiguration getStorageConfiguration() {
		return storageConfiguration;
	}
	public void setStorageConfiguration(StorageConfiguration storageConfiguration) {
		this.storageConfiguration = storageConfiguration;
	}
	public StorageInformation getStorageInformation() {
		return storageInformation;
	}
	public void setStorageInformation(StorageInformation storageInformation) {
		this.storageInformation = storageInformation;
	}
}
