package storageInformation;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fileMetadata.FileMetadata;

public class StorageInformation {
	
	public static final String downloadFileName = "download";
	public static final String datarootDirName = "dataRootDirectory";
	public static final String configJSONFileName = "configuration.json";
	public static final String storageInformationJSONFileName = "storageInformation.json";
	
	private Map<FileMetadata, List<FileMetadata>> storageTreeStructure = new HashMap<FileMetadata, List<FileMetadata>>();
	
	private FileMetadata storageDirectory;
	private FileMetadata datarootDirectory;
	private FileMetadata downloadFile;
	private FileMetadata configJSONfile;
	private FileMetadata storageInformationJSONfile;
	private FileMetadata currentDirectory;
	
	private String storageDirectoryID;
	private String datarootDirectoryID;
	private String downloadFileID;
	private String configJSOnID;
	private String storageTreeStructureJSOnID;
	
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
	public String getDownloadFileID() {
		return downloadFileID;
	}
	public void setDownloadFileID(String downloadFileID) {
		this.downloadFileID = downloadFileID;
	}
	public String getConfigJSOnID() {
		return configJSOnID;
	}
	public void setConfigJSOnID(String configJSOnID) {
		this.configJSOnID = configJSOnID;
	}
	public String getStorageTreeStructureJSOnID() {
		return storageTreeStructureJSOnID;
	}
	public void setStorageTreeStructureJSOnID(String storageTreeStructureJSOnID) {
		this.storageTreeStructureJSOnID = storageTreeStructureJSOnID;
	}
	@Override
	public String toString() {
		final int maxLen = 22;
		return "StorageInformation [storageTreeStructure="
				+ (storageTreeStructure != null ? toString(storageTreeStructure.entrySet(), maxLen) : null)
				+ ", storageDirectory=" + storageDirectory + ", datarootDirectory=" + datarootDirectory
				+ ", downloadFile=" + downloadFile + ", configJSONfile=" + configJSONfile
				+ ", storageInformationJSONfile=" + storageInformationJSONfile + ", currentDirectory="
				+ currentDirectory + ", storageDirectoryID=" + storageDirectoryID + ", datarootDirectoryID="
				+ datarootDirectoryID + ", downloadFileID=" + downloadFileID + ", configJSOnID=" + configJSOnID
				+ ", storageTreeStructureJSOnID=" + storageTreeStructureJSOnID + "]";
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
