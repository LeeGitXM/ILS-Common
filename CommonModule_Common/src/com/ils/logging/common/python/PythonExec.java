/**
 * Copyright 2016 ILS Automation. All rights reserved.
 */
package com.ils.logging.common.python;

import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.core.PyStringMap;

import com.inductiveautomation.ignition.common.model.CommonContext;
import com.inductiveautomation.ignition.common.script.JythonExecException;
import com.inductiveautomation.ignition.common.script.ScriptManager;

/** 
 * An object that can call a particular method in Python. 
 * The code is compiled on first execution, then cached. 
 */
public class PythonExec {
	private static final String RESULT_NAME = "pyCallResult";
	private final String methodName; // package + method name
	private final String[] argNames; // args to the method, if any
	private final Class<?> returnType;	// is null if no return value
	private PyCode compiledCode;	// cached compiled code
	private boolean valid = true;

	private static CommonContext context = null;
	
	public PythonExec(String methodName, Class<?> returnType, String...args) {
		this.methodName = methodName;
		this.argNames = args;
		this.returnType = returnType;
	}
	
	public String getMethodName() { return this.methodName;}
	public boolean isValid()      { return this.valid; }
	
	/** Execute this method and return the result. */
	public Object exec(Object...argValues) throws JythonExecException {
		ScriptManager scriptMgr = context.getScriptManager();
		PyStringMap localMap = scriptMgr.createLocalsMap();

		// Compiling will throw an exception, if unsuccessful
		if(compiledCode == null) {
			compileCode();
		}
		PyStringMap globalsMap = scriptMgr.getGlobals();
		for(int i = 0; i < argNames.length; i++) {
			localMap.__setitem__(argNames[i], Py.java2py(argValues[i]));
		}

		scriptMgr.runCode(compiledCode, localMap, globalsMap);
		if (returnType != null) {
			PyObject pyResult = localMap.__getitem__(RESULT_NAME);
			Object result = pyResult.__tojava__(returnType);
			return result;
		}
		else {
			return null;
		}
	}
	
	public static String getErrorMsg(JythonExecException ex) {
		Throwable lowestCause = getLowestCause(ex);
		String msg1 = lowestCause.getMessage();
		String msg2 = ex.toString();
		String msg = msg1 + "\n\n" + msg2;
		return msg;
	}

	private static Throwable getLowestCause(Throwable ex) {
		if( ex.getCause() == null || ex.getCause().equals(ex)) {
			return ex;
		}
		else {
			return getLowestCause(ex.getCause());
		}		
	}

	/** Compile and cache code to call this method. */
	private void compileCode() {
		StringBuffer buf = new StringBuffer();
		
		int dotIndex = methodName.lastIndexOf(".");
		if(dotIndex != -1) {
			buf.append("import ");
			buf.append(methodName.substring(0, dotIndex));
			buf.append("; ");
		}
		buf.append(RESULT_NAME);
		buf.append(" = ");
		buf.append(methodName);
		buf.append("(");
		for(int i = 0; i < argNames.length; i++) {
			if(i > 0) buf.append(',');
			buf.append(argNames[i]);
		}
		buf.append(')');
		String script = buf.toString();
		compiledCode = Py.compile_flags(script, "ils", CompileMode.exec, CompilerFlags.getCompilerFlags());		
	}

	public static void setContext(CommonContext ctx) {
		context = ctx;
	}

}
