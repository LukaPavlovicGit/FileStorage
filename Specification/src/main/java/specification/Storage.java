package specification;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import configuration.StorageConfiguration;
import exception.DirectoryException;
import exception.InvalidArgumentsExcpetion;
import exception.NamingPolicyException;
import exception.OperationNotAllowed;
import exception.NotFound;
import exception.PathException;
import exception.StorageConnectionException;
import exception.StorageException;
import exception.StoragePathException;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import fileMetadata.FileMetadata;
import fileMetadata.FileMetadata.FileMetadataBuilder;
import storageInformation.StorageInformation;
import storageManager.StorageManager;

public abstract class Storage {
	
	public abstract boolean createStorage(String dest) 
			throws StorageException, NamingPolicyException, PathException, StorageConnectionException, StoragePathException; // mkstrg
	
	public abstract boolean connectToStorage(String src) throws NotFound, StorageException, PathException, StorageConnectionException; // con
	
	public abstract boolean disconnectFromStorage(); // discon
	
	public abstract boolean createDirectory(String dest, Integer... filesLimit) 
			throws StorageSizeException, NamingPolicyException, DirectoryException, StorageConnectionException; // mkdir
	
	public void createDirectories(String dest, Map<String, Integer> dirNameAndFilesLimit) { //mkdirs
		try {
			
			for(String name : dirNameAndFilesLimit.keySet()) {
				Integer filesLimit = dirNameAndFilesLimit.get(name);
				createDirectory(dest + File.separator + name, filesLimit);		
			}
			
		} catch (StorageSizeException | DirectoryException | NamingPolicyException | StorageConnectionException e) {
			e.printStackTrace();
		}
	}
	
	public abstract boolean createFile(String dest) 
			throws StorageSizeException, NamingPolicyException, UnsupportedFileException, StorageConnectionException; // mkfile
	
	public void createFiles(String dest, List<String> names) { // mkfiles
		try {
		
			for(String name : names) {
				createFile(dest + File.separator + name);
			}
		
		} catch (StorageSizeException | NamingPolicyException | UnsupportedFileException | StorageConnectionException e) {	
			e.printStackTrace();
		}
		
	}
	
	public abstract void move(String filePath, String newDest) throws NotFound, DirectoryException, StorageConnectionException; // move 
	
	public abstract void remove(String filePath) throws NotFound, StorageConnectionException; // del
	
	public abstract void rename(String filePath, String newName) throws NotFound, StorageConnectionException; // rename
	
	public abstract void download(String filePath, String downloadDest) throws NotFound, StorageConnectionException; // download
	
	public abstract void copyFile(String filePath, String dest) throws NotFound, StorageConnectionException; // copy
	
	public abstract void writeToFile(String filePath, String text, boolean append) throws NotFound, StorageSizeException, StorageConnectionException; //write
	
	protected abstract boolean checkStorageExistence(String path) throws PathException;
	
	protected abstract void saveToJSON(Object obj);
	
	protected abstract void readFromJSON(Object obj, String path);
	
	public void changeDirectory(String dest) throws NotFound, StorageConnectionException { // cd
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Path path = Paths.get(dest);
		
		if(dest.equals("cd..")) {
			if(storageInformation.getCurrentDirectory().isStorage())
				return;

			storageInformation.setCurrentDirectory(storageInformation.getCurrentDirectory().getParent());
			return;
		}
				
		FileMetadata startFromDirectory = null;		
		
		if(dest.startsWith(storageInformation.getStoragePathPrefix()))
			startFromDirectory = storageInformation.getStorageDirectory();
		else {
			startFromDirectory = storageInformation.getCurrentDirectory();
			path = Paths.get(startFromDirectory.getRelativePath()).resolve(path);
		}
		
		FileMetadata directory = getLastFileMetadataOnPath(path, startFromDirectory.getRelativePath(), storageInformation.getStorageTreeStructure());
		if(directory != null) 
			storageInformation.setCurrentDirectory(directory);
		else 
			throw new NotFound("Location does not exist!");
	}
	
