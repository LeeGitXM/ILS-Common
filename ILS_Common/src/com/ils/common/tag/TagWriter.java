/**
 *   (c) 2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataQuality;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.TagProvider;
import com.inductiveautomation.ignition.gateway.sqltags.model.BasicAsyncWriteRequest;
import com.inductiveautomation.ignition.gateway.sqltags.model.WriteRequest;

/**
 *  Update a SQLTag.
 */
public class TagWriter  {
	protected static final String TAG = "TagWriter";
	private static final String DATETIME_FORMAT = "yyyy/MM/dd hh:mm:ss";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT);
	protected final LoggerEx log;
	protected final GatewayContext context;
	private final List<WriteRequest<TagPath>> list;
	private final ProviderRegistry providerRegistry;
	/**
	 * Constructor.
	 */
	public TagWriter(GatewayContext ctxt,ProviderRegistry reg) {
		this.context = ctxt;
		this.list = new ArrayList<WriteRequest<TagPath>>();
		this.providerRegistry = reg;
		log = LogUtil.getLogger(getClass().getPackage().getName());
	}
	/**
	 * Add a request to the current list. Ignore nulls.
	 * @param path
	 * @param value
	 */
	public void appendRequest(String path,Object value) {
		if( path==null || value==null) return;
		LocalRequest req = new LocalRequest(path,value);
		if(req.isValid)list.add(req);
	}
	
	public void clear() { list.clear(); }
	public ProviderRegistry getProviderRegistry() { return providerRegistry; }
	/**
	 * Update the tags already added to the request list. All tags in the list
	 * are assumed to belong to the same provider. At the end of this update,
	 * the request list is automatically cleared.
	 * 
	 * For tags that belong to a "standard" provider, the timestamp is always
	 * the current time. Otherwise, the timestamp is taken from the qualified
	 * value, when supplied.
	 * 
	 * @param providerName
	 */
	public void updateTags(String providerName) {
		// For a "standard" provider, simply blast the entire list.
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null )  {
			List<Quality> qualities = provider.write(list, null, true);    // true-> isSystem to bypass permission checks
			int index = 0;
			for(Quality q:qualities) {
		    	if(!q.isGood()) {
		    		log.warnf("%s.updateTags: bad write to tag %s; value: %s quality: %s", TAG, 
		    				list.get(index).getTarget().toStringFull(), list.get(index).getValue().toString(), qualities.get(index).getName());
		    	}
		    	index++;
	    	}
		}
		else {
			ILSTagProvider prov = providerRegistry.getProvider(providerName);
			if( prov!=null ) {
				// For a "simple" provider, write a qualified value one by one.
				for(WriteRequest<TagPath> r:list) {
					if( r instanceof LocalRequest ) {
						LocalRequest req = (LocalRequest)r;
						if(!req.isValid ) continue;
						QualifiedValue qv = req.getQualifiedValue();
						if( qv!=null ) {
							prov.updateValue(req.getTarget(), qv);
						}
						else {
							write(prov,req.getTarget(),req.getValue().toString(),null);  // Current time
						}
					}
					else {
						log.warnf("%s.updateTags: Found %s in list instead of LocalRequest",TAG,r.getClass().getName());
					}
				}
			}
			else {
				log.warnf("%s.updateTags: Provider %s not found",TAG,providerName);
			}
		}
		clear();   // Don't attempt to write these again
	}

	/**
	 * Update the tags already added to the request list. All tags in the list
	 * are assumed to belong to the same provider. At the end of this update,
	 * the request list is automatically cleared.
	 * 
	 * The supplied timestamp is used to override the timestamp in the individual
	 * requests. This is currently useful only for "simple" providers.
	 * 
	 * @param providerName
	 * 
	 */
	public void updateTags(String providerName,Date timestamp) {
		// Try the ILS provider first.
		ILSTagProvider prov = providerRegistry.getProvider(providerName);
		if( prov!=null ) {
			// For a "simple" provider, write a qualified value one by one.
			for(WriteRequest<TagPath> r:list) {
				if( r instanceof LocalRequest ) {
					LocalRequest req = (LocalRequest)r;
					if(!req.isValid ) continue;
					QualifiedValue qv = req.getQualifiedValue();
					if( qv!=null ) {
						BasicQualifiedValue bqv = new BasicQualifiedValue(qv.getValue(),qv.getQuality(),timestamp);
						prov.updateValue(req.getTarget(), bqv);
					}
					else {
						write(prov,req.getTarget(),req.getValue().toString(),timestamp); 
					}
				}
				else {
					log.warnf("%s.updateTags: Found %s in list instead of LocalRequest",TAG,r.getClass().getName());
				}
			}
		}
		else {
			// For a "standard" provider, it is not possible to set the timestamp. Don't bother.
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			if( provider != null )  {
				List<Quality> qualities = provider.write(list, null, true);    // true-> isSystem to bypass permission checks
				int index = 0;
				for(Quality q:qualities) {
			    	if(!q.isGood()) {
			    		log.warnf("%s.updateTags: bad write to tag %s; value: %s quality: %s", TAG, 
			    				list.get(index).getTarget().toStringFull(), list.get(index).getValue().toString(), qualities.get(index).getName());
			    	}
			    	index++;
		    	}
			}
			else {
				log.warnf("%s.updateTags: Provider %s not found",TAG,providerName);
			}	
		}
		clear();   // Don't attempt to write these again
	}

	/**
	 * This is the general form of a write. The provider is derived from the 
	 * path "source". If the path is empty, we fail silently.
	 * @param path
	 * @param val
	 */
	public void write(String path, String value,long timestamp) {
		if( path==null || path.isEmpty()) return; 
		TagPath tagPath = TagPathParser.parseSafe(path);
		String providerName = tagPath.getSource();
		ILSTagProvider iprovider = providerRegistry.getProvider(providerName);
		if( iprovider!=null) {
			write(iprovider,tagPath, value, new Date(timestamp));
		}
		else {
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			if( provider!=null ) {
				write(provider,tagPath, value);
			}
		}
	}
	/**
	 * Yet another set of inputs for a write. The provider is derived from the 
	 * path "source".
	 * @param path
	 * @param val
	 */
	public void write(TagPath tagPath, String value,Date timestamp) {
		String providerName = tagPath.getSource();
		ILSTagProvider iprovider = providerRegistry.getProvider(providerName);
		if( iprovider!=null) {
			write(iprovider,tagPath, value, timestamp);
		}
		else {
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			if( provider!=null ) {
				write(provider,tagPath, value);
			}
		}
	}
	
	/**
	 * Update a tag with a single value. If the value is "BAD", then conclude that 
	 * the quality is bad and do not update the value. This method is exclusively
	 * for a "Standard" provider.
	 * 
	 * @param tagPath
	 * @param val, the new tag value.
	 */
	public void write(TagProvider provider,TagPath tagPath, String val) {
		log.debugf("%s.write: %s = %s",TAG,tagPath.toStringFull(),val);

		Tag tag  = provider.getTag(tagPath);
		if( tag!=null ) {
			DataType dtype = tag.getDataType();
			Object value = val;
			DataQuality qual = DataQuality.GOOD_DATA;
			try {
				if( val.equalsIgnoreCase("BAD") || val.equalsIgnoreCase("NaN") ) {
					// leave the value as-is
					qual = DataQuality.OPC_BAD_DATA;
				}
				else if( dtype==DataType.Float4 ||
						dtype==DataType.Float8 )     value = Double.parseDouble(val);
				else if( dtype==DataType.Int1 ||
						dtype==DataType.Int2 ||
						dtype==DataType.Int4 ||
						dtype==DataType.Int8   )     value =  (int)Double.parseDouble(val);
				else if( dtype==DataType.Boolean)    value = Boolean.parseBoolean(val);
				else if( dtype==DataType.DateTime)   value = dateFormat.parse(val);
				else value = val.toString();

				List<WriteRequest<TagPath>> singleRequestList = createTagList(tagPath,value);
				provider.write(singleRequestList, null, true);    // true-> isSystem to bypass permission checks
			}
			catch(ParseException pe) {
				log.warnf("%s.write: ParseException setting %s(%s) to %s (%s - expecting %s)",TAG,
						tagPath.toStringFull(),dtype.name(),val,pe.getLocalizedMessage(),
						DATETIME_FORMAT);
			}
			catch(NumberFormatException nfe) {
				log.warnf("%s.write: NumberFormatException setting %s(%s) to %s (%s)",TAG,
						tagPath.toStringFull(),dtype.name(),val,nfe.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.write: Tag %s, not found",TAG,tagPath.toString());
		}
	}


	/**
	 * Update a tag with a single value. If the value is "BAD", then conclude that 
	 * the quality is bad and do not update the value. This method is exclusively
	 * for a "Simple" provider.
	 * 
	 * @param tagPath
	 * @param val, the new tag value.
	 * @param timestamp - the new value will be assigned this timestamp.
	 */
	public void write(ILSTagProvider provider,TagPath tagPath, String val,Date timestamp) {
		if( timestamp==null ) timestamp = new Date();  // Now
		log.debugf("%s.write %s: %s = %s",TAG,dateFormat.format(timestamp),tagPath.toStringFull(),val);
		Tag tag = provider.getTag(tagPath);
		if( tag!=null ) {
			DataType dtype = tag.getDataType();
			Object value = val;
			Quality qual = DataQuality.GOOD_DATA;
			try {
				if( val.equalsIgnoreCase("BAD") || val.equalsIgnoreCase("NaN") ) {
					// leave the value as-is
					qual = DataQuality.OPC_BAD_DATA;
				}
				else if( dtype==DataType.Float4 ||
						dtype==DataType.Float8 )     value = Double.parseDouble(val);
				else if( dtype==DataType.Int1 ||
						dtype==DataType.Int2 ||
						dtype==DataType.Int4 ||
						dtype==DataType.Int8   )     value =  (int)Double.parseDouble(val);
				else if( dtype==DataType.Boolean)    value = Boolean.parseBoolean(val);
				else if( dtype==DataType.DateTime)   value = dateFormat.parse(val);
				else value = val.toString();

				QualifiedValue qv = new BasicQualifiedValue(value,qual,timestamp);
				provider.updateValue(tagPath, qv);

			}
			catch(ParseException pe) {
				log.warnf("%s.write: ParseException setting %s(%s) to %s (%s - expecting %s)",TAG,
						tagPath.toStringFull(),dtype.name(),val,pe.getLocalizedMessage(),
						DATETIME_FORMAT);
			}
			catch(NumberFormatException nfe) {
				log.warnf("%s.write: NumberFormatException setting %s(%s) to %s (%s)",TAG,
						tagPath.toStringFull(),dtype.name(),val,nfe.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.write: Tag %s, not found",TAG,tagPath.toString());
		}
	}

	
	/** 
	 * Create a list containing a single tag value. (For a standard provider).
	 */
	private List<WriteRequest<TagPath>> createTagList(TagPath path,Object value) {
		List<WriteRequest<TagPath>> singleRequestList = new ArrayList<WriteRequest<TagPath>>();
		LocalRequest req = null;
		//log.infof("%s.createTagList: path = %s",TAG,path);
		req = new LocalRequest(path,value);
		if(req.isValid)singleRequestList.add(req);
		return singleRequestList;
	}
	/**
	 * Create a tag write request. 
	 */
	public class LocalRequest extends BasicAsyncWriteRequest<TagPath> {
		public boolean isValid = false;
		private QualifiedValue qv = null;

		public LocalRequest( TagPath tp,Object value) {
			super();
			initialize(tp,value);
		}

		public LocalRequest(String path,Object val) {
			super();
			try {
				TagPath tp = TagPathParser.parse(path);
				initialize(tp,val);
			}
			catch( IOException ioe) {
				log.warnf("%s.localRequest: Exception parsing %s (%s)",TAG,path,ioe.getMessage());
			}
		}


		private void initialize(TagPath tp,Object value) {
			if( log.isTraceEnabled()) log.tracef("%s: localRequest; adding %s",TAG,tp.toStringFull());
			if( value!=null && value instanceof QualifiedValue  ) {
				this.qv = (QualifiedValue)value;
				this.setTarget(tp);
				this.setValue(qv.getValue());
				this.setResult(qv.getQuality());
				this.isValid = true;
			}
			else if( value!=null)  {
				this.setTarget(tp);
				this.setValue(value);
				if( value.toString().equals("NaN") ||
						value.toString().equals("BAD")	) {
					this.setResult(DataQuality.OPC_BAD_DATA);
				}
				else {
					this.setResult(DataQuality.GOOD_DATA);
				}
				this.isValid = true;
			}
		}


		public QualifiedValue getQualifiedValue() { return qv; }
	}
	
}
