package org.pairprogrammingai.apireciper.core.analyzer.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.pairprogrammingai.apireciper.core.data.ApiInfo;
import org.pairprogrammingai.apireciper.core.data.SequenceInfo;
import org.pairprogrammingai.apireciper.core.utils.FolderOperator;


public class AstParser implements BaseAstParser {
	private String[] classPathEntries = {};
	
	public static class ClassInfo {
		ITypeBinding classBinding = null;
		ITypeBinding extendBinding = null;
		List<ITypeBinding> interfaceBindings = null;
		
		public ClassInfo(){
			interfaceBindings = new ArrayList<ITypeBinding>();
		}
		
		public IMethodBinding checkOverride(ITypeBinding classType, IMethodBinding method, String methodName){
			IMethodBinding comp = null;

			if(classType.isEqualTo(classBinding)){
				// Extend
				comp = this.getExtendClassMethod(methodName);
				if(comp == null){
					// Interface
					comp = this.getInterfaceClassMethod(methodName);
				} else {
					if(!method.overrides(comp)){
						// Override
						comp = null;
					}
				}
			}
			return comp;
		}
		
		private IMethodBinding getExtendClassMethod(String name){
			if(extendBinding != null){
				ITypeBinding iExtendBinding = extendBinding;
				
				while(iExtendBinding.getQualifiedName().startsWith("android")){
					IMethodBinding[] extendClassMethod = iExtendBinding.getDeclaredMethods();
					for(int i = 0; i < extendClassMethod.length; i++){
						if(name.equals(extendClassMethod[i].getName().toString())){
							return extendClassMethod[i];
						}
					}
					
					iExtendBinding = iExtendBinding.getSuperclass();
				}
			}
			return null;
		}
		
		private IMethodBinding getInterfaceClassMethod(String name){
			if(interfaceBindings != null){
				for(int i = 0; i < interfaceBindings.size(); i++){
					IMethodBinding[] interfaceClassMethod = interfaceBindings.get(i).getDeclaredMethods();
					for(int j = 0; j < interfaceClassMethod.length; j++){
						if(name.equals(interfaceClassMethod[j].getName().toString())){
							return interfaceClassMethod[j];
						}
					}
				}
			}
			return null;
		}
	}
	
	public static class FindClassInfo extends ASTVisitor {		
		private List<ClassInfo> info;
		
		public FindClassInfo(){
			info = new ArrayList<ClassInfo>();
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
			ClassInfo classInfo = new ClassInfo();
			classInfo.classBinding = node.resolveBinding();
			
			if(node.getSuperclassType() != null){
				classInfo.extendBinding = node.getSuperclassType().resolveBinding();
			}
			
			if(node.superInterfaceTypes() != null){
				List<Type> typeList = node.superInterfaceTypes();
				for(int i = 0; i < typeList.size(); i++){
					classInfo.interfaceBindings.add(typeList.get(i).resolveBinding());
				}				
			} else {
				classInfo.interfaceBindings = null;
			}
			info.add(classInfo);
	        return super.visit(node);
		}
		
		@Override
		public boolean visit(EnumDeclaration node){
			ClassInfo classInfo = new ClassInfo();
			classInfo.classBinding = node.resolveBinding();
			
			if(node.superInterfaceTypes() != null){
				List<Type> typeList = node.superInterfaceTypes();
				for(int i = 0; i < typeList.size(); i++){
					classInfo.interfaceBindings.add(typeList.get(i).resolveBinding());
				}				
			} else {
				classInfo.interfaceBindings = null;
			}
			info.add(classInfo);
	        return super.visit(node);
		}		
		
		public List<ClassInfo> getInfo(){
			return info;
		}
		
		public void print(){
			if(info.size() > 0){
				for(int j = 0; j < info.size(); j++){
					if(info.get(j).classBinding != null){
						System.out.println("------" + info.get(j).classBinding.getName().toString() + " " +  info.get(j).classBinding.getQualifiedName() + "------");
					}
					if(info.get(j).extendBinding != null){
						System.out.println("[E]" + info.get(j).extendBinding.getName().toString() + " " + info.get(j).extendBinding.getQualifiedName());
					}
					if(info.get(j).interfaceBindings != null){
						List<ITypeBinding> cInfoInterface = info.get(j).interfaceBindings;
						for(int k = 0; k < cInfoInterface.size(); k++){
							System.out.println("[I" + String.valueOf(k) + "]" + cInfoInterface.get(k).getName().toString());
						}
					}
				}
			}
		}
	}
	
