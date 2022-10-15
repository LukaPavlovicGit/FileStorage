package fileMetadata;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FileMetadata {
	
	private String fileID;
	private String name;
	private String extension;
	private String srcDir;
	private String absolutePath;
	private Date timeCreated;
	private Date timeModified;
	private boolean isFile;
	private boolean isDirectory;
	private boolean isStorage;
	private boolean isConfigFile;
	private Integer depthInTreeStructure;
	
	//if directory
	private Integer numOfFilesLimit;
	
	// if storage
	private Long storageSize;
	private Set<String> unsupportedFiles = new HashSet<>();
	
	
	private FileMetadata() {
		
	}
	
	public FileMetadata(FileMetadataBuilder builder) {
		this.fileID = builder.fileID;
		this.name = builder.name;
		this.extension = builder.extension;
		this.srcDir = builder.srcDir;
		this.timeCreated = new Date(System.currentTimeMillis());;
		this.timeModified =new Date(System.currentTimeMillis());;
		this.absolutePath = builder.absolutePath;
		this.isFile = builder.isFile;
		this.isDirectory = builder.isDirectory;
		this.isStorage = builder.isStorage;
		this.isConfigFile = builder.isConfigFile;
		this.depthInTreeStructure = builder.depthInTreeStructure;
		
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
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getSrcDir() {
		return srcDir;
	}
	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
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

	public boolean isConfigFile() {
		return isConfigFile;
	}

	public void setConfigFile(boolean isConfigFile) {
		this.isConfigFile = isConfigFile;
	}

	public Integer getDepthInTreeStructure() {
		return depthInTreeStructure;
	}

	public void setDepthInTreeStructure(Integer depthInTreeStructure) {
		this.depthInTreeStructure = depthInTreeStructure;
	}
	
	public FileMetadata clone() {
		FileMetadata file = new FileMetadata();
		
		return file;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }
        
        final FileMetadata other = (FileMetadata) obj;
        if(this.name.endsWith(other.name) && this.srcDir.equals(other.srcDir))
        	return true;
		
		return false;
	}
	
	


	@Override
	public String toString() {
		final int maxLen = 18;
		return "FileMetadata [" + (fileID != null ? "fileID=" + fileID + ", " : "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (extension != null ? "extension=" + extension + ", " : "")
				+ (srcDir != null ? "srcDir=" + srcDir + ", " : "")
				+ (absolutePath != null ? "absolutePath=" + absolutePath + ", " : "")
				+ (timeCreated != null ? "timeCreated=" + timeCreated + ", " : "")
				+ (timeModified != null ? "timeModified=" + timeModified + ", " : "") + "isFile=" + isFile
				+ ", isDirectory=" + isDirectory + ", isStorage=" + isStorage + ", isConfigFile=" + isConfigFile + ", "
				+ (depthInTreeStructure != null ? "depthInTreeStructure=" + depthInTreeStructure + ", " : "")
				+ (numOfFilesLimit != null ? "numOfFilesLimit=" + numOfFilesLimit + ", " : "")
				+ (storageSize != null ? "storageSize=" + storageSize + ", " : "")
				+ (unsupportedFiles != null ? "unsupportedFiles=" + toString(unsupportedFiles, maxLen) : "") + "]";
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
		private String extension;
		private String srcDir;
		private String absolutePath;
		//private Date timeCreated;
		//private Date timeModified;
		private boolean isFile;
		private boolean isDirectory;
		private boolean isStorage;
		private boolean isConfigFile;
		private Integer depthInTreeStructure;
		
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
		public FileMetadataBuilder withExtension(String extension) {
			this.extension = extension;
			return this;
		}
		public FileMetadataBuilder withSrcDir(String srcDir) {
			this.srcDir = srcDir;
			return this;
		}
		/*public FileMetadataBuilder withTimeCreated(Date timeCreated) {
			this.timeCreated = timeCreated;
			return this;
		}
		public FileMetadataBuilder withTimeModified(Date timeModified) {
			this.timeModified = timeModified;
			return this;
		}*/
		public FileMetadataBuilder withAbsolutePath(String absolutePath) {
			this.absolutePath = absolutePath;
			return this;
		}
		public FileMetadataBuilder withIsFile(boolean isFile) {
			this.isFile = isFile;
			return this;
		}
		public FileMetadataBuilder withIsDirectory(boolean isDirectory) {
			this.isDirectory = isDirectory;
			return this;
		}
		public FileMetadataBuilder withIsStorage(boolean isStorage) {
			this.isStorage = isStorage;
			return this;
		}
		public FileMetadataBuilder withIsConfigFile(boolean isConfigFile) {
			this.isConfigFile = isConfigFile;
			return this;
		}
		public FileMetadataBuilder withDepthInTreeStructure(Integer depthInTreeStrucure) {
			this.depthInTreeStructure = depthInTreeStrucure;
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
