package org.pairprogrammingai.apireciper.core.filter;

import org.pairprogrammingai.apireciper.core.data.ApiInfo;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;

import java.util.ArrayList;
import java.util.List;

public class FusionApiInfo {
		
	public List<SequenceInfo> extractPrivateFunction(String name, List<SequenceInfo> _models){
		List<SequenceInfo> newModels = new ArrayList<SequenceInfo>();
		
		for(int i = 0; i < _models.size(); i++){
			SequenceInfo seq = _models.get(i);
			ApiInfo defineApiInfo = seq.getDefineApiInfo();
			List<ApiInfo> seqseq = seq.getSequence();
			
			if(isAndroidApi(defineApiInfo)){
				List<ApiInfo> newSeq = new ArrayList<ApiInfo>();

				for(int j = 0; j < seqseq.size(); j++){
					List<ApiInfo> defineApiInfos = new ArrayList<ApiInfo>();
					defineApiInfos.add(defineApiInfo);
					
					ApiInfo apiInfo = seqseq.get(j);
					newSeq.addAll(getAndroidApi(defineApiInfos, apiInfo, _models));
				}
				
				if(newSeq.size() > 0){
					SequenceInfo seqInfo = new SequenceInfo(name, defineApiInfo);
					seqInfo.setSequence(newSeq);
					newModels.add(seqInfo);
				}
			}
		}
		return newModels;
	}
	
	private List<ApiInfo> getAndroidApi(List<ApiInfo> defineApiInfos, ApiInfo apiInfo, List<SequenceInfo> _models){
		List<ApiInfo> newSeq = new ArrayList<ApiInfo>();
		
		if(isAndroidApi(apiInfo)){
			// Androidのフレームワークに属するAPIの場合
			newSeq.add(apiInfo);
		} else {
			// 自身のクラスに属するメソッドの場合
			if(!isRecursiveMethod(defineApiInfos, apiInfo)){
				// 再帰的なメソッドでない場合、他のAPI呼び出し系列を探す
				for(int i = 0; i < _models.size(); i++){
					SequenceInfo seq = _models.get(i);
					ApiInfo seqDefineApiInfo = seq.getDefineApiInfo();
					
					if(seqDefineApiInfo.eq(apiInfo)){
						// 他のAPI呼び出し系列で該当のAPI定義が見つかった場合
						defineApiInfos.add(seqDefineApiInfo);
						
						List<ApiInfo> seqseq = seq.getSequence();
						for(int j = 0; j < seqseq.size(); j++){
							ApiInfo aInfo = seqseq.get(j);
							List<ApiInfo> newGetSeq = getAndroidApi(defineApiInfos, aInfo, _models);
							if(newGetSeq != null){
								newSeq.addAll(newGetSeq);
							}
						}
						defineApiInfos.remove(seqDefineApiInfo);
					}
				}				
			}
		}
		return newSeq;
	}
	
	private boolean isRecursiveMethod(List<ApiInfo> defineApiInfos, ApiInfo apiInfo){
		for(int i = 0; i < defineApiInfos.size(); i++){
			ApiInfo defineApiInfo = defineApiInfos.get(i);
			if(defineApiInfo.eq(apiInfo)){
				return true;
			}
		}
		return false;
	}
		
	private boolean isAndroidApi(ApiInfo apiInfo){
		if(apiInfo != null){
			String className = apiInfo.getClassName();
			if(className != null){
				if(className.startsWith("android")){
					return true;
				}
			}
		}
		return false;
	}
}
