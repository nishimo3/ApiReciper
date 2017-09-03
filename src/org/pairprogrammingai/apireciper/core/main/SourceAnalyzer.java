package org.pairprogrammingai.apireciper.core.main;

import org.pairprogrammingai.apireciper.core.analyzer.SourceAnalyzerMain;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;
import org.pairprogrammingai.apireciper.core.utils.FolderOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SourceAnalyzer {
	private SourceAnalyzerMain analyzer;
	private int id = 0;
	
	public SourceAnalyzer(String androidSdkPath, String androidSdkVersion, boolean useWalaFlg){
		analyzer = new SourceAnalyzerMain(androidSdkPath, androidSdkVersion, useWalaFlg);
	}
	
	public void execute(String extractFolder, List<SequenceInfo> models){
		List<String> retFiles = new ArrayList<String>();
		FolderOperator.readFolder(new File(extractFolder), retFiles, "java");
		subExecute(retFiles, models);
	}
	
	public void execute(List<String> retFiles, List<SequenceInfo> models){
		subExecute(retFiles, models);
	}
	
	private void subExecute(List<String> retFiles, List<SequenceInfo> models){
		id = 0;
		for(int i = 0; i < retFiles.size(); i++){
			String filepath = retFiles.get(i);
//			System.out.println("--------" + filepath);
			
			List<SequenceInfo> seqInfos = analyzer.createApiSet(filepath, id);
			if((seqInfos != null) && (seqInfos.size() > 0)){
				for(int j = 0; j < seqInfos.size(); j++){
					SequenceInfo seqInfo = seqInfos.get(j);
					seqInfo.setClassName("\"" + filepath + " " + seqInfo.getClassName() + "\"" );
				}
				
				models.addAll(seqInfos);
			}
			id++;
		}
	}
}
