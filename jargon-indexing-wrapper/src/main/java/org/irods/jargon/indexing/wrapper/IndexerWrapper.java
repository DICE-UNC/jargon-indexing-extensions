package org.irods.jargon.indexing.wrapper;

import java.util.List;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.indexing.wrapper.IndexingConstants.actionsEnum;
import org.irods.jargon.indexing.wrapper.event.FileEvent;
import org.irods.jargon.indexing.wrapper.event.MetadataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import databook.listener.Indexer;
import databook.listener.Scheduler;
import databook.listener.service.IndexingService;
import databook.persistence.rule.rdf.ruleset.AVU;
import databook.persistence.rule.rdf.ruleset.DataEntity;
import databook.persistence.rule.rdf.ruleset.Message;
import databook.persistence.rule.rdf.ruleset.Messages;
import databook.persistence.rule.rdf.ruleset.DataObject;

/**
 * Wrapper around indexing service that provides discrete event hooks and the
 * ability to register listeners for these event hooks. This masks some of the
 * complexity of indexing and provides a simple and testable POJO interaction
 * style.
 * 
 * @author Mike Conway - DICE
 * 
 */
public class IndexerWrapper implements Indexer {

	public static final String METADATA_OBJECT = "metadataObject";
	private IndexingService indexingService;
	private Scheduler scheduler;

	public static final Logger log = LoggerFactory
			.getLogger(IndexerWrapper.class);

	public void setIndexingService(final IndexingService is) {
		indexingService = is;
	}

	public void startup() {
		log.info("startup()");
		indexingService.regIndexer(this);
		onStartup();
	}

	public void shutdown() {
		indexingService.unregIndexer(this);
		onShutdown();
	}

	@Override
	public void messages(final Messages messages) {
		try {

			log.info("messages:{}", messages);

			for (Message message : messages.getMessages()) {
				onMessage(message, messages);
			}

		} catch (Exception e) {
			log.error("error", e);
			throw new GeneralIndexerRuntimeException(
					"unknown exception occurred in indexer on processing of messages",
					e);
		}
	}

	/**
	 * Framework method that will be called on the receipt of each message. This
	 * will in turn call the appropriate event handler with the correctly parsed
	 * message
	 * 
	 * @param message
	 *            {@link Message} in a potential group of messages receieved
	 * @param ofMessages
	 *            {@link} the group of messages of which this message is a
	 *            member, in cases where additional mining of information is
	 *            required
	 */
	void onMessage(final Message message, final Messages ofMessages) {
		// TODO: for now simply call the event here, think about adding a
		// message handler that can interpret and plug it in here, may need to
		// look at whole set of message to derive the right event? - MC

		log.info("onMessage:{}", message);

		log.info("check message operation:{}", message.getOperation());
		if (message.getOperation().equals(IndexingConstants.OPERATION_UNION)) {
			log.info("process as a union");
			processUnionOperation(message, ofMessages);
		} else if (message.getOperation().equals(
				IndexingConstants.OPERATION_DIFF)) {
			log.info("process as a diff");
			processDiffOperation(message, ofMessages);
		} else if (message.getOperation().equals(
				IndexingConstants.OPERATION_CREATE)) {
			log.info("process as a create");
			processCreateOperation(message, ofMessages);
		} else {
			log.info("message discarded as no relevant events are included");
		}

	}

	private void processCreateOperation(Message message, Messages ofMessages) {
		log.info("processCreateOperation()");
		log.info("message:{}", message);

		// look at message part for a part that is the metadata
		String absolutePath = null;
		for (DataEntity part : message.getHasPart()) {

			log.info("part:{}", part);
			if (part instanceof DataObject) {
				if (part.getLabel() != null) {
					absolutePath = part.getLabel();
					log.info("established absolutePath as:{}", absolutePath);
				}
	
				// FIXME: https://github.com/DICE-UNC/indexing/issues/9
				// really should be able to figure out the type here
	
				FileEvent fileEvent = new FileEvent();
				fileEvent.setIrodsAbsolutePath(absolutePath);
				fileEvent.setObjectType(ObjectType.DATA_OBJECT);
				fileEvent.setActionsEnum(actionsEnum.ADD);
				log.info("calling onFileAdd with:{}", fileEvent);
				// TODO: data size not yet provisioned
				this.onFileAdd(fileEvent);
			}

		}

	}

