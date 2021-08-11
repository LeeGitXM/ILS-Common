/**
 *   (c) 2013-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.ignition.common.model.CommonContext;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.tags.model.SecurityContext;
import com.inductiveautomation.ignition.common.tags.model.TagManager;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;

/**
 *  A Tag reader obtains the current value of a tag without
 *  creating a subscription.
 */
public class TagReader  {
	private static final String CLSS = "TagReader";
	private final ILSLogger log;
	private final CommonContext context;
	
	/**
	 * Constructor.
	 */
	public TagReader(CommonContext ctx) {
		log = LogMaker.getLogger(this);
		this.context = ctx;
	}

	/**
	 * Read the value of a tag given a tag path string. The time assigned is the current
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
			TagManager tm = context.getTagManager();
			List<TagPath> paths = new ArrayList<>();
			paths.add(tp);
			CompletableFuture<List<QualifiedValue>> future = tm.readAsync(paths,SecurityContext.systemContext());
			List<QualifiedValue> values;
			try {
				values = future.get();
				if( values!=null && !values.isEmpty()) value = values.get(0);
				if( !tp.getSource().equalsIgnoreCase("system")  )log.debugf("%s.readTag: %s = %s",CLSS,path,value.toString());
			} 
			catch (InterruptedException iex) {
				log.warnf("%s.readTag: Interupted getting value for path %s",CLSS,path);
			} 
			catch (ExecutionException eex) {
				log.warnf("%s.readTag: Execution exception for path %s (%s)",CLSS,path,eex.getLocalizedMessage());
			}
		}
		catch(IOException ioe) {
			log.warnf("%s.readTag: Exception parsing path %s (%s)",CLSS,path,ioe.getCause().getMessage());
		}
		catch(NullPointerException npe) {
			log.warnf("%s.readTag: Null value for path %s",CLSS,path);
		}
		return value;
	}
	
}
