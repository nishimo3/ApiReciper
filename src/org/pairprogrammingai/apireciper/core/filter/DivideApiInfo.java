package org.pairprogrammingai.apireciper.core.filter;

import org.pairprogrammingai.apireciper.core.data.ApiInfo;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;

import java.util.ArrayList;
import java.util.List;


public class DivideApiInfo {
	public static final int DIVIDING_MODE_CLASS = 0;
	public static final int DIVIDING_MODE_CLASSPARTIAL = 1;
	public static final int DIVIDING_MODE_CLASSPARTIAL_NAMEKAI = 2;
	public static final int DIVIDING_MODE_PACKAGE_LAYER2 = 3;
	public static final int DIVIDING_MODE_PACKAGE = 4;
	public static final int DIVIDING_MODE_PACKAGE_NAMEKAI = 5;
	
	private static int dividingMode = DIVIDING_MODE_CLASS;
	
	static class GroupClass {
		public String groupName;
		public List<String> classNames;
		
		public GroupClass(String _groupName){
			groupName = _groupName;
			classNames = new ArrayList<String>();
		}
		
		public void addClassName(String className){
			classNames.add(className);
		}
		
		public boolean isInclude(String _className){
			boolean ret = false;
			for(int i = 0; i < classNames.size(); i++){
				String className = classNames.get(i);
				if(className.equals(_className)){
					ret = true;
					break;
				}
			}
			return ret;			
		}
		
		public void print(){
			System.out.println("GroupName:" + groupName);
			for(int i = 0; i < classNames.size(); i++){
				System.out.println(" " + i + ",\"" + classNames.get(i) + "\"");
			}
		}
	}
	
	public DivideApiInfo(int mode){
		dividingMode = mode;
	}
	
	public List<SequenceInfo> exec(List<SequenceInfo> models){
		// クラス毎に整理する
		List<String> classNames = getClassKinds(models);
		List<GroupClass> groupClasses  = getGroupClasses(classNames);
/*
		for(int i = 0; i < groupClasses.size(); i++){
			GroupClass gClass = groupClasses.get(i);
			gClass.print();
		}
*/
		models = dividngModel(models, groupClasses);
//		printModels(models);
		
		return models;
	}
	
