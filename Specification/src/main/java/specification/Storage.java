package specification;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import configuration.StorageConfiguration;
import exception.DirectoryException;
import exception.NamingPolicyException;
import exception.NotFound;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import fileMetadata.FileMetadata;
import storageInformation.StorageInformation;
import storageManager.StorageManager;

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
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<Integer, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		Integer currentTreeDepth = storageInformation.getCurrentTreeDepth();
		
		if(path.equals("cd..")) {
			if(currentTreeDepth == 0)
				return;
			storageInformation.setCurrentTreeDepth(--currentTreeDepth);
			return;
		}
		
		Path p = Paths.get(path);
		Integer startFromDepth = currentTreeDepth;
		String prefix = storageInformation.getStorageName() + File.separator + StorageInformation.datarootDirName;
		
		if(path.contains(prefix))
			startFromDepth = 0;
		
		int[] depthWrapper = { startFromDepth };
		
		if(checkPath(p.iterator(), depthWrapper, storageTreeStracture)) {
			storageInformation.getCurrentDirName(p.getFileName().toString());
			storageInformation.setCurrentTreeDepth(depthWrapper[0] - 1);
		}
		else 
			throw new NotFound("Location does not exist!");
	}
	
	public boolean find(String src, String name) {
		
		return false;
	}
	
	public Map<String, Boolean> find(String src, List<String> names){
		
		return null;
	}
	
	public List<String> findDestinantion(String name) throws NotFound {
		
		return null;
	}
	
	public Map<Integer,List<FileMetadata>> listDirectory(String src, 
														 boolean onlyDirs, 
														 boolean onlyFiles,
														 boolean searchSubDirecories, 
														 String extension,
														 String prefix,
														 String sufix,
														 String subWord) throws NotFound {

		StorageManager storageManager = StorageManager.getInstance();
		Path path = Paths.get(src);
		Map<Integer, List<FileMetadata>> storageTreeStracture = storageManager.getStorageInformation().getStorageTreeStructure();
		Integer startFromDepth = storageManager.getStorageInformation().getCurrentTreeDepth();
		String storagePathPrefix = storageManager.getStorageInformation().getStoragePathPrefix();
		
		if(src.contains(storagePathPrefix))
			startFromDepth = 0;
		
		int[] depthWrapper = { startFromDepth };
		
		if(!checkPath(path.iterator(), depthWrapper, storageTreeStracture))
			throw new NotFound("Source directory not found!");
		
		// fix parameters
		if(onlyDirs==true)
			onlyFiles = false;
		if(prefix.length()>0) {
			sufix = null;
			subWord = null;
		}
		if(sufix.length()>0)
			subWord = null;
		
		// nalazimo direktorijum koji se pretrazuje
		FileMetadata directory = null;
		Integer currTreeDepth = storageManager.getStorageInformation().getCurrentTreeDepth();
		List<FileMetadata> list =storageTreeStracture.get(currTreeDepth);
		for(FileMetadata f : list) {
			if(f.getName().equals(path.getFileName().toString())) {
				directory = f;
				break;
			}
		}
		
		Map<Integer, List<FileMetadata>> result = new HashMap<>();
		Integer idx=0;
		startFromDepth = directory.getDepthInTreeStructure();
		int X = storageTreeStracture.keySet().size();
		for(int i=startFromDepth ; i<X ; i++) {
			
			List<FileMetadata> files = storageTreeStracture.get(i);
			List<FileMetadata> tmp = new ArrayList<>();
		
			for(FileMetadata f : files) {
				
				if(onlyDirs==true && f.isDirectory()==false)
					continue;
				if(onlyFiles==true && f.isFile()==false)
					continue;
				if(extension!=null && f.getName().endsWith(extension)==false)
					continue;
				if(prefix!=null && f.getName().startsWith(prefix)==false)
					continue;
				if(sufix!=null && f.getName().endsWith(sufix)==false)
					continue;
				if(subWord!=null && f.getName().contains(subWord)==false)
					continue;
				
				tmp.add(f);
			}
			
			result.put(idx++, tmp);
			
			if(searchSubDirecories==false)
				break;
		}
		
		return result;
	}
	
	public Map<Integer, List<FileMetadata>> resultSort(Map<Integer,List<FileMetadata>> result, 
													   boolean byName,
													   boolean byCreationDate,
													   boolean byModificationDate,
													   boolean ascending,
													   boolean descending) {
		
		Comparator<FileMetadata> comparator = Comparator.comparing(FileMetadata::getName);
		
		if(byName && byCreationDate && byModificationDate)
			comparator.thenComparing(FileMetadata::getTimeCreated).thenComparing(FileMetadata::getTimeModified);
		
		else if(!byName && byCreationDate && byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getTimeCreated).thenComparing(FileMetadata::getTimeModified);
		
		else if(byName && !byCreationDate && byModificationDate)
			comparator.thenComparing(FileMetadata::getTimeModified);
		
		else if(byName && byCreationDate && !byModificationDate)
			comparator.thenComparing(FileMetadata::getTimeCreated);
		
		else if(byName && !byCreationDate && !byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getName);
		
		else if(!byName && byCreationDate && !byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getTimeCreated);
		
		else if(!byName && !byCreationDate && byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getTimeModified);
		
		
		for(int i=0 ; i<result.keySet().size() ; i++) {
			List<FileMetadata> list = result.get(i);
			if(ascending)
				result.put(i, list.stream().sorted(comparator).collect(Collectors.toList()));
			else {
				list = list.stream().sorted(comparator).collect(Collectors.toList());
				Collections.reverse(list);
				result.put(i, list);
			}
		}
		
		
		return result;
	}
	
	public List<FileMetadata> resultFilter(List<FileMetadata> result,
										   boolean includeName,
										   boolean includeAbsolutePath,
										   boolean invludeSize, 
										   boolean includeCreatinDate,
										   boolean includeModificationDate,
										   Date startPeriod,
										   Date endPeriod) {
		
		return null;
	}
	
	
	private boolean checkPath(Iterator<Path> iterator, int[] treeDepth, Map<Integer, List<FileMetadata>> storageTreeStracture) {
		if(!iterator.hasNext())
			return true;
		
		Path path = iterator.next();
		List<FileMetadata> list = storageTreeStracture.get(treeDepth[0]);
		
		boolean flag = false;
		for(FileMetadata fileMetadata : list) {
			if(fileMetadata.getName().equals(path.toString())) {
				flag = true;
				break;
			}
		}
		++treeDepth[0];
		return (flag == true) ? checkPath(iterator, treeDepth, storageTreeStracture) : false;
	}
}