	public boolean find(String filePath) throws StorageConnectionException { // hit
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		Path path = Paths.get(filePath);
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		
		FileMetadata startFromDirectory = null;
		
		if(filePath.startsWith(storageInformation.getStoragePathPrefix()))
			startFromDirectory = storageInformation.getStorageDirectory();
		else {
			startFromDirectory = storageInformation.getCurrentDirectory();
			path = Paths.get(startFromDirectory.getRelativePath()).resolve(path);
		}

		return checkPath(path, startFromDirectory.getRelativePath(), storageInformation.getStorageTreeStructure());
	}
	
	public Map<String, Boolean> find(List<String> filePaths) throws StorageConnectionException { // hit -l
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		Map<String, Boolean> result = new HashMap<>();
		for(String path : filePaths) 
			result.put(path, find(path));
		
		return result;
	}
	
	public List<String> findDestinantions(String name) throws StorageConnectionException { // dest
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		List<String> result = new ArrayList<>();
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		
		for(String relativePath : storageTreeStracture.keySet()) {
			
			for(FileMetadata f : storageTreeStracture.get(relativePath)) {
				
				Path path = Paths.get(f.getAbsolutePath());
				
				if(path.getFileName().toString().equals(name))
					result.add(f.getAbsolutePath());
					
			}
		}
		
		return result;
	}
	
