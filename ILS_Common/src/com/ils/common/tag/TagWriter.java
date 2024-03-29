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

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.sqltags.BasicTagValue;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataQuality;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagValue;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.TagProvider;
import com.inductiveautomation.ignition.gateway.sqltags.execution.tags.StaticTag;
import com.inductiveautomation.ignition.gateway.sqltags.model.BasicAsyncWriteRequest;
import com.inductiveautomation.ignition.gateway.sqltags.model.WriteRequest;

/**
 *  Update a SQLTag.
 */
public class TagWriter  {
	protected static final String CLSS = "TagWriter";
	private static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT);
	protected final LoggerEx log;
	protected final GatewayContext context;
	protected final List<WriteRequest<TagPath>> list;
	/**
	 * Constructor.
	 */
	public TagWriter(GatewayContext ctxt) {
		this.context = ctxt;
		this.list = new ArrayList<WriteRequest<TagPath>>();
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
		if(req.isValid)getList().add(req);
	}
	
	public void clear() { getList().clear(); }
	
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
			List<Quality> qualities = provider.write(getList(), null, true);    // true-> isSystem to bypass permission checks
			int index = 0;
			for(Quality q:qualities) {
		    	if(!q.isGood()) {
		    		log.warnf("%s.updateTags: bad write to tag %s; value: %s quality: %s", CLSS, 
		    				getList().get(index).getTarget().toStringFull(), getList().get(index).getValue().toString(), qualities.get(index).getName());
		    	}
		    	index++;
	    	}
		}
		clear();   // Don't attempt to write these again
	}

	/**
	 * Update the tags already added to the request list. All tags in the list
	 * are assumed to belong to the same provider. At the end of this update,
	 * the request list is automatically cleared.
	 * 
	 * The supplied time-stamp is used to override the time-stamp in the individual
	 * requests, if non-null. This is the interface used by AED.
	 * 
	 * @param providerName
	 * 
	 */
	public void updateTags(String providerName,Date timestamp) {
		if( timestamp==null ) {
			updateTags(providerName);  // Use the simpler form.
		}
		else {
			// For a "standard" provider, in order to set the time-stamp, the quantities must be a TagValue
			List<WriteRequest<TagPath>> tagValueList = convertRequests(getList());
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			if( provider != null )  {
				List<Quality> qualities = provider.write(tagValueList, null, true);    // true-> isSystem to bypass permission checks
				int index = 0;
				for(Quality q:qualities) {
					if(!q.isGood()) {
						log.warnf("%s.updateTags: bad write to tag %s; value: %s quality: %s", CLSS, 
								getList().get(index).getTarget().toStringFull(), getList().get(index).getValue().toString(), qualities.get(index).getName());
					}
					index++;
				}
			}
			else {
				log.warnf("%s.updateTags: Provider %s not found",CLSS,providerName);
			}	

		}
		clear();   // Don't attempt to write these again
	}

	/**
	 * Yet another set of inputs for a write. The provider is derived from the 
	 * path "source". If the path is empty, we fail silently.
	 * @param path
	 * @param val
	 */
	public void write(String path, String value,long timestamp) {
		if( path==null || path.isEmpty()) return;
		TagPath tagPath = TagPathParser.parseSafe(path);
		Date ts = new Date(timestamp);
		write(tagPath,value,ts);
	}
	/**
	 * This is the general form of a write, assuming good quality.
	 * @param tagPath
	 * @param value
	 * @param timestamp
	 */
	public void write(TagPath tagPath,String value,Date timestamp) {
		write(tagPath,value,DataQuality.GOOD_DATA,timestamp);
	}
	/**
	 * This is the general form of a write. The provider is derived from the 
	 * path "source". If time-stamp is null, then we get the current time.
	 * This is the only version (currently) that uses the history delegate.
	 * 
	 * NOTE: We have not been able to set the tag's value to bad. Suggest using a
	 * null as a marker for a bad value.
	 * 
	 * @param tagPath
	 * @param value
	 * @param timestamp
	 */
	public void write(TagPath tagPath,String value,DataQuality quality,Date timestamp) {
		String providerName = tagPath.getSource();

		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider!=null ) {
			if( timestamp!=null ) {
				TagValue tv = new BasicTagValue(value,quality,timestamp);
				write(provider,tagPath, tv);
			}
			else {
				write(provider,tagPath, value);
			}
		}
	}
	
	/**
	 * Update a tag with a single value at the current time. If the value is "BAD",
	 * then conclude that the quality is bad. We are unable to actually set the
	 * tag's quality, so instead we set the value to null.
	 * 
	 * @param tagPath
	 * @param val, the new tag value.
	 */
	public void write(TagProvider provider,TagPath tagPath, String val) {
		log.debugf("%s.write: %s = %s",CLSS,tagPath.toStringFull(),val);

		Tag tag  = provider.getTag(tagPath);
		if( tag!=null ) {
			DataType dtype = tag.getDataType();
			
			try {
				if( val!=null && (val.equalsIgnoreCase("BAD") || val.equalsIgnoreCase("NaN")) ) {
					val = null;
				}
				Object value = val;
				
				if( dtype==DataType.Float4 ||
						 dtype==DataType.Float8 )    {
					if( val==null ) value = Double.NaN;
					else value = Double.parseDouble(val);
				}
				else if( dtype==DataType.Int1 ||
						 dtype==DataType.Int2 ||
						 dtype==DataType.Int4 ||
						 dtype==DataType.Int8   )  {
					if( val==null ) value = (int) Double.NaN;
					value =  (int)Double.parseDouble(val);
				}
				else if( dtype==DataType.Boolean)  {
					if( val!=null ) value = Boolean.parseBoolean(val);
				}
				else if( dtype==DataType.DateTime)  {
					if( val!=null ) value = dateFormat.parse(val);
				}
				else {
					if(val!=null) value = val.toString();
				}

				List<WriteRequest<TagPath>> singleRequestList = createTagList(tagPath,value);
				provider.write(singleRequestList, null, true);    // true-> isSystem to bypass permission checks
			}
			catch(ParseException pe) {
				log.warnf("%s.write: ParseException setting %s(%s) to %s (%s - expecting %s)",CLSS,
						tagPath.toStringFull(),dtype.name(),val,pe.getLocalizedMessage(),
						DATETIME_FORMAT);
			}
			catch(NumberFormatException nfe) {
				log.warnf("%s.write: NumberFormatException setting %s(%s) to %s (%s)",CLSS,
						tagPath.toStringFull(),dtype.name(),val,nfe.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.write: Tag %s, not found",CLSS,tagPath.toString());
		}
	}
	

	/**
	 * Update a tag with a single value at the specified time-stamp (maybe).
	 * Make sure the data value matches the tag type. The Ignition
	 * default conversion has trouble with scientific notation.
	 * 
	 * NOTE: By using tag.updateCurrentValue() we can set the timestamp to
	 * something other than NOW. However, we cannot set the quality to other
	 * then GOOD_DATA.
	 * 
	 * @param tagPath
	 * @param qv, the new tag value. Use the time-stamp, but ignore the quality.
	 */
	public void write(TagProvider provider,TagPath tagPath, QualifiedValue qv) {
		StaticTag tag = (StaticTag)provider.getTag(tagPath);
		if( tag!=null ) {
			DataType dt = tag.getDataType();
			Object obj = qv.getValue();
			if( obj !=null && obj instanceof String ) {
				if( obj!=null && (obj.toString().equalsIgnoreCase("BAD") || 
						          obj.toString().equalsIgnoreCase("NaN")) ) {
					obj = null;
				}
				// Do our own conversion to match tag type
				try {
					if( dt.equals(DataType.Float4) || dt.equals(DataType.Float8)) {
						if( obj==null) {
							obj = Double.NaN;
						}
						else {
							obj = Double.parseDouble(obj.toString());
						}
						
					}
					else if( dt==DataType.Int1 || dt==DataType.Int2 ||
							dt==DataType.Int4 || dt==DataType.Int8   )    {
						if( obj==null) {
							obj = (int)Double.NaN;
						}
						else {
							obj =  (int)Double.parseDouble(obj.toString());
						}
					}
					else if( dt==DataType.Boolean)    {
						if(obj!=null) obj = Boolean.parseBoolean(obj.toString());
					}
					else if( dt==DataType.DateTime)  {
						obj = dateFormat.parse(obj.toString());
					}
					TagValue tv = new BasicTagValue(obj,DataQuality.GOOD_DATA,qv.getTimestamp());
					tag.updateCurrentValue(tv);
					//log.infof("%s.write: %s value: %s at %s", CLSS, tagPath.toStringFull(),tv.getValue(),tv.getTimestamp());
					
				}
				catch(ParseException pe) {
					log.warnf("%s.write: ParseException setting %s(%s) to %s (%s - expecting %s)",CLSS,
							tagPath.toStringFull(),dt.name(),obj,pe.getLocalizedMessage(),DATETIME_FORMAT);
				}
				catch(NumberFormatException nfe ) {
					log.warnf("%s.write: Attempt to write %s to %s, a numeric tag",CLSS,qv.getValue().toString(),tagPath.toString());
				}
			}
			// Attempt to write the object as-is
			else {
				//log.infof("%s.write: %s value: %s at %s", CLSS, tagPath.toStringFull(),qv.getValue(),qv.getTimestamp());
				TagValue tv = new BasicTagValue(qv.getValue(),DataQuality.GOOD_DATA,qv.getTimestamp());
				tag.updateCurrentValue(tv);
			}
		}
		else {
			log.warnf("%s.write: Tag %s, not found",CLSS,tagPath.toString());
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
	
	private List<WriteRequest<TagPath>> convertRequests(List<WriteRequest<TagPath>> inList) {
		List<WriteRequest<TagPath>> outList = new ArrayList<>();
		for( WriteRequest<TagPath>req:inList ) {
			if( req instanceof LocalRequest ) {
				outList.add(new LocalRequest((LocalRequest)req));  // Upgrades to TagValue
			}
			else {
				outList.add(req);  // Shouldn't happen
			}
		}
		return outList;
	}
	protected List<WriteRequest<TagPath>> getList() {
		return list;
	}
	// ==================================== Local Request ==========================
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
		
		/**
		 * Copy constructor that converts a qualified value
		 * in the original to a TagValue in the result
		 * @param req
		 */
		public LocalRequest( LocalRequest req) {
			super(req.getTarget(),req.getQualifiedValue());
			if( qv!=null ) {
				qv = new BasicTagValue(qv.getValue(),qv.getQuality(),qv.getTimestamp());
			}
		}

		public LocalRequest(String path,Object val) {
			super();
			try {
				TagPath tp = TagPathParser.parse(path);
				initialize(tp,val);
			}
			catch( IOException ioe) {
				log.warnf("%s.localRequest: Exception parsing %s (%s)",CLSS,path,ioe.getMessage());
			}
		}


		private void initialize(TagPath tp,Object value) {
			if( log.isTraceEnabled()) log.tracef("%s: localRequest; adding %s",CLSS,tp.toStringFull());
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
