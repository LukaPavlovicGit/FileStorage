package fileMetadata;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FileMetadata {
	
	private String fileID;
	private String name;
	private String absolutePath;
	private String relativePath;
	private Long size = 0L;
	private FileMetadata parent;
	private Date timeCreated;
	private Date timeModified;
	private Boolean isFile = false;
	private Boolean isDirectory = false;
	private Boolean isStorage = false;
	private Boolean isDownloadFile = false;
	private Boolean isDataRoot = false;
	private Boolean isConfigJSONFile = false;
	private Boolean isStrorageTreeStructureJSONFile = false;
	
	//if directory
	private Integer numOfFilesLimit;
	
	// if storage
	private Long storageSize;
	private Set<String> unsupportedFiles = new HashSet<>();
	
	
	public FileMetadata() {
		
	}
	
	public FileMetadata(FileMetadataBuilder builder) {
		this.fileID = builder.fileID;
		this.name = builder.name;
		this.timeCreated = builder.timeCreated;
		this.timeModified = builder.timeModified;
		this.absolutePath = builder.absolutePath;
		this.relativePath = builder.relativePath;
		this.parent = builder.parent;
		this.isFile = builder.isFile;
		this.isDirectory = builder.isDirectory;
		this.isStorage = builder.isStorage;
		this.isDownloadFile = builder.isDownloadFile;
		this.isDataRoot = builder.isDataRoot;
		this.isConfigJSONFile = builder.isConfigJSONFile;
		this.isStrorageTreeStructureJSONFile = builder.isStrorageTreeStructureJSONFile;
		
		this.numOfFilesLimit = builder.numOfFilesLimit;
		
		this.storageSize = builder.storageSize;
		this.unsupportedFiles = builder.unsupportedFiles;
	}
	
	
	
	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getTimeCreated() {
		return timeCreated;
	}
	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}
	public Date getTimeModified() {
		return timeModified;
	}
	public void setTimeModified(Date timeModified) {
		this.timeModified = timeModified;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	
	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public FileMetadata getParent() {
		return parent;
	}

	public void setParent(FileMetadata parent) {
		this.parent = parent;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public boolean isStorage() {
		return isStorage;
	}

	public void setStorage(boolean isStorage) {
		this.isStorage = isStorage;
	}
	
	public boolean isDownloadFile() {
		return isDownloadFile;
	}

	public void setDownloadFile(boolean isDownloadFile) {
		this.isDownloadFile = isDownloadFile;
	}

	public boolean isDataRoot() {
		return isDataRoot;
	}

	public void setDataRoot(boolean isDataRoot) {
		this.isDataRoot = isDataRoot;
	}

	public boolean isStrorageTreeStructureJSONFile() {
		return isStrorageTreeStructureJSONFile;
	}

	public void setStrorageTreeStructureJSONFile(boolean isStrorageTreeStructureJSONFile) {
		this.isStrorageTreeStructureJSONFile = isStrorageTreeStructureJSONFile;
	}

	public Integer getNumOfFilesLimit() {
		return numOfFilesLimit;
	}

	public void setNumOfFilesLimit(Integer numOfFilesLimit) {
		this.numOfFilesLimit = numOfFilesLimit;
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

	public boolean isConfigJSONFile() {
		return isConfigJSONFile;
	}

	public boolean isConfigFile() {
		return isConfigJSONFile;
	}

	public void setConfigJSONFile(boolean isConfigJSONFile) {
		this.isConfigJSONFile = isConfigJSONFile;
	}
	

	public FileMetadata clone() {
		
		FileMetadata file = new FileMetadata();
		
		file.fileID = this.fileID;
		file.name = this.name;
		file.absolutePath = this.absolutePath;
		file.relativePath = this.relativePath;
		file.size = this.size;
		file.parent = this.parent;
		file.timeCreated = this.timeCreated;
		file.timeModified = this.timeModified;
		file.isFile = this.isFile;
		file.isDirectory = this.isDirectory;
		file.numOfFilesLimit = this.numOfFilesLimit;
		
		return file;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return (fileID != null ? "fileID=" + fileID + ", " : "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (absolutePath != null ? "absolutePath=" + absolutePath + ", " : "")
				+ (relativePath != null ? "relativePath=" + relativePath + ", " : "")
				+ (size != null && size!=0L ? "size=" + size + ", " : "") + (parent != null ? "parent=" + parent + ", " : "")
				+ (timeCreated != null ? "timeCreated=" + timeCreated + ", " : "")
				+ (timeModified != null ? "timeModified=" + timeModified + ", " : "")
				+ (isFile != null ? "isFile=" + isFile + ", " : "")
				+ (isDirectory != null ? "isDirectory=" + isDirectory + ", " : "")
				+ (numOfFilesLimit != null ? "numOfFilesLimit=" + numOfFilesLimit + ", " : "")
				+ (storageSize != null ? "storageSize=" + storageSize + ", " : "")
				+ (unsupportedFiles != null && !unsupportedFiles.isEmpty() ? "unsupportedFiles=" + toString(unsupportedFiles, maxLen) : "") ;
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

	// Builder
	public static class FileMetadataBuilder{
		
		private String fileID;
		private String name;
		private String absolutePath;
		private String relativePath;
		private FileMetadata parent;
		private Date timeCreated;
		private Date timeModified;
		private Boolean isFile;
		private Boolean isDirectory;
		private Boolean isStorage;
		private Boolean isDownloadFile;
		private Boolean isDataRoot;
		private Boolean isConfigJSONFile;
		private Boolean isStrorageTreeStructureJSONFile;
		
		//if directory
		private Integer numOfFilesLimit;
		
		// if storage
		private Long storageSize;
		private Set<String> unsupportedFiles = new HashSet<>();
		
		public FileMetadataBuilder withFileID(String ID) {
			this.fileID = ID;
			return this;
		}
		public FileMetadataBuilder withName(String name) {
			this.name = name;
			return this;
		}
		public FileMetadataBuilder withTimeCreated(Date timeCreated) {
			this.timeCreated = timeCreated;
			return this;
		}
		public FileMetadataBuilder withTimeModified(Date timeModified) {
			this.timeModified = timeModified;
			return this;
		}
		public FileMetadataBuilder withAbsolutePath(String absolutePath) {
			this.absolutePath = absolutePath;
			return this;
		}
		public FileMetadataBuilder withRelativePath(String relativePath) {
			this.relativePath = relativePath;
			return this;
		}
		public FileMetadataBuilder withParent(FileMetadata parent) {
			this.parent = parent;
			return this;
		}
		public FileMetadataBuilder withIsFile(Boolean isFile) {
			this.isFile = isFile;
			return this;
		}
		public FileMetadataBuilder withIsDirectory(Boolean isDirectory) {
			this.isDirectory = isDirectory;
			return this;
		}
		public FileMetadataBuilder withIsStorage(Boolean isStorage) {
			this.isStorage = isStorage;
			return this;
		}
		public FileMetadataBuilder withIsDownloadFile(Boolean isDownloadFile) {
			this.isDownloadFile = isDownloadFile;
			return this;
		}
		public FileMetadataBuilder withIsDataRoot(Boolean isDataRoot) {
			this.isDataRoot = isDataRoot; 
			return this;
		}
		public FileMetadataBuilder withIsConfigJSONFile(Boolean isConfigJSONFile) {
			this.isConfigJSONFile = isConfigJSONFile;
			return this;
		}
		public FileMetadataBuilder withIsStrorageTreeStructureJSONFile(Boolean isStrorageTreeStructureJSONFile) {
			this.isStrorageTreeStructureJSONFile = isStrorageTreeStructureJSONFile;
			return this;
		}
		public FileMetadataBuilder withNumOfFilesLimit(Integer num) {
			this.numOfFilesLimit = num;
			return this;
		}
		public FileMetadataBuilder withUnsupportedFiles(Set<String> unsupportedFiles) {
			this.unsupportedFiles = unsupportedFiles;
			return this;
		}
		public FileMetadataBuilder withStorageSize(Long size) {
			this.storageSize = size;
			return this;
		}
		
		public FileMetadata build() {
			return new FileMetadata(this);
		}
	}
	
}
