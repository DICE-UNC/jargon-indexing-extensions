/**
 * 
 */
package org.irods.jargon.indexing.hive.metadata;

import org.irods.jargon.indexing.wrapper.IndexerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indexer responds to metadata updates to maintain a triple store of iRODS data
 * 
 * @author Mike Conway - DICE
 *
 */
public class HiveMetadataIndexer extends IndexerWrapper {

	public static final Logger log = LoggerFactory
			.getLogger(IndexerWrapper.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.indexing.wrapper.IndexerWrapper#onStartup()
	 */
	@Override
	protected void onStartup() {
		log.info(">>>>>>>>> startup of hive metadata indexer");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.indexing.wrapper.IndexerWrapper#onShutdown()
	 */
	@Override
	protected void onShutdown() {
		log.info("<<<<<<<<<< shutdown of hive metadata indexer");

	}

}
