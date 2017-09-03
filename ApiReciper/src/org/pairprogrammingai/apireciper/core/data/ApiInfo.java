package org.pairprogrammingai.apireciper.core.data;

public class ApiInfo {
	public static final int KIND_NOTHING = 0;
	public static final int KIND_METHOD = 1;
	public static final int KIND_VARIABLE = 2;
	public static final int KIND_CONSTANT = 3;
	
	private static final String sepSymbol = "->";

	public String apiName;
	public String className;
	public int lineSPos;
	public int lineEPos;
	public int kind;
	
	public String iName; // Name of Instance Value
	public String aName; // Name of Assignment Value 
		
	public ApiInfo(String _className, String _apiName, int _kind, int _lineSPos, int _lineEPos){
		className = _className;
		apiName = _apiName;
		kind = _kind;
		lineSPos = _lineSPos;
		lineEPos = _lineEPos;
	}
	
	public ApiInfo(String _className, String _apiName, String _iName, String _aName, int _lineSPos){
		className = _className;
		apiName = _apiName;
		iName = _iName;
		aName = _aName;
		lineSPos = _lineSPos;
		lineEPos = 0; // T.B.D.
	}
	
	public ApiInfo(String data){
		String[] sep = data.split(sepSymbol);
		if(sep.length == 3){
			className = sep[0];
			apiName = sep[1];
			kind = Integer.valueOf(sep[2]);
			lineSPos = 0; // T.B.D.
			lineEPos = 0; // T.B.D.
		} else if(sep.length == 2){
			className = sep[0];
			apiName = sep[1];
			lineSPos = 0; // T.B.D.
			lineEPos = 0; // T.B.D.
		} else {
			className = "";
			apiName = "";
			kind = KIND_NOTHING;
			lineSPos = 0; // T.B.D.
			lineEPos = 0; // T.B.D.
		}
	}
	
	public String getApiName(){
		return apiName;
	}
	
	public String getClassName(){
		return className;
	}
	
	public void println(){
		String _iName = "";
		if(iName != null){
			_iName = iName;
		}
		String _aName = "";
		if(aName != null){
			_aName = aName;
		}
		System.out.println(className + sepSymbol + apiName + " " + _iName + " " + _aName);
	}
	
	public void print(){
		System.out.print(className + sepSymbol + apiName);
	}
	
	public String getString(){
		return (className + sepSymbol + apiName + sepSymbol + String.valueOf(kind));
	}
	
	public boolean eq(ApiInfo _apiInfo){
		if(apiName.equals(_apiInfo.apiName) && className.equals(_apiInfo.className) && (kind == _apiInfo.kind)){
			return true;
		}
		return false;
	}
}
