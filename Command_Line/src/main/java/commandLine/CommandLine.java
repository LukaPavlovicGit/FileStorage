package commandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import configuration.StorageConfiguration;
import specification.Storage;
import storageManager.StorageManager;

public class CommandLine {
	
	private static Scanner scanner;

	public static void main(String[] args) {
		
		
			try {
				Class.forName("localStorage.LocalStorageImplementation");
				// Class.forName("localStorage.LocalStorageImplementation");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Storage storage = StorageManager.getStorage();
			scanner = new Scanner(System.in);
	        String command = "";
        
	        System.out.println("If you need some instructions, please type command help");
	        System.out.println("If you want to quit application, please type command exit");
	        
	        
	        
	        while(true) {
	        	command = scanner.nextLine();
	        	
	        	if (command.equals("exit")) {
	                return;
	            }

	            if (command.equals("help")) {
	                help();
	                continue;
	            }
	        	
	            String[] commArray = command.split(" ");
	            try {
	            	
	            	if(commArray.length == 2 && commArray[0].equals("mkstrg")) {
	            		
	            		String dest = commArray[1];
	            		
	            		System.out.println("Storage configuration guide...");
            			System.out.println("To skip this step and keep default configuration press 'n'.");
            			
            			StorageConfiguration configuration = new StorageConfiguration();
            			command = scanner.nextLine();
            			
            			if(!command.equals("n")) {
            				
            				Long size = null;
            				Set<String> set = new HashSet<>();
            				
            				boolean correct = true;
            				do {
            					System.out.println("Enter storage size in bytes...");
            					command = scanner.nextLine();
            					System.out.println(command);
            					
            					for(int i=0 ; i<command.length() ; i++) {
            						if(Character.isDigit(command.charAt(i)) == false) {
            							correct = false;
            							break;
            						}
            					}
            					if(correct)
            						size = Long.valueOf(command);
            					
            				} while (!correct);
            				
            				System.out.println("Enter extensions which are unsupporetable in storage."
            						+ "Every extension should begin with '.' and separated by space.");
            				System.out.println("Example: .ext1 .ext2 .ext3");
            				
            				command = scanner.nextLine();
            				String[] exts = command.split(" ");
            				for(String s : exts)
            					set.add(s);
            				
            				configuration.setStorageSize(size);
            				configuration.setUnsupportedFiles(set);
            				
            			}
            			if(storage.createStorage(dest, configuration))
            				System.out.println("Storage created successfully!");
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("con")) {
	            		String src = commArray[1];
	            		if(storage.connectToStorage(src))
	            			System.out.println("Connected to storage successfully!");
	            	}
	            	else if(commArray.length == 1 && commArray[0].equals("discon")) {
	            		if(storage.disconnectFromStorage())
	            			System.out.println("Disconnected from storage successfully!");
	            	}
	            	else if((commArray.length == 2 || commArray.length == 3) && commArray[0].equals("mkdir")) {
	            		String dest = commArray[1];
	            		if(commArray.length == 2) {
	            			storage.createDirectory(dest);
	            		}
	            		
	            		else {
	            			Integer filesLimit = null;
	            			try {
	            				filesLimit = Integer.valueOf(commArray[1]);
	            			} catch (NumberFormatException e) {
								System.out.println(e.getMessage());
								continue;
							}
	            			if(storage.connectToStorage(commArray[1]))
		            			System.out.println("Connected to storage successfully!");
	            			
	            			storage.createDirectory(dest, filesLimit);
	            		}
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("mkdirs")) {
	            		String dest = commArray[1];
	            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
	            		Map<String, Integer> dirNameAndFilesLimit =	new HashMap<>();
	            		
	            		for(int i=0 ; i<subArr.length ; i++) {
	            			if(!subArr[i].contains(",")) {
	            				System.out.println("Command is not recognized...");
	            				System.out.println("Type '?mkdirs' to se corrent syntax.");
	            				continue;
	            			}
	            			
	            			String name = subArr[i].split(",")[0].trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "");
	            			Integer filesLimig = null;
	            			try {
	            				filesLimig = Integer.valueOf(subArr[i].split(",")[1].trim());
	            			} catch (NumberFormatException e) {
	            				System.out.println(e.getMessage());
	            				continue;
							}
	            			dirNameAndFilesLimit.put(name, filesLimig);	            		
	            		}	            		
	            		storage.createDirectories(dest, dirNameAndFilesLimit);	
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("mkfile")) {
	            		String dest = commArray[1];
	            		storage.createFile(dest);
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("mkfiles")) {
	            		String dest = commArray[1];
	            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
	            		List<String> names = new ArrayList<>();
	            		
	            		for(String name : subArr)
	            			names.add( name.replaceAll("[^a-zA-Z0-9\\.\\-]", "") );	            	
	            		
	            		storage.createFiles(dest, names);
	            	}
	            	else if(commArray.length == 3 && commArray[0].equals("move")) {
	            		String filePath = commArray[1];
	            		String newDest = commArray[1];
	            		storage.move(filePath, newDest);
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("del")) {
	            		String filePath = commArray[1];
	            		storage.remove(filePath);
	            	}
	            	else if(commArray.length == 3 && commArray[0].equals("download")) {
	            		String filePath = commArray[1];
	            		String downloadDest = commArray[2];
	            		storage.download(filePath,downloadDest);
	            	}
	            	else if(commArray.length == 3 && commArray[0].equals("copy")) {
	            		String filePath = commArray[1];
	            		String dest = commArray[1];
	            		storage.copyFile(filePath, dest);
	            	}
	            	else if((commArray.length == 4 || commArray.length == 3 )&& commArray[0].equals("write")) {
	            		String filePath = commArray[1];
	            		String text = commArray[2];
	            		boolean append = (commArray.length==4 && commArray[3].equals("y")) ? true : false;
	            		storage.writeToFile(filePath,text,append);
	            	}
	            	
	            }catch (Exception e) {
	            	System.out.println(e.getMessage());
	            }
	        }
	}

	private static void help() {
		// TODO Auto-generated method stub
		
	}
}
