/**
 *   (c) 2013-2021  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  Create a list of classes in the jar of a specified class. Filter out to
 *  include only those with a specified annotation. 
 *  
 */
public class ClassList {
	private static final String TAG = "ClassList";
	private final LoggerEx log; 
	
	public ClassList() {
		log = LogUtil.getLogger(getClass().getPackage().getName());
	}
	
	/** 
	 * Scan all the classes in the jar file specified by the jar pattern, returning those
	 * that have the specified annotation. The jar file
	 * must be serviced by the context class loader of current thread. 
	 * 
	 * NOTE: The absolute jar file name will be different in the Designer and Gateway contexts.
	 * 
	 * @param jarNamePattern a pattern that specifies the jar file we are interested in ...
	 * @param annotation the class of annotation (constructor) that we are interested in ..
	 * @param pattern for root of Java packages to be considered, as in "com/ils/"
	 * @return a list of loaded classes that match the criteria.
	 */
	public List<Class<?>> getAnnotatedClasses(String jarNamePattern,Class<? extends Annotation> annotation,String pattern) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		log.debugf("%s: getAnnotatedClasses ...",TAG);
		JarFile jar = jarForPattern(jarNamePattern);
		if( jar!=null ) {
			classes = findAnnotatedClassesInJar(jar,annotation,pattern);
		}
		return classes;
	}
	
	/**
	 * Find the jar file indicated by the specified pattern. We make the assumption
	 * that the target jar is served by the same class loader as this class.
	 * 
	 * NOTE: The compiler may complain about a resource leak, but it's a really bad
	 *       idea to close the class loader.
	 * @param pattern the target jar file path contains this string 
	 */
	private JarFile jarForPattern(String pattern) {
		JarFile jar = null;
		log.debugf("%s: jarForPattern - %s",TAG,pattern);
		//ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
		ClassLoader cl = getClass().getClassLoader();
		if( cl!=null && cl instanceof URLClassLoader ) {
			@SuppressWarnings("resource")
			URLClassLoader classLoader = (URLClassLoader)cl;
			URL[] urls = classLoader.getURLs();
			URL url = null;
			int index = 0;
			while( index<urls.length) {
				url = urls[index];
				log.tracef("%s: jarForPattern: resource =  %s",TAG,url.toExternalForm());
				if( url.toExternalForm().contains(pattern) &&
					url.toExternalForm().contains(".jar")      ) break;
				index++;
			}
			
			if( index<urls.length && url!=null ) {
				// Success, we've located the jar
				String path = null;
				try {
					// it is necessary to remove any URL encoded characters--e.g C:\Program%20Files
					path = java.net.URLDecoder.decode(url.getFile(), "UTF-8");
				} 
				catch (UnsupportedEncodingException e) {
					log.error("UTF-8 encoding not supported");
				}
				log.debugf("%s: jarForPattern %s - found %s",TAG,pattern,path);
				File file = new File(path);
				if( file.isFile() ) {
					try {
						jar = new JarFile(file);
					} 
					catch (IOException ioe) {
						log.warnf("%s.jarForPattern - file %s is not a jar (%s)",TAG,url.toExternalForm(),ioe.getLocalizedMessage());
					}
				}
				else {
					log.warnf("%s.jarForPattern - jar file %s does not exist",TAG, file.getPath());
				}			
			}
			else {
				log.warnf("%s.jarForPattern - jar %s not found in directory",TAG,pattern);
			}
		}
		else {
			log.warnf("%s.jarForPattern - ClassLoader not found",TAG);
		}
		return jar;
	}
	
	
	/** 
	 * Loop through all classes in the jar file. Return the subset
	 * with the indicated annotation. 
	 * The search involves instantiating classes that match. Since
	 * instantiating random classes can have unexpected side effects
	 * we limit the search to classes that belong to some parent package.
	 * Specify the package with '/', as in "com/ils/block/".
	 *
	 * @param jar the jar file containing the classes to search
	 * @param annotation the constructor annotation to test for
	 * @param pattern first part of package to be considered. 
	 * @return the classes (loaded) 
	 */
	private List<Class<?>> findAnnotatedClassesInJar(JarFile jar,Class<? extends Annotation> annotation,String pattern)  { 
		List<Class<?>> classes = new ArrayList<Class<?>>();
		Enumeration<JarEntry> jarWalker = jar.entries();
		while( jarWalker.hasMoreElements()) {
			JarEntry entry = jarWalker.nextElement();
			if( entry.getName().startsWith(pattern) && entry.getName().endsWith(".class") && !entry.isDirectory()) {
				//log.tracef("%s: findAnnotatedClassesInJar - %s",TAG,entry.getName());
				// Reject anonymous internal classes
				if( entry.getName().contains("$")) continue;
				// Convert the path to a classname
				StringBuilder className = new StringBuilder();
				for( String pak:entry.getName().split("/")) {
					if( className.length()!=0) className.append(".");
					className.append(pak);
					if( pak.endsWith(".class")) className.setLength(className .length()-".class".length());
				}
				Class<?> clss = null;
				try {
					log.tracef("%s: findAnnotatedClassesInJar - class %s",TAG,className.toString());
					clss = Class.forName(className.toString());
					if( clss!=null ) {
						if( clss.getAnnotation(annotation) !=null ) {
							log.debugf("%s: %s is annotated",TAG,className.toString());
							classes.add(clss);
						}
					}
				}
				catch(Exception cnf) {
					log.warnf("%s: findAnnotatedClassesInJar - error instantiating %s (%s) ",TAG,entry.getName(),cnf.getLocalizedMessage());
				}
			}

		} 
		return classes; 
	} 
}
