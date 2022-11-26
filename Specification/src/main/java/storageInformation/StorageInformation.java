package storageInformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fileMetadata.FileMetadata;

/**
 * Holds all necessery information about the storage
 * 
 * @author Luka Pavlovic
 *
 */

public class StorageInformation {
	
	public static final String datarootDirName = "dataRootDirectory";
	public static final String storageInformationJSONFileName = "storageInformation.json";
	
	private boolean storageConnected = false;
	
	private Map<String, List<FileMetadata>> storageTreeStructure = new HashMap<String, List<FileMetadata>>();
	
	// configuration
	private Long storageSize = 1024L; // 1KB
	private Set<String> unsupportedFiles = new HashSet<>();
	private Map<String, Integer> dirNumberOfFilesLimit = new HashMap<>();
	// ================================================================================

	private FileMetadata storageDirectory;
	private FileMetadata datarootDirectory;
	private FileMetadata downloadFile;
	private FileMetadata configJSONfile;
	private FileMetadata storageInformationJSONfile;
	private FileMetadata currentDirectory;
	
	private String storageDirectoryID;
	private String datarootDirectoryID;
	private String configJSOnID;
	
	
	public boolean isStorageConnected() {
		return storageConnected;
	}
	public void setStorageConnected(boolean storageConnected) {
		this.storageConnected = storageConnected;
	}
	public Map<String, List<FileMetadata>> getStorageTreeStructure() {
		return storageTreeStructure;
	}
	public void setStorageTreeStructure(Map<String, List<FileMetadata>> storageTreeStructure) {
		this.storageTreeStructure = storageTreeStructure;
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
	public FileMetadata getStorageInformationJSONfile() {
		return storageInformationJSONfile;
	}
	// <storage_name>\<dataRootDirectory>
	public String getDataRootPathPrefix() {
		return datarootDirectory.getRelativePath();
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
	public FileMetadata getDownloadFile() {
		return downloadFile;
	}
	public void setDownloadFile(FileMetadata downloadFile) {
		this.downloadFile = downloadFile;
	}
	public FileMetadata getConfigJSONfile() {
		return configJSONfile;
	}
	public void setConfigJSONfile(FileMetadata configJSONfile) {
		this.configJSONfile = configJSONfile;
	}
	public FileMetadata getStorageTreeStructureJSON() {
		return storageInformationJSONfile;
	}
	public void setStorageInformationJSONfile(FileMetadata storageInformationJSONfile) {
		this.storageInformationJSONfile = storageInformationJSONfile;
	}
	public FileMetadata getCurrentDirectory() {
		return currentDirectory;
	}
	public void setCurrentDirectory(FileMetadata currentDirectory) {
		this.currentDirectory = currentDirectory;
	}
	public String getStorageDirectoryID() {
		return storageDirectoryID;
	}
	public void setStorageDirectoryID(String storageDirectoryID) {
		this.storageDirectoryID = storageDirectoryID;
	}
	public String getDatarootDirectoryID() {
		return datarootDirectoryID;
	}
	public void setDatarootDirectoryID(String datarootDirectoryID) {
		this.datarootDirectoryID = datarootDirectoryID;
	}
	public String getConfigJSOnID() {
		return configJSOnID;
	}
	public void setConfigJSOnID(String configJSOnID) {
		this.configJSOnID = configJSOnID;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return "StorageInformation [storageConnected=" + storageConnected + ", "
				+ (storageTreeStructure != null
						? "storageTreeStructure=" + toString(storageTreeStructure.entrySet(), maxLen) + ", "
						: "")
				+ (storageSize != null ? "storageSize=" + storageSize + ", " : "")
				+ (unsupportedFiles != null ? "unsupportedFiles=" + toString(unsupportedFiles, maxLen) + ", " : "")
				+ (dirNumberOfFilesLimit != null
						? "dirNumberOfFilesLimit=" + toString(dirNumberOfFilesLimit.entrySet(), maxLen) + ", "
						: "")
				+ (storageDirectory != null ? "storageDirectory=" + storageDirectory + ", " : "")
				+ (datarootDirectory != null ? "datarootDirectory=" + datarootDirectory + ", " : "")
				+ (downloadFile != null ? "downloadFile=" + downloadFile + ", " : "")
				+ (configJSONfile != null ? "configJSONfile=" + configJSONfile + ", " : "")
				+ (storageInformationJSONfile != null
						? "storageInformationJSONfile=" + storageInformationJSONfile + ", "
						: "")
				+ (currentDirectory != null ? "currentDirectory=" + currentDirectory + ", " : "")
				+ (storageDirectoryID != null ? "storageDirectoryID=" + storageDirectoryID + ", " : "")
				+ (datarootDirectoryID != null ? "datarootDirectoryID=" + datarootDirectoryID + ", " : "")
				+ (configJSOnID != null ? "configJSOnID=" + configJSOnID + ", " : "")
				+ "]";
	}
	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}
}
