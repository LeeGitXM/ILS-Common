/**
 *   (c) 2015-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.TagProp;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel;
import com.inductiveautomation.ignition.common.tags.model.SecurityContext;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.model.TagProvider;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 *  Update a SQLTag.
 */
public class TagWriter  {
	protected static final String CLSS = "TagWriter";
	private static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT);
	protected final LoggerEx log;
	protected final GatewayContext context;
	/**
	 * Constructor.
	 */
	public TagWriter(GatewayContext ctxt) {
		this.context = ctxt;
		log = LogUtil.getLogger(getClass().getPackage().getName());
	}

	/**
	 * Yet another set of inputs for a write. The provider is derived from the 
	 * path "source". If the path is empty, we fail silently.
	 * @param path
	 * @param val
	 */
	public QualityCode write(String path, String value,long timestamp) {
		if( path==null || path.isEmpty()) return QualityCode.Error_InvalidPathSyntax;
		TagPath tagPath = TagPathParser.parseSafe(path);
		Date ts = new Date(timestamp);  // Now
		return write(tagPath,value,ts);
	}
	/**
	 * This is the general form of a write, assuming good quality.
	 * @param tagPath
	 * @param value
	 * @param timestamp
	 */
	public QualityCode write(TagPath tagPath,String value,Date timestamp) {
		return write(tagPath,value,QualityCode.Good,timestamp);
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
	 */
	public QualityCode write(TagPath tagPath,QualifiedValue value) {
		String providerName = tagPath.getSource();
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider!=null ) {

			return write(provider,tagPath,value);
		}
		else {
			log.warnf("%s.write: Provider not found in path %s",CLSS,providerName);
			return QualityCode.Error_InvalidPathSyntax;
		}
	}
	/**
	 * This is a simplified form of a tag write given the value as a string. 
	 * We convert the datatype to match the datatype of the tag.
	 * 
	 * @param tagPath
	 * @param value
	 */
	public QualityCode write(TagPath tagPath,String value) {
		return write(tagPath,value,QualityCode.Good,new Date());
	}
	/**
	 * This is the general form of a write given the value as a string. The provider is derived from the 
	 * path "source". We convert the datatype to match the datatype of the tag.
	 * 
	 * NOTE: We have not been able to set the tag's value to bad. Suggest using a
	 * null as a marker for a bad value.
	 * 
	 * @param tagPath
	 * @param value
	 * @param quality
	 * @param timestamp
	 */
	public QualityCode write(TagPath tagPath,String val,QualityCode quality,Date timestamp) {
		String providerName = tagPath.getSource();
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider!=null ) {
			if( timestamp==null ) timestamp = new Date();
			QualifiedValue tv = convertDatatype(provider,tagPath,val);
			return write(provider,tagPath, tv);
		}
		else {
			log.warnf("%s.write: Provider not found in path %s",CLSS,tagPath.toStringFull());
			return QualityCode.Error_InvalidPathSyntax;
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
	private synchronized QualifiedValue convertDatatype(TagProvider provider,TagPath tagPath, String val) {
		log.debugf("%s.convertDatatype: %s = %s",CLSS,tagPath.toStringFull(),val);
		QualityCode q = QualityCode.Good;
		List<TagPath> paths = new ArrayList<>();
		paths.add(tagPath);
		CompletableFuture<List<TagConfigurationModel>> future  = provider.getTagConfigsAsync(paths,false,false);
		List<TagConfigurationModel> tags;
		Object value = val;
		try {
			tags = future.get();
			if( tags!=null && !tags.isEmpty()) {
				DataType dtype = (DataType)tags.get(0).get(TagProp.DataType);
				try {
					if( val!=null && (val.equalsIgnoreCase("BAD") || val.equalsIgnoreCase("NaN")) ) {
						val = null;
					}


					if( dtype==DataType.Float4 || dtype==DataType.Float8 )    {
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
				}
				catch(ParseException pe) {
					log.warnf("%s.convertDatatype: ParseException setting %s(%s) to %s (%s - expecting %s)",CLSS,
							tagPath.toStringFull(),dtype.name(),val,pe.getLocalizedMessage(),
							DATETIME_FORMAT);
					q = QualityCode.Error_TypeConversion;
				}
				catch(NumberFormatException nfe) {
					log.warnf("%s.convertDatatype: NumberFormatException setting %s(%s) to %s (%s)",CLSS,
							tagPath.toStringFull(),dtype.name(),val,nfe.getLocalizedMessage());
					q = QualityCode.Error_TypeConversion;
				}

			}
			else {
				log.warnf("%s.convertDatatype: Tag %s, not found during attempted write",CLSS,tagPath.toString());
			}
		} 
		catch (InterruptedException ie) {
			log.warnf("%s.convertDatatype: Interrupted getting %s (%s)",CLSS,tagPath.toStringFull(),ie.getLocalizedMessage());
			q = QualityCode.Error_TimeoutExpired;	
		} 
		catch (ExecutionException ee) {
			log.warnf("%s.convertDatatype: Execution getting %s (%s)",CLSS,tagPath.toStringFull(),ee.getLocalizedMessage());
			q = QualityCode.Error_TagExecution;

		}
		return new BasicQualifiedValue(value,q,new Date());  // Always now.
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
	public synchronized QualityCode write(TagProvider provider,TagPath tagPath, QualifiedValue qv) {
		if(tagPath==null  ) return QualityCode.Error_InvalidPathSyntax;    // Path not set
		List<TagPath> paths = new ArrayList<>();
		paths.add(tagPath);
		List<QualifiedValue> values = new ArrayList<>();
		values.add(qv);
		List<QualityCode> codes = write(provider,paths,values);
		QualityCode q = QualityCode.Bad;
		if( codes!=null && codes.size()>0 ) q = codes.get(0);
		return q;
	}

	/**
	 * Write multiple tags.
	 * 
	 * @param tagPath
	 * @param qv, the new tag value. Use the time-stamp, but ignore the quality.
	 */
	public List<QualityCode> write(TagProvider provider,List<TagPath>paths, List<QualifiedValue> values) {
		List<QualityCode> qualities = new ArrayList<>();
		if( paths.size()>0) {
			if( context==null) {
				for(TagPath tp:paths) {
					qualities.add(QualityCode.Bad_Failure);                   // Not initialized yet.
				}
			}
			else {
				CompletableFuture<List<QualityCode>> future = provider.writeAsync(paths, values,SecurityContext.systemContext());
				try {
					qualities = future.get();
					qualities.add(QualityCode.Good); 

				}
				catch (InterruptedException iex) {
					log.warnf("%s.write: Interrupted getting value for multiple paths",CLSS);
					qualities.add(QualityCode.Bad_Failure);                   // Not initialized yet.
				} 
				catch (ExecutionException eex) {
					log.warnf("%s.write: Execution exception for multiple paths (%s)",CLSS,eex.getLocalizedMessage());
					qualities.add(QualityCode.Bad_Failure);                   // Not initialized yet.
				}
			}
		}
		return qualities;
	}
}
