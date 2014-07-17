/**
 * 
 */
package org.irods.jargon.indexing.hive.metadata;

import org.irods.jargon.hive.external.utils.JenaHiveConfiguration;
import org.irods.jargon.hive.irods.IRODSHiveService;
import org.irods.jargon.indexing.wrapper.IndexerWrapper;
import org.irods.jargon.indexing.wrapper.event.MetadataEvent;
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

	private JenaHiveConfiguration jenaHiveConfiguration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.indexing.wrapper.IndexerWrapper#onStartup()
	 */
	@Override
	protected void onStartup() {
		log.info(">>>>>>>>> startup of hive metadata indexer");
		log.info("HIVE indexer version:{}", HiveIndexerVersion.VERSION);
		log.info("HIVE indexer build time:{}", HiveIndexerVersion.BUILD_TIME);
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

	@Override
	protected void onMetadataAdd(MetadataEvent addMetadataEvent) {
		log.info("HIVE avu add?");

		if (!isHiveAvu(addMetadataEvent)) {
			log.info("ignored...not a HIVE AVU");
			return;
		}

		log.info("process this as a HIVE AVU:{}", addMetadataEvent);

	}

	private boolean isHiveAvu(MetadataEvent addMetadataEvent) {
		if (addMetadataEvent.getAvuData().getUnit()
				.equals(IRODSHiveService.VOCABULARY_AVU_UNIT)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.indexing.wrapper.IndexerWrapper#onMetadataDelete(org
	 * .irods.jargon.indexing.wrapper.event.MetadataEvent)
	 */
	@Override
	protected void onMetadataDelete(MetadataEvent deleteMetadataEvent) {
		log.info("HIVE avu delete?");

		if (!isHiveAvu(deleteMetadataEvent)) {
			log.info("ignored...not a HIVE AVU");
			return;
		}

		log.info("process this as a HIVE AVU:{}", deleteMetadataEvent);
	}

	public JenaHiveConfiguration getJenaHiveConfiguration() {
		return jenaHiveConfiguration;
	}

	public void setJenaHiveConfiguration(
			JenaHiveConfiguration jenaHiveConfiguration) {
		this.jenaHiveConfiguration = jenaHiveConfiguration;
	}

}
