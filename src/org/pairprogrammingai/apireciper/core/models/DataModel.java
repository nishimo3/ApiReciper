package org.pairprogrammingai.apireciper.core.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pairprogrammingai.apireciper.core.data.SequenceInfo;
import org.pairprogrammingai.apireciper.core.utils.FolderOperator;

public class DataModel {
	public List<SequenceInfo> models;
	
	public DataModel(){
		models = new ArrayList<SequenceInfo>();
	}
	
	public void store(String filepath){
		store(models, filepath, false);
	}
	
	public void storeWithTag(String filepath){
		store(models, filepath, true);
	}
	
	public void load(String filepath){
		load(models, filepath);
	}
	
	public void loads(String filepath){
		List<String> fileLists = new ArrayList<String>();
		FolderOperator.readFolder(new File(filepath), fileLists, "csv");
		if(fileLists != null){
			int loadSize = fileLists.size();
			for(int i = 0; i < loadSize; i++){
				System.out.println("Loading " + (i + 1) + "/" + loadSize);
				String fileList = fileLists.get(i);
				System.out.println(fileList);
				load(fileList);
			}
		}
		/*
		List<String> fileLists = readFileInFolder(filepath);
		if(fileLists != null){
			int loadSize = fileLists.size();
			for(int i = 0; i < loadSize; i++){
				System.out.println("Loading " + (i + 1) + "/" + loadSize);
				String fileList = fileLists.get(i);
				System.out.println(fileList);
				load(fileList);
			}
		}
		*/
	}
	
	private void store(List<SequenceInfo> _models, String filepath, boolean withTag){
		if(_models.size() > 0){
			try{
				File outFile = new File(filepath);
				FileWriter out = new FileWriter(outFile);
				for(int i = 0; i < _models.size(); i++){
					SequenceInfo seq = _models.get(i);
					if(withTag){
//						out.write(seq.dumpWithTag() + "\n");
					} else {
						out.write(seq.dump() + "\n");
					}
				}
				out.close();			
			} catch(IOException e) {
	        }
		} else {
			System.out.println("Not Output Data.");
		}
	}
	
	private void load(List<SequenceInfo> _models, String filepath){
		try{
			File file = new File(filepath);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String str = br.readLine();
			while(str != null){
				_models.add(new SequenceInfo(str));
			    str = br.readLine();
			}
			br.close();
		}catch(IOException e){
		}
	}
	
	public void print(){
		
		for(int i = 0; i < models.size(); i++){
			models.get(i).print();
		}
	}
	/*
	private List<String> readFileInFolder(String folderPath){
		List<String> retfiles = new ArrayList<String>();
		
		File dir = new File(folderPath);
		File[] files = dir.listFiles();
		
		if(files == null){
			return null;
		}
		
		for(File file : files){
			if(file.exists() == false){
				continue;
			} else if(file.isFile()){
				retfiles.add(file.getPath());
			}
		}
		return retfiles;
	}
	*/
}
