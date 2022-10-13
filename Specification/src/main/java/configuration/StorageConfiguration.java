package configuration;

import java.util.Map;
import java.util.Set;

public class StorageConfiguration {
	
	private Long storageSize;
	private Set<String> unsupportedFiles;
	private Map<String, Integer> dirNumberOfFilesLimit;
	
	
	public StorageConfiguration() {
		
	}
	
	public StorageConfiguration(Long storageSize, Set<String> unsupportedFiles, Map<String, Integer> dirNumberOfFilesLimit) {
		this.storageSize = storageSize;
		this.unsupportedFiles = unsupportedFiles;
		this.dirNumberOfFilesLimit = dirNumberOfFilesLimit;
	}

	
	public Long getStorageSize() {
		return storageSize;
	}
	public void setStorageSize(Long storageSize) {
		this.storageSize = storageSize;
	}
	
	public Set<String> getUnsupportedFiles() {
		return unsupportedFiles;
	}

	public void setUnsupportedFiles(Set<String> unsupportedFiles) {
		this.unsupportedFiles = unsupportedFiles;
	}

	public Map<String, Integer> getDirNumberOfFilesLimit() {
		return dirNumberOfFilesLimit;
	}

	public void setDirNumberOfFilesLimit(Map<String, Integer> dirNumberOfFilesLimit) {
		this.dirNumberOfFilesLimit = dirNumberOfFilesLimit;
	}
	
}
