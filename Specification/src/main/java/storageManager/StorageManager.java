package storageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configuration.StorageConfiguration;
import fileMetadata.FileMetadata;
import storageInformation.StorageInformation;

public class StorageManager {
	/* Skladiste je predstavljeno kao stablo. U svakom nivou stabla imamo foldere i fajlove sa jedinstvenim nazivima,
	 ukoliko je u pitanju folder onda ce imati listu foldera i fajlova koje sadrzi, u suprotnom lista ce biti null; */ 
	//protected Map<Integer, Map<String, List<FileMetadata>>> StorageTreeStructure = new HashMap<Integer, Map<String, List<FileMetadata>>>();
	
	private static StorageManager instance = null;
	private Map<Integer, List<FileMetadata>> StorageTreeStructure;
	private StorageConfiguration storageConfiguration;
	private StorageInformation storageInformation;
	
	
	private StorageManager() {
		this.StorageTreeStructure = new HashMap<Integer, List<FileMetadata>>();
		this.storageConfiguration = new StorageConfiguration();
		this.storageInformation = new StorageInformation();
	}
	
	public static StorageManager getStorageManager() {
		if(instance == null) {
			
			synchronized (StorageManager.class) {
				if(instance == null)
					instance = new StorageManager();
			}
		}
		
		return instance;
	}
	
	
	
	public Map<Integer, List<FileMetadata>> getStorageTreeStructure() {
		return StorageTreeStructure;
	}
	public void setStorageTreeStructure(Map<Integer, List<FileMetadata>> storageTreeStructure) {
		StorageTreeStructure = storageTreeStructure;
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
