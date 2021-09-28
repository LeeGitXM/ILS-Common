/**
 *   (c) 2012-2021  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  The introspector is a debug tool that uses Java reflection
 *  to log object characteristics.
 *  
 *  Taken from "Core Java: Vol 1", by Cay Horstmann, Gary Cornell
 */
public class Introspector {
	private static LoggerEx logger = LogUtil.getLogger(Introspector.class.getPackage().getName());
	private final static String INDENT = "   "; // Indent for each level of a nested dump
	private Object obj;
	
	public Introspector(Object target) {
		obj = target;
	}
	
	/**
	 * Dump all properties of a class to the log4j output.
	 */
	public void log() {
		try {
		    logClass();
		    logConstructors();
		    logMethods();
		    logMembers();
		}
		catch(ClassNotFoundException cnfe) {
			logger.info("log: Exception ("+cnfe.getLocalizedMessage()+")");
		}
	}
	
	/**
	 * Log the class, superclass and interfaces to the log4j output.
	 */
	public void logClass() throws ClassNotFoundException {
		Class<?> clss = obj.getClass();
		Class<?> sclss= clss.getSuperclass();
		String modifiers = Modifier.toString(clss.getModifiers());
		logger.info(modifiers+" "+clss.getName()+" extends "+sclss.getName());
	}
	/**
	 * Log all the constructors for class of the target object.
	 */
	public void logConstructors() {
		logger.info("Constructors:");
		Class<?> clss = obj.getClass();
		Constructor<?>[] constructors = clss.getDeclaredConstructors();
		for(Constructor<?> c: constructors) {
			String name = c.getName();
			String modifiers = Modifier.toString(c.getModifiers());
			
			String parameters = "";
			Class<?>[] paramClasses = c.getParameterTypes();
			for (Class<?> pClass : paramClasses) {
				if(parameters.length()>0) parameters = parameters+",";
				parameters = parameters+pClass.getName();
			}
			logger.info("   "+modifiers+" "+name+"("+parameters+")");
		}
	}
	public void logMembers() {
		logger.info("Members:");
		Class<?> clss = obj.getClass();
		Field[] fields = clss.getDeclaredFields();
		for(Field f: fields) {
			String name = f.getName();
			String modifiers = Modifier.toString(f.getModifiers());
			
			logger.info("   "+modifiers+" "+name);
		}
	}
	
	public void logMethods() {
		logger.info("Methods:");
		Class<?> clss = obj.getClass();
		Method[] methods = clss.getDeclaredMethods();
		for(Method m: methods) {
			String name = m.getName();
			String modifiers = Modifier.toString(m.getModifiers());
			Class<?> rtn = m.getReturnType();
			
			String parameters = "";
			Class<?>[] paramClasses = m.getParameterTypes();
			for (Class<?> pClass : paramClasses) {
				if(parameters.length()>0) parameters = parameters+",";
				parameters = parameters+pClass.getName();
			}
			logger.info("   "+modifiers+" "+name+"("+parameters+") returns "+rtn.getSimpleName());
		}
	}
	/**
	 * Traverse a component tree, logging the children. Start at the top.
	 * @param c the component
	 * @param maxDepth
	 */
	public static void tree(Component c,int maxDepth) { 
		tree(c,maxDepth,0);
	}
	/**
	 * Traverse a component tree, logging the children.
	 * @param c the component
	 * @param maxDepth
	 * @param currentDepth
	 */
	public static void tree(Component c,int maxDepth,int currentDepth) {
		// Indent the different between depths
		StringBuilder indent = new StringBuilder();
		int index = maxDepth - currentDepth;
		while( index<maxDepth ) {
			indent.append(INDENT);
			index++;
		}
		logger.info("Tree: "+indent.toString()+c.getClass().getSimpleName()+"\t"+c.getName());
		if( c instanceof java.awt.Container ) {
			Container container = (Container)c;
			Component[] children = container.getComponents();
			currentDepth++;
			for( Component child:children ) {
				tree(child,maxDepth,currentDepth);
			}
		}
	}
}
