package localStorage;

import java.io.BufferedReader; 
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import exception.DirectoryException;
import exception.NamingPolicyException;
import exception.NotFound;
import exception.OperationNotAllowed;
import exception.PathException;
import exception.StorageConnectionException;
import exception.StorageException;
import exception.StoragePathException;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import fileMetadata.FileMetadata;
import specification.Storage;
import storageInformation.StorageInformation;
import storageManager.StorageManager;

public class LocalStorageImplementation extends Storage {
		
	static {
		StorageManager.registerStorage(new LocalStorageImplementation());
	}
		
	private LocalStorageImplementation() {
		
	}
	// onemoguciti da se u okviru storege-a pravi drugi storage
	@Override
	public boolean createStorage(String dest) 
			throws StorageException, NamingPolicyException, PathException, StorageConnectionException, StoragePathException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected())
			throw new StorageConnectionException("Disconnect from the current storage in order to create the new  one storage!");
		
		if(!dest.startsWith(FileUtils.getUserDirectoryPath()))
			throw new PathException(String.format("Storage must reside in the User's directory! Make sure that storage path starts with '%s'", FileUtils.getUserDirectoryPath()));
		
		Path path = Paths.get(dest);		
		String storageName = path.getFileName().toString();
		Path parentPath = path.getParent();
	
		File parentDirectory = new File(parentPath.toString());
		if(!parentDirectory.exists())
			parentDirectory.mkdirs();
		
		if(checkStorageExistence(parentPath.toString()))
			throw new StoragePathException("Storage path is not valid! Along the given path storage already exist.");			
		
		for(String fName : parentDirectory.list()) {
			if(fName.equals(storageName))
				throw new NamingPolicyException("Naming policy violation. Choose different storage name!");
		}
		
		File storage = new File(dest);
		storage.mkdir();
		
		File dataRootDirectory = new File(dest + File.separator + StorageInformation.datarootDirName);		
		File storageInformationJSONfile = new File(dest + File.separator + StorageInformation.storageInformationJSONFileName);		
		
		try {			
			dataRootDirectory.mkdir();			
			storageInformationJSONfile.createNewFile();						
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		createStorageTreeStructure(dest);
		StorageManager.getInstance().getStorageInformation().setStorageConnected(true);
		return true;
	}

	@Override
	public boolean connectToStorage(String src) throws NotFound, StorageException, StorageConnectionException, PathException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected())
			throw new StorageConnectionException("Disconnection from the current storage is requiered in order to connect to the new one!");
		
		if(!src.startsWith(FileUtils.getUserDirectoryPath()))
			throw new PathException(String.format("Given path is incorrect! Make sure that storage path starts with '%s'", FileUtils.getUserDirectoryPath()));
		
		Path path = Paths.get(src);		
		File directory = new File(path.toString());
		if(!directory.exists())
			throw new NotFound("Given destination does not exist!");
		
		int numOfDefaultFiles = 0;
		
		for(String fName : directory.list()) {
			if( fName.equals(StorageInformation.storageInformationJSONFileName) ||
				fName.equals(StorageInformation.datarootDirName) ) {
				
				if(++numOfDefaultFiles == 2)
					break;
			}
		}
		
		if(numOfDefaultFiles < 2)
			throw new StorageException("Given path does not represent the storage!");
		
		readFromJSON(new StorageInformation(), src);	
		return true;
	}
	
	@Override
	public boolean disconnectFromStorage() {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			return true;
		
		saveToJSON(new StorageInformation());
		StorageManager.getInstance().getStorageInformation().setStorageConnected(false);
		
		return true;
	}

	@Override
	public boolean createDirectory(String dest, Integer... filesLimit) throws StorageConnectionException { // throws StorageSizeException, NamingPolicyException, DirectoryException
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
								
		try {

			FileMetadata fileMetadata = new FileMetadata();
			fileMetadata.setDirectory(true);
			fileMetadata.setNumOfFilesLimit( filesLimit.length>0 ? filesLimit[0] : null );
			addFileMetadataToStorage(dest, fileMetadata, filesLimit);

		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException | OperationNotAllowed e) {
			e.printStackTrace();
			return false;
		}
		
		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!dest.startsWith(dataRootAbsolutePath) && !dest.startsWith(dataRootRelativePath))
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest;
		
		else if(dest.startsWith(dataRootRelativePath)) 
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest.substring(dataRootRelativePath.length() + File.separator.length());
		
		return new File(dest).mkdir();
	}

	@Override
	public boolean createFile(String dest) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		try {
			
			FileMetadata fileMetadata = new FileMetadata();
			fileMetadata.setFile(true);
			addFileMetadataToStorage(dest, fileMetadata);
		
		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException | OperationNotAllowed e) {
			e.printStackTrace();
		}
		
		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!dest.startsWith(dataRootAbsolutePath) && !dest.startsWith(dataRootRelativePath))
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest;
		
		else if(dest.startsWith(dataRootRelativePath)) 
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest.substring(dataRootRelativePath.length() + File.separator.length());
				
		try {			
			return new File(dest).createNewFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public boolean move(String src, String newDest) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		try {			
			moveFileMetadata(src, newDest);			
			
		} catch (DirectoryException | NotFound e) {
			e.printStackTrace();
			return false;
		}
		
		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!src.startsWith(dataRootAbsolutePath) && !src.startsWith(dataRootRelativePath))
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src;		
		else if(src.startsWith(dataRootRelativePath)) 
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src.substring(dataRootRelativePath.length() + File.separator.length());
				
		if(!newDest.startsWith(dataRootAbsolutePath) && !newDest.startsWith(dataRootRelativePath))
			newDest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + newDest;		
		else if(newDest.startsWith(dataRootRelativePath)) 
			newDest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + newDest.substring(dataRootRelativePath.length() + File.separator.length());
		
		try {
			FileUtils.moveToDirectory(new File(src), new File(newDest), false);
		} catch (IOException e) {			
			e.printStackTrace();
			return false;
		}	
		
		return true;
	}

	@Override
	public boolean remove(String path) throws StorageConnectionException{
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		try {			
			removeFileMetadataFromStorage(path);			
			
		} catch (NotFound e) {
			e.printStackTrace();
			return false;
		}
		
		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!path.startsWith(dataRootAbsolutePath) && !path.startsWith(dataRootRelativePath))
			path = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + path;
		
		else if(path.startsWith(dataRootRelativePath)) 
			path = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + path.substring(dataRootRelativePath.length() + File.separator.length());		
						
		remove(new File(path));
		return true;
	}
	
	private void remove(File file) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		if(file.isFile()) {
			file.delete();
			return;
		}
		
		for(File f : file.listFiles()) {
			remove(f);
		}
		
		file.delete();
	}

	@Override
	public boolean rename(String src, String newName) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		
		try {
			// ako u direktorijumu vec postoji fajl sa imenom newName, konkateniramo ga sa '*'
			newName = renameFileMetadata(src, newName);
			
		} catch (NotFound e) {
			e.printStackTrace();
			return false;
		}
		
		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!src.startsWith(dataRootAbsolutePath) && !src.startsWith(dataRootRelativePath))
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src;		
		else if(src.startsWith(dataRootRelativePath)) 
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src.substring(dataRootRelativePath.length() + File.separator.length());
	
		Path oldDest = Paths.get(src);
		Path newDest = oldDest.getParent().resolve(newName);
		
		try {
			FileUtils.moveToDirectory(new File(oldDest.toString()), new File(newDest.toString()), false);
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}

	@Override
	public boolean download(String src, String dest) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
						
		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!src.startsWith(dataRootAbsolutePath) && !src.startsWith(dataRootRelativePath))
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src;		
		else if(src.startsWith(dataRootRelativePath)) 
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src.substring(dataRootRelativePath.length() + File.separator.length());
		
		if(!dest.startsWith(dataRootAbsolutePath) && !dest.startsWith(dataRootRelativePath))
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest;		
		else if(dest.startsWith(dataRootRelativePath)) 
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest.substring(dataRootRelativePath.length() + File.separator.length());
				
		Path srcPath = Paths.get(src);
		Path destPath = Paths.get(dest);		
		
		try {
			FileUtils.copyToDirectory(new File(srcPath.toString()), new File(destPath.toString()));
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	@Override
	public void copyFile(String src, String dest) throws StorageConnectionException{
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		try {			
			copyFileMetadata(src, dest);
		
		} catch (StorageSizeException | NotFound | DirectoryException e) {
			e.printStackTrace();
			return;
		}
		
		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!src.startsWith(dataRootAbsolutePath) && !src.startsWith(dataRootRelativePath))
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src;		
		else if(src.startsWith(dataRootRelativePath)) 
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src.substring(dataRootRelativePath.length() + File.separator.length());
		
		if(!dest.startsWith(dataRootAbsolutePath) && !dest.startsWith(dataRootRelativePath))
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest;		
		else if(dest.startsWith(dataRootRelativePath)) 
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest.substring(dataRootRelativePath.length() + File.separator.length());
				
		Path srcPath = Paths.get(src);
		Path destPath = Paths.get(dest);
		
		try {			
			FileUtils.copyToDirectory(new File(srcPath.toString()), new File(destPath.toString()));
		
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}
	}
	
	@Override
	public void writeToFile(String src, String text, boolean append) throws NotFound, StorageConnectionException, StorageSizeException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");

		String dataRootAbsolutePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = StorageManager.getInstance().getStorageInformation().getDatarootDirectory().getRelativePath();
		
		if(!src.startsWith(dataRootAbsolutePath) && !src.startsWith(dataRootRelativePath))
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src;		
		else if(src.startsWith(dataRootRelativePath)) 
			src = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + src.substring(dataRootRelativePath.length() + File.separator.length());

		
		File file = new File(src);
		if(!file.exists() || !file.isFile())
			throw new NotFound("File not found");
		
		Long size = text.length() + ((append == true) ? file.length() : 0L);
		
		Long storageSize = StorageManager.getInstance().getStorageInformation().getStorageSize();
		if(storageSize != null) {
			if(storageSize - size < 0)
				throw new StorageSizeException("Storage size limit has been reached!");
			
			StorageManager.getInstance().getStorageInformation().setStorageSize(storageSize - size);;
		}
		
		writeToFileMetadata(dataRootRelativePath, text, append); // TREBA DA SE IMPLEMENTIRA
		
		try (FileWriter fileOut = new FileWriter(src, append)) {
	           
			fileOut.write(text);
	    
		} catch (Exception e) {
			e.printStackTrace();
	    }		
	}
	
	@Override
	protected boolean checkStorageExistence(String dest) throws PathException{
						
		String userDirectoryPath = FileUtils.getUserDirectoryPath();
		if(!dest.startsWith(userDirectoryPath))
			throw new PathException(String.format("Storage must reside in the User's directory! Make sure that storage path starts with '%s'", userDirectoryPath));
				
		// ne radimo proveru za C:\Users\Luka, vec za C:\Users\Luka\...\...\...
		Path path = Paths.get(dest.substring(userDirectoryPath.length()));
		Path currPath = Paths.get(userDirectoryPath);	
		
		Iterator<Path> iterator = path.iterator();

		while(iterator.hasNext()) {

			currPath = currPath.resolve(iterator.next());
			File directory = new File(currPath.toString());
			int numOfDefaultFiles = 0;
			
			for(String fName : directory.list()) {
				if( fName.equals(StorageInformation.storageInformationJSONFileName) ||
					fName.equals(StorageInformation.datarootDirName) ) {
										
					if(++numOfDefaultFiles == 2)
						return true;
				}				
			}
		}
		
		return false;
	}

	@Override
	public void saveToJSON(Object obj) {
		
		String path = null;
		String jsonString = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		if(obj instanceof StorageInformation) { 
			
			path = StorageManager.getInstance().getStorageInformation().getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.storageInformationJSONFileName;
		
			StorageManager.getInstance().getStorageInformation().setCurrentDirectory(StorageManager.getInstance().getStorageInformation().getDatarootDirectory());
			jsonString = gson.toJson((StorageInformation) obj);
		}
		
		if(path == null || jsonString == null)
			return;
		
		try (FileWriter fileOut = new FileWriter(path, false)) {
           
			fileOut.write(jsonString);
	    
		} catch (Exception e) {
			e.printStackTrace();
	    }
	
	}

	@Override
	public void readFromJSON(Object obj, String src) {
		
		String path = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		if(obj instanceof StorageInformation) 
			path = src + File.separator + StorageInformation.storageInformationJSONFileName;
	
		if(path == null)
			return;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
		
			if(obj instanceof StorageInformation) {
				StorageManager.getInstance().setStorageInformation(gson.fromJson(reader, StorageInformation.class));
				StorageManager.getInstance().getStorageInformation().setStorageConnected(true);
			}			

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
