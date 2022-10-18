package localStorage;

import java.io.BufferedReader; 
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	}

	@Override
	public void connectToStorage(String src) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createDirectory(String dest, String name, Integer... filesLimit)
			throws StorageSizeException, NamingPolicyException, DirectoryException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createFile(String dest, String name)
			throws StorageSizeException, NamingPolicyException, UnsupportedFileException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void move(String name, String src, String newDest) throws NotFound {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(String name, String src) throws NotFound {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rename(String newName, String name, String src) throws NotFound, NamingPolicyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(String name, String src, String dest) throws NotFound {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveToJSON(Object obj) {
		String path = null;
		String jsonString = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		if(obj instanceof StorageInformation) { 
			path = storageInformation.getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.storageInformationJSONFileName;
		
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
	public void readFromJSON(Object obj) {
		
		String path = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		if(obj instanceof StorageInformation) 
			path = storageInformation.getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.storageInformationJSONFileName;
		else if(obj instanceof StorageConfiguration)
			path = storageInformation.getStorageDirectory().getAbsolutePath() + File.separator + StorageInformation.configJSONFileName;
	
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
