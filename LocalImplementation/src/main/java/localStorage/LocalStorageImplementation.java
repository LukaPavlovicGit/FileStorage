package localStorage;

import java.io.BufferedReader; 
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import configuration.StorageConfiguration; 
import exception.DirectoryException;
import exception.NamingPolicyException;
import exception.NotAllowedOperation;
import exception.NotFound;
import exception.PathException;
import exception.StorageConnectionException;
import exception.StorageException;
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
	
	@Override
	public void createStorage(String dest, StorageConfiguration storageConfiguration) 
			throws StorageException, NamingPolicyException, PathException, StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected())
			throw new StorageConnectionException("Disconnect from the current storage in order to create new one storage!");
		
		Path path = Paths.get(dest);
		
		String storageName = path.getFileName().toString();
		Path parentPath = path.getParent();
	
		File parentDirectory = new File(parentPath.toString());
		if(!parentDirectory.exists())
			throw new PathException("Given destination does not exist!");
		
		for(String fName : parentDirectory.list()) {
			if(fName.equals(storageName))
				throw new NamingPolicyException("Naming policy violation. Choose different storage name!");
		}
		
		File storage = new File(dest);
		storage.mkdir();
		
		File dataRootDirectory = new File(dest + File.separator + StorageInformation.datarootDirName);
		File configurationFSONfile = new File(dest + File.separator + StorageInformation.configJSONFileName);
		File storageInformationJSONfile = new File(dest + File.separator + StorageInformation.storageInformationJSONFileName);
		File downloadFile = new File(dest + File.separator + StorageInformation.downloadFileName);
		
		try {
			
			dataRootDirectory.mkdir();
			configurationFSONfile.createNewFile();
			storageInformationJSONfile.createNewFile();
			downloadFile.createNewFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(storageConfiguration != null)
			StorageManager.getInstance().setStorageConfiguration(storageConfiguration);
		
		super.createStorageTreeStructure(dest);
		StorageManager.getInstance().getStorageInformation().setStorageConnected(true);
	}

	@Override
	public void connectToStorage(String src) throws NotFound, StorageException, StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected())
			throw new StorageConnectionException("Disconnection from the current storage is requiered in order to connect to new one!");
		
		Path path = Paths.get(src);
		
		File sourceDirectory = new File(path.toString());
		if(!sourceDirectory.exists())
			throw new NotFound("Given destination does not exist!");
		
		int numOfDefaultFiles = 0;
		
		for(String fName : sourceDirectory.list()) {
			if( fName.equals(StorageInformation.storageInformationJSONFileName) ||
				fName.equals(StorageInformation.datarootDirName) ||
				fName.equals(StorageInformation.configJSONFileName) ) {
				
				if(++numOfDefaultFiles == 3)
					break;
			}
		}
		
		if(numOfDefaultFiles < 3)
			throw new StorageException("Given path does not represent the storage!");
		
		readFromJSON(new StorageInformation(), src);
		readFromJSON(new StorageConfiguration(), src);
		StorageManager.getInstance().getStorageInformation().setStorageConnected(true);
	}
	
	@Override
	public void disconnectFromStorage() {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			return;
		
		saveToJSON(new StorageInformation());
		saveToJSON(new StorageConfiguration());
		StorageManager.getInstance().getStorageInformation().setStorageConnected(false);
		
	}

	@Override
	public boolean createDirectory(String dest, Integer... filesLimit) throws StorageConnectionException { // throws StorageSizeException, NamingPolicyException, DirectoryException
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		try {
		
			FileMetadata fileMetadata = new FileMetadata();
			fileMetadata.setDirectory(true);
			fileMetadata.setNumOfFilesLimit( filesLimit.length>0 ? filesLimit[0] : null );
			super.addFileMetadataToStorage(dest, fileMetadata);
		
		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		if(!dest.startsWith(storagePathPrefix))
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest;
		
		Path path = Paths.get(dest);
		new File(path.toString()).mkdir();
		
		return true;
	}

	@Override
	public boolean createFile(String dest) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		if(!dest.startsWith(storagePathPrefix))
			dest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + dest;
		
		try {
			
			FileMetadata fileMetadata = new FileMetadata();
			fileMetadata.setFile(true);
			super.addFileMetadataToStorage(dest, fileMetadata);
		
		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		try {
			
			Path path = Paths.get(dest);
			new File(path.toString()).createNewFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void move(String filePath, String newDest) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		
		if(!filePath.startsWith(storagePathPrefix))
			filePath = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + filePath;
		if(!newDest.startsWith(storagePathPrefix))
			newDest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + newDest;
		
		try {
			
			super.moveFileMetadata(filePath, newDest);
			FileUtils.moveToDirectory(new File(filePath), new File(newDest), false);
			
		} catch (DirectoryException | NotFound | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void remove(String filePath) throws StorageConnectionException{
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		
		if(!filePath.startsWith(storagePathPrefix))
			filePath = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + filePath;
		
		try {
			
			super.removeFileMetadataFromStorage(filePath);
			
		} catch (NotFound e) {
			e.printStackTrace();
		}
		
		remove(new File(filePath));
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
	public void rename(String filePath, String newName) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		if(!filePath.startsWith(storagePathPrefix))
			filePath = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + filePath;
		
		try {
			// ako u direktorijumu vec postoji fajl sa imenom newName, konkateniramo ga sa '*'
			newName = super.renameFileMetadata(filePath, newName);
			
			Path oldDest = Paths.get(filePath);
			Path newDest = oldDest.getParent().resolve( newName );
			
			FileUtils.moveToDirectory(new File(oldDest.toString()), new File(newDest.toString()), false);
			
		} catch (NotFound | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void download(String filePath, String dest) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		Path srcPath = Paths.get(filePath);
		Path destPath = Paths.get(dest);
		
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		String userDirectory = System.getProperty("user.home");
		
		if(!filePath.startsWith(storagePathPrefix))
			srcPath = Paths.get(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath()).resolve(filePath);
		
		if(!dest.startsWith(userDirectory))
			destPath = Paths.get(userDirectory).resolve(destPath.getFileName());
		
		
		try {
			FileUtils.copyToDirectory(new File(srcPath.toString()), new File(destPath.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void copyFile(String filePath, String dest) throws StorageConnectionException{
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		try {
			
			super.copyFileMetadata(filePath, dest);
		
		} catch (StorageSizeException | NotFound | DirectoryException e) {
			e.printStackTrace();
		}
		
		Path srcPath = Paths.get(filePath);
		Path destPath = Paths.get(dest);
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();

		if(!filePath.startsWith(storagePathPrefix))
			srcPath = Paths.get(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath()).resolve(filePath);
		
		if(!dest.startsWith(storagePathPrefix))
			destPath = Paths.get(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath()).resolve(destPath);
		
		try {
			
			FileUtils.copyToDirectory(new File(srcPath.toString()), new File(destPath.toString()));
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeToFile(String filePath, String text, boolean append) throws NotFound, StorageConnectionException, StorageSizeException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		String storagePathPrefix = StorageManager.getInstance().getStorageInformation().getStoragePathPrefix();
		if(!filePath.startsWith(storagePathPrefix))
			filePath = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + File.separator + filePath;
		
		File file = new File(filePath);
		if(!file.exists() || !file.isFile())
			throw new NotFound("File not found");
		
		Long size = text.length() + ((append == true) ? file.length() : 0L);
		
		Long storageSize = StorageManager.getInstance().getStorageConfiguration().getStorageSize();
		if(storageSize != null) {
			if(storageSize - size < 0)
				throw new StorageSizeException("Storage size limit has been reached!");
			
			StorageManager.getInstance().getStorageConfiguration().setStorageSize(storageSize - size);;
		}
		
		try (FileWriter fileOut = new FileWriter(filePath, append)) {
	           
			fileOut.write(text);
	    
		} catch (Exception e) {
	        e.printStackTrace();
	    }
		
	}

	@Override
	public void saveToJSON(Object obj) {
		
		String path = null;
		String jsonString = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		if(obj instanceof StorageInformation) { 
			path = StorageManager.getInstance().getStorageInformation().getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.storageInformationJSONFileName;
		
			StorageManager.getInstance().getStorageInformation().setCurrentDirectory(StorageManager.getInstance().getStorageInformation().getDatarootDirectory());
			StorageManager.getInstance().getStorageInformation().dismantleStorageTreeStructure();
			jsonString = gson.toJson((StorageInformation) obj);
			StorageManager.getInstance().getStorageInformation().buildStorageTreeStructure();
		}
		else if(obj instanceof StorageConfiguration) {
			path = StorageManager.getInstance().getStorageInformation().getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.configJSONFileName;
			jsonString = gson.toJson((StorageConfiguration) obj);	
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
		else if(obj instanceof StorageConfiguration)
			path = src + File.separator + StorageInformation.configJSONFileName;
	
		if(path == null)
			return;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
		
			if(obj instanceof StorageInformation) {
				StorageManager.getInstance().setStorageInformation(gson.fromJson(reader, StorageInformation.class));
				StorageManager.getInstance().getStorageInformation().buildStorageTreeStructure();
			}
			else if(obj instanceof StorageConfiguration) 
				StorageManager.getInstance().setStorageConfiguration(gson.fromJson(reader, StorageConfiguration.class));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
