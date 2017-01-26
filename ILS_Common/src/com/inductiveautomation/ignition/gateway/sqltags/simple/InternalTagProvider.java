/**
 *   (c) 2015-2017  ILS Automation. All rights reserved.
 *   Code based on de-compiled Inductive Automation class
 *   "InternalTagProvider", embedded in a SimpleTagProvider.
 */
package com.inductiveautomation.ignition.gateway.sqltags.simple;

import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.sqltags.BasicTagValue;
import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagPathTree;
import com.inductiveautomation.ignition.common.sqltags.model.TagProp;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataQuality;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.ExtendedTagType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagValue;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.contexts.TagUninitializeContext;
import com.inductiveautomation.ignition.gateway.sqltags.execution.TagBasedConfigProvider;
import com.inductiveautomation.ignition.gateway.sqltags.model.TagExecutor;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.EntityId;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.TagConfig;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.TagStoreObject;
/**
 * This class is a TagProvider used internally by our ILSTagProviders.
 * The package is dictated because the base class has a "package" scope.
 */
public class InternalTagProvider extends SimpleTagProviderInternal {
	private final Object configLock = new Object(); 
	private final TagPathTree<TagStoreObject<TagConfig>> extensions = new TagPathTree<>();

	public InternalTagProvider(GatewayContext ctx,String name) {
		super(name);
		setContext(ctx);
	}
	protected void addTag(TestFrameTag t) {
		TagStoreObject<TagConfig> obj = null;
		TagDefinition ext = null;
		synchronized (this.configLock) {
			obj = (TagStoreObject<TagConfig>)this.extensions.get(t.getTagPath());
			ext = obj == null ? null : (TagDefinition)((TagConfig)obj.getEntity()).getDefinition();

			t.applyExtension(ext);

			this.tree.insertTag(t.getTagPath(), t);
		}
		notifyFolder(t.getTagPath().getParentPath());
	}
	
	public TagDefinition getTagDefinition(TagPath path) {
		TagStoreObject<TagConfig> obj = null;
		TagDefinition tagdef = null;
		synchronized (this.configLock) {
			obj = (TagStoreObject<TagConfig>)this.extensions.get(path);
			if( obj!=null ) {
				tagdef = (TagDefinition)((TagConfig)obj.getEntity()).getDefinition();
			}
		}
		return tagdef;
	}
	public void setupTag(TagPath path, DataType type, ExtendedTagType tagType) {
		Tag t = getTag(path);
		if ((t == null) || (!(t instanceof TestFrameTag)))
			addTag(new TestFrameTag(path, type, tagType));
		else
			((TestFrameTag)t).configureTypes(type, tagType);
	}
	
	/**
	 * We use these for testing. SimpleTag is a nested class of SimpleTagProviderInternal
	 */
	protected class TestFrameTag extends SimpleTag {
		private static final long serialVersionUID = 1L;
		private final TagValue value = new BasicTagValue(null, DataQuality.STALE);
		private TagDefinition ext = null;   // Shadows super-class
		private ExtendedTagType tagType = TagType.Custom;

		public TestFrameTag(EntityId id) {
			super(id);
		}

		public TestFrameTag(TagPath path) {
			super(path);
			setTagPath(path);
		}

		public TestFrameTag(TagPath path, DataType type) {
			this(path);
			setDataType(type);
		}

		public TestFrameTag(TagPath path, DataType type, ExtendedTagType tagType) {
			this(path, type);
			this.tagType = tagType;
		}


		public String getHistoricalScanclass() {
			return this.historizer == null ? null : this.historizer.getHistoricalScanclass();
		}

		public String getHistoricalProvider() {
			return this.historizer == null ? null : this.historizer.getProvider();
		}
/*
		public void configureTypes(DataType dType, ExtendedTagType tType) {
			boolean changed = (getDataType() != dType) || (!TypeUtilities.equals(this.tagType, tType));

			setDataType(dType);
			this.tagType = tType;
			if (changed)
				notifyChange(null);
		}
*/
		public void applyExtension(TagDefinition extensionDef)   {
			if (isInitialized()) {
				uninitialize(false);
			}
			this.ext = extensionDef;
			if ((extensionDef != null) && (extensionDef.getDataType() == null)) {
				extensionDef.setDataType(getDataType());
			}
			if (this.ext == null) {
				setConfigProvider(null);
			} else {
				setConfigProvider(new TagBasedConfigProvider(this.ext));
			}
			configure(this.ext);
			resetEvaluation();
			evaluate();
		}

		public void uninitialize(boolean deleted) {
			TagUninitializeContext ctx = new TagUninitializeContext();
			ctx.setShutdownQuality(deleted ? DataQuality.CONFIG_ERROR : null);
			unInitialize(ctx);

			InternalTagProvider.this.unregisterHistoricalTag(this);
		}

		public void configure(Tag def) {
			super.configure(def);
			initialize(null);
			InternalTagProvider.this.registerHistoricalTag(this);
		}

		public boolean isEnabled() {return true; }

		public TagExecutor getExecutor()  {
			return InternalTagProvider.this.getSimpleTagExecutor();
		}

		public void updateValue(Object val) {
			updateCurrentValue(new BasicTagValue(val, DataQuality.GOOD_DATA));
		}

		public void updateValue(Object val, Quality quality) {
			updateCurrentValue(new BasicTagValue(val, quality));
		}

		public void updateQuality(Quality quality) {
			updateCurrentValue(new BasicTagValue(this.value.getValue(), quality));
		}

		public boolean supportsCurrentValueUpdate() {
			return true;
		}

		public TagValue getAttribute(TagProp prop) {
			if (prop == TagProp.TagTypeSubCode) {
				return new BasicTagValue(this.tagType.getSubType());
			}

			return super.getAttribute(prop);
		}

		public TagType getType() {
			return this.tagType.getCoreType();
		}
	}
}