	private List<String> getClassKinds(List<SequenceInfo> _models){
		// Classの種類の抽出
		List<String> classNames = new ArrayList<String>();
		for(int i = 0; i < _models.size(); i++){
			SequenceInfo seq1 = _models.get(i);
			List<ApiInfo> seq1seq = seq1.getSequence();
			
			for(int j = 0; j < seq1seq.size(); j++){
				// シーケンシャルパターンのAPI呼び出しのクラス名を収集する(重複しないように)
				ApiInfo apiInfo = seq1seq.get(j);
				String apiInfoClassName = apiInfo.getClassName();
				
				boolean isFindFlg = false;
				for(int k = 0; k < classNames.size(); k++){
					if(apiInfoClassName.equals(classNames.get(k))){
						isFindFlg = true;
						break;						
					}
				}
				if(!isFindFlg){
					if(isAndroidApi(apiInfo)){
						classNames.add(apiInfoClassName);						
					}
				}				
			}
		}		
		return classNames;
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
	
	private List<GroupClass> getGroupClasses(List<String> classNames){	
		List<GroupClass> groupClasses = new ArrayList<GroupClass>();			
		for(int i = 0; i < classNames.size(); i++){
			String className = classNames.get(i);			
			switch(dividingMode){
			case DIVIDING_MODE_CLASS:
			{
				GroupClass gClass = new GroupClass(className);
				gClass.addClassName(className);
				groupClasses.add(gClass);
			}
				break;
			case DIVIDING_MODE_CLASSPARTIAL:
				// SensorやSensorManagerなど部分的にクラス名が一致するものは一つの分離単位とみなす
			case DIVIDING_MODE_CLASSPARTIAL_NAMEKAI:
			{
				boolean isFindFlg = false;
				for(int j = 0; j < groupClasses.size(); j++){
					GroupClass gClass = groupClasses.get(j);
					String compStr = compareStrings(className, gClass.groupName);
					if(compStr != null){
						isFindFlg = true;						
						gClass.groupName = compStr;
						gClass.addClassName(className);
						break;
					}
				}
				if(!isFindFlg){
					GroupClass newClass = new GroupClass(className);
					newClass.addClassName(className);
					groupClasses.add(newClass);
				}
			}
				break;
			case DIVIDING_MODE_PACKAGE_LAYER2:
				// android.hardware.*のようにpackageの2階層目を1つの分離単位とみなす
			case DIVIDING_MODE_PACKAGE:
				// android.hardware.Sensorのようにpackageとクラスの境目を1つの分離単位とみなす
			case DIVIDING_MODE_PACKAGE_NAMEKAI:
			{
				String[] classNameSplit = className.split("\\.");
				String name = null;
				if(dividingMode == DIVIDING_MODE_PACKAGE_LAYER2){
					if(classNameSplit.length >= 3){
						name = classNameSplit[1];
					}
				} else if(dividingMode == DIVIDING_MODE_PACKAGE){
					for(int j = 0; j < classNameSplit.length; j++){
						if(Character.isUpperCase(classNameSplit[j].charAt(0))){
							//先頭が大文字 android.hardware.Sensor -> hardware
							name = classNameSplit[j - 1];
							break;
						}
					}
				} else if(dividingMode == DIVIDING_MODE_PACKAGE_NAMEKAI){
					for(int j = 0; j < classNameSplit.length; j++){
						if(Character.isUpperCase(classNameSplit[j].charAt(0))){
							//先頭が大文字 android.hardware.Sensor -> android.hardware
							name = classNameSplit[0];
							for(int k = 1; k < j; k++){
								name = name  + "." + classNameSplit[k];
							}
							break;
						}
					}
					
				}
				if(name != null){
					boolean isFindFlg = false;
					for(int j = 0; j < groupClasses.size(); j++){
						GroupClass gClass = groupClasses.get(j);
						if(name.equals(gClass.groupName)){
							isFindFlg = true;
							gClass.addClassName(className);
							break;
						}
					}
					if(!isFindFlg){
						GroupClass newClass = new GroupClass(name);
						newClass.addClassName(className);
						groupClasses.add(newClass);
					}
				}
			}
				break;
			default:
				break;
			}
		}
		return groupClasses;
	}
	
	
	private String compareStrings(String _name1, String _name2){
		String ret = null;
		String p1 = getPackageName(_name1);
		String p2 = getPackageName(_name2);
		boolean checkFlg = false;
		if(dividingMode == DIVIDING_MODE_CLASSPARTIAL){
			checkFlg = true;
		} else if(dividingMode == DIVIDING_MODE_CLASSPARTIAL_NAMEKAI){
			if(p1.equals(p2)){
				checkFlg = true;
			}
		}
		if(checkFlg){
			String c1 = _name1.substring(p1.length() + 1);
			String c2 = _name2.substring(p2.length() + 1);
			String cc1 = cutSymbolLessThan(c1);
			String cc2 = cutSymbolLessThan(c2);
			String ccc1 = cutSymbolOfDot(cc1);
			String ccc2 = cutSymbolOfDot(cc2);
			List<String> ccc1s = splitCamelCase(ccc1);
			List<String> ccc2s = splitCamelCase(ccc2);
			
			int length = 0;
			if(ccc1s.size() < ccc2s.size()){
				length = ccc1s.size();
			} else {
				length = ccc2s.size();
			}
			
			int pos = -1;
			int count = 0;
			for(int i = 0; i < length; i++){
				if(i != count){
					break;
				}
				if(ccc1s.get(i).equals(ccc2s.get(i))){
					pos = i;					
					count++;
				}
			}

			if(pos != -1){
				ret = p1 + ".";
				for(int i = 0; i <= pos; i++){
					ret = ret + ccc1s.get(i);
				}
			}
		}
		return ret;
	}
	
	
	private List<String> splitCamelCase(String _name){
		List<String> ret = new ArrayList<String>();
		int start = -1;
		for(int i = 0; i < _name.length(); i++){
			if(Character.isUpperCase(_name.charAt(i))){
				if(start != -1){
					ret.add(_name.substring(start, i));
				}
				start = i;
			}
		}
		if(start != -1){
			ret.add(_name.substring(start));
		} else {
			ret.add(_name);
		}
		return ret;		
	}
	
	private String cutSymbolLessThan(String name){
		String ret = name;
		if(name.length() > 0){
			for(int i = 0;  i < name.length(); i++){
				if(name.charAt(i) == '<'){
					ret = name.substring(0, i);
					break;
				}
			}
		}
		return ret;
	}

	private String cutSymbolOfDot(String _name){
		String ret = "";
		String[] nameSplit = _name.split("\\.");
		if(nameSplit.length > 0){
			for(int i = 0; i < nameSplit.length; i++){
				ret = ret + nameSplit[i];
			}
		} else {
			ret = _name;
		}
		return ret;
	}
		
	private String getPackageName(String _name){
		String ret = "";
		String[] nameSplit = _name.split("\\.");
		for(int i = 0; i < nameSplit.length; i++){
			if(nameSplit[i].length() > 0){
				if(Character.isUpperCase(nameSplit[i].charAt(0))){
					//先頭が大文字 android.hardware.Sensor -> android.hardware
					ret = nameSplit[0];
					for(int j = 1; j < i; j++){
						ret = ret + "." + nameSplit[j];
					}
					break;
				}
			}
		}
		return ret;
	}
	
	private List<SequenceInfo> dividngModel(List<SequenceInfo> _models, List<GroupClass> _groupClasses){
		// Method 抽出
		List<SequenceInfo> newModels = new ArrayList<SequenceInfo>();
		for(int i = 0; i < _groupClasses.size(); i++){
			GroupClass groupClass = _groupClasses.get(i);

			for(int j = 0; j < _models.size(); j++){
				SequenceInfo seq = _models.get(j);
				List<ApiInfo> seqseq = seq.getSequence();
				
				SequenceInfo newSeq = new SequenceInfo(seq.getClassName() + " " + groupClass.groupName, seq.getDefineApiInfo());
//				SequenceInfo newSeq = new SequenceInfo(seq.getClassName(), seq.getDefineApiInfo());
//				SequenceInfo newSeq = new SequenceInfo(className, seq.getDefineApiInfo());
				boolean isExistNewSeq = false;
				for(int k = 0; k < seqseq.size(); k++){
					ApiInfo apiInfo = seqseq.get(k);					
					if(groupClass.isInclude(apiInfo.getClassName())){
						newSeq.add(apiInfo);
						isExistNewSeq = true;
					}
				}
				if(isExistNewSeq){
					newModels.add(newSeq);
				}
			}
		}
		return newModels;
	}
	
}
