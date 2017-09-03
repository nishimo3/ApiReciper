package org.pairprogrammingai.apireciper.core.main;

import org.pairprogrammingai.apireciper.core.data.ApiInfo;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;
import org.pairprogrammingai.apireciper.core.filter.DivideApiInfo;

import java.util.ArrayList;
import java.util.List;

public class Filter {
	
	public List<SequenceInfo> delDoubleApi(List<SequenceInfo> models){
		// 1つのAPI集合の中で複数回，同じAPIが表れる場合は，1つにする
		List<SequenceInfo> newModels = new ArrayList<SequenceInfo>();
		
		for(int i = 0 ; i < models.size(); i++){
			SequenceInfo seqInfo = models.get(i);
			List<ApiInfo> seq = seqInfo.getSequence();
			
			List<ApiInfo> newSeq = new ArrayList<ApiInfo>();			
			for(int j = 0; j < seq.size(); j++){
				ApiInfo apiInfo = seq.get(j);
				
				boolean findFlg = false;
				for(int k = 0; k < newSeq.size(); k++){
					ApiInfo _apiInfo = newSeq.get(k);
					if(apiInfo.eq(_apiInfo)){						
						findFlg = true;
						break;
					}
				}
				if(!findFlg){
					newSeq.add(apiInfo);
				}
			}
			if(newSeq.size() > 0){
				seqInfo.setSequence(newSeq);
				newModels.add(seqInfo);
			}
		}		
		return newModels;
	}
	
	public List<SequenceInfo> delNotAndroidApi(List<SequenceInfo> models){
		// AndroidのAPIでないものは削除する
		List<SequenceInfo> newModels = new ArrayList<SequenceInfo>();
		
		for(int i = 0 ; i < models.size(); i++){
			SequenceInfo seqInfo = models.get(i);
			List<ApiInfo> seq = seqInfo.getSequence();
			
			List<ApiInfo> newSeq = new ArrayList<ApiInfo>();			
			for(int j = 0; j < seq.size(); j++){
				ApiInfo apiInfo = seq.get(j);
				if(isAndroidApi(apiInfo)){
					newSeq.add(apiInfo);
				}
			}
			if(newSeq.size() > 0){
				seqInfo.setSequence(newSeq);
				newModels.add(seqInfo);
			}
		}
		return newModels;
	}
	
	public List<SequenceInfo> delDoubleApiSet(List<SequenceInfo> models){
		// 同じAPI集合は削除する
		List<SequenceInfo> newModels = new ArrayList<SequenceInfo>();
		
		for(int i = 0 ; i < models.size(); i++){
			SequenceInfo seqInfo = models.get(i);
			boolean findFlg = false;
			for(int j = 0; j < newModels.size(); j++){
				SequenceInfo newSeqInfo = newModels.get(j);
				if(seqInfo.isEqualApiSet(newSeqInfo)){
					findFlg = true;
					break;
				}
			}
			if(!findFlg){
				newModels.add(seqInfo);
			}
		}
		return newModels;
	}
	
	public List<SequenceInfo> divideModels(SequenceInfo seqInfo){
		List<SequenceInfo> models = new ArrayList<SequenceInfo>();
		models.add(seqInfo);
		
		int divingMode = DivideApiInfo.DIVIDING_MODE_CLASSPARTIAL;
		DivideApiInfo divideApiInfo = new DivideApiInfo(divingMode);
		return divideApiInfo.exec(models);
	}
	
	public List<SequenceInfo> deleteDoubleApiCall(List<SequenceInfo> _models){
		// 一つのAPI呼び出し系列内に連続して表れるAPI呼び出しは1つだけにする
		int compNum =  _models.size();
		for(int i = 0; i < compNum; i++){
			SequenceInfo seqInfo = _models.get(i);
			List<ApiInfo> seq = seqInfo.getSequence();
			
			if(seq.size() > 1){
				ApiInfo prevApiInfo = seq.get(0);
				List<ApiInfo> newSeq = new ArrayList<ApiInfo>();
				newSeq.add(prevApiInfo);
				for(int j = 0; j < seq.size(); j++){
					ApiInfo apiInfo = seq.get(j);
					if(!prevApiInfo.eq(apiInfo)){
						newSeq.add(apiInfo);
					}					
					prevApiInfo = apiInfo;
				}
				seqInfo.setSequence(newSeq);
			}
		}
		return _models;
	}
	
	private boolean isAndroidApi(ApiInfo apiInfo){		
		if(apiInfo != null){
			String className = apiInfo.getClassName();
			int kind = apiInfo.kind;
			if(className != null){
				if(className.startsWith("android") 
					&& ((kind == ApiInfo.KIND_CONSTANT) || (kind == ApiInfo.KIND_METHOD))){
					return true;
				}
			}
		}
		return false;
	}
}
