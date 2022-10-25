package googleDriveStorage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import configuration.StorageConfiguration;
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

public class GoogleDriveStorage extends Storage {
	
	private Drive service;
	private StringBuilder sb = new StringBuilder();
	

	/**
     * Application name.
     */
    private static final String APPLICATION_NAME = "remote-file-storage-implementation";

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials at
     * ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveStorage.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
	
	static {
		StorageManager.registerStorage(new GoogleDriveStorage());
	}
	
	private GoogleDriveStorage() {
		try {
			this.service = getDriveService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean createStorage(String dest)
			throws StorageException, NamingPolicyException, PathException, StorageConnectionException, StoragePathException {
			
			if(StorageManager.getInstance().getStorageInformation().isStorageConnected())
				throw new StorageConnectionException("Disconnect from the current storage in order to create new one storage!");	
			
			
			// u lokalu cuvamo podatke o remote skladistima
			java.io.File file = new java.io.File(StorageInformation.storageInformationJSONFileName);
			if(!file.exists()) {				
				try{			
					file.createNewFile();									
				} catch (IOException e) {				
					e.printStackTrace();
				}									
			}
			
			Path path = Paths.get(dest);			
			dest = path.getFileName().toString();
			
			if(!checkStorageExistence(dest))
				throw new StoragePathException("Storage path exception!");			
			
			try {						
				File storageMetadata = new File();
				storageMetadata.setName(dest);
				storageMetadata.setMimeType("application/vnd.google-apps.folder");
				File storage = service.files().create(storageMetadata).setFields("id").execute();
				
				File datarootMetadata = new File();
				datarootMetadata.setName(StorageInformation.datarootDirName);
				datarootMetadata.setMimeType("application/vnd.google-apps.folder");
				datarootMetadata.setParents(Collections.singletonList(storage.getId()));
				File dataroot = service.files().create(datarootMetadata).setFields("id").execute();
				
				StorageManager.getInstance().getStorageInformation().setStorageDirectoryID(storage.getId());
				StorageManager.getInstance().getStorageInformation().setDatarootDirectoryID(dataroot.getId());
																
				createStorageTreeStructure(dest);
				StorageManager.getInstance().getStorageInformation().setStorageConnected(true);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		return true;
	}

	@Override
	public boolean connectToStorage(String src) throws NotFound, StorageException, StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected())
			throw new StorageConnectionException("Disconnection from the current storage is requiered in order to connect to the new one!");
		
		readFromJSON(new StorageInformation(), src);
		
		return (StorageManager.getInstance().getStorageInformation().isStorageConnected()==true) ? true : false;
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
	public boolean createDirectory(String dest, Integer... filesLimit)
			throws StorageSizeException, NamingPolicyException, DirectoryException, StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		FileMetadata fileMetadata = null;
		
		try {
			
			fileMetadata = new FileMetadata();
			fileMetadata.setDirectory(true);
			fileMetadata.setNumOfFilesLimit( filesLimit.length>0 ? filesLimit[0] : null );
			addFileMetadataToStorage(dest, fileMetadata);
			
		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException | OperationNotAllowed e) {
			e.printStackTrace();
		}
		
		String name = Paths.get(dest).getFileName().toString();
		Path parentPath = null;
		
		if(dest.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix())) 
			parentPath = Paths.get(dest).getParent();							
		else
			parentPath = Paths.get(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getRelativePath()).resolve(parentPath).getParent();		
		
		if(filesLimit.length>0) {
			Map<String, Integer> map = StorageManager.getInstance().getStorageInformation().getDirNumberOfFilesLimit();
			map.put(dest, filesLimit[0]);
		}
		
		try {
			
			File file = new File();
			file.setName(name);
			file.setMimeType("application/vnd.google-apps.folder");
			String parentId = getLastFileIdOnPath(parentPath, StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());			
			file.setParents(Collections.singletonList(parentId));
			
			service.files().create(file).setFields("id").execute();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	public boolean createFile(String dest)
			throws StorageSizeException, NamingPolicyException, UnsupportedFileException, StorageConnectionException {

		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		FileMetadata fileMetadata = null;
		
		try {
			
			fileMetadata = new FileMetadata();
			fileMetadata.setFile(true);			
			addFileMetadataToStorage(dest, fileMetadata);
			
		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException | OperationNotAllowed e) {
			e.printStackTrace();
		}
		
		String name = Paths.get(dest).getFileName().toString();
		Path parentPath = null;
		
		if(dest.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix())) 
			parentPath = Paths.get(dest).getParent();							
		else
			parentPath = Paths.get(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getRelativePath()).resolve(parentPath).getParent();		
		
		try {
			
			File file = new File();
			file.setName(name);
			file.setMimeType("application/vnd.google-apps.document");
			String parentId = getLastFileIdOnPath(parentPath, StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());			
			file.setParents(Collections.singletonList(parentId));
			
			service.files().create(file).setFields("id").execute();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void move(String filePath, String newDest) throws StorageConnectionException {

		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		
		if(!filePath.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix()))
			filePath = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + java.io.File.separator + filePath;
		
		if(!newDest.startsWith(StorageManager.getInstance().getStorageInformation().getStoragePathPrefix()))
			newDest = StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getAbsolutePath() + java.io.File.separator + newDest;
		
		
		try {
			moveFileMetadata(filePath, newDest);
		} catch (NotFound | DirectoryException e) {
			e.printStackTrace();
		}
		
		String fileId = getLastFileIdOnPath(Paths.get(filePath), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
		String folderId = getLastFileIdOnPath(Paths.get(newDest), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());		
		
		try {
			// Retrieve the existing parents to remove
		    File file = service.files().get(fileId)
		        .setFields("parents")
		        .execute();
		    StringBuilder previousParents = new StringBuilder();
		    for (String parent : file.getParents()) {
		      previousParents.append(parent);
		      previousParents.append(',');
		    }
		   
		    // Move the file to the new folder
		    file = service.files().update(fileId, null)
	          .setAddParents(folderId)
	          .setRemoveParents(previousParents.toString())
	          .setFields("id, parents")
	          .execute();

	    } catch (IOException e) {
	      e.printStackTrace();
	    }
		
		
	}

	@Override
	public void remove(String filePath) throws NotFound, StorageConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rename(String filePath, String newName) throws NotFound, StorageConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(String filePath, String downloadDest) throws NotFound, StorageConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyFile(String filePath, String dest) throws NotFound, StorageConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToFile(String filePath, String text, boolean append)
			throws NotFound, StorageSizeException, StorageConnectionException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected boolean checkStorageExistence(String dest) {
		
		try {
			Path path = Paths.get(dest); 						
			java.io.File file = new java.io.File(StorageInformation.storageInformationJSONFileName);												
			Gson gson = new Gson();			
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Type type = new TypeToken<ArrayList<StorageInformation>>() {}.getType();
			ArrayList<StorageInformation> list = gson.fromJson(reader, type);

			if(list == null)
				return true;
			
			for(StorageInformation si : list) 
				if(si.getStorageDirectory().getName().equals(path.getFileName().toString()))
					return false;			
													
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	protected void saveToJSON(Object obj) {

		String jsonString = null;
		Gson gson = new Gson();
		
		if(obj instanceof StorageInformation) { 						
			
			// prvo proveravamo da li je storage prethodno upisan u JSON file, ako jeste samo updatujemo podatke
			java.io.File file = new java.io.File(StorageInformation.storageInformationJSONFileName);			
			try( BufferedReader reader = new BufferedReader(new FileReader(file)) ){				
				
				if(file.length() > 0) {					
					
					Type type = new TypeToken<ArrayList<StorageInformation>>() {}.getType();
					ArrayList<StorageInformation> list = gson.fromJson(reader, type);					
					
					if(list != null) {																		
						for(int i=0 ; i<list.size() ; i++) {
							
							StorageInformation si = list.get(i);							
							if(si.getStorageDirectory().getName().equals(StorageManager.getInstance().getStorageInformation().getStorageDirectory().getName())) {
								
								list.add(i, StorageManager.getInstance().getStorageInformation());
								
								StorageManager.getInstance().getStorageInformation().setCurrentDirectory(StorageManager.getInstance().getStorageInformation().getDatarootDirectory());
								jsonString = gson.toJson(list, type);
								
								try (FileWriter fileOut = new FileWriter(file)) {																								
									fileOut.write(jsonString);		    								
								} catch (Exception e) {
									e.printStackTrace();
							    }	
								
								return;
							}
						}												
					}
				}				
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			// ako ne postoji u JSON fajlu upisujemo ga sada
			StorageManager.getInstance().getStorageInformation().setCurrentDirectory(StorageManager.getInstance().getStorageInformation().getDatarootDirectory());
			jsonString = gson.toJson((StorageInformation) obj);
				
			if(jsonString == null)
				return;
									
			try(BufferedReader reader = new BufferedReader(new FileReader(file))){													
				
				if(file.length() == 0) {
					sb.append("[");
					sb.append(jsonString);
					sb.append("]");
				}
				else {
					String line = reader.readLine();
					sb = new StringBuilder(line);
					sb.deleteCharAt(sb.length() - 1);
					sb.append(",");
					sb.append(jsonString);
					sb.append("]"); 
				}
				
				try (FileWriter fileOut = new FileWriter(file)) {																				
					fileOut.write(String.valueOf(sb));		    				
				} catch (Exception e) {
					e.printStackTrace();
			    }
															
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	protected void readFromJSON(Object obj, String src) {
		if(obj instanceof StorageInformation) {
			try {
				Path path = Paths.get(src); 						
				java.io.File file = new java.io.File(StorageInformation.storageInformationJSONFileName);												
				Gson gson = new Gson();			
				BufferedReader reader = new BufferedReader(new FileReader(file));
				Type type = new TypeToken<ArrayList<StorageInformation>>() {}.getType();
				ArrayList<StorageInformation> list = gson.fromJson(reader, type);
	
				if(list == null)
					return;
				
				for(StorageInformation si : list) {
					
					if(si.getStorageDirectory().getName().equals(path.getFileName().toString())) {						
						StorageManager.getInstance().setStorageInformation(si);
						StorageManager.getInstance().getStorageInformation().setStorageConnected(true);									
					}
				}				
														
			} catch (IOException e) {			
				e.printStackTrace();
			}
		}
	}
	
	// path uvek treba biti oblika <storageName>\"rootDataDirectory"\...\...
	// pocetna vrednost za parentID ce uvek treba biti ID od storge direktorijuma 
	private String getLastFileIdOnPath(Path path, String parentID) {
		
		Iterator<Path> iterator = path.iterator();
		
		if(!iterator.hasNext())
			return parentID;
		
		iterator.next();
		
		while(iterator.hasNext()) {
			
			String nextDirName = iterator.next().toString();
			String pageToken = null;
			
			try {
			    
		    	do {
			      FileList result = service.files().list()
			          .setQ("'" + parentID + "'" + " in parents")
			          .setSpaces("drive")
			          .setFields("nextPageToken, files(id,size,mimeType,parents,name,trashed)")
			          .setPageToken(pageToken)
			          .execute();
			     
			      if(result.getFiles() == null) 
			    	  return parentID;
			      
			      for (File file : result.getFiles()) {
			        if(file.getName().equals(nextDirName)) {			        	
			        	parentID = file.getId();
			        	break;
			        }
			      }
				     	
			      pageToken = result.getNextPageToken();
			      
			    } while (pageToken != null);
			    
		    }catch(IOException e) {
		    	e.printStackTrace();
		    }
			
		}
		
		return parentID;
	}

}
