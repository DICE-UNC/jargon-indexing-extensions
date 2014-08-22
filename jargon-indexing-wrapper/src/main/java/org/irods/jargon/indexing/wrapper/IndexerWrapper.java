package org.irods.jargon.indexing.wrapper;

import java.util.List;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.indexing.wrapper.IndexingConstants.actionsEnum;
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

	public void setIndexingService(IndexingService is) {
		this.indexingService = is;
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
		} else {
			log.info("message discarded as no relevant events are included");
		}

	}

	private void processUnionOperation(Message message, Messages ofMessages) {
		log.info("processUnionOperation()");
		log.info("message:{}", message);

		// look at message part for a part that is the metadata
		String absolutePath = null;
		for (DataEntity part : message.getHasPart()) {

			/*
			 * Look for AVU create event
			 */

			if (part.getAdditionalProperties() == null
					|| part.getAdditionalProperties().isEmpty()) {
				log.info("no additional properties");
				continue;
			}
			if (part.getUri() != null) {
				absolutePath = part.getUri().toString();
				log.info("established uri as:{}", absolutePath);
			}

			log.info("inspecting additional properties for avu create");

			if (part.getAdditionalProperties().get(METADATA_OBJECT) != null) {
				log.info("have a metadataObject property for this part, process as a metadata create");
			} else {
				log.info("no metadata");
				continue;
			}

			log.info("have metadata object:{}", part.getAdditionalProperties()
					.get(METADATA_OBJECT));
			List<Map<String, String>> avuEntries = (List<Map<String, String>>) part
					.getAdditionalProperties().get(METADATA_OBJECT);

			log.info("avuEntries:{}", avuEntries);
			log.info("with size:{}", avuEntries.size());

			Map<String, String> entry = avuEntries.get(0);

			/*
			 * if (avuDataEntry.size() != 3) {
			 * log.warn("expected 3 elements in avu data...discarded");
			 * continue; }
			 * 
			 * String attribute = (String) avuDataEntry.get(2); String value =
			 * (String) avuDataEntry.get(1); String unit = (String)
			 * avuDataEntry.get(0);
			 */

			String attribute = entry.get("attribute");
			String value = entry.get("value");
			String unit = entry.get("unit");

			log.info("process as AVU add event{}", part);
			MetadataEvent addMetadataEvent = new MetadataEvent();
			addMetadataEvent.setActionsEnum(actionsEnum.ADD);
			addMetadataEvent.setIrodsAbsolutePath(absolutePath);
			try {
				AvuData avuData = AvuData.instance(attribute, value, unit);

				/*
				 * AvuData avuData = AvuData.instance(attribute.substring(10),
				 * value.substring(6), unit.substring(5));
				 */

				addMetadataEvent.setAvuData(avuData);
				this.onMetadataAdd(addMetadataEvent);
			} catch (JargonException e) {
				log.error("error", e);
				throw new GeneralIndexerRuntimeException(
						"jargon exception occurred processing AVU", e);
			}
		}

	}

	private void processDiffOperation(Message message, Messages ofMessages) {
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
						this.onMetadataDelete(deleteMetadataEvent);
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
	public void setScheduler(Scheduler s) {
		this.scheduler = s;
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

}

class Property<T> {
	public String key;
	public T Value;
}
