package storageInformation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fileMetadata.FileMetadata;

public class StorageInformation {
	public static final String storageName = "storage";
	public static final String datarootDirName = "dataRootDirectory";
	public static final String configJSONFileName = "configuration.json";
	public static final String strorageTreeStructureJSONFileName = "storageTreeStructure.json";
	private Map<FileMetadata, List<FileMetadata>> storageTreeStructure = new HashMap<FileMetadata, List<FileMetadata>>();
	private FileMetadata storageDirectory;
	private FileMetadata datarootDirectory;
	private FileMetadata configJSON;
	private FileMetadata strorageTreeStructureJSON;
	private FileMetadata currentDirectory;
	
	public Map<FileMetadata, List<FileMetadata>> getStorageTreeStructure() {
		return storageTreeStructure;
	}
	public void setStorageTreeStructure(Map<FileMetadata, List<FileMetadata>> storageTreeStructure) {
		this.storageTreeStructure = storageTreeStructure;
	}
	public String getStoragePathPrefix() {
		return storageDirectory.getName() + File.separator + datarootDirectory.getName();
	}
	public FileMetadata getStorageDirectory() {
		return storageDirectory;
	}
	public void setStorageDirectory(FileMetadata storageDrectory) {
		this.storageDirectory = storageDrectory;
	}
	public FileMetadata getDatarootDirectory() {
		return datarootDirectory;
	}
	public void setDatarootDirectory(FileMetadata datarootDirectory) {
		this.datarootDirectory = datarootDirectory;
	}
	
	public FileMetadata getConfigJSON() {
		return configJSON;
	}
	public void setConfigJSON(FileMetadata configJSON) {
		this.configJSON = configJSON;
	}
	public FileMetadata getStrorageTreeStructureJSON() {
		return strorageTreeStructureJSON;
	}
	public void setStrorageTreeStructureJSON(FileMetadata strorageTreeStructureJSON) {
		this.strorageTreeStructureJSON = strorageTreeStructureJSON;
	}
	public FileMetadata getCurrentDirectory() {
		return currentDirectory;
	}
	public void setCurrentDirectory(FileMetadata currentDirectory) {
		this.currentDirectory = currentDirectory;
	}
}
