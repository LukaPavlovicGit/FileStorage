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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.script.ScriptEngineFactory;

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
	
	public boolean createDirectories(String dest, Map<String, Integer> dirNameAndFilesLimit ) { //mkdirs
		try {
			
			for(String name : dirNameAndFilesLimit.keySet()) {
				Integer filesLimit = dirNameAndFilesLimit.get(name);
				createDirectory(dest + File.separator + name, filesLimit);		
			}
			
		} catch (StorageSizeException | DirectoryException | NamingPolicyException | StorageConnectionException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public abstract boolean createFile(String dest) 
			throws StorageSizeException, NamingPolicyException, UnsupportedFileException, StorageConnectionException; // mkfile
	
	public boolean createFiles(String dest, List<String> names) { // mkfiles
		try {
		
			for(String name : names) {
				createFile(dest + File.separator + name);
			}
		
		} catch (StorageSizeException | NamingPolicyException | UnsupportedFileException | StorageConnectionException e) {	
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public abstract boolean move(String filePath, String newDest) throws NotFound, DirectoryException, StorageConnectionException; // move 
	
	public abstract boolean remove(String filePath) throws NotFound, StorageConnectionException; // del
	
	public abstract boolean rename(String filePath, String newName) throws NotFound, StorageConnectionException; // rename
	
	public abstract boolean download(String filePath, String downloadDest) throws NotFound, StorageConnectionException, PathException; // download
	
	public abstract boolean copyFile(String filePath, String dest) throws NotFound, StorageConnectionException; // copy
	
	public abstract boolean writeToFile(String filePath, String text, boolean append) throws NotFound, StorageSizeException, StorageConnectionException; //write
	
	protected abstract boolean checkStorageExistence(String path) throws PathException;
	
	protected abstract void saveToJSON(Object obj);
	
	protected abstract void readFromJSON(Object obj, String path);
	
	public boolean changeDirectory(String dest) throws NotFound, StorageConnectionException { // cd
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
					
		if(dest.equals("cd..")) {
			if(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().isStorage())
				return true;

			StorageManager.getInstance().getStorageInformation().setCurrentDirectory(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getParent());
			return true;
		}
								
		FileMetadata directory = getLastFileMetadataOnPath(getRelativePath(dest), StorageManager.getInstance().getStorageInformation().getStorageTreeStructure());
		
		if(directory == null)
			throw new NotFound("Location does not exist!");
		
		StorageManager.getInstance().getStorageInformation().setCurrentDirectory(directory);
		return true;
	}
	
	
	public boolean find(String src) throws StorageConnectionException { // hit
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");

		return checkPath(getRelativePath(src), StorageManager.getInstance().getStorageInformation().getStorageTreeStructure());
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

		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();	

		FileMetadata directory = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		if(directory == null)
			throw new NotFound("Source directory not found!");
		
		// fix parameters
		if(onlyDirs)
			onlyFiles = false;
		if(prefix !=null && prefix.length()>0) {
			sufix = null;
			subWord = null;
		}
		if(sufix != null && sufix.length()>0)
			subWord = null;
		
		Map<String, List<FileMetadata>> result = new HashMap<>();
		Queue<String> pq = new LinkedList<>();
		pq.add(directory.getRelativePath());
		
		// BFS
		while(!pq.isEmpty()) {
			
			String relativePath = pq.poll();
			result.put(relativePath, storageTreeStracture.get(relativePath));
			
			if(searchSubDirecories==false)
				break;
			
			for(FileMetadata f : storageTreeStracture.get(relativePath)) {
				if(f.isDirectory())
					pq.add(f.getRelativePath());
			}
		}
		
		if(onlyDirs || onlyFiles || (extension != null) || (prefix != null) || (sufix != null) || (subWord != null) ) {
			
			for(String key : result.keySet()) {
				
				List<FileMetadata> tmp = new ArrayList<>();
				
				for(FileMetadata f : result.get(key)) {
					
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
				
				result.put(key, tmp);
				
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
		
		
		Map<String, List<FileMetadata>> resultClone = new HashMap<>();
		
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
					builder.withRelativePath(f.getRelativePath());
				if(atributes[3])
					builder.withAbsolutePath(f.getAbsolutePath());
				if(atributes[4])
					builder.withTimeCreated(f.getTimeCreated());
				if(atributes[5])
					builder.withTimeModified(f.getTimeModified());
				if(atributes[6])
					builder.withIsFile(f.isFile());
				if(atributes[7])
					builder.withIsDirectory(f.isDirectory());								
				
				filtered.add(builder.build());
			}
			
			resultClone.put(relativePath, filtered);
		}
		
		return resultClone;
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
	protected boolean addFileMetadataToStorage(String dest, FileMetadata fileMetadata, Integer... filesLimit) 
			throws NotFound, NamingPolicyException, StorageSizeException, DirectoryException, UnsupportedFileException, OperationNotAllowed {
		
		String name = Paths.get(dest).getFileName().toString();
		dest = Paths.get(dest).getParent().toString(); // parent path
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

		FileMetadata parent = getLastFileMetadataOnPath(getRelativePath(dest), storageTreeStracture);
		
		// implementiraj da se naprave svi direktorijumi na putanji ako ne postoje
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
		fileMetadata.setSize(0L);
		fileMetadata.setAbsolutePath(parent.getAbsolutePath() + File.separator + name);
		fileMetadata.setRelativePath(parent.getRelativePath() + File.separator + name);
		fileMetadata.setParent(parent);
		fileMetadata.setTimeCreated(new Date());
		fileMetadata.setTimeModified(new Date());
		
		if(fileMetadata.isDirectory()) {
			storageTreeStracture.put(fileMetadata.getRelativePath(), new ArrayList<FileMetadata>());
			
			if(filesLimit.length>0) {
				Map<String, Integer> map = StorageManager.getInstance().getStorageInformation().getDirNumberOfFilesLimit();
				map.put(fileMetadata.getRelativePath(), filesLimit[0]);
			}
		}
		
		storageTreeStracture.get(parent.getRelativePath()).add(fileMetadata);		
		return true;
	}
	
	protected boolean removeFileMetadataFromStorage(String dest) throws NotFound {
				
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
								
		FileMetadata file = getLastFileMetadataOnPath(getRelativePath(dest), storageTreeStracture);
		
		if(file == null)
			throw new NotFound("Path does not exist!");
		
		storageTreeStracture.get(file.getParent().getRelativePath()).remove(file);
		return true;
	}
	
	protected boolean moveFileMetadata(String src, String newDest) throws NotFound, DirectoryException, OperationNotAllowed {
		
	
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();		

		FileMetadata srcFile = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		FileMetadata destFile = getLastFileMetadataOnPath(getRelativePath(newDest), storageTreeStracture);
		
		if(srcFile == null)
			throw new NotFound("File path not correct!");
		if(destFile == null)
			throw new NotFound("Destination path not correct!");
		if(!destFile.isDirectory())
			throw new DirectoryException("Destination path does not represent the directory!");
		if(destFile.getRelativePath().startsWith(srcFile.getRelativePath())) 
			throw new OperationNotAllowed("The destination folder is a subfolder of the source folder!");
		
		
		if(destFile.getNumOfFilesLimit() != null) {
			if(destFile.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			destFile.setNumOfFilesLimit(destFile.getNumOfFilesLimit() - 1);
		}
		
		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se premesta
		List<FileMetadata> list = storageTreeStracture.get(destFile.getRelativePath());
		for(int i = 0 ; i < list.size() ; i++) {
			
			FileMetadata f = list.get(i);
			
			if(f.getName().startsWith(srcFile.getName()) && f.getName().endsWith(srcFile.getName())) {
				srcFile.setName(srcFile.getName() + "*");
				i = 0;
			}
		}
		
		if(srcFile.isDirectory()) 
			pathFix(srcFile.getRelativePath(), destFile.getRelativePath() + File.separator + srcFile.getName(), storageTreeStracture);
				
		storageTreeStracture.get(srcFile.getParent().getRelativePath()).remove(srcFile);
		storageTreeStracture.get(destFile.getRelativePath()).add(srcFile);
		srcFile.setParent(destFile);
		srcFile.setAbsolutePath(destFile.getAbsolutePath() + File.separator + srcFile.getName());
		srcFile.setRelativePath(destFile.getRelativePath() + File.separator + srcFile.getName());		
		srcFile.setTimeModified(new Date());

		return true;
	}
	
	
	protected String renameFileMetadata(String src, String newName) throws NotFound{
		
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		
		FileMetadata file = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		
		if(file == null)
			throw new NotFound("File path not correct!");

		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se premesta
		List<FileMetadata> list = storageTreeStracture.get(file.getParent().getRelativePath());
		for(int i = 0 ; i < list.size() ; i++) {
			
			FileMetadata f = list.get(i);
			
			if(f.getName().startsWith(newName) && f.getName().endsWith(newName)) {
				newName += "*";
				i = 0;
			}
		}
		
		if(file.isDirectory()) 
			pathFix(file.getRelativePath(), file.getParent().getRelativePath() + File.separator + newName, storageTreeStracture);	
		
		file.setName(newName);
		file.setTimeModified(new Date());
		file.setAbsolutePath(file.getParent().getAbsolutePath() + File.separator + newName);
		file.setRelativePath(file.getParent().getRelativePath() + File.separator + newName);
		file.setTimeModified(new Date());

		return newName;
	}
	
	private void pathFix(String oldKey, String newKey, Map<String, List<FileMetadata>> storageTreeStracture) {
		
		List<FileMetadata> list = storageTreeStracture.get(oldKey);
		storageTreeStracture.put(newKey, list);
		storageTreeStracture.remove(oldKey);

		for(FileMetadata f : list) {

			if(f.isDirectory()) 
				pathFix(f.getRelativePath(), newKey + File.separator + f.getName(), storageTreeStracture);

			// newKey je relativna path
			f.setAbsolutePath(newKey + File.separator + f.getName());
			f.setRelativePath(newKey + File.separator + f.getName());
		}
	}
	
	protected void copyFileMetadata(String src, String dest) throws NotFound, DirectoryException, StorageSizeException, OperationNotAllowed {
		
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();		
		
		FileMetadata srcFile = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		FileMetadata destDir = getLastFileMetadataOnPath(getRelativePath(dest), storageTreeStracture);				
		
		if(srcFile == null)
			throw new NotFound("File path not correct!");
		if(destDir == null)
			throw new NotFound("Destination path not correct!");
		if(!destDir.isDirectory())
			throw new DirectoryException("Destination path does not represent the directory!");		
		if(destDir.getRelativePath().startsWith(srcFile.getRelativePath())) 
			throw new OperationNotAllowed("The destination folder is a subfolder of the source folder!");
		
		Long storageSize = StorageManager.getInstance().getStorageInformation().getStorageSize();
		if(storageSize != null) {
			if(storageSize - srcFile.getSize() < 0)
				throw new StorageSizeException("Storage size limit has been reached!");
			
			 StorageManager.getInstance().getStorageInformation().setStorageSize(storageSize - srcFile.getSize());
		}
		
		if(destDir.getNumOfFilesLimit() != null) {
			if(destDir.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			destDir.setNumOfFilesLimit(destDir.getNumOfFilesLimit() - 1);
		}
		
		FileMetadata srcFileClone = srcFile.clone();		
		List<FileMetadata> list = storageTreeStracture.get(destDir.getRelativePath());
		
		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se kopira		
		for(int i=0 ; i < list.size() ; i++) {
			
			FileMetadata f = list.get(i);
			
			if(f.getName().startsWith(srcFileClone.getName()) && f.getName().endsWith(srcFileClone.getName())) {
				srcFileClone.setName(srcFileClone.getName() + "*");								
				i = 0;
			}
		}
		
		srcFileClone.setParent(destDir);
		srcFileClone.setAbsolutePath(destDir.getAbsolutePath() + File.separator + srcFileClone.getName());
		srcFileClone.setRelativePath(destDir.getRelativePath() + File.separator + srcFileClone.getName());
		storageTreeStracture.get(destDir.getRelativePath()).add(srcFileClone);		
		
		if(srcFileClone.isDirectory()) 
			pathClone(srcFile.getRelativePath(), srcFileClone.getRelativePath(), srcFileClone, storageTreeStracture);								
	}
	
	private void pathClone(String fromKey, String toKey, FileMetadata parent, Map<String, List<FileMetadata>> storageTreeStracture) {
				
		Queue<String> fromKeys = new LinkedList<>();
		Queue<String> toKeys = new LinkedList<>();
		Queue<FileMetadata> parents = new LinkedList<>();				
				
		for(;;) {	
			
			storageTreeStracture.put(toKey, new ArrayList<>());
			
			for(FileMetadata f : storageTreeStracture.get(fromKey)) {
				
				FileMetadata clone = f.clone();
				clone.setParent(parent);
				clone.setAbsolutePath(parent.getAbsolutePath() + File.separator + clone.getName());
				clone.setRelativePath(parent.getRelativePath() + File.separator + clone.getName());		
				storageTreeStracture.get(toKey).add(clone);		
				
				if(f.isDirectory()) {
					fromKeys.add(f.getRelativePath());
					toKeys.add(clone.getRelativePath());
					parents.add(clone);
				}
					
			}
			
			if(fromKeys.isEmpty())
				return;
			
			fromKey = fromKeys.poll();
			toKey = toKeys.poll();
			parent = parents.poll();
		}		
	}
	
	protected boolean writeToFileMetadata(String filePath, String text, boolean append) {

		
		return true;
	}

	
	private FileMetadata getLastFileMetadataOnPath(Path path, final Map<String, List<FileMetadata>> storageTreeStracture) {
	
		FileMetadata ans = null;
		Iterator<Path> iterator = path.iterator();		
	
		if(!iterator.hasNext())
			return null;
		String parent = iterator.next().toString();		
		
		while(iterator.hasNext()) {
									
			String nextDirName = iterator.next().toString();
			List<FileMetadata> list = storageTreeStracture.get(parent);
			if(list==null) {
				System.out.println("parent:"+parent);
				System.out.println("path:"+path.toString());
			}
			for(int i=0 ; i<list.size() ; i++) {				
				FileMetadata f = list.get(i);					
				if(f.getName().equals(nextDirName)){
					parent += (File.separator + nextDirName);
					ans = f;
					break;
				}
				
				if(i == list.size() - 1) {
					System.out.println("parent:"+parent);
					System.out.println("nextDirName not found:"+nextDirName);
					return null;
				}
			}			
		}	
		
		if(ans == null)
		{
			System.out.println("parent:"+parent);
			System.out.println("nextDirName not found: nije usao u while");			
		}
		
		return ans;
	}
	
	private boolean checkPath(Path path, final Map<String, List<FileMetadata>> storageTreeStracture) {
		
		Iterator<Path> iterator = path.iterator();		
	
		if(!iterator.hasNext())
			return false;
		String parent = iterator.next().toString();		
				
		while(iterator.hasNext()) {
	
			String nextDirName = iterator.next().toString();
			List<FileMetadata> list = storageTreeStracture.get(parent);
		
			for(int i=0 ; i<list.size() ; i++) {				
				FileMetadata f = list.get(i);							
				if(f.getName().equals(nextDirName)){
					parent += (File.separator + nextDirName);
					break;
				}
				
				if(i == list.size() - 1)
					return false;
			}			
		}	
		
		return true;
	}
	
	protected Path getRelativePath(String path) {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		String dataRootAbsolutePath = storageInformation.getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = storageInformation.getDatarootDirectory().getRelativePath();
		Path relativePath = null;

		// racunamo relativnu putanju u odnosu na trenutni direktorijum	
		if(!path.startsWith(dataRootAbsolutePath) && !path.startsWith(dataRootRelativePath)) 
			relativePath = Paths.get(storageInformation.getCurrentDirectory().getRelativePath()).resolve(Paths.get(path));
		
		else if(path.startsWith(dataRootRelativePath)) 
			relativePath = Paths.get(path);
			/* npr. ako je :
								path = storage\dataRootDirectory\dir1\dir2
					 relativePath je = storage\dataRootDirectory\dir1\dir2
			*/
		else if(path.startsWith(dataRootAbsolutePath)){
			path = path.substring(dataRootAbsolutePath.length());
			relativePath = Paths.get(storageInformation.getDatarootDirectory().getRelativePath()).resolve(Paths.get(path));
		
			 /* npr. ako je:
							       path = C:\Users\Luka\Desktop\storage\dataRootDirectory\dir1\dir2
				   dataRootAbsolutePath = C:\Users\Luka\Desktop\storage\dataRootDirectory
				   		   novi path je = dir1\dir2
			            relativePath je = storage\dataRootDirectory\dir1\dir2
			 */			
		}	
		
		return relativePath;
	}
	
	protected Path getAbsolutePath(String path) {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		String dataRootAbsolutePath = storageInformation.getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = storageInformation.getDatarootDirectory().getRelativePath();
			
		if(!path.startsWith(dataRootAbsolutePath) && !path.startsWith(dataRootRelativePath))
			path = storageInformation.getCurrentDirectory().getAbsolutePath() + File.separator + path;		
		else if(path.startsWith(dataRootRelativePath)) 
			path = storageInformation.getDatarootDirectory().getAbsolutePath() + File.separator + path.substring(dataRootRelativePath.length());
		
		return Paths.get(path);
	}
}
