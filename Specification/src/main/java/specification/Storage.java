package specification;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configuration.StorageConfiguration;
import exception.DirectoryException;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import exception.storageConfigurationException.StorageConfigurationException;
import fileMetadata.FileMetadata;
import storageInformation.StorageInformation;

public abstract class Storage {
	/* Skladiste je predstavljeno kao stablo. U svakom nivou stabla imamo foldere i fajlove sa jedinstvenim nazivima,
	 ukoliko je u pitanju folder onda ce imati listu foldera i fajlova koje sadrzi, u suprotnom lista ce biti null; */ 
	protected Map<Integer, Map<String, List<FileMetadata>>> StorageTreeStructure = new HashMap<Integer, Map<String, List<FileMetadata>>>();
	protected StorageConfiguration storageConfiguration = new StorageConfiguration();
	protected StorageInformation storageInformation = new StorageInformation();
	

	public abstract void createStorage(String dest, StorageConfiguration storageConfiguration);
	
	public abstract boolean createDirectory(String dest, String name, Integer... filesLimit) throws StorageSizeException, DirectoryException;
	
	public void createDirectories(String dest, Map<String, Integer> dirNameAndFilesLimit) throws StorageConfigurationException {
		for(String name : dirNameAndFilesLimit.keySet()) {
			
			Integer filesLimit = dirNameAndFilesLimit.get(name);
			
			if(!createDirectory(dest, name, filesLimit))
				throw new StorageConfigurationException("Storage Configuration Exception!");		
		}
	}
	
	public abstract boolean createFile(String dest, String name) throws UnsupportedFileException;
	
	public void createFiles(String dest, List<String> names) throws UnsupportedFileException{
		for(String name : names) {
			
			if(!createFile(dest, name))
				throw new UnsupportedFileException("Unsupported File Exception!");
		}
	}
	
	public abstract void move(String name, String src, String newDest);
	
	public abstract void delete(String name, String src);
	
	public abstract void download(String name, String src, String dest);
	
	public abstract void rename(String newName, String name, String src);
	
	public List<FileMetadata> listDirectory(String src, 
											boolean onlyDirs, 
											boolean onlyFiles,
											boolean searchSubDirecories, 
											String extension,
											String prefix,
											String sufix,
											String subWord) throws FileNotFoundException {
		
		return null;
	}
	
	public boolean find(String src, String name) {
		
		return false;
	}
	
	public Map<String, Boolean> find(String src, List<String> names){
		
		return null;
	}
	
	public List<String> findLocation(String name){
		
		return null;
	}
	
	public List<FileMetadata> resultSort(List<FileMetadata> result, 
										 boolean byName,
										 boolean byCreationDate,
										 boolean byModificationDate,
										 Date startPeriod,
										 Date endPeriod ){
		
		return null;
	}
	
	public List<FileMetadata> resultFilter(List<FileMetadata> result,
										   boolean includeName,
										   boolean includeAbsolutePath,
										   boolean invludeSize, 
										   boolean includeCreatinDate,
										   boolean includeModificationDate ){
		
		return null;
	}
}
