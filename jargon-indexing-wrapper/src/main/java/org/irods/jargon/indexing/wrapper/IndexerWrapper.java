package org.irods.jargon.indexing.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import databook.listener.Indexer;
import databook.listener.Scheduler;
import databook.listener.service.IndexingService;
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
		// onStartup();
	}

	public void shutdown() {
		indexingService.unregIndexer(this);
		// onShutdown();
	}

	@Override
	public void messages(final Messages messages) {
		try {

			log.info("messages:{}", messages);

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

}

class Property<T> {
	public String key;
	public T Value;
}
