package fileMetadata;

import java.util.Date;

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
	
	
	public FileMetadata() {
		
	}
	
	public FileMetadata(FileMetadataBuilder builder) {
		this.fileID = builder.fileID;
		this.name = builder.name;
		this.extension = builder.extension;
		this.srcDir = builder.srcDir;
		this.timeCreated = builder.timeCreated;
		this.timeModified = builder.timeModified;
		this.absolutePath = builder.absolutePath;
		this.isFile = builder.isFile;
		this.isDirectory = builder.isDirectory;
		this.isStorage = builder.isStorage;
		this.isConfigFile = builder.isConfigFile;
		this.depthInTreeStructure = builder.depthInTreeStructure;
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
	
	
	// Builder
	public static class FileMetadataBuilder{
		
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
		public FileMetadata build() {
			return new FileMetadata(this);
		}
	}
	
}
