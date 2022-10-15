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
import exception.InvalidArgumentsExcpetion;
import exception.NamingPolicyException;
import exception.NotFound;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import fileMetadata.FileMetadata;
import fileMetadata.FileMetadata.FileMetadataBuilder;
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
	
	public abstract void remove(String name, String src) throws NotFound;
	
	public abstract void rename(String newName, String name, String src) throws NotFound, NamingPolicyException;
	
	public abstract void download(String name, String src, String dest) throws NotFound;
	
	public abstract void saveConfigurationFile(String name);
	
	public abstract void readConfigurationFile(String name);
	
	public void changeDirectory(String path) throws NotFound {
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<Integer, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		Integer currentTreeDepth = storageInformation.getCurrentTreeDepth();
		Path p = Paths.get(path);
		
		if(path.equals("cd..")) {
			if(currentTreeDepth == 0)
				return;
			
			String updatedCurrDirName = null;
			for(FileMetadata f : storageTreeStracture.get(currentTreeDepth)) {
				if(f.getName().equals(storageInformation.getCurrentDirName())) {
					updatedCurrDirName = f.getParentName();
					break;
				}
			}
			
			storageInformation.setCurrentDirName(updatedCurrDirName);
			storageInformation.setCurrentTreeDepth(--currentTreeDepth);
			
			return;
		}
		
		
		Integer startFromDepth = currentTreeDepth;
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		if(path.contains(storagePathPrefix))
			startFromDepth = 0;
		
		int[] depthWrapper = { startFromDepth };
		
		if(checkPath(p.iterator(), depthWrapper, storageTreeStracture)) {
			storageInformation.setCurrentDirName(p.getFileName().toString());
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

		Path path = Paths.get(src);
		StorageManager storageManager = StorageManager.getInstance();
		Map<Integer, List<FileMetadata>> storageTreeStracture = storageManager.getStorageInformation().getStorageTreeStructure();
		String storagePathPrefix = storageManager.getStorageInformation().getStoragePathPrefix();
		
		if(src.contains(storagePathPrefix)) {
			
			int[] depthWrapper = { 0 };
			
			if(!checkPath(path.iterator(), depthWrapper, storageTreeStracture))
				throw new NotFound("Source directory not found!");
		}
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
		List<FileMetadata> list = storageTreeStracture.get(currTreeDepth); // NAPRAVI FUNKCIJU KOJA VRACA FileMetadata NA OSNOVU src
		for(FileMetadata f : list) {
			if(f.getName().equals(path.getFileName().toString())) {
				directory = f;
				break;
			}
		}
		
		Map<Integer, List<FileMetadata>> result = new HashMap<>();
		Integer idx=0;
		Integer startFromDepth = directory.getDepthInTreeStructure();
		int X = storageTreeStracture.keySet().size();
		for(int i=startFromDepth ; i<X ; i++) {
			
			List<FileMetadata> files = storageTreeStracture.get(i);
			List<FileMetadata> tmp = new ArrayList<>();
		
			for(FileMetadata f : files) {
				
				if(onlyDirs && !f.isDirectory())
					continue;
				if(onlyFiles && !f.isFile())
					continue;
				if(extension != null && !f.getName().endsWith(extension))
					continue;
				if(prefix != null && !f.getName().startsWith(prefix))
					continue;
				if(sufix != null && !f.getName().endsWith(sufix))
					continue;
				if(subWord != null && !f.getName().contains(subWord))
					continue;
				
				tmp.add(f);
			}
			
			result.put(idx++, tmp);
			
			if(searchSubDirecories==false)
				break;
		}
		
		return result;
	}
	
	public Map<Integer, List<FileMetadata>> resultSort(Map<Integer, List<FileMetadata>> result, 
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
	
	// atributes inicilalizovati na false ( Arrays.fill(atributes, Boolean.FALSE) )
	
	public Map<Integer,List<FileMetadata>> resultFilter(Map<Integer,List<FileMetadata>> result,
													    boolean[] atributes,
													    Date[][] periods) throws InvalidArgumentsExcpetion{
		
		Date createdTimeLowerBound = null;
		Date createdTimeUpperBound = null;
		if(periods[0][0] != null && periods[0][1] != null) {
			createdTimeLowerBound = periods[0][0];
			createdTimeUpperBound = periods[0][1];
			
			if(createdTimeLowerBound.after(createdTimeUpperBound))
				throw new InvalidArgumentsExcpetion("Invalid arguments! createdTimeLowerBound > endPeriod");
		}
		
		Date modifedTimeLowerBound = null;
		Date modifiedTimeUpperBound = null;
		if(periods[1][0] != null && periods[1][1] != null) {
			modifedTimeLowerBound = periods[1][0];
			modifiedTimeUpperBound = periods[1][1];
			
			if(modifedTimeLowerBound.after(modifiedTimeUpperBound))
				throw new InvalidArgumentsExcpetion("Invalid arguments! modifedTimeLowerBound > modifiedTimeUpperBound");
		}
		// atributes[0] = "fileID"
		// atributes[1] = "name"
		// atributes[2] = "extension"
		// atributes[3] = "parentName"
		// atributes[4] = "absolutePath"
		// atributes[5] = "timeCreated"
		// atributes[6] = "fimeModified"
		// atributes[7] = "isFile"
		// atributes[8] = "isDirectory"
		
		for(int i=0 ; i<result.keySet().size() ; i++) {
			
			List<FileMetadata> filtered = new ArrayList<>();
			
			for(FileMetadata f : result.get(i)) {
				
				if(createdTimeLowerBound != null && createdTimeUpperBound != null) {
					Date time = f.getTimeCreated();
					if(time.before(createdTimeLowerBound) || time.after(createdTimeUpperBound))
						continue;
				}
				if(modifedTimeLowerBound != null && modifiedTimeUpperBound != null) {
					Date time = f.getTimeModified();
					if(time.before(modifedTimeLowerBound) || time.after(modifiedTimeUpperBound))
						continue;
				}
				
				FileMetadataBuilder builder = new FileMetadataBuilder();
				
				if(atributes[0])
					builder.withFileID(f.getFileID());
				if(atributes[1])
					builder.withName(f.getName());
				if(atributes[2])
					builder.withExtension(f.getExtension());
				if(atributes[3])
					builder.withParentName(f.getParentName());
				if(atributes[4])
					builder.withAbsolutePath(f.getAbsolutePath());
				if(atributes[5])
					builder.withTimeCreated(f.getTimeCreated());
				if(atributes[6])
					builder.withTimeModified(f.getTimeModified());
				if(atributes[7])
					builder.withIsFile(f.isFile());
				if(atributes[8])
					builder.withIsDirectory(f.isDirectory());
				
				
				filtered.add(builder.build());
			}
			
			result.put(i, filtered);
		}
		
		return result;
	}
	
	public boolean addFileMetadataToStorage(String dst, FileMetadata fileMetadata) {
		
		Path path = Paths.get(dst);
		Map<Integer, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		Integer startFromDepth = StorageManager.getInstance().getStorageInformation().getCurrentTreeDepth();
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		
		if(dst.contains(storagePathPrefix))
			startFromDepth = 0;
		
		int[] depthWrapper = { startFromDepth };
		
		if(!checkPath(path.iterator(), depthWrapper, storageTreeStracture))
			return false;
		
		Integer depth = depthWrapper[0] - 1;
		storageTreeStracture.get(depth).add(fileMetadata);
		
		return true;
	}
	
	public boolean removeFileMetadataFromStorage(String name, String src) {
		
		return false;
	}
	
	public boolean moveFileMetada(String name, String src, String newDest) {
		
		return false;
	}
	
	public boolean renameFileMetadata(String newName, String name, String src) {
		
		return false;
	}
	
	
	private boolean checkPath(Iterator<Path> iterator, int[] treeDepth, final Map<Integer, List<FileMetadata>> storageTreeStracture) {
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
