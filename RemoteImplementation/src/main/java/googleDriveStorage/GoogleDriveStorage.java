package googleDriveStorage;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
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
			fileMetadata.setNumOfFilesLimit( (filesLimit.length > 0) ? filesLimit[0] : null );
			addFileMetadataToStorage(dest, fileMetadata); 
			
		} catch (NotFound | StorageSizeException | NamingPolicyException | DirectoryException | UnsupportedFileException | OperationNotAllowed e) {
			e.printStackTrace();
		}
		
		String name = Paths.get(dest).getFileName().toString();
		Path parentPath = getAbsolutePath(Paths.get(dest).getParent().toString());
								
		try {			
			File file = new File();
			file.setName(name);
			file.setMimeType("application/vnd.google-apps.folder");
			File parent = getLastFileOnPath(parentPath, StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());			
			file.setParents(Collections.singletonList(parent.getId()));
			
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
		Path parentPath = getAbsolutePath(Paths.get(dest).getParent().toString());	
		
		try {			
			File file = new File();
			file.setName(name);
			file.setMimeType("application/vnd.google-apps.document");
			File parent = getLastFileOnPath(parentPath, StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());			
			file.setParents(Collections.singletonList(parent.getId()));
			
			service.files().create(file).setFields("id").execute();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public boolean move(String filePath, String newDest) throws StorageConnectionException {

		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		try {
			moveFileMetadata(filePath, newDest);
			
		} catch (NotFound | DirectoryException | OperationNotAllowed e) {
			e.printStackTrace();
			return false;
		}
		
		File src = getLastFileOnPath(getAbsolutePath(filePath), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
		File dest = getLastFileOnPath(getAbsolutePath(newDest), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());		
		
		try {
			// Retrieve the existing parents to remove
		    File file = service.files().get(src.getId())
		        .setFields("parents")
		        .execute();
		    
		    StringBuilder previousParents = new StringBuilder();
		    for (String parent : file.getParents()) {
		      previousParents.append(parent);
		      previousParents.append(',');
		    }		   
		    
		    // Move the file to the new folder
		    file = service.files().update(src.getId(), null)
	          .setAddParents(dest.getId())
	          .setRemoveParents(previousParents.toString())
	          .setFields("id, parents")
	          .execute();

	    } catch (IOException e) {
	      e.printStackTrace();
	      return false;
	    }		
		
		return true;
	}

	@Override // resi ako folder ima file limit!!!!
	public boolean remove(String filePath) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");		
		
		try {
			removeFileMetadataFromStorage(filePath);
		} catch (NotFound e) {			
			e.printStackTrace();
			return false;
		}
		
		File f = getLastFileOnPath(getAbsolutePath(filePath), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
		
		try {
			if(f.getMimeType().equals("application/vnd.google-apps.folder"))
				remove(f);			
			else 		
				service.files().delete(f.getId()).execute();							
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void remove(File directory) {
		
		try {
			
			List<File> files = getFilesByParentId(directory.getId());
			
			for(File f : files) {
				if(f.getMimeType().equals("application/vnd.google-apps.folder")) 
					remove(f);				
								
				service.files().delete(f.getId()).execute();							
			}					
			//service.files().delete(directory.getId()).execute();	
		
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}

	@Override
	public boolean rename(String filePath, String newName) throws StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");				
		
		try {
			// ako u direktorijumu vec postoji fajl sa imenom newName, konkateniramo ga sa '*'			
			newName = renameFileMetadata(filePath, newName);			
			File f = getLastFileOnPath(getAbsolutePath(filePath), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());	
			
//			File updated = f.clone();
//			updated.setName(newName);
//								
//			service.files().delete(f.getId());
//			service.files().create(updated).setFields("id").execute();					
//			
		} catch(NotFound /*| IOException*/ e) {
			e.printStackTrace();
		}
		
		return true;
		
//		try {
//		      // First retrieve the file from the API.
//		      File file = service.files().get(fileId).execute();
//
//		      // File's new metadata.
//		      file.setTitle(newTitle);
//		      file.setDescription(newDescription);
//		      file.setMimeType(newMimeType);
//
//		      // File's new content.
//		      java.io.File fileContent = new java.io.File(newFilename);
//		      FileContent mediaContent = new FileContent(newMimeType, fileContent);
//
//		      // Send the request to the API.
//		      File updatedFile = service.files().update(fileId, file, mediaContent).execute();
//
//		      return updatedFile;
//		    } catch (IOException e) {
//		      System.out.println("An error occurred: " + e);
//		      return null;
//		    }
//		  }
		
	}

	@Override
	public boolean download(String filePath, String downloadDest) throws NotFound, StorageConnectionException, PathException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected())
			throw new StorageConnectionException("Disconnect from the current storage in order to create the new  one storage!");
		
		if(!downloadDest.startsWith(FileUtils.getUserDirectoryPath()))
			throw new PathException(String.format("Storage must reside in the User's directory! Make sure that storage path starts with '%s'", FileUtils.getUserDirectoryPath()));
		
		java.io.File downloadDestLocalFile = new java.io.File(downloadDest);
		if(!downloadDestLocalFile.exists())
			downloadDestLocalFile.mkdirs();
		
		File googleDriveFile = getLastFileOnPath(Paths.get(filePath), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
		
		if(googleDriveFile.getMimeType().equals("application/vnd.google-apps.folder")) 
			return download(googleDriveFile, downloadDest + java.io.File.separator + googleDriveFile.getName());
		
		else if(googleDriveFile.getMimeType().equals("application/vnd.google-apps.document")) {
			try {
				
				java.io.File localFile = new java.io.File(downloadDest + java.io.File.separator + googleDriveFile.getName());
				if(!localFile.exists())
					localFile.createNewFile();
				
				OutputStream outputStream = new ByteArrayOutputStream();
				service.files().export(googleDriveFile.getId(), "application/pdf").executeMediaAndDownloadTo(outputStream);
				FileWriter fileWriter = new FileWriter(localFile);
            	fileWriter.write(String.valueOf(outputStream));
           		fileWriter.close();
          		outputStream.close();
          		
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	private boolean download(File file, String destPath) {
		
		java.io.File downloadDestLocalFile = new java.io.File(destPath);
		if(!downloadDestLocalFile.exists())
			downloadDestLocalFile.mkdir();
		
		List<File> list = getFilesByParentId(file.getId());
		for(File f : list) {
			
			if(f.getMimeType().equals("application/vnd.google-apps.folder")) 
				return download(f, destPath + java.io.File.separator + f.getName());
			
			else if(f.getMimeType().equals("application/vnd.google-apps.document")) {
				try {
					
					java.io.File localFile = new java.io.File(destPath + java.io.File.separator + f.getName());
					if(!localFile.exists())
						localFile.createNewFile();
					
					OutputStream outputStream = new ByteArrayOutputStream();
					service.files().export(f.getId(), "application/pdf").executeMediaAndDownloadTo(outputStream);
					FileWriter fileWriter = new FileWriter(localFile);
	            	fileWriter.write(String.valueOf(outputStream));
	           		fileWriter.close();
	          		outputStream.close();
	          		
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		
		return true;
	}

	@Override // obezbediti i funcionalnist i ako destinacija ne postoji
	public boolean copyFile(String filePath, String dest) throws NotFound, StorageConnectionException {
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
			
		File srcFile = getLastFileOnPath(getAbsolutePath(filePath), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
		File destFile = getLastFileOnPath(getAbsolutePath(dest), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
		
		if(destFile == null) { // treba implementirati da ako neki directory na putanji ne postoji da se napravi (i u specifikaciji i ina local/remote storage)
			try {
				createDirectory(dest);
				destFile = getLastFileOnPath(Paths.get(dest), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
				
			} catch (StorageSizeException | NamingPolicyException | DirectoryException | StorageConnectionException e) {
				e.printStackTrace();
			}
			
			if(destFile == null)
				System.out.println("JBG NESTO SI ZAJEBO");
		}
		
		try {
			copyFileMetadata(filePath, dest);
		} catch (NotFound | DirectoryException | StorageSizeException | OperationNotAllowed e) {
			e.printStackTrace();
			return false;
		}
		
		if(srcFile.getMimeType().equals("application/vnd.google-apps.folder")) {
			if(copyFile(srcFile, dest))
				System.out.println("Successfully copied!");
			else
				System.out.println("NJET Unuccessfully NOOOO!!");
		}
		
		else if(srcFile.getMimeType().equals("application/vnd.google-apps.document")) {
			
			try {
				createFile(dest + java.io.File.separator + srcFile.getName());
			} catch (StorageSizeException | NamingPolicyException | UnsupportedFileException | StorageConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			
//			 File copy = new File();
//			 copy.setParents(Collections.singletonList(destFile.getId()));
//			 copy.setName(srcFile.getName());
//			 
//			 try {
//				service.files().copy(destFile.getId(), copy).execute();
//			} catch (IOException e) {				
//				e.printStackTrace();
//			}
		}
		
		return true;

	}
	
	private boolean copyFile(File srcFile, String dest) {
				
		List<File> toBeCopied = getFilesByParentId(srcFile.getId());
		File destFile = getLastFileOnPath(Paths.get(dest), StorageManager.getInstance().getStorageInformation().getStorageDirectoryID());
		
		for(File f : toBeCopied) {
			
			if(f.getMimeType().equals("application/vnd.google-apps.folder")) {
				try {
					createDirectory(dest + java.io.File.separator + f.getName());
					copyFile(f, dest + java.io.File.separator + f.getName());
				} catch (StorageSizeException | NamingPolicyException | DirectoryException	| StorageConnectionException e) {
					e.printStackTrace();
				}
			}
			else if(f.getMimeType().equals("application/vnd.google-apps.document")) {
				try {
					createFile(dest + java.io.File.separator + srcFile.getName());
				} catch (StorageSizeException | NamingPolicyException | UnsupportedFileException | StorageConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}								 				
			}
		}
		
		return true;
	}

	@Override
	public boolean writeToFile(String filePath, String text, boolean append)
			throws NotFound, StorageSizeException, StorageConnectionException {

		
		return true;
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
	
	private List<File> getFilesByParentId(String parentId){
		
		List<File> files = new ArrayList<File>();		
	    String pageToken = null;
	    
		try {			
		    do {
		      FileList result = service.files().list()
		    	  .setQ("'" + parentId + "'" + " in parents")
		          .setSpaces("drive")
		          .setFields("nextPageToken, files(id,size,mimeType,parents,name,trashed)")
		          .setPageToken(pageToken)
		          .execute();
		      for (File file : result.getFiles()) {
		        System.out.printf("Found file: %s (%s)\n",
		            file.getName(), file.getId());
		      }
	
		      files.addAll(result.getFiles());
	
		      pageToken = result.getNextPageToken();
		    } while (pageToken != null);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		    		    
		return files;		
	}
	
	// path uvek treba biti oblika <storageName>\"rootDataDirectory"\...\...
	// pocetna vrednost za parentID ce uvek treba biti ID od storge direktorijuma 
	private File getLastFileOnPath(Path path, String parentID) {
		
		Iterator<Path> iterator = path.iterator();
		
		if(!iterator.hasNext())
			return null;		
		iterator.next();
		
		String nextDirName = null;
		File ans = null;
		
		while(iterator.hasNext()) {
			
			nextDirName = iterator.next().toString();
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
			    	  return null;
			      
			      for (File file : result.getFiles()) {
			        if(file.getName().equals(nextDirName)) {			        	
			        	parentID = file.getId();
			        	ans = file;
			        	break;
			        }			        			        
			      }
				  			        	  			      
			      pageToken = result.getNextPageToken();
			      
			    } while (pageToken != null);
		    	
		      // ako na putanji nismo uspeli da nadjemo file sa trazenim imenom
		      if(ans == null)
		    	  return null;			      
		      // ako smo stigli do kraja tokena, a nismo azurirali odgovor
		      if(ans != null && !ans.getName().equals(nextDirName))
		    	  return null;			  		    	
			    
		    }catch(IOException e) {
		    	e.printStackTrace();
		    }
			
		}
		
		return ans;
	}

}
