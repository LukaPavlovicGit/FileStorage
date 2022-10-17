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
import java.util.PriorityQueue;
import java.util.Queue;
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
	
	// za konfuguraciju i strorageTreeStructure
	public abstract void saveToJSON(String path);
	
	// za konfuguraciju i strorageTreeStructure
	public abstract void readFromJSON(String path);
	
	public void changeDirectory(String path) throws NotFound {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		FileMetadata currentDirectory = storageInformation.getCurrentDirectory();
		Path p = Paths.get(path);
		
		if(path.equals("cd..")) {
			if(currentDirectory.getDepthInTreeStructure() == 0)
				return;
			
			FileMetadata parent = storageInformation.getCurrentDirectory().getParent();
			storageInformation.setCurrentDirectory(parent);
			return;
		}
		
		FileMetadata desiredDirectory = null;
		FileMetadata startFromDirectory = currentDirectory;
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		if(path.startsWith(storagePathPrefix))
			startFromDirectory = storageInformation.getStorageDirectory();
		
		desiredDirectory = getLastDirectoryOnPath(p.iterator(), startFromDirectory, storageTreeStracture);
		if(desiredDirectory != null) 
			storageInformation.setCurrentDirectory(desiredDirectory);
		else 
			throw new NotFound("Location does not exist!");
	}
	
	public boolean find(String filePath) {
		
		Path path = Paths.get(filePath);
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		FileMetadata currentDirectory = storageInformation.getCurrentDirectory();
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		FileMetadata startFromDirectory = currentDirectory;
		if(filePath.startsWith(storagePathPrefix))
			startFromDirectory =  storageInformation.getStorageDirectory();

		return checkPath(path.iterator(), startFromDirectory, storageTreeStracture);
	}
	
	public Map<String, Boolean> find(List<String> filePaths){
		
		Map<String, Boolean> result = new HashMap<>();
		for(String path : filePaths) 
			result.put(path, find(path));
		
		return result;
	}
	
	public List<String> findDestinantions(String name) {
		
		List<String> result = new ArrayList<>();
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		
		for(FileMetadata dir : storageTreeStracture.keySet()) {
			
			for(FileMetadata f : storageTreeStracture.get(dir)) {
				
				Path path = Paths.get(f.getAbsolutePath());
				if(path.getFileName().toString().equals(name))
					result.add(f.getAbsolutePath());
					
			}
		}
		
		return result;
	}
	
	public Map<FileMetadata, List<FileMetadata>> listDirectory(String src, 
															   boolean onlyDirs, 
															   boolean onlyFiles,
															   boolean searchSubDirecories, 
														       String extension,
															   String prefix,
															   String sufix,
															   String subWord) throws NotFound {

		Path path = Paths.get(src);
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		FileMetadata currentDirectory = storageInformation.getCurrentDirectory();
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		FileMetadata startFromDirectory = currentDirectory;
		if(src.startsWith(storagePathPrefix))
			startFromDirectory =  storageInformation.getStorageDirectory();

		FileMetadata directory = getLastDirectoryOnPath(path.iterator(), startFromDirectory, storageTreeStracture);
		if(directory == null)
			throw new NotFound("Source directory not found!");
		
		// fix parameters
		if(onlyDirs)
			onlyFiles = false;
		if(prefix.length()>0) {
			sufix = null;
			subWord = null;
		}
		if(sufix.length()>0)
			subWord = null;
		
		
		Map<FileMetadata, List<FileMetadata>> result = new HashMap<>();
		Queue<FileMetadata> pq = new PriorityQueue<>();
		pq.add(directory);
		
		// BFS
		while(!pq.isEmpty()) {
			
			FileMetadata dir = pq.poll();
			result.put(dir, storageTreeStracture.get(dir));
			
			if(searchSubDirecories==false)
				break;
			
			for(FileMetadata f : storageTreeStracture.get(dir)) {
				if(f.isDirectory())
					pq.add(f);
			}
		}
		
		if(onlyDirs || onlyFiles || (extension != null) || (prefix != null) || (sufix != null) || (subWord != null) ) {
			
			for(FileMetadata dir : result.keySet()) {
				
				List<FileMetadata> tmp = new ArrayList<>();
				
				for(FileMetadata f : result.get(dir)) {
					
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
				
				result.put(dir, tmp);
				
				if(searchSubDirecories==false)
					break;
			}
	
		}
		
		return result;
	}
	
	
	public Map<FileMetadata, List<FileMetadata>> resultSort(Map<FileMetadata, List<FileMetadata>> result, 
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
		
		
		for(FileMetadata dir : result.keySet()) {
			List<FileMetadata> list = result.get(dir);
			if(descending) {
				list = list.stream().sorted(comparator).collect(Collectors.toList());
				Collections.reverse(list);
				result.put(dir, list);
			}
			else 
				result.put(dir, list.stream().sorted(comparator).collect(Collectors.toList()));
		}
		
		return result;
	}
	
	// atributes inicilalizovati na false ( Arrays.fill(atributes, Boolean.FALSE) )
	
	public Map<FileMetadata, List<FileMetadata>> resultFilter(Map<FileMetadata, List<FileMetadata>> result,
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
		
		for(FileMetadata dir : result.keySet()) {
			
			List<FileMetadata> filtered = new ArrayList<>();
			
			for(FileMetadata f : result.get(dir)) {
				
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
			
			result.put(dir, filtered);
		}
		
		return result;
	}
	
	public boolean createStorage(String dest, FileMetadata storage) {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		
		storage.setName(StorageInformation.storageName);
		storage.setAbsolutePath(dest + File.separator + StorageInformation.storageName);
		storageTreeStracture.put(storage, new ArrayList<FileMetadata>());
		
		storageInformation.setCurrentDirectory(storage);
		
		// TO BE CONTINUE...
		
		return true;
	}
	
	public boolean addFileMetadataToStorage(String dst, FileMetadata fileMetadata) throws NotFound {
		
		Path path = Paths.get(dst);
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		FileMetadata currentDirectory = storageInformation.getCurrentDirectory();
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		FileMetadata startFromDirectory = currentDirectory;
		if(dst.startsWith(storagePathPrefix))
			startFromDirectory =  storageInformation.getStorageDirectory();
		
		FileMetadata parent = getLastDirectoryOnPath(path.iterator(), startFromDirectory, storageTreeStracture);
		if(parent == null)
			throw new NotFound("Path is not correct!");
		
		fileMetadata.setAbsolutePath(parent.getAbsolutePath() + File.separator + fileMetadata.getName());
		storageTreeStracture.get(parent).add(fileMetadata);
		
		return true;
	}
	
	public boolean removeFileMetadataFromStorage(String filePath) throws NotFound {
		
		Path path = Paths.get(filePath);
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		FileMetadata currentDirectory = storageInformation.getCurrentDirectory();
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		FileMetadata startFromDirectory = currentDirectory;
		if(filePath.startsWith(storagePathPrefix))
			startFromDirectory =  storageInformation.getStorageDirectory();
			
		FileMetadata file = getLastDirectoryOnPath(path.iterator(), startFromDirectory, storageTreeStracture);
		if(file == null)
			throw new NotFound("Path does not exist!");
		
		FileMetadata parent = file.getParent();
		storageTreeStracture.get(parent).remove(file);

		return true;
	}
	
	public boolean moveFileMetadata(String filePath, String newDest) throws NotFound {
		
		Path path1 = Paths.get(filePath);
		Path path2 = Paths.get(newDest);
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		
		FileMetadata startFromDirectory1 = StorageManager.getInstance().getStorageInformation().getCurrentDirectory(); // za filePath
		FileMetadata startFromDirectory2 = StorageManager.getInstance().getStorageInformation().getCurrentDirectory(); // za newDest
		
		if(filePath.startsWith(storagePathPrefix))
			startFromDirectory1 = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		if(newDest.startsWith(storagePathPrefix))
			startFromDirectory2 = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		
		FileMetadata file = getLastDirectoryOnPath(path1.iterator(), startFromDirectory1, storageTreeStracture);
		FileMetadata dest = getLastDirectoryOnPath(path2.iterator(), startFromDirectory2, storageTreeStracture);
		
		if(file == null)
			throw new NotFound("File path not correct!");
		if(dest == null)
			throw new NotFound("Destination path not correct!");
			
		storageTreeStracture.get(file.getParent()).remove(file);
		storageTreeStracture.get(dest).add(file);
		
		return true;
	}
	
	
	public boolean renameFileMetadata(String filePath, String newName) throws NotFound{
		
		Path path = Paths.get(filePath);
		Map<FileMetadata, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		
		FileMetadata startFromDirectory = StorageManager.getInstance().getStorageInformation().getCurrentDirectory();
		
		if(filePath.contains(storagePathPrefix))
			startFromDirectory = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		
		FileMetadata file = getLastDirectoryOnPath(path.iterator(), startFromDirectory, storageTreeStracture);
		if(file == null)
			throw new NotFound("File path not correct!");
		
		file.setName(newName);
		
		return true;
	}
	
	private FileMetadata getLastDirectoryOnPath(Iterator<Path> iterator, FileMetadata directory, final Map<FileMetadata, List<FileMetadata>> storageTreeStracture) {
		
		if(!iterator.hasNext())
			return directory;
		
		Path path = iterator.next();
		List<FileMetadata> list = storageTreeStracture.get(directory);
		
		for(FileMetadata f : list) {
			if(f.getName().equals(path.toString())) {
				return getLastDirectoryOnPath(iterator, f, storageTreeStracture);
			}
		}
		
		return null;
	}
	
	private boolean checkPath(Iterator<Path> iterator, FileMetadata directory, final Map<FileMetadata, List<FileMetadata>> storageTreeStracture) {
		
		if(!iterator.hasNext())
			return false;
		
		Path path = iterator.next();
		List<FileMetadata> list = storageTreeStracture.get(directory);
		
		for(FileMetadata f : list) {
			if(f.getName().equals(path.toString())) {
				return checkPath(iterator, f, storageTreeStracture);
			}
		}
		
		return false;
	}
	
}
