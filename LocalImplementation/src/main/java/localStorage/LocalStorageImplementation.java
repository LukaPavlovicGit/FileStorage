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
import exception.NotFound;
import exception.PathException;
import exception.StorageException;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import fileMetadata.FileMetadata;
import specification.Storage;
import storageInformation.StorageInformation;
import storageManager.StorageManager;

public class LocalStorageImplementation extends Storage {
	
	private StorageManager storageManager;
	private StorageInformation storageInformation;
	
	static {
		StorageManager.registerStorage(new LocalStorageImplementation());
	}
		
	private LocalStorageImplementation() {
		this.storageManager = StorageManager.getInstance();
		this.storageInformation = StorageManager.getInstance().getStorageInformation();
	}
	
	@Override
	public void createStorage(String dest, StorageConfiguration storageConfiguration) throws StorageException, NamingPolicyException, PathException {
	
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
			storageManager.setStorageConfiguration(storageConfiguration);
		
		super.createStorageTreeStructure(dest);
		
		saveToJSON(storageInformation);
		saveToJSON(storageConfiguration);
	}

	@Override
	public void connectToStorage(String src) throws NotFound, StorageException {
		
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
	}

	@Override
	public boolean createDirectory(String dest, Integer... filesLimit) { // throws StorageSizeException, NamingPolicyException, DirectoryException
		
		try {
		
			FileMetadata fileMetadata = new FileMetadata();
			fileMetadata.setDirectory(true);
			fileMetadata.setNumOfFilesLimit( filesLimit.length>0 ? filesLimit[0] : null );
			super.addFileMetadataToStorage(dest, fileMetadata);
		
		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		if(!dest.startsWith(storagePathPrefix))
			dest = storageInformation.getCurrentDirectory().getAbsolutePath() + File.separator + dest;
		
		Path path = Paths.get(dest);
		new File(path.toString()).mkdir();
		
		return true;
	}

	@Override
	public boolean createFile(String dest) {
		
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		if(!dest.startsWith(storagePathPrefix))
			dest = storageInformation.getCurrentDirectory().getAbsolutePath() + File.separator + dest;
		
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
	public void move(String filePath, String newDest) {
		
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		if(!filePath.startsWith(storagePathPrefix))
			filePath = storageInformation.getCurrentDirectory().getAbsolutePath() + File.separator + filePath;
		if(!newDest.startsWith(storagePathPrefix))
			newDest = storageInformation.getCurrentDirectory().getAbsolutePath() + File.separator + newDest;
		

		try {
			
			super.moveFileMetadata(filePath, newDest);
			FileUtils.moveToDirectory(new File(filePath), new File(newDest), false);
			
		} catch (DirectoryException | NotFound | IOException e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void remove(String filePath){
		
		String storagePathPrefix = storageInformation.getStoragePathPrefix();
		
		if(!filePath.startsWith(storagePathPrefix))
			filePath = storageInformation.getCurrentDirectory().getAbsolutePath() + File.separator + filePath;
		
		try {
			
			super.removeFileMetadataFromStorage(filePath);
			
		} catch (NotFound e) {
			e.printStackTrace();
		}
		
		remove(new File(filePath));
		
	}
	
	private void remove(File file) {
		
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
	public void rename(String filePath, String newName) throws NotFound, NamingPolicyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(String filePath, String downloaDest) throws NotFound {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void copyFile(String filePath, String dest) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeToFile(String filePath, String text) {
		
	
	}

	@Override
	public void saveToJSON(Object obj) {
		
		String path = null;
		String jsonString = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		if(obj instanceof StorageInformation) { 
			path = storageInformation.getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.storageInformationJSONFileName;
		
			
			storageInformation.setCurrentDirectory(storageInformation.getStorageDirectory());
			storageInformation.dismantleStorageTreeStructure();
			jsonString = gson.toJson((StorageInformation) obj);
			storageInformation.buildStorageTreeStructure();
		}
		else if(obj instanceof StorageConfiguration) {
			path = storageInformation.getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.configJSONFileName;
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
				storageManager.setStorageInformation(gson.fromJson(reader, StorageInformation.class));
				storageManager.getStorageInformation().buildStorageTreeStructure();
			}
			else if(obj instanceof StorageConfiguration) 
				storageManager.setStorageConfiguration(gson.fromJson(reader, StorageConfiguration.class));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
