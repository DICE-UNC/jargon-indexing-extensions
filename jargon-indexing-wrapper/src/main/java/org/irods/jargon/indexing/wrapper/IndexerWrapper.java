package org.irods.jargon.indexing.wrapper;

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

		for (DataEntity part : message.getHasPart()) {
			if (part.getMetadata() == null || part.getMetadata().isEmpty()) {
				log.info("no metadata present");
			} else {
				log.info("part being processed:{}", part);

				for (AVU avu : part.getMetadata()) {

					log.info("process as AVU add event{}", part);
					MetadataEvent addMetadataEvent = new MetadataEvent();
					addMetadataEvent.setActionsEnum(actionsEnum.ADD);
					addMetadataEvent
							.setIrodsAbsolutePath(part.getDescription());
					try {
						AvuData avuData = AvuData.instance(avu.getAttribute(),
								avu.getValue(), avu.getUnit());
						addMetadataEvent.setAvuData(avuData);
						this.onMetadataAdd(addMetadataEvent);
					} catch (JargonException e) {
						log.error("error", e);
						throw new GeneralIndexerRuntimeException(
								"jargon exception occurred processing AVU", e);
					}
				}

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
