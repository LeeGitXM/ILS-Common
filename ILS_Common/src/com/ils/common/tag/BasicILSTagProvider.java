package com.ils.common.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagProp;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.ExtendedTagType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagEditingFlags;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagMeta;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagType;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.Flags;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.simple.WriteHandler;

/**
 * A tag provider that is not quite as "simple" as a SimpleTagProvider. This code has 
 * been reverse-engineered from the IA implementation of a SimpleTagProvider. It should 
 * be replaced at the earliest possible opportunity.
 * 
 * This code contains logic for persistent storage of the tags, but currently we avoid
 * its use. Tags created with this provider "disappear" on a Gateway restart.
 */
public class BasicILSTagProvider implements ILSTagProvider {
	protected final String TAG = "TestFrameTagProvider";
	protected final LoggerEx log;
	protected final InternalTagProvider internalProvider;
	protected final GatewayContext context;
	protected final String name;

	public BasicILSTagProvider(GatewayContext ctx,String providerName) {
		this.context = ctx;
		this.name = providerName;
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
		this.internalProvider = new InternalTagProvider(context,this.name);
		// STANDARD_STATUS does not allow renaming or deletion.
		// My attempt at extendedProperties and SUPPORTS_EXPRESSIONS doesn't work.
		Set<TagProp> extendedProperties = new HashSet<>();
		extendedProperties.add(TagProp.Expression);
		extendedProperties.add(TagProp.ExpressionType);
		extendedProperties.add(TagProp.HistoryEnabled);

		configureTagType(TagType.Custom, TagEditingFlags.STANDARD_STATUS.
				or(TagEditingFlags.SUPPORTS_VALUE_EDIT).
				or(TagEditingFlags.SUPPORTS_EXPRESSIONS).
				or(TagEditingFlags.SUPPORTS_EXPRESSION_MODES).
				or(TagEditingFlags.SUPPORTS_HISTORY), extendedProperties);
	}
	
	public GatewayContext getContext() { return this.context;  }
	public String getName() { return this.name;  }
	public void startup(GatewayContext context) {
		this.internalProvider.setContext(context);
		context.getTagManager().registerTagProvider(this.internalProvider);
	}

	public void shutdown() {
		if ((this.internalProvider != null) && (this.internalProvider.getContext() != null))
			this.internalProvider.getContext().getTagManager().unregisterTagProvider(this.name);
	}

	

	protected InternalTagProvider getInternal() {
		return this.internalProvider;
	}

	public void setProviderMetaFlag(int flag, boolean value)
	{
		getInternal().setProviderMetaFlag(flag, value);
	}

	/**
	 * Not public in the SimpleProviderInterface
	 * @param path
	 * @return
	 * @throws IllegalArgumentException if the path doesn't parse.
	 */
	public TagPath getPath(String path) throws IllegalArgumentException {
		try {
			return TagPathParser.parse(getInternal().getName(), sanitizePath(path));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Not public in the SimpleProviderInterface
	 * @param tagpath
	 * @return
	 */
	public Tag getTag(TagPath tagpath)  {
		return getInternal().getTag(tagpath);
	}
	/**
	 * Not public in the SimpleProviderInterface
	 * @param tagpath
	 * @return
	 */
	public TagDefinition getTagDefinition(TagPath tagpath)  {
		return getInternal().getTagDefinition(tagpath);
	}

	protected String sanitizePath(String path) {
		if (path == null) {
			return null;
		}

		return path.replace(".", "_");
	}

	public void updateValue(String path, Object value, Quality quality) {
		updateValue(getPath(path), value, quality);
	}

	public void updateValue(TagPath path, Object value, Quality quality)  {
		this.internalProvider.updateValue(path, value, quality);
	}

	public void updateValue(TagPath path, QualifiedValue value)  {
		this.internalProvider.updateValue(path, value);
	}

	public void configureTag(String path, DataType dType, ExtendedTagType tagType)  {
		configureTag(getPath(path), dType, tagType);
	}

	public void configureTag(TagPath path, DataType dType, ExtendedTagType tagType) {
		this.internalProvider.setupTag(path, dType, tagType);
	}

	public void removeTag(String path) {
		removeTag(getPath(path));
	}

	public void removeTag(TagPath path)  {
		this.internalProvider.removeTags(Arrays.asList(new TagPath[] { path }));
	}

	public void configureTagType(ExtendedTagType tagType, Flags editingFlags, Set<TagProp> bindableProperties) {
		TagMeta newMeta = new TagMeta(tagType, editingFlags, bindableProperties);
		this.internalProvider.configureTagType(newMeta);
	}

	public void registerWriteHandler(String path, WriteHandler handler) {
		registerWriteHandler(getPath(path), handler);
	}

	public void registerWriteHandler(TagPath path, WriteHandler handler) {
		this.internalProvider.registerWriteHandler(path, handler);
	}
}