	private void processUnionOperation(final Message message,
			final Messages ofMessages) {
		log.info("processUnionOperation()");
		log.info("message:{}", message);

		// look at message part for a part that is the metadata
		String absolutePath = null;
		for (DataEntity part : message.getHasPart()) {

			log.info("part:{}", part);

			if (part.getUri() != null) {
				absolutePath = part.getUri().toString();
				log.info("established uri as:{}", absolutePath);
				log.info("lop off after the @ sign because I don't know why yet");
				int idxAmp = absolutePath.lastIndexOf('@');
				if (idxAmp != -1) {
					absolutePath = absolutePath.substring(0, idxAmp);
					log.info("derived abs path from uri:{}", absolutePath);
				}

			}

			/*
			 * Look for AVU create event
			 */

			List<Map<String, String>> avuEntries = checkAndExtractMetadataEntriesFromPart(part);

			if (avuEntries == null) {
				continue;
			}

			for (Map<String, String> entry : avuEntries) {
				String attribute = entry.get("attribute");
				String value = entry.get("value");
				String unit = entry.get("unit");

				log.info("process as AVU add event{}", part);
				MetadataEvent addMetadataEvent = new MetadataEvent();
				addMetadataEvent.setActionsEnum(actionsEnum.ADD);
				addMetadataEvent.setIrodsAbsolutePath(absolutePath);
				try {
					AvuData avuData = AvuData.instance(attribute, value, unit);
					addMetadataEvent.setAvuData(avuData);
					onMetadataAdd(addMetadataEvent);
				} catch (JargonException e) {
					log.error("error", e);
					throw new GeneralIndexerRuntimeException(
							"jargon exception occurred processing AVU", e);
				}
			}

		}

	}

	/**
	 * Given a DataEntity, return any AVU entries that might be available. Will
	 * return <code>null</code> if no entries are found.
	 * 
	 * @param part
	 * @return
	 */
	private List<Map<String, String>> checkAndExtractMetadataEntriesFromPart(
			final DataEntity part) {

		if (part.getAdditionalProperties() == null
				|| part.getAdditionalProperties().isEmpty()) {
			log.info("no additional properties");
			return null;
		}

		if (part.getAdditionalProperties().get(METADATA_OBJECT) != null) {
			log.info("have a metadataObject property for this part, process as a metadata create");
		} else {
			log.info("no metadata");
			return null;
		}

		log.info("have metadata object:{}",
				part.getAdditionalProperties().get(METADATA_OBJECT));
		@SuppressWarnings("unchecked")
		List<Map<String, String>> avuEntries = (List<Map<String, String>>) part
				.getAdditionalProperties().get(METADATA_OBJECT);

		if (avuEntries.isEmpty()) {
			log.info("no entries found, so return null");
			return null;
		}

		log.info("avuEntries:{}", avuEntries);
		log.info("with size:{}", avuEntries.size());
		return avuEntries;
	}

	private void processDiffOperation(final Message message,
			final Messages ofMessages) {
		log.info("processDiffOperation()");

		// look at message part for a part that is the metadata

		for (DataEntity part : message.getHasPart()) {
			if (!part.getMetadata().isEmpty()) {

				for (AVU avu : part.getMetadata()) {

					log.info("process as AVU delete event{}", part);
					MetadataEvent deleteMetadataEvent = new MetadataEvent();
					deleteMetadataEvent.setActionsEnum(actionsEnum.DELETE);
					deleteMetadataEvent.setIrodsAbsolutePath(part
							.getDescription());
					try {
						AvuData avuData = AvuData.instance(avu.getAttribute(),
								avu.getValue(), avu.getUnit());
						deleteMetadataEvent.setAvuData(avuData);
						onMetadataDelete(deleteMetadataEvent);
					} catch (JargonException e) {
						log.error("error", e);
						throw new GeneralIndexerRuntimeException(
								"jargon exception occurred processing AVU", e);
					}
				}

			}
		}

	}

	@Override
	public void setScheduler(final Scheduler s) {
		scheduler = s;
	}

	/**
	 * @return the scheduler
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @return the indexingService
	 */
	public IndexingService getIndexingService() {
		return indexingService;
	}

	/**
	 * Extension point for receiving startup notifications.
	 */
	protected void onStartup() {

	}

	/**
	 * Extension point for receiving shutdown notifications
	 */
	protected void onShutdown() {

	}

	/**
	 * Extension point notified when an individual AVU has been added for a data
	 * object or collection
	 * 
	 * @param addMetadataEvent
	 *            {@link MetadataEvent} that has been encountered
	 */
	protected void onMetadataAdd(final MetadataEvent addMetadataEvent) {

	}

	/**
	 * Extension point notified when an individual AVU has been deleted from a
	 * data object or collection
	 * 
	 * @param addMetadataDelete
	 *            {@link MetadataEvent} that has been encountered
	 */
	protected void onMetadataDelete(final MetadataEvent deleteMetadataEvent) {

	}

	/**
	 * Extension point when a file is added
	 * 
	 * @param fileEvent
	 *            {@link FileEvent} with information about the file add
	 */
	protected void onFileAdd(final FileEvent fileEvent) {

	}

	/**
	 * Extension point when a file is removed
	 * 
	 * @param fileEvent
	 *            {@link FileEvent} with information about the file delete
	 */
	protected void onFileDelete(final FileEvent fileEvent) {

	}

}

class Property<T> {
	public String key;
	public T Value;
}