	public Map<String, List<FileMetadata>> listDirectory(String src, boolean onlyDirs, boolean onlyFiles, boolean searchSubDirecories, 
				String extension, String prefix, String sufix, String subWord) throws NotFound, StorageConnectionException { // ls
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");

		Path path = Paths.get(src);
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		
		FileMetadata startFromDirectory = null;
		
		if(src.startsWith(storageInformation.getStoragePathPrefix()))
			startFromDirectory =  storageInformation.getStorageDirectory();
		else {
			startFromDirectory = storageInformation.getCurrentDirectory();
			path = Paths.get(startFromDirectory.getRelativePath()).resolve(path);
		}

		FileMetadata directory = getLastFileMetadataOnPath(path, startFromDirectory.getRelativePath(), storageInformation.getStorageTreeStructure());
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
		
		Map<String, List<FileMetadata>> result = new HashMap<>();
		Queue<String> pq = new PriorityQueue<>();
		pq.add(directory.getRelativePath());
		
		// BFS
		while(!pq.isEmpty()) {
			
			String relativePath = pq.poll();
			result.put(relativePath, storageInformation.getStorageTreeStructure().get(relativePath));
			
			if(searchSubDirecories==false)
				break;
			
			for(FileMetadata f : storageInformation.getStorageTreeStructure().get(relativePath)) {
				if(f.isDirectory())
					pq.add(f.getRelativePath());
			}
		}
		
		if(onlyDirs || onlyFiles || (extension != null) || (prefix != null) || (sufix != null) || (subWord != null) ) {
			
			for(String relativePath : result.keySet()) {
				
				List<FileMetadata> tmp = new ArrayList<>();
				
				for(FileMetadata f : result.get(relativePath)) {
					
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
				
				result.put(relativePath, tmp);
				
				if(searchSubDirecories==false)
					break;
			}	
		}
		
		return result;
	}
	
	
	public Map<String, List<FileMetadata>> resultSort(Map<String, List<FileMetadata>> result, boolean byName, boolean byCreationDate, 
			boolean byModificationDate, boolean ascending, boolean descending) throws StorageConnectionException{ // sort
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
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
		
		
		for(String relativePath : result.keySet()) {
			List<FileMetadata> list = result.get(relativePath);
			if(descending) {
				list = list.stream().sorted(comparator).collect(Collectors.toList());
				Collections.reverse(list);
				result.put(relativePath, list);
			}
			else 
				result.put(relativePath, list.stream().sorted(comparator).collect(Collectors.toList()));
		}
		
		return result;
	}
	
	// atributes inicilalizovati na false ( Arrays.fill(atributes, Boolean.FALSE) )
	
	public Map<String, List<FileMetadata>> resultFilter(Map<String, List<FileMetadata>> result, boolean[] atributes, Date[][] periods) 
			throws InvalidArgumentsExcpetion, StorageConnectionException{ // filter
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
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
		// atributes[2] = "absolutePath"
		// atributes[3] = "timeCreated"
		// atributes[4] = "fimeModified"
		// atributes[5] = "isFile"
		// atributes[6] = "isDirectory"
		
		for(String relativePath : result.keySet()) {
			
			List<FileMetadata> filtered = new ArrayList<>();
			
			for(FileMetadata f : result.get(relativePath)) {
				
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
					builder.withAbsolutePath(f.getAbsolutePath());
				if(atributes[3])
					builder.withTimeCreated(f.getTimeCreated());
				if(atributes[4])
					builder.withTimeModified(f.getTimeModified());
				if(atributes[5])
					builder.withIsFile(f.isFile());
				if(atributes[6])
					builder.withIsDirectory(f.isDirectory());
				
				
				filtered.add(builder.build());
			}
			
			result.put(relativePath, filtered);
		}
		
		return result;
	}
	
	public void setStorageConfiguration(Long size, Set<String> unsupportedFiles) {
		StorageManager.getInstance().getStorageInformation().setStorageSize(size);
		StorageManager.getInstance().getStorageInformation().setUnsupportedFiles(unsupportedFiles);
	}
	
	protected boolean createStorageTreeStructure(String dest) {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		Path path = Paths.get(dest);

		FileMetadata storage = new FileMetadataBuilder()
			.withFileID(storageInformation.getStorageDirectoryID())
			.withName(path.getFileName().toString())
			.withAbsolutePath(dest)
			.withRelativePath(path.getFileName().toString())
			.withTimeCreated(new Date())
			.withTimeModified(new Date())
			.withIsDirectory(true)
			.withIsStorage(true)
			.withStorageSize(storageInformation.getStorageSize())
			.withUnsupportedFiles(storageInformation.getUnsupportedFiles())
			.build();
		
		FileMetadata dataRoot = new FileMetadataBuilder()
			.withFileID(storageInformation.getDatarootDirectoryID())
			.withName(StorageInformation.datarootDirName)
			.withAbsolutePath(storage.getAbsolutePath() + File.separator + StorageInformation.datarootDirName)
			.withRelativePath(storage.getRelativePath() + File.separator + StorageInformation.datarootDirName)
			.withParent(storage)
			.withTimeCreated(new Date())
			.withTimeModified(new Date())
			.withIsDirectory(true)
			.withIsDataRoot(true)
			.build();
	
		FileMetadata strorageInformationJSONfile = new FileMetadataBuilder()
			.withFileID(storageInformation.getStorageTreeStructureJSOnID())
			.withName(StorageInformation.storageInformationJSONFileName)
			.withAbsolutePath(storage.getAbsolutePath() + File.separator + StorageInformation.storageInformationJSONFileName)
			.withRelativePath(storage.getRelativePath() + File.separator + StorageInformation.storageInformationJSONFileName)
			.withParent(storage)
			.withTimeCreated(new Date())
			.withTimeModified(new Date())
			.withIsFile(true)
			.withIsStrorageTreeStructureJSONFile(true)
			.build();

		
		List<FileMetadata> storageAdjacent = new ArrayList<FileMetadata>();
		storageAdjacent.add(dataRoot);
		storageAdjacent.add(strorageInformationJSONfile);
		
		storageTreeStracture.put(storage.getRelativePath(), storageAdjacent);
		storageTreeStracture.put(dataRoot.getRelativePath(), new ArrayList<FileMetadata>());
		
		storageInformation.setStorageDirectory(storage);
		storageInformation.setDatarootDirectory(dataRoot);
		storageInformation.setStorageInformationJSONfile(strorageInformationJSONfile);
		storageInformation.setCurrentDirectory(dataRoot);
		
		saveToJSON(storageInformation);						
		return true;
	}
		
	// kada se poziva iz drive implementacije, fileMetadata treba da ima podesen fileID !!!!!!
	// ako postoji numOfFilesLimit za direktorijum onda se u obe implementacije treba podestiti pre nego sto se posalje u specifikaciju
	// atribute isDirectory i isFile takodje treba da budu podeseni pre prosledjivanja
	protected boolean addFileMetadataToStorage(String dst, FileMetadata fileMetadata) 
			throws NotFound, NamingPolicyException, StorageSizeException, DirectoryException, UnsupportedFileException, OperationNotAllowed {
		
		Path path = Paths.get(dst);
		String name = path.getFileName().toString();
		path = path.getParent();
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		FileMetadata storage = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		
		if(storage.getStorageSize() != null) {
			if(storage.getStorageSize() < 1)
				throw new StorageSizeException("Storage size limit has been reached!");
		}
		
		if(fileMetadata.isFile() && !storage.getUnsupportedFiles().isEmpty()) {
			for(String extension : storage.getUnsupportedFiles()) {
				if(name.endsWith(extension))
					throw new UnsupportedFileException("Unsupported file!");
			}
		}
		
		FileMetadata startFromDirectory = null;

		if(dst.startsWith(storageInformation.getStoragePathPrefix())) 
			startFromDirectory = storageInformation.getStorageDirectory();
		else {
			 startFromDirectory = storageInformation.getCurrentDirectory();
			 path = Paths.get(startFromDirectory.getRelativePath()).resolve(path);
		}
				
		FileMetadata parent = getLastFileMetadataOnPath(path, startFromDirectory.getRelativePath(), storageTreeStracture);
		
		if(parent == null)
			throw new NotFound("Path is not correct!");
		if(!parent.isDirectory())
			throw new OperationNotAllowed("Given path does not represent directory!");
		
		if(parent.getNumOfFilesLimit() != null) {
			if(parent.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			parent.setNumOfFilesLimit(parent.getNumOfFilesLimit() - 1);
		}
		
		for(FileMetadata f : storageTreeStracture.get(parent.getRelativePath())) {
			if(f.getName().equals(fileMetadata.getName()))
				throw new NamingPolicyException("File with a such name already exist. Choose a different name!");
		}
		
		fileMetadata.setName(name);
		fileMetadata.setAbsolutePath(parent.getAbsolutePath() + File.separator + name);
		fileMetadata.setRelativePath(parent.getRelativePath() + File.separator + name);
		fileMetadata.setParent(parent);
		fileMetadata.setTimeCreated(new Date());
		fileMetadata.setTimeModified(new Date());
		if(fileMetadata.isDirectory()) 
			storageTreeStracture.put(fileMetadata.getRelativePath(), new ArrayList<FileMetadata>());
		
		storageTreeStracture.get(parent.getRelativePath()).add(fileMetadata);
		
		return true;
	}
	
	protected boolean removeFileMetadataFromStorage(String filePath) throws NotFound {
		
		Path path = Paths.get(filePath);
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
	
		FileMetadata startFromDirectory = null;

		if(filePath.startsWith(storageInformation.getStoragePathPrefix())) 
			startFromDirectory = storageInformation.getStorageDirectory();
		else {
			 startFromDirectory = storageInformation.getCurrentDirectory();
			 path = Paths.get(startFromDirectory.getRelativePath()).resolve(path);
		}
			
		FileMetadata file = getLastFileMetadataOnPath(path, startFromDirectory.getRelativePath(), storageTreeStracture);
		if(file == null)
			throw new NotFound("Path does not exist!");
		
		storageTreeStracture.get(file.getParent().getRelativePath()).remove(file);
		return true;
	}
	
	protected boolean moveFileMetadata(String filePath, String newDest) throws NotFound, DirectoryException {
		
		Path path1 = Paths.get(filePath);
		Path path2 = Paths.get(newDest);
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		
		FileMetadata startFromDirectory1 = null; //StorageManager.getInstance().getStorageInformation().getCurrentDirectory(); // za filePath
		FileMetadata startFromDirectory2 = null; //StorageManager.getInstance().getStorageInformation().getCurrentDirectory(); // za newDest
		
		if(filePath.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix()))
			startFromDirectory1 = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		else {
			startFromDirectory1 = StorageManager.getInstance().getStorageInformation().getCurrentDirectory();
			path1 = Paths.get(startFromDirectory1.getRelativePath()).resolve(path1);
		}
		
		if(newDest.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix()))
			startFromDirectory2 = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		else {
			startFromDirectory2 = StorageManager.getInstance().getStorageInformation().getCurrentDirectory();
			path2 = Paths.get(startFromDirectory2.getRelativePath()).resolve(path2);
		}
		
		FileMetadata file = getLastFileMetadataOnPath(path1, startFromDirectory1.getRelativePath(), storageTreeStracture);
		FileMetadata dest = getLastFileMetadataOnPath(path2, startFromDirectory2.getRelativePath(), storageTreeStracture);
		
		if(file == null)
			throw new NotFound("File path not correct!");
		if(dest == null)
			throw new NotFound("Destination path not correct!");
		if(!dest.isDirectory())
			throw new DirectoryException("Destination path does not represent the directory!");
		
		if(dest.getNumOfFilesLimit() != null) {
			if(dest.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			dest.setNumOfFilesLimit(dest.getNumOfFilesLimit() - 1);
		}
		
		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se premesta
		for(FileMetadata f : storageTreeStracture.get(dest.getRelativePath())) {
			if(f.getName().startsWith(file.getName()) && f.getName().endsWith(file.getName())) {
				file.setName(file.getName() + "*");
				break;
			}
		}
			
		storageTreeStracture.get(file.getParent().getRelativePath()).remove(file);
		storageTreeStracture.get(dest.getRelativePath()).add(file);		
		return true;
	}
	
	
	protected String renameFileMetadata(String filePath, String newName) throws NotFound{
		
		Path path = Paths.get(filePath);
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		
		FileMetadata startFromDirectory = null;
		
		if(filePath.contains(storagePathPrefix))
			startFromDirectory = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		else {
			startFromDirectory =  StorageManager.getInstance().getStorageInformation().getCurrentDirectory();
			path = Paths.get(startFromDirectory.getRelativePath()).resolve(path);
		}
		
		FileMetadata file = getLastFileMetadataOnPath(path, startFromDirectory.getRelativePath(), storageTreeStracture);
		if(file == null)
			throw new NotFound("File path not correct!");
		
		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se premesta
		for(FileMetadata f : storageTreeStracture.get(file.getRelativePath())) {
			if(f.getName().startsWith(file.getName()) && f.getName().endsWith(file.getName())) {
				file.setName(file.getName() + "*");
				break;
			}
		}
		
		file.setName(newName);		
		return newName;
	}
	
	protected void copyFileMetadata(String filePath, String destination) throws NotFound, DirectoryException, StorageSizeException {
		
		Path srcPath = Paths.get(filePath);
		Path destPath = Paths.get(destination);
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();				
		
		FileMetadata startFromDirectory1 = null; // za filePath
		FileMetadata startFromDirectory2 = null; // za newDest
		
		if(filePath.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix()))
			startFromDirectory1 = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		else {
			startFromDirectory1 = StorageManager.getInstance().getStorageInformation().getCurrentDirectory();
			srcPath = Paths.get(startFromDirectory1.getRelativePath()).resolve(srcPath);
		}
		
		if(destination.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix()))
			startFromDirectory2 = StorageManager.getInstance().getStorageInformation().getStorageDirectory();
		else {
			startFromDirectory2 = StorageManager.getInstance().getStorageInformation().getCurrentDirectory();
			destPath = Paths.get(startFromDirectory2.getRelativePath()).resolve(destPath);
		}
		
