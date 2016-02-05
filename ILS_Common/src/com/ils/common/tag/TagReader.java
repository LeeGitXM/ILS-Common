/**
 *   (c) 2013-2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.TagProvider;

/**
 *  A Tag reader obtains the current value of a tag without
 *  creating a subscription.
 */
public class TagReader  {
	private static final String TAG = "TagReader";
	private final LoggerEx log;
	private final GatewayContext context;
	
	/**
	 * Constructor.
	 */
	public TagReader(GatewayContext ctx) {
		log = LogUtil.getLogger(getClass().getPackage().getName());
		this.context = ctx;
	}

	/**
	 * Read the value of a tag. The time assigned is the current
	 * time.
	 * 
	 * @param provider tag provider. Use an empty string for the default provider
	 * @param path fully qualified tag path
	 */
	public QualifiedValue readTag(String path) {
		
		if( context==null) return null;                   // Not initialized yet.
		if(path==null || path.isEmpty() ) return null;    // Path not set
		QualifiedValue result = null;
		try {
			TagPath tp = TagPathParser.parse(path);
			String providerName = tp.getSource();
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			if( provider!=null) {
				Tag tag = provider.getTag(tp);
				if( tag!=null ) result = tag.getValue();
			} 
			if( log.isDebugEnabled() && !tp.getSource().equalsIgnoreCase("system")  )log.infof("%s.readTag: %s = %s",TAG,path,result.toString());
		}
		catch(IOException ioe) {
			log.warnf("%s.readTag: Exception parsing path %s",TAG,path);
		}
		catch(NullPointerException npe) {
			log.warnf("%s.readTag: Null value for path %s",TAG,path);
		}
		return result;
	}
	
}
