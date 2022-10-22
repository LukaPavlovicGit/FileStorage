package googleDriveStorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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
import exception.PathException;
import exception.StorageConnectionException;
import exception.StorageException;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import specification.Storage;
import storageManager.StorageManager;

public class GoogleDriveStorage extends Storage {
	
	private Drive service;
	

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
	
		@Override
	public boolean createStorage(String dest, StorageConfiguration storageConfiguration)
			throws StorageException, NamingPolicyException, PathException, StorageConnectionException {
		// TODO Auto-generated method stub
			
			try {
				service = getDriveService();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			File fileMetadata = new File();
			fileMetadata.setName(dest);
			fileMetadata.setMimeType("application/vnd.google-apps.folder");
			try {
				File storageFile = service.files().create(fileMetadata).setFields("id").execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return false;
	}

	@Override
	public boolean connectToStorage(String src) throws NotFound, StorageException, StorageConnectionException {
		
		return false;
	}

	@Override
	public boolean disconnectFromStorage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createDirectory(String dest, Integer... filesLimit)
			throws StorageSizeException, NamingPolicyException, DirectoryException, StorageConnectionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createFile(String dest)
			throws StorageSizeException, NamingPolicyException, UnsupportedFileException, StorageConnectionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void move(String filePath, String newDest) throws NotFound, DirectoryException, StorageConnectionException {
		// TODO Auto-generated method stub
		
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
	protected void saveToJSON(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void readFromJSON(Object obj, String path) {
		// TODO Auto-generated method stub
		
	}

}