		FileMetadata file = getLastFileMetadataOnPath(srcPath, startFromDirectory1.getRelativePath(), storageTreeStracture);
		FileMetadata dest = getLastFileMetadataOnPath(destPath, startFromDirectory2.getRelativePath(), storageTreeStracture);
		
		if(file == null)
			throw new NotFound("File path not correct!");
		if(dest == null)
			throw new NotFound("Destination path not correct!");
		
		Long storageSize = StorageManager.getInstance().getStorageInformation().getStorageSize();
		if(storageSize != null) {
			if(storageSize - file.getSize() < 0)
				throw new StorageSizeException("Storage size limit has been reached!");
			
			 StorageManager.getInstance().getStorageInformation().setStorageSize(storageSize - file.getSize());
		}
		
		if(dest.getNumOfFilesLimit() != null) {
			if(dest.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			dest.setNumOfFilesLimit(dest.getNumOfFilesLimit() - 1);
		}
		
		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se kopira
		for(FileMetadata f : storageTreeStracture.get(dest.getRelativePath())) {
			if(f.getName().startsWith(file.getName()) && f.getName().endsWith(file.getName())) {
				file.setName(file.getName() + "*");
				break;
			}
		}
		
		storageTreeStracture.get(dest.getRelativePath()).add(file);
	}
	
