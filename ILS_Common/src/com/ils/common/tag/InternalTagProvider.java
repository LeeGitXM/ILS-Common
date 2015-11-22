package com.ils.common.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.inductiveautomation.ignition.common.DiagnosticsSample;
import com.inductiveautomation.ignition.common.NamedValue;
import com.inductiveautomation.ignition.common.QualifiedPath;
import com.inductiveautomation.ignition.common.TypeUtilities;
import com.inductiveautomation.ignition.common.expressions.ExpressionParseContext;
import com.inductiveautomation.ignition.common.expressions.FunctionFactory;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.opc.BrowseElement;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.sqltags.BasicTagValue;
import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.TagProviderMetaImpl;
import com.inductiveautomation.ignition.common.sqltags.model.ScanClass;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagDiagnostics;
import com.inductiveautomation.ignition.common.sqltags.model.TagManagerBase;
import com.inductiveautomation.ignition.common.sqltags.model.TagNode;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagPathTree;
import com.inductiveautomation.ignition.common.sqltags.model.TagProp;
import com.inductiveautomation.ignition.common.sqltags.model.TagProviderInformation;
import com.inductiveautomation.ignition.common.sqltags.model.TagProviderMeta;
import com.inductiveautomation.ignition.common.sqltags.model.TagTree;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataQuality;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.ExtendedTagType;
import com.inductiveautomation.ignition.common.sqltags.model.types.ScanClassComparison;
import com.inductiveautomation.ignition.common.sqltags.model.types.ScanClassMode;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagMeta;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagValue;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.sqltags.scanclasses.ScanClassDefinition;
import com.inductiveautomation.ignition.common.sqltags.scripts.TagScriptManager;
import com.inductiveautomation.ignition.common.sqltags.tagpaths.SourceAlteredTagPath;
import com.inductiveautomation.ignition.common.sqltags.tags.FolderTag;
import com.inductiveautomation.ignition.common.sqltags.tags.TagDiff;
import com.inductiveautomation.ignition.common.user.AuthenticatedUser;
import com.inductiveautomation.ignition.common.util.Flags;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.TagProvider;
import com.inductiveautomation.ignition.gateway.sqltags.contexts.DirectTagEvaluationContext;
import com.inductiveautomation.ignition.gateway.sqltags.contexts.TagEvaluationContext;
import com.inductiveautomation.ignition.gateway.sqltags.contexts.TagUninitializeContext;
import com.inductiveautomation.ignition.gateway.sqltags.execution.TagBasedConfigProvider;
import com.inductiveautomation.ignition.gateway.sqltags.execution.scripts.GatewayTagScriptManager;
import com.inductiveautomation.ignition.gateway.sqltags.execution.tags.AbstractExecutableTag;
import com.inductiveautomation.ignition.gateway.sqltags.model.ExecutableScanClass;
import com.inductiveautomation.ignition.gateway.sqltags.model.ExecutableTag;
import com.inductiveautomation.ignition.gateway.sqltags.model.TagExecutor;
import com.inductiveautomation.ignition.gateway.sqltags.model.TagStoreListener;
import com.inductiveautomation.ignition.gateway.sqltags.model.TagSubscriptionModel;
import com.inductiveautomation.ignition.gateway.sqltags.model.TagValueListener;
import com.inductiveautomation.ignition.gateway.sqltags.model.TagWriteValidator;
import com.inductiveautomation.ignition.gateway.sqltags.model.WriteRequest;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.EntityId;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.TagConfig;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.TagPropertyValue;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.TagStore;
import com.inductiveautomation.ignition.gateway.sqltags.model.tagstore.TagStoreObject;
import com.inductiveautomation.ignition.gateway.sqltags.scanclasses.SimpleExecutableScanClass;
import com.inductiveautomation.ignition.gateway.sqltags.simple.CustomTagStore;
import com.inductiveautomation.ignition.gateway.sqltags.simple.ProviderSubscriptionModel;
import com.inductiveautomation.ignition.gateway.sqltags.simple.SimpleTagProviderProfileRecord;
import com.inductiveautomation.ignition.gateway.sqltags.simple.WriteHandler;
/**
 * This class is a TagProvider used internally by our ILSTagProviders.
 */
