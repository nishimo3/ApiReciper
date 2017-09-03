package org.pairprogrammingai.apireciper.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderOperator {
	public static void readFolder(File dir, List<String> retfiles, String extension){
		File[] files = dir.listFiles();
		
		if(files == null){
			return ;
		}
		
		for(File file : files){
			if(file.exists() == false){
				continue;
			} else if(file.isDirectory()){
				readFolder(file, retfiles, extension);
			} else if(file.isFile()){
				if(extension.equals(getSuffix(file.getName()))){
					retfiles.add(file.getPath());
				}
			}
		}
	}
	
	public static void getLibrary(File dir, String version, List<String> ret){
		File[] files = dir.listFiles();
		
		if(files == null){
			return ;
		}
		
		for(File file : files){
			if(file.exists() == false){
				continue;
			} else if(file.isDirectory()){
				getLibrary(file, version, ret);
			} else if(file.isFile()){
				if("jar".equals(getSuffix(file.getName()))){
					/*
					String[] fs = file.getName().split("-");					
					for(int i = 0; i < fs.length; i++){
						if(fs[i].equals(version)){
							ret.add(file.getPath());
							break;
						}
					}
					*/
					if(file.getName().indexOf(version) != -1){
						ret.add(file.getPath());
					}
				}
			}
		}
	}
	
	public static List<File> readFolderInFolder(String folderPath){
		List<File> retfiles = new ArrayList<File>();
		File dir = new File(folderPath);		
		File[] files = dir.listFiles();
		
		if(files == null){
			return null;
		}
		
		for(File file : files){
			if(file.exists() == false){
				continue;
			} else if(file.isDirectory()){
				retfiles.add(file);
			}
		}
		return retfiles;
	}
	
	public static void createDirectory(String filepath){
		File newfile = new File(filepath);
		newfile.mkdir();
	}
		
	private static String getSuffix(String name){
		if(name != null){
			int dot = name.lastIndexOf(".");
			if(dot != -1){
				return name.substring(dot + 1);
			}
		}
		return null;
	}
	
	public static String getNotSuffix(String name){
		if(name != null){
			int dot = name.lastIndexOf(".");
			if(dot != -1){
				return name.substring(0, dot);
			}
		}
		return null;
	}
	
	public static String getClassNameFromFilePath(String filePath){
		String className = "";
		String[] sFilePath = filePath.split("/");
		if(sFilePath.length > 0){
			className = FolderOperator.getNotSuffix(sFilePath[sFilePath.length - 1]);
		}
		return className;
	}
}
