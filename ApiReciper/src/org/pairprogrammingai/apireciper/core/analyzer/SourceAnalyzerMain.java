package org.pairprogrammingai.apireciper.core.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.pairprogrammingai.apireciper.core.analyzer.parser.BaseAstParser;
import org.pairprogrammingai.apireciper.core.analyzer.parser.AstParser;
import org.pairprogrammingai.apireciper.core.data.ApiInfo;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;
import org.pairprogrammingai.apireciper.core.filter.DivideApiInfo;

public class SourceAnalyzerMain {
	private static boolean DIVIDING_FLG  = false;
	private static int divingMode = DivideApiInfo.DIVIDING_MODE_CLASS;
	
	private BaseAstParser astParser;
	private boolean useWalaFlg = false;
	
	public SourceAnalyzerMain(String androidSdkPath, boolean useWalaFlg){
		this.useWalaFlg = useWalaFlg;
		
		if(this.useWalaFlg){
//			astParser = new AstParserWala(androidSdkPath, Options.AndroidSdkVersion, Options.AndroidExternalVersion);
		} else {
			astParser = new AstParser(androidSdkPath);			
		}
	}

	public List<SequenceInfo> createApiSet(String filepath, int id){
		try {
			if(this.useWalaFlg){
				return astParser.execute(filepath, id);
			} else {
				// 解析
				List<SequenceInfo> seqInfo = astParser.execute(filepath, id);
				printModels(seqInfo);
				
				seqInfo = createSubApiSet(seqInfo);

				/* 変数に着目した場合の分離
				if(DIVIDING_FLG){
					seqInfo = divide(seqInfo);
				}
				*/
				
				if(DIVIDING_FLG){
					DivideApiInfo divideApiInfo = new DivideApiInfo(divingMode);
					seqInfo = divideApiInfo.exec(seqInfo);
				}
				return seqInfo;				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private List<SequenceInfo> createSubApiSet(List<SequenceInfo> seqInfos){
		List<SequenceInfo> newSeqInfos = new ArrayList<SequenceInfo>();
		if(seqInfos.size() > 0){
			ApiInfo defineApiInfo = new ApiInfo("", "", ApiInfo.KIND_NOTHING, 0, 0);
			SequenceInfo newSequenceInfo = new SequenceInfo(seqInfos.get(0).getClassName(), defineApiInfo);
			
			for(int i = 0; i < seqInfos.size(); i++){
				SequenceInfo seqInfo = seqInfos.get(i);
				List<ApiInfo> seq = seqInfo.getSequence();
				for(int j = 0; j < seq.size(); j++){
					ApiInfo apiInfo = seq.get(j);
					newSequenceInfo.add(apiInfo);
				}
			}
			
			if(!newSequenceInfo.isEmptySeq()){
				newSeqInfos.add(newSequenceInfo);
			}
		}
		return newSeqInfos;		
	}
	
	public static class DivideGroupRule {
		public List<String> ids;
		
		public DivideGroupRule(String _id){
			ids = new ArrayList<String>();
			add(_id);
		}
		
		public void add(String _id){
			if(!_id.isEmpty()){
				ids.add(_id);
			}
		}
		
		public boolean includeIds(String _id){
			for(int i = 0; i < ids.size(); i++){
				String id = ids.get(i);
				if(id.equals(_id)){
					return true;
				}
			}
			return false;
		}
		
		public void print(){
			for(int i = 0; i < ids.size(); i++){
				System.out.print(ids.get(i) + " ");
			}
			System.out.println("");
		}
	}
	
	private List<SequenceInfo> divide(List<SequenceInfo> _models){
		List<SequenceInfo> models = new ArrayList<SequenceInfo>();
		
		for(int i = 0; i < _models.size(); i++){
			SequenceInfo seqInfo = _models.get(i);
			List<ApiInfo> seq = seqInfo.getSequence();
			List<DivideGroupRule> rules = getDivideGroupsRule(seq);
			
			Boolean[] checkLists = new Boolean[seq.size()];
			for(int j = 0; j < checkLists.length; j++){
				checkLists[j] = false;
			}
			/*
			for(int j = 0; j < rules.size(); j++){
				DivideGroupRule rule = rules.get(j);
				rule.print();
			}
			*/
			
			for(int j = 0; j < rules.size(); j++){
				DivideGroupRule rule = rules.get(j);
				SequenceInfo newSeqInfo = new SequenceInfo(seqInfo);
				
				for(int k = 0; k < seq.size(); k++){
					if(!checkLists[k]){
						ApiInfo apiInfo = seq.get(k);
						String iName = apiInfo.iName;
						String aName = apiInfo.aName;
						
						if(((iName != null) && rule.includeIds(iName)) || ((aName != null) && rule.includeIds(aName))){
							newSeqInfo.add(apiInfo);
							checkLists[k] = true;
						}
					}
				}
				if(newSeqInfo.getSequence().size() >= 1){
					models.add(newSeqInfo);
				}
			}

			List<SequenceInfo> noDivideModels = new ArrayList<SequenceInfo>();
			SequenceInfo newSeqInfo = new SequenceInfo(seqInfo);
			for(int j = 0; j < checkLists.length; j++){
				if(!checkLists[j]){
					newSeqInfo.add(seq.get(j));
				}
			}
			if(newSeqInfo.getSequence().size() >= 1){
				noDivideModels.add(newSeqInfo);
				
				DivideApiInfo divideApiInfo = new DivideApiInfo(divingMode);
				noDivideModels = divideApiInfo.exec(noDivideModels);
				models.addAll(noDivideModels);
			}
		}
		return models;	
	}
	
	private List<DivideGroupRule> getDivideGroupsRule(List<ApiInfo> seq){
		List<DivideGroupRule> divideGroups = new ArrayList<DivideGroupRule>();
		
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			String iName = apiInfo.iName;
			String aName = apiInfo.aName;
			
			if(iName != null){
				boolean findFlg = false;
				for(int j = 0; j < divideGroups.size(); j++){
					DivideGroupRule divideGroupRule = divideGroups.get(j);
					if(divideGroupRule.includeIds(iName)){
						if((aName != null) && !divideGroupRule.includeIds(aName)){
							divideGroupRule.add(aName);
						}
						findFlg = true;
						break;
					}
				}				
				if(!findFlg){
					DivideGroupRule divideGroupRule = new DivideGroupRule(iName);
					if(aName != null){
						divideGroupRule.add(aName);
					}
					divideGroups.add(divideGroupRule);
				}
			}
		}
		return divideGroups;
	}

	public void printModels(List<SequenceInfo> _models){
		System.out.println("<<<<<<<<<<<<<<Model Information>>>>>>>>>>>>>>");
		String classNameOld = "";
		for(int i = 0; i < _models.size(); i++){
			SequenceInfo seq = _models.get(i);
			if(!classNameOld.equals(seq.getClassName())){
				System.out.println("");
				System.out.println("---------------" + seq.getClassName());
			}
			seq.getDefineApiInfo().println();
			List<ApiInfo> seqseq = seq.getSequence();
			for(int j = 0; j < seqseq.size(); j++){
				System.out.print("[" + j + "]");
				seqseq.get(j).println();
			}
			classNameOld = seq.getClassName();
		}
		System.out.println("");
	}

}