	private FileMetadata getLastFileMetadataOnPath(Path path, String relativePath, final Map<String, List<FileMetadata>> storageTreeStracture) {
		
		Iterator<Path> iterator = path.iterator();		
		// parametar directory ce uvek biti folder u okviru kog se pretrazuje
		// ako je path folder1/folder2/folder3... za folder1 proveravamo da li se u njemu nalazi folder2, za folder2 proveravamo da li se u njemu danazi folder3 itd...
		if(!iterator.hasNext())
			return null;
		iterator.next();		
				
		FileMetadata ans = null;
		
		while(iterator.hasNext()) {
			
			Path nextDirPath = iterator.next();
			List<FileMetadata> list = storageTreeStracture.get(relativePath);
		
			for(int i=0 ; i<list.size() ; i++) {
				
				FileMetadata f = list.get(i);
				if(f.getName().equals(nextDirPath.toString())){
					relativePath += (File.separator + f.getName());
					ans = f;
					break;
				}
				
				if(i == list.size() - 1)
					return null;
			}			
		}	
		
		return ans;
	}
	
	private boolean checkPath(Path path, String relativePath, final Map<String, List<FileMetadata>> storageTreeStracture) {
		
		Iterator<Path> iterator = path.iterator();		
		// parametar relativePath ce biti putanja do foldera okviru kog se pretrazuje
		// ako je path folder1/folder2/folder3... za folder1 proveravamo da li se u njemu nalazi folder2, za folder2 proveravamo da li se u njemu nalazi folder3 itd...
		if(!iterator.hasNext())
			return false;
		
		iterator.next();
		
		while(iterator.hasNext()) {
			
			String nextDirName = iterator.next().toString();
			List<FileMetadata> list = storageTreeStracture.get(relativePath);
			
			for(int i=0 ; i<list.size() ; i++) {
			
				FileMetadata f = list.get(i);
				if(f.getName().equals(nextDirName)){
					relativePath += (File.separator + f.getName());				
					break;
				}		
				
				if(i == list.size() - 1)
					return false;
				
			}	
		}
				
		return true;
	}
}
