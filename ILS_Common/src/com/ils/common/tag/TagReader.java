/**
 *   (c) 2013-2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.inductiveautomation.ignition.common.model.BaseContext;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagSubscriptionManager;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  A Tag reader obtains the current value of a tag without
 *  creating a subscription.
 */
public class TagReader  {
	private static final String TAG = "TagReader";
	private final LoggerEx log;
	private final BaseContext context;
	
	/**
	 * Constructor.
	 */
	public TagReader(BaseContext ctx) {
		log = LogUtil.getLogger(getClass().getPackage().getName());
		this.context = ctx;
	}

	/**
	 * Read the value of a tag. The time assigned is the current
	 * time.
	 * 
	 * @param path fully qualified tag path (includes provider)
	 */
	public QualifiedValue readTag(String path) {
		
		if( context==null) return null;                   // Not initialized yet.
		if(path==null || path.isEmpty() ) return null;    // Path not set
		QualifiedValue value = null;
		try {
			TagPath tp = TagPathParser.parse(path);
			TagSubscriptionManager tsm = context.getTagManager();
			List<TagPath> paths = new ArrayList<>();
			paths.add(tp);
			List<QualifiedValue> values = tsm.read(paths);
			if( values!=null && !values.isEmpty()) value = values.get(0);
			if( log.isDebugEnabled() && !tp.getSource().equalsIgnoreCase("system")  )log.infof("%s.readTag: %s = %s",TAG,path,value.toString());
		}
		catch(IOException ioe) {
			log.warnf("%s.readTag: Exception parsing path %s",TAG,path);
		}
		catch(NullPointerException npe) {
			log.warnf("%s.readTag: Null value for path %s",TAG,path);
		}
		return value;
	}
	
}
