package org.pairprogrammingai.apireciper.core.data;

import java.util.ArrayList;
import java.util.List;

public class SequenceInfo {
	private static final String sepSymbol1 = "###";
	private static final String sepSymbol2 = "&&&";
	
	private String className;
	private ApiInfo defineApiInfo;
	private List<ApiInfo> seq;

	public SequenceInfo(String _className, ApiInfo _defineApiInfo){
		className = _className;
		seq = new ArrayList<ApiInfo>();	
		defineApiInfo = _defineApiInfo;
	}
	
	/* data = className, defineApiInfo, seq */
	public SequenceInfo(String data){
		String[] comma = data.split(sepSymbol1);

		/*
		for(int i = 0; i < comma.length; i++){
			System.out.println(": " + comma[i]);
		}
		*/
		
		if(comma.length == 4){
		} else if(comma.length == 3){
			seq = new ArrayList<ApiInfo>();
			
			className = comma[0];
			defineApiInfo = new ApiInfo(comma[1]);
			
			String[] _seq = comma[2].split(sepSymbol2);
			for(int i = 0; i < _seq.length; i++){
				seq.add(new ApiInfo(_seq[i]));
			}
		}
	}
		
	public SequenceInfo(SequenceInfo _seqInfo){
		className = _seqInfo.className;
		seq = new ArrayList<ApiInfo>();
		defineApiInfo = _seqInfo.defineApiInfo;
	}
	
	public void add(ApiInfo apiInfo){
		seq.add(apiInfo);
	}
	
	public void addAll(List<ApiInfo> apiInfos){
		seq.addAll(apiInfos);
	}
	
	public boolean isEmptySeq(){
		if(seq.size() == 0){
			return true;
		} else {
			return false;
		}
	}
	
	public ApiInfo getApiInfo(String apiName){
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			if(apiName.equals(apiInfo.apiName)){
				return apiInfo;
			}
		}
		return null;
	}
	
	public boolean isFindApiInfo(ApiInfo _apiInfo){
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			if(apiInfo.eq(_apiInfo)){
				return true;
			}
		}
		return false;		
	}
	
	public ApiInfo getDefineApiInfo(){
		return defineApiInfo;
	}
	
	public void setDefineApiInfo(ApiInfo _apiInfo){
		defineApiInfo = _apiInfo;
	}
	
	public List<ApiInfo> getSequence(){
		return seq;
	}
	
	public void setSequence(List<ApiInfo> _seq){
		seq = _seq;
	}

	public double calcSimilarityOfSequence(List<ApiInfo> _seq){
		if((seq != null) && (_seq != null)){
			if(seq.size() == _seq.size()){
				int sameCount = 0;
				for(int i = 0; i < seq.size(); i++){
					ApiInfo api1 = seq.get(i);
					ApiInfo api2 = _seq.get(i);
					if(api1.eq(api2)){
						sameCount++;
					}
				}
				if(sameCount == 0){
					return 0.0;
				} else {
					return (double)sameCount / (double) seq.size();
				}
			} else {
				int sameCount = 0;
				int start = 0;
				for(int i = 0; i < seq.size(); i++){
					ApiInfo api1 = seq.get(i);
					for(int j = start; j < _seq.size(); j++){
						ApiInfo api2 = _seq.get(j);
						if(api1.eq(api2)){
							sameCount++;
							start = j + 1;
							break;
						}
					}
				}
				if(sameCount == 0){
					return 0.0;
				} else {
					if(seq.size() > _seq.size()){
						return (double)sameCount / (double) seq.size();
					} else {
						return (double)sameCount / (double) _seq.size();
					}
				}
			}
		}
		return 0.0;
	}
	
	public boolean isEqualSequence(List<ApiInfo> _seq){
		if((seq != null) && (_seq != null)){
			if(seq.size() == _seq.size()){
				for(int i = 0; i < seq.size(); i++){
					ApiInfo api1 = seq.get(i);
					ApiInfo api2 = _seq.get(i);
					if(!api1.eq(api2)){
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean isEqual(SequenceInfo _seqInfo){
		if(_seqInfo != null){
			if((className != null) && (_seqInfo.className != null)){
				if(!className.equals(_seqInfo.className)){
					return false;
				}
				
				if((defineApiInfo != null) && (_seqInfo.defineApiInfo != null)){
					if(!defineApiInfo.eq(_seqInfo.defineApiInfo)){
						return false;
					}
					
					if(isEqualSequence(_seqInfo.seq)){
						return true;
					}
				}
			}
		}
		return false;
	}
		
	public boolean isInclude(SequenceInfo _seqInfo){
		List<ApiInfo> _seq = _seqInfo.getSequence();		
		for(int i = 0; i < seq.size(); i++){
			ApiInfo api1 = seq.get(i);
			for(int j = 0; j < _seq.size(); j++){
				ApiInfo api2 = _seq.get(j);
				if(api1.eq(api2)){
					return true;
				}
			}
		}
		return false;
	}
	
	public List<String> getStringSeq(){
		List<String> ret = new ArrayList<String>();
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			ret.add(apiInfo.getString());
		}
		return ret;
	}
	
	public String getClassName(){
		return className;
	}
	
	public void setClassName(String name){
		className = name;
	}
	
	public int getClassKindNumforSeq(){
		List<String> classNames = new ArrayList<String>();
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			String className = apiInfo.className;
			
			boolean isFind = false;
			for(int j = 0; j < classNames.size(); j++){
				String _className = classNames.get(j);
				if(className.equals(_className)){
					isFind = true;
					break;
				}
			}
			if(!isFind){
				classNames.add(className);
			}
		}
		return classNames.size();
	}
	
	public int getApiCount(){
		int count = 0;
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			if(apiInfo.kind == ApiInfo.KIND_METHOD){
				count++;
			}
		}
		return count;
	}
	
	public boolean isEqualApiSet(SequenceInfo _seqInfo){
		boolean ret = false;
		
		List<ApiInfo> _seq = _seqInfo.getSequence();		
		if(seq.size() == _seq.size()){
			int count = 0;
			for(int i = 0; i < seq.size(); i++){
				ApiInfo apiInfo = seq.get(i);
				
				for(int j = 0; j < _seq.size(); j++){
					ApiInfo _apiInfo = _seq.get(j);
					if(apiInfo.eq(_apiInfo)){
						count++;
						break;
					}
				}				
			}
			
			if(count == seq.size()){
				ret = true;
			}
		}
		return ret;
	}
	
	public String getApiCallLineNumberList(){
		String ret = "";
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			ret = ret + apiInfo.lineSPos + ",";
		}
		return ret;
	}

	public void print(){
		System.out.print("--------------------------------");
//		System.out.println(className);
		defineApiInfo.println();
		
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			System.out.print("[S" + i + "] ");
			apiInfo.println();
		}
	}
	
	public String dump(){
		String ret = "";
		ret = className + sepSymbol1 + defineApiInfo.getString() + sepSymbol1;
		
		for(int i = 0; i < seq.size(); i++){
			ApiInfo apiInfo = seq.get(i);
			if(i < seq.size() - 1){
				ret = ret + apiInfo.getString() + sepSymbol2;
			} else {
				ret = ret + apiInfo.getString();
			}
		}
		return ret;
	}
}