public class InternalTagProvider implements TagProvider {
	private GatewayContext context;
	private ProviderSubscriptionModel subModel;
	private TagScriptManager tagScriptMgr;
	private final Object configLock = new Object();
	private final TagTree tree = new TagTree();
	private final TagPathTree<TagStoreObject<TagConfig>> extensions = new TagPathTree<>();
	private final TagPathTree<WriteHandler> writeHandlers = new TagPathTree<>();
	private final Map<EntityId, TagPath> extMap = new HashMap<>();
	private final Map<EntityId, SimpleExecutableScanClass> scanClasses = new HashMap<>();
	private final Map<String, Set<ExecutableTag>> histTags = new HashMap<>();
	private Set<String> histProvSet = new HashSet<>();
	private final TagProviderMetaImpl meta;
	private final QualifiedPath igPath;
	private TagStore internalTagStore = null;
	private final Logger log;
	private final SimpleTagExecutor tagExecutor = new SimpleTagExecutor();

	private static boolean profileTableVerified = false;

	public InternalTagProvider(GatewayContext ctx,String name) {
		this.meta = new TagProviderMetaImpl(name, "");
		this.igPath = QualifiedPath.of(new String[] { "prov", name });
		this.meta.setFlag(1, true);
		this.log = Logger.getLogger(String.format("SQLTags.TagProviders.Provider[%s]", new Object[] { name }));
	}

	public String getName() {
		return this.meta.getName();
	}

	protected Logger getLogger() {
		return this.log;
	}

	public void setProviderMetaFlag(int flag, boolean value) {
		this.meta.setFlag(flag, value);
	}

	private static void checkProfileRecordTable(GatewayContext context) throws Exception {
		synchronized (InternalTagProvider.class) {
			if (!profileTableVerified) {
				profileTableVerified = true;
				context.getSchemaUpdater().updatePersistentRecords(new RecordMeta[] { SimpleTagProviderProfileRecord.META });
			}
		}
	}

