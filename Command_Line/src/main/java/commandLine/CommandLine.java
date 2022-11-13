package commandLine;

import java.nio.file.Paths; 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import fileMetadata.FileMetadata;
import specification.Storage;
import storageManager.StorageManager;

public class CommandLine {
	
	private static Scanner scanner;

	public static void main(String[] args) {
		
		
			try {
				Class.forName("localStorage.LocalStorageImplementation");
				//Class.forName("googleDriveStorage.GoogleDriveStorage");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Storage storage = StorageManager.getStorage();
			List<Map<String, List<FileMetadata>>> resultSet = new ArrayList<>();
			
			scanner = new Scanner(System.in);
	        String command = "";	        	        
	   	       
	        System.out.println("Type 'help' when help is needed.\n");
	        
	        help();
	        
	        while(true) {
	        	
	        	command = scanner.nextLine();	        	
	            String[] commArray = command.split(" ");
	            
	            try {
	            	
	            	if(commArray.length == 2 && commArray[0].equals("mkstrg")) {
	            		
	            		String dest = commArray[1];
	            		
            			System.out.println("To manually configure storage press 'Y'");
            			
            			Long size = null;
        				Set<String> unsupportedFiles = null;
            			command = scanner.nextLine();
            			
            			if(command.equals("Y")) {
            				
            				unsupportedFiles = new HashSet<>();
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
            				
            				System.out.println("Enter extensions which are unsupporetable in storage. For example: .ext1 .ext2 .ext3 ");            				
            				
            				command = scanner.nextLine();
            				String[] exts = command.split(" ");
            				for(String s : exts)
            					unsupportedFiles.add(s);            				            				            				
            			}
            			if(storage.createStorage(dest)) {
            				if(size != null && unsupportedFiles != null)
            					storage.setStorageConfiguration(size, unsupportedFiles);
            				
            				System.out.println("Storage created successfully!");
            			}
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("con")) {
	            		String src = commArray[1];
	            		if(storage.connectToStorage(src))
	            			System.out.println("Connected to storage successfully!");
	            	}
	            	else if(commArray.length == 1 && commArray[0].equals("discon")) {
	            		if(storage.disconnectFromStorage())
	            			System.out.println("Successfully disconnected from the storage!");
	            	}
	            	else if((commArray.length == 2 || commArray.length == 3) && commArray[0].equals("mkdir")) {
	            		String dest = commArray[1];
	            		if(commArray.length == 2) {
	            			if(!storage.createDirectory(dest))
	            				System.out.println("Something went wrong!");
	            		}
	            		
	            		else {
	            			Integer filesLimit = null;
	            			try {
	            				filesLimit = Integer.valueOf(commArray[1]);
	            			} catch (NumberFormatException e) {
	            				System.out.println(String.format("Because of NumberFormatException, the folder '%s' has been assigned default file limit value! ", Paths.get(dest).getFileName()));
	            				filesLimit = 20;
							}
	            			if(!storage.createDirectory(dest, filesLimit))
	            				System.out.println("Something went wrong!");
	            		}
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("mkdirs")) {
	            		String dest = commArray[1];
	            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
	            		Map<String, Integer> dirNameAndFilesLimit =	new HashMap<>();
	            		// <dirname1>,<num1> <dirname2>,<num2> <dirname3>,<num3> 
	            		for(int i=0 ; i<subArr.length ; i++) {
	            			
	            			String name = null;
	            			Integer filesLimit = null;
	            			
	            			if(subArr[i].contains(",")) {
	            				name = subArr[i].split(",")[0].trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "");
		            			filesLimit = null;
		            			try {
		            				filesLimit = Integer.valueOf(subArr[i].split(",")[1].trim());
		            			} catch (NumberFormatException e) {
		            				System.out.println(String.format("Because of NumberFormatException, the folder '%s' is not given the limit number for the files which can hold! ", name));		            			
								}
	            			}
	            			else
	            				name = subArr[i].split(" ")[0].trim().replaceAll("[^a-zA-Z0-9\\.\\-]", "");
	            			
	            			dirNameAndFilesLimit.put(name, filesLimit);	            		
	            		}
	            		
	            		if(!storage.createDirectories(dest, dirNameAndFilesLimit))
	            			System.out.println("Something went wrong!");
	            		            	
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("mkfile")) {
	            		String dest = commArray[1];
	            		if(!storage.createFile(dest))
	            			System.out.println("Something went wrong!");
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("mkfiles")) {
	            		String dest = commArray[1];
	            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
	            		List<String> names = new ArrayList<>();
	            		
	            		for(String name : subArr)
	            			names.add( name.replaceAll("[^a-zA-Z0-9\\.\\-]", "") );	            	
	            		
	            		if(!storage.createFiles(dest, names))
	            			System.out.println("Something went wrong!");
	            	}
	            	else if(commArray.length == 3 && commArray[0].equals("move")) {
	            		String filePath = commArray[1];
	            		String newDest = commArray[2];
	            		if(!storage.move(filePath, newDest))
	            			System.out.println("Something went wrong!");
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("del")) {
	            		String filePath = commArray[1];
	            		if(!storage.remove(filePath))
	            			System.out.println("Something went wrong!");
	            	}
	            	else if(commArray.length == 3 && commArray[0].equals("rename")) {
	            		String filePath = commArray[1];
	            		String newName = commArray[2];
	            		if(!storage.rename(filePath, newName))
	            			System.out.println("Something went wrong!");
	            	}
	            	else if(commArray.length == 3 && commArray[0].equals("download")) {
	            		String filePath = commArray[1];
	            		String downloadDest = commArray[2];
	            		storage.download(filePath,downloadDest);
	            	}
	            	else if(commArray.length == 3 && commArray[0].equals("copy")) {
	            		String filePath = commArray[1];
	            		String dest = commArray[2];
	            		if(!storage.copyFile(filePath, dest))
	            			System.out.println("Something went wrong!");
	            	}
	            	else if( commArray.length > 2 && commArray[0].equals("write")) {
	            		String filePath = commArray[1];
	            		String text = null;
	            		boolean append = (commArray[commArray.length-1].equals("true")) ? true : false;
	            		
	            		for(int i=1 ; i < commArray.length - (commArray[commArray.length-1].equals("true") ? 1 : 0) ; i++) {
	            			text += commArray[i];
	            			
	            			if(i - 1 != commArray.length - (commArray[commArray.length-1].equals("true") ? 1 : 0))
	            				text += " ";
	            		}
	            		
	            		if(!storage.writeToFile(filePath,text,append))
	            			System.out.println("Something went wrong!");
	            	}
	            	else if(commArray.length == 1 && commArray[0].equals("cd..")) {	          
	            		String dest = commArray[0];
	            		storage.changeDirectory(dest);
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("cd")) {	          
	            		String dest = commArray[1];
	            		storage.changeDirectory(dest);
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("hit")) {	          
	            		String filePath = commArray[1];
	            		
	            		if(storage.find(filePath)) 
	            			System.out.println("TRUE");
	            		else
	            			System.out.println("FALSE");
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("hit") && commArray[1].equals("-l")) {
	            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
	            		List<String> list = new ArrayList<>();
	            		
	            		for(String s : subArr)
	            			list.add(s);
	            		
	            		Map<String, Boolean> map = storage.find(list);
	            		for(String s : map.keySet()) 
	            			System.out.println(s + " : " + map.get(s));	            		
	            	}
	            	else if(commArray.length == 2 && commArray[0].equals("dest")) {	          
	            		String filePath = commArray[1];
	            		List<String> list = storage.findDestinantions(filePath);
	            		for(String s : list)
	            			System.out.println(s);
	            	}
	            	else if(commArray.length > 1 && commArray[0].equals("ls")) {	          
	            		String src = commArray[1];

	            		if(commArray.length == 2) {
	            			resultSet = storage.listDirectory(src, false, false, false, null, null, null, null);	            			
	            		}
	            		else {
		            		boolean onlyDirs=false, onlyFiles=false, searchSubDirecories=false;
		            		String ext=null, pref=null, suf=null, sub=null;
	
		            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
		            		
		            		for(int i=0 ; i<subArr.length ; i++) {
		            			
		            			if(subArr[i].contains("-d")) onlyDirs = true;
		            			else if(subArr[i].contains("-f")) onlyFiles = true;
		            			else if(subArr[i].contains("-ssd")) searchSubDirecories = true;
		            			else if(subArr[i].contains("-ex:")) ext = subArr[i].split("-ex:")[1];
		            			else if(subArr[i].contains("-p:")) pref = subArr[i].split("-p:")[1];
		            			else if(subArr[i].contains("-s:")) suf = subArr[i].split("-s:")[1];
		            			else if(subArr[i].contains("-sw:")) sub = subArr[i].split("-sw:")[1];
		            			
		            		}		
		            		
		            		resultSet = storage.listDirectory(src, onlyDirs, onlyFiles, searchSubDirecories, ext, pref, suf, sub);
	            		}	            		
	            		
	            		printResult(resultSet);
	            		
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("rez") && commArray[1].equals("-sort")) {	          
	            		if(commArray.length==2){
	            			resultSet = storage.resultSort(resultSet, true, false, false, true, false);
	            		}
	            		else {
	            			
	            			boolean byName=false, byCreation=false, byModification=false, asc=true, desc=false;	            			
	            			String[] subArr = Arrays.copyOfRange(commArray, 3, commArray.length);
	            			
	            			for(int i=0 ; i<subArr.length ; i++) {
	            				if(subArr[i].contains("-n")) byName = true;
	            				else if(subArr[i].contains("-ct")) byCreation = true;
	            				else if(subArr[i].contains("-mt")) byModification = true;
	            				else if(subArr[i].contains("-asc")) asc = true;
	            				else if(subArr[i].contains("-desc")) desc = true;
	            			}
	            			
	            			resultSet = storage.resultSort(resultSet, byName, byCreation, byModification, asc, desc);
	            		}
	            		
	            		printResult(resultSet);
	            		
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("rez") && commArray[1].equals("-fil")) {	          
	            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
	            		boolean[] attributes = new boolean[10];
	            		Arrays.fill(attributes, Boolean.FALSE);
	            		Date[][] periods = new Date[2][2];
	            		SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
	            		
	            		for(int i=0 ; i<subArr.length ; i++) {
	            			// -tc-p dd-M-yyyy hh:mm:ss|dd-M-yyyy hh:mm:ss -tm-p dd-M-yyyy hh:mm:ss|dd-M-yyyy hh:mm:ss
	            			if(subArr[i].contains("-tc-p")) {	            				
	            				String subStr = command.substring(command.indexOf("-tc-p"));
	            				
	            				String start = subStr.substring(5, subStr.indexOf("|"));	            	            				
	            				String end = subStr.substring(subStr.indexOf("|") + 1, Math.min((subStr.indexOf("-") == -1) ? Integer.MAX_VALUE : subStr.indexOf("-"), subStr.length()) );
	            				
	            				periods[0][0] = formatter.parse(start);
	            				periods[0][1] = formatter.parse(end);	            				
	            				continue;
	            			}
	            			else if(subArr[i].contains("-tm-p")) {	            				
	            				String subStr = command.substring(command.indexOf("-tm-p"));
	            				
	            				String start = subStr.substring(5, subStr.indexOf("|"));	            	            				
	            				String end = subStr.substring(subStr.indexOf("|") + 1, Math.min((subStr.indexOf("-") == -1) ? Integer.MAX_VALUE : subStr.indexOf("-"), subStr.length()) );
	            				
	            				periods[1][0] = formatter.parse(start);
	            				periods[1][1] = formatter.parse(end);	            				
	            				continue;
	            			}
	            			else if(subArr[i].contains("-id")) attributes[0] = true;
	            			else if(subArr[i].contains("-n")) attributes[1] = true;
	            			else if(subArr[i].contains("-rp")) attributes[2] = true;
	            			else if(subArr[i].contains("-ap")) attributes[3] = true;
	            			else if(subArr[i].contains("-tc")) attributes[4] = true;
	            			else if(subArr[i].contains("-tm")) attributes[5] = true;
	            			else if(subArr[i].contains("-f")) attributes[6] = true;
	            			else if(subArr[i].contains("-d")) attributes[7] = true;
	            			
	            		}            		
	            		
	            		List<Map<String, List<FileMetadata>>> resultClone = storage.filterAttributes(resultSet, attributes, periods);
	            		
	            		for(int depth = 0 ; depth < resultClone.size() ; depth++) {
	            			Map<String, List<FileMetadata>> map =  resultClone.get(depth);
	            			
	                		for(String relativePath : map.keySet()) {
	                			
	                			if(map.get(relativePath).isEmpty())
	                				continue;
	                			
	                			System.out.println(relativePath);
	            				
	                			for(FileMetadata ff : map.get(relativePath))
	            					System.out.println("-"+ff.toString());
	            	    	}   
	            		}
	            	}
	            	else if((commArray.length == 3 || commArray.length == 4) && commArray[0].equals("write")) {	          
	            		String filePath = commArray[1];
	            		String text = commArray[2];
	            		boolean append = false;
	            		
	            		if(commArray.length == 4 && commArray[3].equals("true"))
	            			append = true;
	            		
	            		if(!storage.writeToFile(filePath, text, append))
	            			System.out.println("Something went wrong!");
	            		
	            	}
	   	            	
	            }catch (Exception e) {
	            	System.out.println("Type 'help' for more information.");
	            	e.printStackTrace();          		            
	            }
	            	        		        	
	            if (command.equals("exit")) {
	                return;
	            }
	        	
	            if (command.equals("help")) {
					help();	
		            continue;
		        }
		    	
				else if (command.equals("?mkstrg")) {
		    		System.out.println("Creates storage.");
		    		System.out.println("syntax: 'mkstrg <absolute_path>'.");
		    		System.out.println("Creation of the storage is not possible if:");
		    		System.out.println("a) some storage already exist on the given path.");
		    		System.out.println("b) some storage is already connected. (command 'discon' will solve the problem!)");
		    		System.out.println("ATTENTION: To manually configure storage press 'Y' after command mkstrg is called.");
		    		System.out.println("Storage configuration implies setting the size (in bytes) which storage can hold and setting extensions which storage won't support.");	
		            continue;
		        }
				
		    	else if (command.equals("?con")) {
		    		System.out.println("Connects to the existing storage.");
		    		System.out.println("syntax: 'con <absolute_path_to_the_storage>'.");
		    		System.out.println("Connection to the storage is not possigle if:");
		    		System.out.println("a) some storage is already connected. (command 'discon' will solve the problem!)");
		    		System.out.println("Examples:");
		    		System.out.println("1. con C:\\Users\\Luka\\Desktop\\<storage_name>");
		            continue;
		        }
		    	
		    	else if (command.equals("?discon")) {
		    		System.out.println("Disconnects from the storage.");
		    		System.out.println("syntax: 'discon'.");		    		
		    		System.out.println("Examples:");
		    		System.out.println("1. discon");
		            continue;
		        }
		    	
		    	else if (command.equals("?mkdir")) {
		    		System.out.println("Creates a directory.");
		    		System.out.println("syntax: 'mkdir <absolute_path|relative_path> optinal:<capacity>'.");
		    		System.out.println("Keep in mind that the last name on the path will denotes the name of the folder you are creating!");
		    		System.out.println("If the name of the folder already exists in the parent directory, the software will automatically concatenate some number on the name to make it unique.");
		    		System.out.println("All folders along the path must exist.");
		    		System.out.println("Examples:");
		    		System.out.println("1. mkdir C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2");
		    		System.out.println("2. mkdir C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2 75");
		    		System.out.println("3. mkdir folder1\\folder2");
		    		System.out.println("4. mkdir folder1\\folder2 75");
		            continue;
		        }
		    	
		    	else if (command.equals("?mkdirs")) {
		    		System.out.println("Creates a list of directories.");
		    		System.out.println("syntax: 'mkdirs <absolute|relative_path> <dirname1>,<num1> <dirname2>,<num2> <dirname3>,<num3>'.");	    	
		    		System.out.println("If the some of numbers are not specified or are not in the correct format, the corresponding folder won't be given any limitation in terms of files which can hold!");
		    		System.out.println("If the some of folder names already exists in the parent directory, the software will automatically concatenate some number on them to make them unique.");
		    		System.out.println("All folders along the path must exist.");
		    		System.out.println("Examples:");
		    		System.out.println("1. mkdirs C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2 folder1,20 folder2,14 folder3,79");
		    		System.out.println("2. mkdirs folder1\\folder2 folder1,20 folder2,14 folder3,79");
		            continue;
		        }
		
		    	else if (command.equals("?mkfile")) {
		    		System.out.println("Creates a file.");
		    		System.out.println("syntax: 'mkfile <absolute|relative_path>'.");
		    		System.out.println("Keep in mind that the last name on the path will denotes the name of the folder you are creating!");
		    		System.out.println("If the name of the folder already exists in the parent directory, the software will automatically concatenate some number on the name to make it unique.");
		    		System.out.println("All folders along the path must exist.");
		    		System.out.println("Examples:");
		    		System.out.println("1. mkfile C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3\\file1");
		    		System.out.println("2. mkfile folder2\\folder3\\file1");
		            continue;
		        }
		    	else if (command.equals("?mkfiles")) {
		    		System.out.println("Creates a list of files.");
		    		System.out.println("syntax: 'mkfile <absolute|relative_path> <name1> <name2> <name2>'.");
		    		System.out.println("If the some of files names already exists in the parent directory, the software will automatically concatenate some number on them to make them unique.");
		    		System.out.println("All folders along the path must exist.");
		    		System.out.println("Examples:");
		    		System.out.println("1. mkfile C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 file1 file2 file3 file4");
		    		System.out.println("2. mkfile folder2\\folder3 file1 file2 file3 file4");
		            continue;
		        }
		    	
		    	else if (command.equals("?move")) {
		    		System.out.println("Moves a file or folder.");
		    		System.out.println("syntax: 'move <absolute|relative_path_of_the_target_file|folder> <absolute_path|relative_path of_the_destination>'.");
		    		System.out.println("If the name of the file|folder already exists in the parent directory, the software will automatically concatenate some number on the name to make it unique.");
		    		System.out.println("All folders along the path must exist.");
		    		System.out.println("Examples:");
		    		System.out.println("1. move C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder4");
		    		System.out.println("2. move C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3  folder1\\folder4");
		    		System.out.println("3. move folder1\\folder2\\folder3  folder1\\folder4");
		    		continue;
		        }
		    	
		    	else if (command.equals("?del")) {
		    		System.out.println("Deletes file or folder.");
		    		System.out.println("syntax: 'del <absolute|relative_path_of_the_target_file|folder>'.");
		    		System.out.println("All folders along the path must exist.");
		    		System.out.println("Examples:");
		    		System.out.println("1. del C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3");
		    		System.out.println("2. del folder1\\folder2\\folder3");		
		            continue;
		        }
		    	
		    	else if (command.equals("?rename")) {
		    		System.out.println("Renames file or folder.");
		    		System.out.println("syntax: 'rename <absolute|relative_path_of_the_target_file|folder>' <new_name>.");
		    		System.out.println("If the new name of the file|folder already exists in the parent directory, the software will automatically concatenate some number on the name to make it unique.");
		    		System.out.println("Examples:");
		    		System.out.println("1. rename C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 folder123456789");
		    		System.out.println("2. rename folder1\\folder2\\folder3 folder123456789");	
		            continue;
		        }
		    	
		    	else if (command.equals("?download")) {
		    		System.out.println("Downloads a file or folder.");
		    		System.out.println("syntax: 'download <absolute|relative_path_of_the_target_file|folder> <absolute_path_for_download_destination>'.");
		    		System.out.println("Destination folder must be outside of the storage.");
		    		System.out.println("Examples:");
		    		System.out.println("1. download C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 C:\\Users\\Luka\\Desktop\\downloads");
		    		System.out.println("2. download folder1\\folder2\\folder3 C:\\Users\\Luka\\Desktop\\downloads");
		            continue;
		        }
		    	
		    	else if (command.equals("?copy")) {
		    		System.out.println("Copies file or folder.");
		    		System.out.println("syntax: 'copy <absolute|relative_path_of_the_target_file|folder> <absolute|relative_path_of_the_destination_folder>'.");
		    		System.out.println("Examples:");
		    		System.out.println("1. copy C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder123456789");
		    		System.out.println("2. copy folder1\\folder2\\folder3 folder1\\folder123456789");
		            continue;
		        }
		
		    	else if (command.equals("?write")) {
		    		System.out.println("Writes data to a target file.");
		    		System.out.println("syntax: 'write <absolute|relative_path_of_the_target_file> <text> optional:<append:boolean>'.");
		    		System.out.println("If append is set to true, then text is appened on the existing text in the target file.");
		    		System.out.println("To set append to true it's necessery that the last word in the <text> is 'true'.");
		    		System.out.println("Examples:");
		    		System.out.println("1. write C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\file1 Upisuje se neki tekst u fajl. Broj recenica nije ogranicen");
		    		System.out.println("2. write C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\file1 Upisuje se neki tekst u fajl. Broj recenica nije ogranicen true");
		            continue;
		        }
		    	else if (command.equals("?cd")) {
		    		System.out.println("Changes current directory.");
		    		System.out.println("syntax: 'cd <absolute|relative_path_of_the_target_folder>'.");
		    		System.out.println("syntax: 'cd..' ---> to go in the parent directory!");
		    		System.out.println("Examples:");
		    		System.out.println("1. cd C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3");
		    		System.out.println("2. cd folder1\\folder2\\folder3");
		    		System.out.println("3. cd..");
		            continue;
		        }
		    	
		    	else if (command.equals("?hit")) {
		    		System.out.println("Returns whether path exist.");
		    		System.out.println("syntax: 'hit <absolute|relative_path_of_the_target_file|folder>'.");
		    		System.out.println("Examples:");
		    		System.out.println("1. hit C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3");
		    		System.out.println("2. hit folder1\\folder2\\folder3");
		    		System.out.println("3. hit folder3");
		            continue;
		        }
		    	
		    	else if (command.equals("?hit -l")) {
		    		System.out.println("Returns whether paths exists.");
		    		System.out.println("syntax: 'hit -l <list:absolute|relative_path_of_the_target_file|folder>'.");
		    		System.out.println("Examples:");
		    		System.out.println("1. hit -l C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder2");
		    		System.out.println("2. hit -l C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 folder2 folder3");
		    		System.out.println("3. hit -l folder3 folder2 folder2\\folder3");
		    		System.out.println("4. hit -l folder3 folder2 folder3");
		            continue;
		        }
		    	
		    	else if (command.equals("?dest")) {
		    		System.out.println("Returns all locatins that contains file|folder with a given name.");
		    		System.out.println("syntax: 'dest <file|folder_name>'.");
		    		System.out.println("Examples:");
		    		System.out.println("1. dest file123456789");
		            continue;
		        }
		
		    	else if (command.equals("?ls")) {
		    		System.out.println("Returns list of all items from the folder. (OPTINAL: additional requirements could be added in the request for searching engine)");
		    		System.out.println("syntax: 'ls <absolute|relative_path_of_the_target_file|folder>'.");
		    		System.out.println("Additional requirements:");
		    		System.out.println("-d -searches only for the directories.");
		    		System.out.println("-f -searches only for the files.");
		    		System.out.println("-ssd -searches continues in the sub-directories.");
		    		System.out.println("-ex:<ext> -files or folders with the specified extension.");
		    		System.out.println("-p:<prefix> -files or folders with the specified prefix.");
		    		System.out.println("-s:<sufix> -files or folders with the specified sufix.");
		    		System.out.println("-sw:<sub_word> -files or folders with the specified sub_word.");
		    		System.out.println("Examples:");
		    		System.out.println("2. ls C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3");
		    		System.out.println("2. ls C:\\Users\\Luka\\Desktop\\<storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 -d -ssb");
		    		System.out.println("3. ls <storage_name>\\dataRootDirectory\\folder1\\folder2\\folder3 -f -ssb -ex:exe");
		    		System.out.println("4. ls folder2\\folder3 -ssd -p:fold");
		            continue;
		        }
		    	else if (command.equals("?rez -sort")) {
		    		System.out.println("Sorts the result by the given parameters. If parametars are not provided then the default sort is applied (by name, ascending)");
		    		System.out.println("If some of parametars are in conflict, then the software handles it on it's way.");
		    		System.out.println("syntax: 'rez -sort'.");
		    		System.out.println("Parametars:");
		    		System.out.println("-n -sort by name.");
		    		System.out.println("-ct -sort by time creation.");
		    		System.out.println("-cm -sort by time modification.");
		    		System.out.println("-asc -ascending");
		    		System.out.println("-desc -descending");
		    		System.out.println("Examples:");
		    		System.out.println("1. rez -sort");
		    		System.out.println("2. rez -sort -n -desc");
		    		System.out.println("3. rez -sort -ct -asc");
		            continue;
		        }
				
		    	
		    	else if (command.equals("?rez -fil")) {
		    		System.out.println("Filters the result attributes which will appear on the screen.");
		    		System.out.println("Also it's possible to show only items which are created or modified in some period of time.");
		    		System.out.println("syntax: 'rez -fil param1 param2 param3 ...'");
		    		System.out.println("Parametars:");
		    		System.out.println("-tc-p dd-M-yyyy hh:mm:ss|dd-M-yyyy hh:mm:ss -only files|folder which are created between those two periods. ");//28/09/2013 09:57:19
		    		System.out.println("-tm-p dd-M-yyyy hh:mm:ss|dd-M-yyyy hh:mm:ss -only files|folder which are created between those two periods. ");
		    		System.out.println("-id file|folder -id will be included.");
		    		System.out.println("-n file|folder -name will be included.");
		    		System.out.println("-rp file|folder -relative path will be included.");
		    		System.out.println("-ap file|folder -absolute path will be included.");
		    		System.out.println("-tc file|folder -time creation will be included.");
		    		System.out.println("-tm file|folder -time modification will be included.");
		    		System.out.println("-f file|folder -whether file|folder is a file is included.");
		    		System.out.println("-d file|folder -whether file|folder is a folder is included.");
		    		System.out.println("Examples:");
		    		System.out.println("1. rez -fil -id -rp -d -tc");
		    		System.out.println("2. rez -fil -n -d -ap");
		    		System.out.println("3. rez -tc-p 28-09-2022 09:30:30|28-10-2022 15:00:00");
		            continue;
		        }	        	        		        
	        }
	       
	}
	