	/*
	public static class FindMethodInfo extends ASTVisitor {
		private final static String FILTER_KEYWORD = "android";
		private List<String> info;
		private List<ClassInfo> classInfo;
		
		public FindMethodInfo(List<ClassInfo> _classInfo) {
			info = new ArrayList<String>();
			classInfo = _classInfo;
		}

		@Override
		public boolean visit(MethodDeclaration node){			
			info.add("D_" + node.getName().toString());
			IMethodBinding method = (IMethodBinding)node.resolveBinding();
			ITypeBinding classType = getClassType(method);
			for(int i = 0; i < classInfo.size(); i++){
				IMethodBinding result = classInfo.get(i).checkOverride(classType, method, node.getName().toString());
				if(result != null){
					method = result;
					break;
				}
			}
			
			String className = getClassName(method, FILTER_KEYWORD);
			System.out.println("[D] " + className + "->" + node.getName().toString());			
			return super.visit(node);
		}
		
		@Override
		public boolean visit(SuperMethodInvocation node){
			IMethodBinding method = (IMethodBinding)node.resolveMethodBinding();
			String className = getClassName(method, FILTER_KEYWORD);
			System.out.println("[S] " + className + "->" + node.getName().toString());
			info.add("R_" + className + "->" + node.getName().toString());
			return super.visit(node);
		}
		
		@Override
		public boolean visit(MethodInvocation node){			
			IMethodBinding method = (IMethodBinding)node.resolveMethodBinding();
			String className = getClassName(method, FILTER_KEYWORD);
			System.out.println("[R] " + className + "->" + node.getName().toString());
			
			List tmp = node.arguments();
			ITypeBinding[] a =  method.getParameterTypes();			
			for(int i = 0; i < a.length; i++){
				System.out.println("   <ARG>" + a[i].getName().toString() + " " + tmp.get(i));
			}
			
			System.out.println("   <RET>" + method.getReturnType().getName().toString());
			
			if(!className.equals("")){
				info.add("R_" + className + "->" + node.getName().toString());
			}
			return super.visit(node);
		}
		
		private String getClassName(IMethodBinding method, String filterWord){
			String ret = "";
			if(method != null){
				ITypeBinding binding = method.getDeclaringClass();
				if(binding != null){
					String className = binding.getQualifiedName();
					if(!filterWord.equals("")){
						if(className.indexOf(filterWord) != -1){
							return className;
						}
					} else {
						return className;
					}
				}
			}
			return ret;
		}
		
		private ITypeBinding getClassType(IMethodBinding method){
			if(method != null){
				return method.getDeclaringClass();
			}
			return null;
		}
		
		public List<String> getInfo(){
			return info;
		}		
	}
	*/
	
	public static class FindMethodInfo extends ASTVisitor {
		private final static String FILTER_KEYWORD = "android";
		private List<ClassInfo> classInfo;
		private List<SequenceInfo> sequenceInfo;
		private SequenceInfo sequence = null;
		private int id = 0;
		private CompilationUnit unit;
		
		public FindMethodInfo(List<ClassInfo> _classInfo, int _id, CompilationUnit _unit) {
			classInfo = _classInfo;
			id = _id;
			unit = _unit;
			sequenceInfo = new ArrayList<SequenceInfo>();
		}

