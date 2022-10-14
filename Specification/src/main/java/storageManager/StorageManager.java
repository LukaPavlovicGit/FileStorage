package storageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configuration.StorageConfiguration;
import fileMetadata.FileMetadata;
import storageInformation.StorageInformation;

public class StorageManager {
	
	private static StorageManager instance = null;
	
	private StorageConfiguration storageConfiguration;
	private StorageInformation storageInformation;
	
	
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