	protected TagPath getPath(String path) throws IllegalArgumentException {
		try {
			return TagPathParser.parse(getName(), path);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
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

	protected DataType typeFromValue(Object val) {
		return val == null ? DataType.String : DataType.getTypeForClass(val.getClass());
	}

	protected TestFrameTag getOrAdd(TagPath path, Object defaultVal) {
		Tag t = getTag(path);
		if ((t == null) || (!(t instanceof TestFrameTag))) {
			DataType defaultType = typeFromValue(defaultVal);
			t = new TestFrameTag(path, defaultType);
			addTag((TestFrameTag)t);
		}
		return (TestFrameTag)t;
	}

	protected void notifyFolder(TagPath folderPath) {
		if (this.subModel == null) {
			return;
		}
		Tag folder = null;
		synchronized (this.configLock) {
			folder = this.tree.getTag(folderPath);
			if (folder == null) {
				folder = new FolderTag(folderPath);
			}
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().debug("Notifying folder path of change: " + folderPath == null ? "[null]" : folderPath
					.toStringFull());
		}
		this.subModel.notifyTagChange(folderPath, folder, null);
	}

	public void notifyFolders(Collection<TagPath> paths) {
		if ((paths == null) || (paths.size() == 0)) {
			return;
		}
		for (TagPath p : paths)
			notifyFolder(p);
	}

	public void setupTag(TagPath path, DataType type, ExtendedTagType tagType) {
		Tag t = getTag(path);
		if ((t == null) || (!(t instanceof TestFrameTag)))
			addTag(new TestFrameTag(path, type, tagType));
		else
			((TestFrameTag)t).configureTypes(type, tagType);
	}

	public void updateValue(TagPath path, Object value, Quality quality) {
		getOrAdd(path, value).updateValue(value, quality);
	}

	public void updateQuality(TagPath path, Quality value) {
		getOrAdd(path, null).updateQuality(value);
	}

	public void updateValue(TagPath path, QualifiedValue value) {
		getOrAdd(path, value.getValue()).updateCurrentValue(new BasicTagValue(value));
	}

	public void configureTagType(TagMeta meta) {
		this.meta.registerTypeType(meta);
	}

	public List<Tag> browse(TagPath path) {
		return new ArrayList<Tag>(this.tree.getChildren(path));
	}

	public Tag getTag(TagPath path) {
		synchronized (this.configLock) {
			return this.tree.getTag(path);
		}
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
	public GatewayContext getContext() {
		return this.context;
	}

	public void setContext(GatewayContext context) {
		this.context = context;
		this.tagScriptMgr = new GatewayTagScriptManager(context);
	}

	protected TagStore getTagStore() {
		return this.internalTagStore;
	}

	public void startup(TagSubscriptionModel model) {
		try {
			checkProfileRecordTable(getContext());
		} catch (Exception e) {
			getLogger()
			.error("Error verifying profile table. Stored tag configurations will likely not be available.", e);
		}

		this.subModel = new ProviderSubscriptionModel(this, model);

		SimpleTagProviderProfileRecord rec = (SimpleTagProviderProfileRecord)this.context.getPersistenceInterface()
				.find(SimpleTagProviderProfileRecord.META, new Object[] { 
						getName() });
		long storeId = 0L;
		if (rec != null)
		{
			rec.resetUseCount();
			this.context.getPersistenceInterface().save(rec);
			storeId = rec.getProviderId();
		}

		if (storeId > 0L)
			try {
				startTagStore(this.context.getTagManager().createTagStore(storeId).getStore());
			} catch (Exception e) {
				getLogger()
				.error("Error instantiating storage engine for provider. Stored tag configuration won't be available.", e);
			}
	}

	protected void commissionNewTagStore() {
		getLogger().info("Creating new tag store for provider '" + getName() + "'.");

		SimpleTagProviderProfileRecord rec = (SimpleTagProviderProfileRecord)this.context.getPersistenceInterface()
				.find(SimpleTagProviderProfileRecord.META, new Object[] { 
						getName() });
		CustomTagStore store = null;
		try {
			if (rec == null) {
				rec = (SimpleTagProviderProfileRecord)this.context.getPersistenceInterface().createNew(SimpleTagProviderProfileRecord.META);
				rec.setName(getName());
				rec.resetUseCount();
			}
			store = this.context.getTagManager().createTagStore(0L);
			rec.setProviderId(store.getId());
			this.context.getPersistenceInterface().save(rec);

			if (store.getId() > 0L) {
				startTagStore(store.getStore());

				addScanClasses(Arrays.asList(new ScanClass[] { new ScanClassDefinition("Default Historical", ScanClassMode.Direct, 10000, 10000, "", ScanClassComparison.Equal, 0.0D, 
						Integer.valueOf(10000), 
						new Flags()) }));
			}
		} catch (Exception e) {
			getLogger()
			.error("Error instantiating storage engine for provider. Stored tag configuration won't be available.", e);
		}
	}

	public void shutdown() {
		if (this.subModel != null) {
			this.subModel.shutdown();
		}

		if (this.internalTagStore != null) {
			this.internalTagStore.shutdown();
		}
		if (this.context != null)
			this.context.getTagManager().unregisterTagProvider(getName());
	}

	protected void startTagStore(TagStore store) {
		if (store == null) {
			getLogger().warn("Tag store did not get loaded correctly for provider " + getName());
			return;
		}
		try {
			this.internalTagStore = store;
			this.internalTagStore.addConfigurationListener(new TagStoreListener()
			{
				public void tagstoreItemsRemoved(Collection<EntityId> tagIds, Collection<EntityId> scanclassIds)
				{
					InternalTagProvider.this.applyDeletes(tagIds, scanclassIds);
				}

				public void tagstoreItemsAdded(Collection<TagStoreObject<TagConfig>> tags, Collection<TagStoreObject<ScanClass>> scanClasses)
				{
					InternalTagProvider.this.applyUpdates(tags, scanClasses);
				}
			});
			this.internalTagStore.startup();
			getLogger().debug("Tag store started successfully.");
		} catch (Exception e) {
			getLogger().error("Error starting tag store for provider '" + getName() + "'", e);
		}
	}

	protected void applyUpdates(Collection<TagStoreObject<TagConfig>> tags, Collection<TagStoreObject<ScanClass>> scanClasses) {
		Set<TagPath> parents = new HashSet<>();
		List<TestFrameTag> toNotify = new ArrayList<>();
		synchronized (this.configLock) {
			if ((tags != null) && (tags.size() > 0)) {
				for (TagStoreObject<TagConfig> tso : tags) {
					TagConfig cfg = (TagConfig)tso.getEntity();
					TagPath p = new SourceAlteredTagPath((TagPath)cfg.getPath(), getName());

					TestFrameTag ez = (TestFrameTag)this.tree.getTag(p);
					if (ez != null) {
						ez.applyExtension((TagDefinition)cfg.getDefinition());
						toNotify.add(ez);
					}
					this.extensions.insertValue(p, tso);
					this.extMap.put(tso.getId(), p);
					parents.add(p.getParentPath());
				}
			}
			if ((scanClasses != null) && (scanClasses.size() > 0)) {
				for (TagStoreObject<ScanClass> sc : scanClasses) {
					SimpleExecutableScanClass newSC = new SimpleScanClass((ScanClass)sc.getEntity(), getName(), sc.getId());
					SimpleExecutableScanClass existing = (SimpleExecutableScanClass)this.scanClasses.get(sc.getId());
					if (existing != null)
						existing.transferExecution(newSC);
					else {
						newSC.startExecution();
					}
					this.scanClasses.put(sc.getId(), newSC);
				}
			}
		}

		for (TestFrameTag t : toNotify) {
			this.subModel.notifyTagChange(t.getTagPath(), t, null);
		}

	}

	protected void applyDeletes(Collection<EntityId> tagIds, Collection<EntityId> scIds) {
		Set parents = new HashSet<>();
		synchronized (this.configLock) {
			if ((tagIds != null) && (tagIds.size() > 0)) {
				for (EntityId id : tagIds) {
					TagPath path = (TagPath)this.extMap.remove(id);
					if (this.extensions.remove(path) != null) {
						TestFrameTag r = (TestFrameTag)this.tree.getTag(path);
						if (r != null)
						{
							r.applyExtension(null);
							parents.add(path.getParentPath());
						}
					}
				}
			}
			if ((scIds != null) && (scIds.size() > 0)) {
				for (EntityId id : scIds) {
					SimpleExecutableScanClass sc = (SimpleExecutableScanClass)this.scanClasses.remove(id);
					if (sc != null) {
						sc.stopExecution();
					}
				}
			}
		}
		notifyFolders(parents);
	}

	public boolean isPublic() {
		return true;
	}

	public TagProviderMeta getInformation() {
		return this.meta;
	}

	public TagProviderInformation getStatusInformation() {
		TagProviderInformation ret = new TagProviderInformation(getName());

		List scInfo = new ArrayList();
		synchronized (this.configLock) {
			for (ExecutableScanClass sc : this.scanClasses.values()) {
				List info = sc.getStatusInformation();
				scInfo.addAll(info);
			}
			ret.getProperties().add(new NamedValue("Status.SQLTags.ProviderInfo.TagCount", Integer.valueOf(this.tree.size())));
			ret.getProperties().add(new NamedValue("Status.SQLTags.ProviderInfo.ScanClassCount", Integer.valueOf(this.scanClasses.size())));
		}
		ret.setScanClassInfo(scInfo);

		return ret;
	}

	public TagDiagnostics getTagDiagnostics(TagPath path) {
		return null;
	}

	public List<QualifiedValue> read(List<TagPath> paths, AuthenticatedUser user, boolean isSystem)   {
		List<QualifiedValue> ret = new ArrayList<>(paths.size());
		for (int i = 0; i < paths.size(); i++) {
			TagPath path = (TagPath)paths.get(i);
			Tag tag = getTag(path);
			if (tag == null)
				ret.add(TagValue.NOT_FOUND);
			else {
				ret.add(tag.getAttribute(path.getProperty()));
			}
		}
		return ret;
	}

	public void registerWriteHandler(TagPath path, WriteHandler handler) {
		synchronized (this.writeHandlers) {
			this.writeHandlers.insertValue(path, handler);
		}
	}

	protected WriteHandler getWriteHandler(TagPath path) {
		synchronized (this.writeHandlers) {
			return (WriteHandler)this.writeHandlers.get(path);
		}
	}

	public List<Quality> write(List<WriteRequest<TagPath>> requests, AuthenticatedUser user, boolean isSystem)  {
		if ((requests == null) || (requests.size() == 0)) {
			return null;
		}
		TagWriteValidator validator = new TagWriteValidator(this, user, isSystem);
		List<Quality> results = new ArrayList<>(requests.size());
		for (int i = 0; i < requests.size(); i++) {
			WriteRequest req = (WriteRequest)requests.get(i);
			WriteHandler handler = getWriteHandler((TagPath)req.getTarget());
			Quality res = null;
			if (handler == null)
				res = DataQuality.ACCESS_DENIED;
			else {
				res = validator.validateEntry(req);
			}
			if (res == null) {
				res = handler.write((TagPath)req.getTarget(), req.getValue());
			}
			results.add(res);
		}
		return results;
	}

	public List<ScanClass> getScanClasses() {
		synchronized (this.configLock) {
			List ret = new ArrayList();
			for (SimpleExecutableScanClass sc : this.scanClasses.values()) {
				ret.add(sc.getScanClass());
			}
			return ret;
		}
	}

	public void addScanClasses(List<ScanClass> scanClasses) throws Exception {
		if (this.internalTagStore == null) {
			commissionNewTagStore();
		}
		if (this.internalTagStore != null)
			getTagStore().addScanClasses(scanClasses);
	}

	protected List<EntityId> getIdsForSCNames(List<String> names) {
		synchronized (this.configLock) {
			List ret = new ArrayList();
			for (SimpleExecutableScanClass sc : this.scanClasses.values()) {
				ret.add(sc.getEntityId());
			}
			return ret;
		}
	}

	public void modifyScanClass(String scName, ScanClass newDefinition) throws Exception {
		List ids = getIdsForSCNames(Arrays.asList(new String[] { scName }));
		if (ids.size() == 0) {
			throw new IllegalArgumentException(String.format("Scan class '%s' could not be found.", new Object[] { scName }));
		}
		getTagStore().modifyScanClass((EntityId)ids.get(0), newDefinition);
	}

	public void removeScanClasses(List<String> scanclassNames) {
		List ids = getIdsForSCNames(scanclassNames);
		getTagStore().deleteScanClasses(ids);
	}

	public void addTags(TagPath parentFolder, List<TagNode> tag, TagManagerBase.CollisionPolicy policy)
			throws Exception
	{
	}

	public void editTags(List<TagPath> paths, TagDiff edit) throws Exception
	{
		if (this.internalTagStore == null)
			commissionNewTagStore();
		Map<TagPath,TagDefinition> toAdd = null;
		if (this.internalTagStore != null) {
			List<EntityId> toModify = new ArrayList<>();
			toAdd = new HashMap<>();
			Tag existing = null;
			synchronized (this.configLock) {
				for (TagPath p : paths) {
					TagStoreObject<TagConfig> ext = (TagStoreObject<TagConfig>)this.extensions.get(p);
					if (ext == null) {
						TagDefinition newTag = new TagDefinition(edit, TagType.Custom);
						newTag.setName(p.getItemName());
						existing = getTag(p);
						if (existing != null) {
							newTag.setDataType(existing.getDataType());
						}
						toAdd.put(p.getParentPath(), newTag);
					} else {
						toModify.add(ext.getId());
					}
				}
			}

			if (toModify.size() > 0) {
				this.internalTagStore.modifyTags(edit, toModify);
			}
			if (toAdd.size() > 0)
				for (TagPath path : toAdd.keySet()) {
					List<TagDefinition> defs = new ArrayList<>();
					defs.add(toAdd.get(path));
					this.internalTagStore.addTags(path,defs);
				}
		}
	}

	public void removeTags(List<TagPath> tagPath)
	{
		Set<EntityId> toRemove = new HashSet<>();
		Set<TagPath> toNotify = new HashSet<>();
		List<TestFrameTag> toUpdate = new ArrayList<>();
		synchronized (this.configLock) {
			for (TagPath p : tagPath)
			{
				Collection<TagPath> childrenPaths = this.tree.getChildrenPaths(p);
				if (childrenPaths != null) {
					for (TagPath cp : childrenPaths)
					{
						Tag ct = (Tag)this.tree.get(cp);
						processRemoval(ct, cp, toRemove, toNotify, toUpdate);
					}
				}

				Tag t = (Tag)this.tree.remove(p);
				processRemoval(t, p, toRemove, toNotify, toUpdate);
			}
		}

		if ((toRemove.size() > 0) && (this.internalTagStore != null)) {
			try {
				this.internalTagStore.deleteTags(toRemove);
			} catch (Exception e) {
				this.log.error("Error removing tag extensions from the tag store.", e);
			}
		}
		if (toUpdate.size() > 0) {
			for (TestFrameTag t : toUpdate) {
				t.updateQuality(DataQuality.NOT_FOUND);
			}
		}
		notifyFolders(toNotify);
	}

	protected void processRemoval(Tag t, TagPath p, Set<EntityId> toRemove, Set<TagPath> toNotify, List<TestFrameTag> toUpdate)
	{
		TagStoreObject tso = (TagStoreObject)this.extensions.get(p);

		if (tso != null)
			toRemove.add(tso.getId());
		else if (t != null) {
			toNotify.add(p.getParentPath());
		}
		if ((t != null) && ((t instanceof TestFrameTag)))
			toUpdate.add((TestFrameTag)t);
	}

	public List<BrowseElement> browseOPC(String driver, BrowseElement root)
			throws Exception
			{
		return null;
			}

	public List<String> getDrivers()
	{
		return null;
	}

	protected TagExecutor getSimpleTagExecutor() {
		return this.tagExecutor;
	}

	protected void registerHistoricalTag(TestFrameTag tag) {
		String histSC = tag.getHistoricalScanclass();
		if (!StringUtils.isBlank(histSC))
			synchronized (this.configLock) {
				histSC = histSC.toLowerCase();
				Set tags = (Set)this.histTags.get(histSC);
				if (tags == null) {
					tags = new HashSet();
					this.histTags.put(histSC, tags);
				}
				tags.add(tag);
				this.histProvSet = null;
			}
	}

	protected void unregisterHistoricalTag(TestFrameTag tag)
	{
		String histSC = tag.getHistoricalScanclass();
		if (histSC != null)
			synchronized (this.configLock) {
				Set tags = (Set)this.histTags.get(histSC.toLowerCase());
				if (tags != null) {
					tags.remove(tag);
				}

				this.histProvSet = null;
			}
	}

	protected Set<ExecutableTag> getHistoricalTagsForSC(String name)
	{
		synchronized (this.configLock) {
			Set ret = (Set)this.histTags.get(name.toLowerCase());
			if (ret == null) {
				ret = Collections.emptySet();
			}
			return ret;
		}
	}

	protected Set<String> getHistoricalTagProviderSet() {
		synchronized (this.configLock) {
			if (this.histProvSet == null) {
				this.histProvSet = new HashSet<>();
				for (Set<ExecutableTag> st : this.histTags.values()) {
					for (ExecutableTag t : st) {
						String prov = ((TestFrameTag)t).getHistoricalProvider();
						if (prov != null) {
							this.histProvSet.add(prov);
						}
					}
				}
			}
			return this.histProvSet;
		}
	}

	protected class SimpleScanClass extends SimpleExecutableScanClass
	{
		public SimpleScanClass(ScanClass scDefinition, String name, EntityId id)
		{
			super(scDefinition,name, id);
			setOwner(new InternalTagProvider.SimpleTagExecutor());
		}

		protected Set<ExecutableTag> getCurrentHistTagSet()
		{
			return InternalTagProvider.this.getHistoricalTagsForSC(getName());
		}

		public Set<String> getHistoryProviderSet()
		{
			return InternalTagProvider.this.getHistoricalTagProviderSet();
		}
	}

	protected class SimpleTagExecutor implements TagExecutor, TagValueListener
	{
		DirectTagEvaluationContext tagContext = new DirectTagEvaluationContext()
		{
			protected GatewayContext getContext() {
				return InternalTagProvider.this.getContext();
			}

			protected TagValueListener getTagValueListener()
			{
				return InternalTagProvider.SimpleTagExecutor.this;
			}

			protected Logger getTagLogger()
			{
				return InternalTagProvider.this.getLogger();
			}
		};

		protected SimpleTagExecutor()
		{
		}

		public QualifiedPath getObjectPath()
		{
			return InternalTagProvider.this.igPath;
		}

		public void tagValuesChanged(List<TagPropertyValue> values)
		{
		}

		public TagEvaluationContext getEvaluationContext()
		{
			return this.tagContext;
		}

		public String getName()
		{
			return InternalTagProvider.this.getName();
		}

		public String getDisplayNameForPath(TagPath path)
		{
			return path.toStringPartial();
		}

		public String getDefaultDatasource()
		{
			return null;
		}

		public Logger getTagErrorLogger()
		{
			return InternalTagProvider.this.getLogger();
		}

		public String getDefaultTagSource()
		{
			return null;
		}

		public GatewayContext getGatewayContext()
		{
			return InternalTagProvider.this.context;
		}

		public TagManagerBase getTagManager()
		{
			return getGatewayContext().getTagManager();
		}

		public ScriptManager getScriptManager()
		{
			return getGatewayContext().getScriptManager();
		}

		public TagScriptManager getTagScriptManager()
		{
			return InternalTagProvider.this.tagScriptMgr;
		}

		public String getOpcSubscriptionName()
		{
			return null;
		}

		public FunctionFactory getExpressionFunctionFactory()
		{
			return null;
		}

		public ExpressionParseContext createExpressionParseContext(TagPath parentPath)
		{
			return null;
		}

		public Date getLastEvaluationTime()
		{
			return null;
		}

		public boolean isAsyncExec()
		{
			return true;
		}

		public void sampleDiagnostics(DiagnosticsSample sample)
		{
		}
	}

	protected class TestFrameTag extends AbstractExecutableTag
	{
		private final TagValue value = new BasicTagValue(null, DataQuality.STALE);
		private TagDefinition ext = null;
		private EntityId eid;
		private DataType coreDtype = null;
		private ExtendedTagType tagType = TagType.Custom;

		public TestFrameTag(EntityId id)
		{
			this.eid = id;
		}

		public TestFrameTag(TagPath path) {
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

		public EntityId getId()
		{
			return this.eid;
		}

		public String getHistoricalScanclass() {
			return this.historizer == null ? null : this.historizer.getHistoricalScanclass();
		}

		public String getHistoricalProvider() {
			return this.historizer == null ? null : this.historizer.getProvider();
		}

		public void configureTypes(DataType dType, ExtendedTagType tagType) {
			boolean changed = (getDataType() != dType) || (!TypeUtilities.equals(this.tagType, tagType));

			setDataType(dType);
			this.tagType = tagType;
			if (changed)
				notifyChange(null);
		}

		public void setDataType(DataType value) {
			super.setDataType(value);
			if (this.coreDtype == null)
				this.coreDtype = value;
		}

		public void applyExtension(TagDefinition extensionDef)   {
			if (isInitialized()) {
				uninitialize(false);
			}
			this.ext = extensionDef;
			if ((extensionDef != null) && (extensionDef.getDataType() == null)) {
				extensionDef.setDataType(getDataType());
			}
			if (this.ext == null) {
				setDataType(this.coreDtype);
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

		public void updateValue(Object value) {
			updateCurrentValue(new BasicTagValue(value, DataQuality.GOOD_DATA));
		}

		public void updateValue(Object value, Quality quality) {
			updateCurrentValue(new BasicTagValue(value, quality));
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

		public void notifyChange(TagProp prop) {
			if (InternalTagProvider.this.subModel != null)
				InternalTagProvider.this.subModel.notifyTagChange(getTagPath(), this, prop);
		}

		protected void setValue(TagValue value) {
			super.setValue(value);
			notifyChange(TagProp.Value);
		}

		public TagType getType() {
			return this.tagType.getCoreType();
		}
	}
}