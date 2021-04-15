/**
 *   (c) 2013  ILS Automation. All rights reserved. 
 */
package com.ils.common;

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;

/**
 * This class is designed as a Singleton for easy access in the client/designer scopes.
 * It manages text and images resources  for purposes of internationalization. 
 * 
 * Instead of being wrapped into an internal bundle (inside a .jar file), these
 * resources are located in an external file.
 *
 * Two static methods are given for construction. The first (which must be called
 * for actual creation of the Singleton) accepts the name of the resource collection 
 * and path. The second, no-argument constructor, simply retrieves the configured instance.
 */
public class ResourceManager  {
	public static String TAG = "ResourceManager";
	private String root;
	private String path;
	private ResourceBundle bundle;
	private static boolean initialized = false;
	private static ResourceManager instance = null;
	private final ILSLogger log;
	
	/**
	 * This method is invoked once the ResourceManager has been 
	 * configured. An IllegalStateException is thrown if the
	 * class has never been initialized.
	 * 
	 * @return ResourceManager the instance.
	 */
	public static ResourceManager getInstance() {
		synchronized(ResourceManager.class) {
			if( !initialized || instance==null ) throw new IllegalStateException(TAG+"instance has never been initialized with resource path");
		}
		return instance;
	}
	/**
	 * Per the Singleton design pattern, the actual constructor is private.
	 * .
	 * @param name default properties are in a file named <root>.properties
	 * @param path absolute path the the resources root directory
	 */
	private ResourceManager(String root,String path)  {
		this.root = root;
		this.path = path;
		this.log = LogMaker.getLogger(getClass().getPackage().getName());
	    File file = new File(path);  
	    try {
	    	URL[] urls = {file.toURI().toURL()};  
	    	URLClassLoader loader = new URLClassLoader(urls); 
	    	bundle = ResourceBundle.getBundle(root,Locale.getDefault(),loader);
	    }
	    catch(MalformedURLException mue) {
	    	log.error(TAG+": Bad resource path: "+path+" ("+mue.getLocalizedMessage()+")");
	    }
	    initialized = true;
	}

	/**
	 * Create an instance of an ILSResourceManager given a
	 * path to the root of the resource directory.
	 * @param name root name of the property files containing text resources
	 * @param path absolute path the the resources root directory
	 */
	public static ResourceManager createResourceManager(String name,String path) {
		synchronized(ResourceManager.class) {
			if( instance==null ) {
				instance = new ResourceManager(name,path);
			}
		}
		return instance;
	}
	
	/**
	 * Look up the keyed resource in the resource bundle. If the string is
	 * not found, return the key in square brackets.
	 * 
	 * @param key resource key
	 * @return the localized resource
	 */
	public String getString(String key) {
		String text = null;
		try {
			text = bundle.getString(key);
		}
		catch(Exception ex) {
			log.debug(TAG+": Missing resource: "+key+" ("+ex.getLocalizedMessage()+")");
			text = "["+key+"]";
		}
		return text;
	}
	
	/**
	 * Retrieve an image from the "image" sub-directory of the 
	 * resource root directory.
	 * 
	 * @param name the fileName of the image.
	 * @return the image linked to the key. If no image is found,
	 * return a button-with-question-mark icon.
	 */
	public Image getImage(String name) {
		String imagePath=path+File.pathSeparator+"images"+File.pathSeparator+name;
		Image img = new ImageIcon(imagePath).getImage();
		if( img==null ) {
			imagePath = "/images/unknown.png";
			img = new ImageIcon(getClass().getResource(imagePath)).getImage();
		}
		return img;
	}
	
	/**
	 * Reset the locale used for resource retrieval. Note that locale
	 * can be constructed from either a language or language/country pair.
	 * 
	 * @param locale the new locale.
	 */
	public void setLocale(Locale locale) {
		File file = new File(path);  
	    try {
	    	URL[] urls = {file.toURI().toURL()};  
	    	URLClassLoader loader = new URLClassLoader(urls); 
	    	bundle = ResourceBundle.getBundle(root,locale,loader);
	    }
	    catch(MalformedURLException mue) {
	    	log.error(TAG+": Changing locale: Bad resource path: "+path+" ("+mue.getLocalizedMessage()+")");
	    }
	}
}