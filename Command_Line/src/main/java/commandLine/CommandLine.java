package commandLine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import configuration.StorageConfiguration;
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
			Map<String, List<FileMetadata>> resultSet = new HashMap<>();
			
			scanner = new Scanner(System.in);
	        String command = "";	        	        
	        
	        System.out.println("If you need some instructions, please type command help");
	        System.out.println("If you want to quit application, please type command exit");
	        
	        while(true) {
	        	
	        	command = scanner.nextLine();	        	
	            String[] commArray = command.split(" ");
	            
	            try {
	            	
	            	if(commArray.length == 2 && commArray[0].equals("mkstrg")) {
	            		
	            		String dest = commArray[1];
	            		
	            		System.out.println("Storage configuration guide...");
            			System.out.println("To skip this step and keep default configuration press 'n'.");
            			
            			Long size = null;
        				Set<String> unsupportedFiles = null;
            			command = scanner.nextLine();
            			
            			if(!command.equals("n")) {
            				
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
            				
            				System.out.println("Enter extensions which are unsupporetable in storage."
            						+ "Every extension should begin with '.' and separated by space.");
            				System.out.println("Example: .ext1 .ext2 .ext3");
            				
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
	            				System.out.println(String.format("Because of NumberFormatException, the folder '%s' has been assigned default file limit value! ", name));
	            				filesLimig = 20;
							}
	            			dirNameAndFilesLimit.put(name, filesLimig);	            		
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
	            	else if((commArray.length == 4 || commArray.length == 3 )&& commArray[0].equals("write")) {
	            		String filePath = commArray[1];
	            		String text = commArray[2];
	            		boolean append = (commArray.length==4 && commArray[3].equals("y")) ? true : false;
	            		if(!storage.writeToFile(filePath,text,append))
	            			System.out.println("Something went wrong!");
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
		            		boolean onlyDirs=false, onleFiles=false, searchSubDirecories=false;
		            		String ext=null, pref=null, suf=null, sub=null;
	
		            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
		            		
		            		for(int i=0 ; i<subArr.length ; i++) {
		            			
		            			if(subArr[i].contains("-d")) onlyDirs = true;
		            			else if(subArr[i].contains("-f")) onleFiles = true;
		            			else if(subArr[i].contains("-ssd")) searchSubDirecories = true;
		            			else if(subArr[i].contains("-ex:")) ext = subArr[i].split("-ex:")[1];
		            			else if(subArr[i].contains("-p:")) pref = subArr[i].split("-p:")[1];
		            			else if(subArr[i].contains("-s:")) suf = subArr[i].split("-s:")[1];
		            			else if(subArr[i].contains("-sw:")) sub = subArr[i].split("-sw:")[1];
		            			
		            		}		
		            		
		            		resultSet = storage.listDirectory(src, onlyDirs, onleFiles, searchSubDirecories, ext, pref, suf, sub);
	            		}	            		
	            		for(String relativePath : resultSet.keySet()) {
	            			System.out.println(relativePath + " :");
	            			for(FileMetadata ff : resultSet.get(relativePath))
	            				System.out.println("\t" + ff.getAbsolutePath());
	            			
	            			System.out.println();
	            		}	           
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("rez") && commArray[1].equals("-sort")) {	          
	            		if(commArray.length==2){
	            			resultSet = storage.resultSort(resultSet, true, false, false, true, false);
	            		}
	            		else {
	            			
	            			boolean byName=false, byCreation=false, byModification=false, asc=false, desc=false;	            			
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
	            		
	            		for(String relativePath : resultSet.keySet()) {
	            			System.out.println(relativePath + " :");
	            			for(FileMetadata ff : resultSet.get(relativePath))
	            				System.out.println("\t" + ff.getAbsolutePath());
	            			
	            			System.out.println();
	            		}	   
	            	}
	            	else if(commArray.length > 2 && commArray[0].equals("rez") && commArray[1].equals("-fil")) {	          
	            		String[] subArr = Arrays.copyOfRange(commArray, 2, commArray.length);
	            		boolean[] attributes = new boolean[7];
	            		Arrays.fill(attributes, Boolean.FALSE);
	            		Date[][] periods = new Date[2][2];
	            		SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
	            		
	            		for(int i=0 ; i<subArr.length ; i++) {
	            			// -tc-p dd-M-yyyy hh:mm:ss|dd-M-yyyy hh:mm:ss -tm-p dd-M-yyyy hh:mm:ss|dd-M-yyyy hh:mm:ss
	            			if(subArr[i].contains("-tc-p")) {	            				
	            				String subStr = command.substring(command.indexOf("-tc-p"));
	            				
	            				String start = subStr.substring(5, subStr.indexOf("|"));	            	            				
	            				String end = subStr.substring(subStr.indexOf("|") + 1, 
	            						Math.min((subStr.indexOf("-") == -1) ? Integer.MAX_VALUE : subStr.indexOf("-"), subStr.length()) );
	            				
	            				periods[0][0] = formatter.parse(start);
	            				periods[0][1] = formatter.parse(end);	            				
	            				continue;
	            			}
	            			else if(subArr[i].contains("-tm-p")) {	            				
	            				String subStr = command.substring(command.indexOf("-tm-p"));
	            				
	            				String start = subStr.substring(5, subStr.indexOf("|"));	            	            				
	            				String end = subStr.substring(subStr.indexOf("|") + 1, 
	            						Math.min((subStr.indexOf("-") == -1) ? Integer.MAX_VALUE : subStr.indexOf("-"), subStr.length()) );
	            				
	            				periods[1][0] = formatter.parse(start);
	            				periods[1][1] = formatter.parse(end);	            				
	            				continue;
	            			}
	            			else if(subArr[i].contains("-id")) attributes[0] = true;
	            			else if(subArr[i].contains("-n")) attributes[1] = true;
	            			else if(subArr[i].contains("-path")) attributes[2] = true;
	            			else if(subArr[i].contains("-tc")) attributes[3] = true;
	            			else if(subArr[i].contains("-tm")) attributes[4] = true;
	            			else if(subArr[i].contains("-f")) attributes[5] = true;
	            			else if(subArr[i].contains("-d")) attributes[6] = true;
	            			
	            		}	            		
	            		
	            		resultSet = storage.resultFilter(resultSet, attributes, periods);
	            		for(String relativePath : resultSet.keySet()) {
	            			System.out.println(relativePath + ":");
	            			for(FileMetadata ff : resultSet.get(relativePath))
	            				System.out.println("\t" + ff.getAbsolutePath());
	            			
	            			System.out.println();
	            		}
	            	}
	   	            	
	            }catch (Exception e) {
	            	System.out.println(e.getMessage());	            		            
	            }
	            	        		        	
	            if (command.equals("exit")) {
	                return;
	            }
	        	
	        	else if (command.equals("help")) {
	               
	                continue;
	            }
	        	
	        	else if (command.equals("?mkstrg")) {
	        		
	        		System.out.println("Command examples:");
	        		System.out.println("Command examples:");
	        		
	                continue;
	            }
	        	
	        	else if (command.equals("?con")) {
	              
	                continue;
	            }
	        	
	        	else if (command.equals("?discon")) {
	                
	                continue;
	            }
	        	
	        	else if (command.equals("?mkdir")) {
	                
	                continue;
	            }
	        	
	        	else if (command.equals("?mkdirs")) {
	                
	                continue;
	            }

	        	else if (command.equals("?mkfile")) {
	               
	                continue;
	            }
	        	else if (command.equals("?mkfiles")) {
	               
	                continue;
	            }
	        	
	        	else if (command.equals("?move")) {
	        		
	        		continue;
	            }
	        	
	        	else if (command.equals("?del")) {
	                
	                continue;
	            }
	        	
	        	else if (command.equals("?")) {
	               
	                continue;
	            }
	        	
	        	else if (command.equals("?download")) {
	               
	                continue;
	            }
	        	
	        	else if (command.equals("?copy")) {
	               
	                continue;
	            }

	        	else if (command.equals("?write")) {
	                
	                continue;
	            }
	        	else if (command.equals("?cd")) {
	                
	                continue;
	            }
	        	
	        	else if (command.equals("?hit")) {
	                
	                continue;
	            }
	        	
	        	else if (command.equals("?hit -l")) {
	              
	                continue;
	            }
	        	
	        	else if (command.equals("?dest")) {
	               
	                continue;
	            }

	        	else if (command.equals("?ls")) {
	                
	                continue;
	            }
	        	else if (command.equals("?sort")) {
	                
	                continue;
	            }
	        	
	        	else if (command.equals("?fil")) {
	               
	                continue;
	            }
	        	
	        	else if (command.equals("?dest")) {
	                
	                continue;
	            }

	        	else if (command.equals("commands")) {
	                listCommands();
	                continue;
	            }	        	        	
	            
	            System.out.print(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getRelativePath() + ">");
	        }
	}

	private static void listCommands() {
		// TODO Auto-generated method stub
		
	}
}
