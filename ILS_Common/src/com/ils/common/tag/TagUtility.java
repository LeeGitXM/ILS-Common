package com.ils.common.tag;



/**
 * This class presents several static utility methods dealing with
 * tags and tag paths.
 */
public class TagUtility
{
	/**
	 * Replace tag provider in a fully-qualified tag path string
	 * @return a short-form abbreviation for the specified unit.
	 */
	public static String providerNameFromPath(String path) {
		String provider = "";
		if( path.startsWith("[") ) {
			int index = path.indexOf(']');
			if( index>0 ) {
				provider = path.substring(1,index);
			}
		}
		return provider;
	}
	/**
	 * Replace tag provider in a fully-qualified tag path string
	 * @return a short-form abbreviation for the specified unit.
	 */
	public static String replaceProviderInPath(String provider,String path) {
		String tagPath = path;
		
		int pos = path.indexOf("]");
		if(pos>0) path = path.substring(pos+1);
		tagPath = String.format("[%s]%s",provider,path);
			
		return tagPath;
	}
}
