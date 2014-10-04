/**
 *  Copyright (c) 2014  ILS Automation. All rights reserved. 
 */
package com.ils.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Given a Java class, creates a .py file with stub functions corresponding
 *  to the static functions in the class.
 */
/**
 * @author rforbes
 *
 */
public class PythonStubGenerator {
	
	public static void main(String[] args) {
		try {
			if(args.length != 3) {
				System.out.println("usage: PythonStubGenerator <fullClassName> <pyPackage> <dirPath>");
				System.exit(1);
			}
			String className = args[0];
			String pkg = args[1];
			String dirPath = args[2];
			generateStubs(className, pkg, dirPath);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Given a Java class, create a .py file with stub functions corresponding
     *  to the static functions in the class.
	 * @param className the full name of the Java class (i.e. including package)
	 * @param pyPackage the Python module name, dot separated
	 * @param dir	the directory to put the .py file in (not including package sub-dirs)
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public static void generateStubs(String className, String pyModule, String dir) throws FileNotFoundException, ClassNotFoundException {
		Class<?> aClass = Class.forName(className);
		String filepath = dir + File.separator + pyModule.replace(".", "/") + ".py";
		File file = new File(filepath);
		file.getParentFile().mkdirs();
		PrintWriter out = new PrintWriter(file);
		for(Method method: aClass.getMethods()) {
			if(Modifier.isStatic(method.getModifiers())) {
				out.print("def " + method.getName() + "(");
				int pnum = 0;
				Class<?>[] paramTypes = method.getParameterTypes();
				for(int i = 0; i < paramTypes.length; i++ ) {
					if(i > 0) {
						out.print(", ");
					}
					String pname = paramTypes[i].getSimpleName().toLowerCase() + pnum++;
					pname = pname.replace("[]", "Array");
					out.print(pname);					
				}
				out.println("):");
				out.println("\tpass");
				out.println();
			}
		}
		out.close();
	}
	
	
 /*
  * Sample use of this class within an ant build script 
  * 
	 <!-- This target is used within the Block Language Toolkit to
	     build stubs for Python exposed through the ScriptManager -->
	<target name="generateBLTStubs" >
		<java classname="com.ils.common.PythonStubGenerator" failonerror="true">
			<arg value="com.ils.blt.gateway.GatewayBlockScriptFunctions"/>
			<arg value="system.ils.blt.block"/>
			<arg value="${basedir}/src"/>
			<classpath>
				<pathelement location="${basedir}/../../../common/workspace/ILS_Common/bin"/>
				<pathelement location="${basedir}/../BLT_Gateway/bin"/>
				<pathelement location="${basedir}/../../../ignition-77/lib/ignition-common.jar"/>
				<pathelement path="${java.class.path}"/>
			</classpath>
		</java>
		<java classname="com.ils.common.PythonStubGenerator" failonerror="true">
			<arg value="com.ils.blt.common.ApplicationScriptFunctions"/>
			<arg value="system.ils.blt.block"/>
			<arg value="${basedir}/src"/>
			<classpath>
				<pathelement location="${basedir}/../../../common/workspace/ILS_Common/bin"/>
				<pathelement location="${basedir}/../BLT_Gateway/bin"/>
				<pathelement location="${basedir}/../../../ignition-77/lib/ignition-common.jar"/>
				<pathelement path="${java.class.path}"/>
			</classpath>
		</java>
	</target>
  */
}