		@Override
		public boolean visit(MethodDeclaration node){
			IMethodBinding method = (IMethodBinding)node.resolveBinding();
			if(method == null){
				return super.visit(node);
			}
						
			String orgClassName = getClassName(method, "");
			ITypeBinding classType = getClassType(method);
			
			for(int i = 0; i < classInfo.size(); i++){
				IMethodBinding result = classInfo.get(i).checkOverride(classType, method, node.getName().toString());
				if(result != null){
					method = result;
					break;
				}
			}

			String className = getClassName(method, FILTER_KEYWORD);			
//			sequence = new SequenceInfo(orgClassName + id, new ApiInfo(className, node.getName().toString(), null, unit.getLineNumber(node.getStartPosition())));
			
			int lineNumber = unit.getLineNumber(node.getStartPosition());
			int lineLength = unit.getLineNumber(node.getStartPosition() + node.getLength());
			sequence = new SequenceInfo(orgClassName , new ApiInfo(className, node.getName().toString(), ApiInfo.KIND_METHOD, lineNumber, lineLength));
			sequenceInfo.add(sequence);
			/*
			System.out.println("[D] " + className + "->" + node.getName().toString());			
			if(method != null){
				ITypeBinding binding = method.getDeclaringClass();
				if(binding != null){
					String cName = binding.getQualifiedName();
					ITypeBinding superClass = binding.getSuperclass();
					String sCName = superClass.getQualifiedName();
					String cBName = binding.getName();
					System.out.println("DEBUG: " + cName + " " + cBName + " " + sCName);
				}
			}
			*/

			return super.visit(node);
		}
		
/*
		@Override
		public boolean visit(ConstructorInvocation node){
			System.out.println("1");
			return super.visit(node);
		}
		
		@Override
		public boolean visit(SuperMethodReference node){
			System.out.println("2");
			return super.visit(node);
		}
		
		@Override
		public boolean visit(Initializer node){
			System.out.println("3");
			return super.visit(node);
		}

		@Override
		public boolean visit(SuperConstructorInvocation node){
			System.out.println("4");			
			return super.visit(node);
		}
*/		
		@Override
		public boolean visit(SimpleName node){
			IBinding type = (IBinding)node.resolveBinding();
			if((type != null) && (type.getKind() == IBinding.VARIABLE)){
				IVariableBinding ivBinding = (IVariableBinding)node.resolveBinding();
				if(ivBinding != null){
					String className = ivBinding.getType().getQualifiedName();
					if(ivBinding.getDeclaringClass() != null){
						className = ivBinding.getDeclaringClass().getQualifiedName();
					}
//					System.out.println("\"" + className + "\",\"" + node.toString() + "\"");
					
					int lineNumber = unit.getLineNumber(node.getStartPosition());
					int lineLength = unit.getLineNumber(node.getStartPosition() + node.getLength());
					
					if(sequence != null){
						int mode = ApiInfo.KIND_VARIABLE;
						
						if(node.resolveConstantExpressionValue() != null){
							mode = ApiInfo.KIND_CONSTANT;
						}
						sequence.add(new ApiInfo(className, node.toString(), mode, lineNumber, lineLength));
					}
				}
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(ClassInstanceCreation node){
			ITypeBinding iTypeBinding = node.getType().resolveBinding();
			if(iTypeBinding != null){
				String className = iTypeBinding.getQualifiedName();
				String methodName = iTypeBinding.getName();
				
				int lineNumber = unit.getLineNumber(node.getStartPosition());
				int lineLength = unit.getLineNumber(node.getStartPosition() + node.getLength());
				if(sequence != null){
					sequence.add(new ApiInfo(className, methodName, ApiInfo.KIND_METHOD, lineNumber, lineLength));
				}
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(SuperMethodInvocation node){
			IMethodBinding method = (IMethodBinding)node.resolveMethodBinding();
			String className = getClassName(method, FILTER_KEYWORD);
			if(!className.equals("")){
//				String fullMethodString = getFullMethodString(node, className);
				if(sequence != null){
//					System.out.println("[DEBUG!] " + node.getName().toString());
					int lineNumber = unit.getLineNumber(node.getStartPosition());
					int lineLength = unit.getLineNumber(node.getStartPosition() + node.getLength());
					sequence.add(new ApiInfo(className, node.getName().toString(), ApiInfo.KIND_METHOD, lineNumber, lineLength));
				}
			}
			
//			System.out.println("[S] " + className + "->" + node.getName().toString());
			return super.visit(node);
		}
		
		private String getFullMethodString(SuperMethodInvocation node, String className){
			String fullMethodString = "";
			IMethodBinding method = (IMethodBinding)node.resolveMethodBinding();
			
			// Method Body
			fullMethodString = fullMethodString + className + " " + node.getName().toString() + " ";
			System.out.println("[R] " + className + "->" + node.getName().toString());

			List tmp = node.arguments();
			ITypeBinding[] a =  method.getParameterTypes();
			for(int i = 0; i < a.length; i++){
				System.out.println("   [ARG] " + a[i].getName().toString() + " " + tmp.get(i));
//				System.out.println("   [ARG] " + a[i].getName().toString());			
				fullMethodString = fullMethodString + a[i].getName().toString() + " " + tmp.get(i) + " ";
			}
			
			String retClassName = "";
			if(node.getParent().getNodeType() == ASTNode.CAST_EXPRESSION){
				CastExpression ce = (CastExpression)node.getParent();
				ITypeBinding binding = (ITypeBinding)ce.resolveTypeBinding();
				if(binding != null){
					retClassName = binding.getQualifiedName();
//					System.out.println("   [RETCAST] " + binding.getQualifiedName());
				}
			} else {
				retClassName = method.getReturnType().getName().toString();
//				System.out.println("   [RET] " + method.getReturnType().getName().toString());				
			}
			fullMethodString = fullMethodString + retClassName;
//			System.out.println("   [RET] " + retClassName);
//			System.out.println("   " + fullMethodString);
			
//			extractKeyword(retClassName);
			
//			String searchString = className + " " + node.getName().toString() + " " + retClassName;
//			System.out.println(searchString);
			return fullMethodString;
		}
		
		@Override
		public boolean visit(MethodInvocation node){
			IMethodBinding method = (IMethodBinding)node.resolveMethodBinding();
			String className = getClassName(method, FILTER_KEYWORD);
			
			if(!className.equals("")){
//				String fullMethodString = getFullMethodString(node, className);
				if(sequence != null){
//					System.out.println("[DEBUG!] " + node.getName().toString());
					int lineNumber = unit.getLineNumber(node.getStartPosition());
					int lineLength = unit.getLineNumber(node.getStartPosition() + node.getLength());
					sequence.add(new ApiInfo(className, node.getName().toString(), ApiInfo.KIND_METHOD, lineNumber, lineLength));
				}
			}
//			System.out.println("[R] " + className + "->" + node.getName().toString() + " " + String.valueOf(unit.getLineNumber(node.getStartPosition())));
			return super.visit(node);
		}
		
		private String getFullMethodString(MethodInvocation node, String className){
			String fullMethodString = "";
			IMethodBinding method = (IMethodBinding)node.resolveMethodBinding();
			
			// Method Body
			fullMethodString = fullMethodString + className + " " + node.getName().toString() + " ";
			System.out.println("[R] " + className + "->" + node.getName().toString());

			List tmp = node.arguments();
			ITypeBinding[] a =  method.getParameterTypes();
			for(int i = 0; i < a.length; i++){
				System.out.println("   [ARG] " + a[i].getName().toString() + " " + tmp.get(i));
//				System.out.println("   [ARG] " + a[i].getName().toString());			
				fullMethodString = fullMethodString + a[i].getName().toString() + " " + tmp.get(i) + " ";
			}
			
			String retClassName = "";
			if(node.getParent().getNodeType() == ASTNode.CAST_EXPRESSION){
				CastExpression ce = (CastExpression)node.getParent();
				ITypeBinding binding = (ITypeBinding)ce.resolveTypeBinding();
				if(binding != null){
					retClassName = binding.getQualifiedName();
//					System.out.println("   [RETCAST] " + binding.getQualifiedName());
				}
			} else {
				retClassName = method.getReturnType().getName().toString();
//				System.out.println("   [RET] " + method.getReturnType().getName().toString());				
			}
			fullMethodString = fullMethodString + retClassName;
//			System.out.println("   [RET] " + retClassName);
//			System.out.println("   " + fullMethodString);
			
//			extractKeyword(retClassName);
			
//			String searchString = className + " " + node.getName().toString() + " " + retClassName;
//			System.out.println(searchString);
			return fullMethodString;
		}
/*
		private String extractKeyword(String typeName){
			String[] dotSplit = typeName.split("\\.");
			String last = "";
			if(dotSplit.length > 1){
				last = dotSplit[dotSplit.length - 1];
			} else {
				last = typeName;
			}
			
			MorphologicalAnalysis ma = new MorphologicalAnalysis();
			ma.exec(last);
			return "";
		}
*/
		/*
		@Override
		public boolean visit(CastExpression node){
			Expression exp = node.getExpression();
			if(exp.getNodeType() == ASTNode.METHOD_INVOCATION){
				ITypeBinding binding = (ITypeBinding)node.resolveTypeBinding();
				if(binding != null){
					System.out.println("[CE]" + binding.getQualifiedName());
				}
			}
			return super.visit(node);
		}
		*/
		
		private boolean isMyClass(String myClassName){
			for(int i = 0; i < classInfo.size(); i++){
				ClassInfo info = classInfo.get(i);
				String className = info.classBinding.getQualifiedName();
				if(myClassName.equals(className)){
					return true;
				}
			}
			return false;
		}
		
		private String getClassName(IMethodBinding method, String filterWord){
			String ret = "";
			if(method != null){
				ITypeBinding binding = method.getDeclaringClass();
				if(binding != null){
					String className = binding.getQualifiedName();					
					if(!filterWord.equals("")){
//						if((className.indexOf(filterWord) != -1) || isMyClass(className)){
						if((className.startsWith(filterWord) == true) || isMyClass(className)){
							return className;
						}
					} else {
						return className;
					}
				}
			}
			return ret;
		}
		
		private ITypeBinding getClassType(IMethodBinding method){
			if(method != null){
				return method.getDeclaringClass();
			}
			return null;
		}
						
		public List<SequenceInfo> getSequenceInfo(){
			return sequenceInfo;
		}
	}
	
	public AstParser(String androidSdkPath, String androidSdkVersion){
		String androidClassPath = androidSdkPath + "platforms/android-" + androidSdkVersion +  "/android.jar";
		String androidLibraryDir = androidSdkPath + "extras/android/AndroidExtraLibrary";
		List<String> libraryPath = new ArrayList<String>();
		
		FolderOperator.getLibrary(new File(androidLibraryDir), "22.2.1", libraryPath);
		libraryPath.add(androidClassPath);
		classPathEntries = (String[])libraryPath.toArray(new String[0]);
	}

	public List<SequenceInfo> execute(String name, int id) throws Exception {
		if(name !=  null){
			// Preparing for Parser			
			final List<CompilationUnit> compList = new ArrayList<CompilationUnit>();
			String[] sourceFilePaths = {name};
			String[] sourcepathEntries = {""};

			ASTParser parser = ASTParser.newParser(AST.JLS8);
	        parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setEnvironment(classPathEntries, sourcepathEntries, null, true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setStatementsRecovery(true);

			Map<String, String> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options);

			FileASTRequestor requestor = new FileASTRequestor(){
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					compList.add(ast);
				}

				@Override
				public void acceptBinding(String bindingKey, IBinding binding) {
					// do nothing
				}
			};
			parser.createASTs(sourceFilePaths, null, new String[]{}, requestor, null);					

			// Parsing
			List<SequenceInfo> retSequence = new ArrayList<SequenceInfo>();
			for(int i = 0; i < compList.size(); i++){
				CompilationUnit unit = compList.get(i);
				
				FindClassInfo classInfo = new FindClassInfo();
				unit.accept(classInfo);
				
				FindMethodInfo methodInfo = new FindMethodInfo(classInfo.getInfo(), id, unit);
				unit.accept(methodInfo);
				
				retSequence.addAll(methodInfo.getSequenceInfo());
			}
			
			deleteEmptySequence(retSequence);
			return retSequence;
		} else {
			return null;
		}
	}
	
	private void deleteEmptySequence(List<SequenceInfo> in){
		// Search Target of Delete
		List<SequenceInfo> deleteSequence = new ArrayList<SequenceInfo>();
		for(int i = 0; i < in.size(); i++){
			SequenceInfo seq = in.get(i);
			if(seq.isEmptySeq()){
				deleteSequence.add(seq);
			}
		}
		// Delete
		for(int i = 0; i < deleteSequence.size(); i++){
			SequenceInfo seq = deleteSequence.get(i);
			in.remove(seq);
		}
	}	
}
