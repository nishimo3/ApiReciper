package org.pairprogrammingai.apireciper.core.main;

import org.pairprogrammingai.apireciper.core.adviser.NotEnoughApiLucene;
import org.pairprogrammingai.apireciper.core.adviser.SearchLucene;
import org.pairprogrammingai.apireciper.core.analyzer.SourceAnalyzerMain;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;

import java.util.List;

public class AdviserLucene {
	private String androidSdkPath = "";
	private String androidSdkVersion = "";
	private String luceneIndexPath = "";
	private String adviseStr = "";
	
	public AdviserLucene(String _androidSdkPath, String _androidSdkVersion, String _luceneIndexPath){
		androidSdkPath = _androidSdkPath;
		androidSdkVersion = _androidSdkVersion;
		luceneIndexPath = _luceneIndexPath;
	}
	
	public void execute(String sourceFileName, String keyword, boolean useWalaFlg){
		// Parsing source code
		SourceAnalyzerMain analyzer = new SourceAnalyzerMain(androidSdkPath, androidSdkVersion, useWalaFlg);
		List<SequenceInfo> sources = analyzer.createApiSet(sourceFileName, -1);

		Filter filter = new Filter();
		sources = filter.delNotAndroidApi(sources);
		sources = filter.delDoubleApi(sources);
		sources = filter.delDoubleApiSet(sources);
		

		System.out.println("<<<<<<<<<<<<<<Source Code Information>>>>>>>>>>>>>>");
		for(int i = 0; i < sources.size(); i++){
			SequenceInfo seq = sources.get(i);
			seq.print();
		}
		System.out.println("");
		
		System.out.println("<<<<<<<<<<<< KEYWORD >>>>>>>>>>>>>>>>>>>");
		System.out.println(keyword);
		
		System.out.println("<<<<<<<<<<<< Not Enough API >>>>>>>>>>>>>>>>>>>");

		// Inference Not Enough API
		if((sources != null) && (sources.size() >= 1)){
			SearchLucene searchLucene = new SearchLucene();
			List<SequenceInfo> searchResults = searchLucene.search(luceneIndexPath, keyword);
			System.out.println("searchResults:" + searchResults.size());
			NotEnoughApiLucene notEnoughApiLucene = new NotEnoughApiLucene();
			adviseStr = notEnoughApiLucene.execute(sources, searchResults);
		}
	}

	public String getAdvisetoString(){
		return adviseStr;
	}
}
