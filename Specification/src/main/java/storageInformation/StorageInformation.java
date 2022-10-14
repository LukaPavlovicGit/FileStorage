package storageInformation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fileMetadata.FileMetadata;

public class StorageInformation {
	public static final String datarootDirName = "dataRootDirectory";
	public static final String configFileName = "configuration.json";
	private Map<Integer, List<FileMetadata>> StorageTreeStructure = new HashMap<Integer, List<FileMetadata>>();
	private Integer currentTreeDepth = 0;
	private String currentDirName = "";
	private String storageAbsolutePath = "";
	private String storageName =  "";
	
	
	public Map<Integer, List<FileMetadata>> getStorageTreeStructure() {
		return StorageTreeStructure;
	}
	public void setStorageTreeStructure(Map<Integer, List<FileMetadata>> storageTreeStructure) {
		StorageTreeStructure = storageTreeStructure;
	}
	public String getCurrentDirName() {
		return currentDirName;
	}
	public void getCurrentDirName(String getCurrentDirName) {
		this.currentDirName = getCurrentDirName;
	}
	public String getStorageAbsolutePath() {
		return storageAbsolutePath;
	}
	public void setStorageAbsolutePath(String storageAbsolutePath) {
		this.storageAbsolutePath = storageAbsolutePath;
	}
	public Integer getCurrentTreeDepth() {
		return currentTreeDepth;
	}
	public void setCurrentTreeDepth(Integer currentTreeDepth) {
		this.currentTreeDepth = currentTreeDepth;
	}
	public String getStorageName() {
		return storageName;
	}
	public void setStorageName(String storageName) {
		this.storageName = storageName;
	}
	public String getStoragePathPrefix() {
		return storageName + File.separator + datarootDirName;
	}
}
