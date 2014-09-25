/**
 *
 */
package org.irods.jargon.indexing.fileactlog;

import org.irods.jargon.indexing.wrapper.IndexerWrapper;
import org.irods.jargon.indexing.wrapper.event.FileEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indexer that simply logs file events (versus metadata)
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FileActionLoggingIndexer extends IndexerWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(FileActionLoggingIndexer.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.indexing.wrapper.IndexerWrapper#onStartup()
	 */
	@Override
	protected void onStartup() {
		log.info(">>>>>>>>> startup of indexer");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.indexing.wrapper.IndexerWrapper#onShutdown()
	 */
	@Override
	protected void onShutdown() {
		log.info("<<<<<<<<<< shutdown of  indexer");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.indexing.wrapper.IndexerWrapper#onFileAdd(org.irods.
	 * jargon.indexing.wrapper.event.FileEvent)
	 */
	@Override
	protected void onFileAdd(FileEvent fileEvent) {
		log.info(">>>>> File Add");
		log.info("fileEvent:{}", fileEvent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.indexing.wrapper.IndexerWrapper#onFileDelete(org.irods
	 * .jargon.indexing.wrapper.event.FileEvent)
	 */
	@Override
	protected void onFileDelete(FileEvent fileEvent) {
		log.info(">>>>> File Delete");
		log.info("fileEvent:{}", fileEvent);
	}

}