	static void printResult(List<Map<String, List<FileMetadata>>> result) {
		
		for(int depth = 0 ; depth < result.size() ; depth++) {
			Map<String, List<FileMetadata>> map =  result.get(depth);
			
    		for(String relativePath : map.keySet()) {
    			
    			if(map.get(relativePath).isEmpty())
    				continue;
    			
    			System.out.println(relativePath);
				
    			for(FileMetadata ff : map.get(relativePath))
					System.out.println("  " + ff.getName());
	    		}   
		}
	}
	
	static void help() {		
		System.out.println("Type '?<command_name>' to see command explanation and it's usage examples.");		
		System.out.println("You can use absolute and relative paths for storage operations. Relative paths are relative to the location of the current directory.");
		System.out.println("If you use dates make sure you are using this FORMAT: DD-M-YYYY HH:MM:SS, for other formats is not guaranteed that the software will work as expected.");
		System.out.println();
		System.out.println("COMMANDS:");
		System.out.println("mkstrg");
		System.out.println("con");
		System.out.println("discon");
		System.out.println("mkdir");
		System.out.println("mkdirs");
		System.out.println("mkfile");
		System.out.println("mkfiles");
		System.out.println("move");
		System.out.println("del");
		System.out.println("rename");
		System.out.println("download");
		System.out.println("copy");
		System.out.println("write");
		System.out.println("cd");
		System.out.println("hit");
		System.out.println("hit -l");
		System.out.println("des");
		System.out.println("ls");
		System.out.println("rez -sort");
		System.out.println("rez -fil");//rez -sort	
	}
}
