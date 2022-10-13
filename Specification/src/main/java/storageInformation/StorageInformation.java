package storageInformation;

import java.io.File;

public class StorageInformation {
	public static final String datarootDirName = File.separator + "dataRootDirectory";
	public static final String configFileName = File.separator + "configuration.json";
	private String currentPath = "";
	private String storageAbsolutePath = "";
	private Integer currentTreeDepth;
	
	
	public String getCurrentPath() {
		return currentPath;
	}
	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
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
	

}
