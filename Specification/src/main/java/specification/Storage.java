package specification;
import java.util.Date;
import java.util.List;
import java.util.Map;

import configuration.StorageConfiguration;
import exception.DirectoryException;
import exception.NamingPolicyException;
import exception.NotFound;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import fileMetadata.FileMetadata;

public abstract class Storage {
	
	public abstract void createStorage(String dest, StorageConfiguration storageConfiguration);
	
	public abstract boolean createDirectory(String dest, String name, Integer... filesLimit) throws StorageSizeException, NamingPolicyException, DirectoryException;
	
	public void createDirectories(String dest, Map<String, Integer> dirNameAndFilesLimit) {
		try {
			
			for(String name : dirNameAndFilesLimit.keySet()) {
				Integer filesLimit = dirNameAndFilesLimit.get(name);
				createDirectory(dest, name, filesLimit);		
			}
			
		} catch (StorageSizeException | DirectoryException | NamingPolicyException e) {
			e.printStackTrace();
		}
	}
	
	public abstract boolean createFile(String dest, String name) throws StorageSizeException, NamingPolicyException, UnsupportedFileException;
	
	public void createFiles(String dest, List<String> names) {
		try {
		
			for(String name : names) {
				createFile(dest, name);
			}
		
		} catch (StorageSizeException | NamingPolicyException | UnsupportedFileException e) {	
			e.printStackTrace();
		}
		
	}
	
	public abstract void move(String name, String src, String newDest) throws NotFound;
	
	public abstract void delete(String name, String src) throws NotFound;
	
	public abstract void download(String name, String src, String dest) throws NotFound;
	
	public abstract void rename(String newName, String name, String src) throws NotFound, NamingPolicyException;
	
	public abstract void saveConfigurationFile(String name);
	
	public abstract void readConfigurationFile(String name);
	
	public void changeDirectory(String path) throws NotFound {
		
	}
	
	
	public List<FileMetadata> listDirectory(String src, 
											boolean onlyDirs, 
											boolean onlyFiles,
											boolean searchSubDirecories, 
											String extension,
											String prefix,
											String sufix,
											String subWord) throws NotFound {
		
		return null;
	}
	
	public boolean find(String src, String name) {
		
		return false;
	}
	
	public Map<String, Boolean> find(String src, List<String> names){
		
		return null;
	}
	
	public List<String> findLocation(String name) throws NotFound {
		
		return null;
	}
	
	public List<FileMetadata> resultSort(List<FileMetadata> result, 
										 boolean byName,
										 boolean byCreationDate,
										 boolean byModificationDate,
										 Date startPeriod,
										 Date endPeriod) {
		
		return null;
	}
	
	public List<FileMetadata> resultFilter(List<FileMetadata> result,
										   boolean includeName,
										   boolean includeAbsolutePath,
										   boolean invludeSize, 
										   boolean includeCreatinDate,
										   boolean includeModificationDate) {
		
		return null;
	}
}
