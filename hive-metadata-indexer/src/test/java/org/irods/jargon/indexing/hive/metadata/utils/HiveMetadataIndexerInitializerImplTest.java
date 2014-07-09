package org.irods.jargon.indexing.hive.metadata.utils;

import static org.junit.Assert.fail;

import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.hive.external.utils.JenaHiveConfiguration;
import org.irods.jargon.indexing.hive.metadata.HiveIndexerPropertiesHelper;
import org.irods.jargon.indexing.hive.metadata.utils.testing.HiveMetadataIndexerTestSetupUtils;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;

public class HiveMetadataIndexerInitializerImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "HiveMetadataIndexerInitializerImplTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static HiveIndexerPropertiesHelper testingPropertiesLoader;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingPropertiesLoader = new HiveIndexerPropertiesHelper();
		testingProperties = testingPropertiesLoader.getProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		// irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		// irodsTestSetupUtilities
		// .initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testInitializeAndBatchIndexOntologyModel() {
		fail("Not yet implemented");
	}

	@Test
	public void testInitializeBareOntologyModel() throws Exception {
		String subdir = "testInitializeBareOntologyModel";

		HiveMetadataIndexerTestSetupUtils.deleteJenaSubdir(testingProperties,
				subdir);

		JenaHiveConfiguration jenaHiveConfiguration = new JenaHiveConfiguration();
		jenaHiveConfiguration.setAutoCloseJenaModel(false);
		jenaHiveConfiguration.setIdropContext(testingProperties
				.getProperty(HiveIndexerPropertiesHelper.KEY_IDROP_CONTEXT));
		jenaHiveConfiguration.setIrodsRDFFileName(testingProperties
				.getProperty(HiveIndexerPropertiesHelper.KEY_IRODS_RDF));
		jenaHiveConfiguration.setJenaDbDriverClass(testingProperties
				.getProperty(HiveIndexerPropertiesHelper.KEY_JENA_DB_DRIVER));
		jenaHiveConfiguration.setJenaDbPassword(testingProperties
				.getProperty(HiveIndexerPropertiesHelper.KEY_JENA_DB_PASSWORD));
		jenaHiveConfiguration.setJenaDbType(testingProperties
				.getProperty(HiveIndexerPropertiesHelper.KEY_JENA_DB_TYPE));
		jenaHiveConfiguration.setJenaDbUri(HiveMetadataIndexerTestSetupUtils
				.buildJenaUriForTempDirectory(testingProperties, subdir));
		jenaHiveConfiguration.setJenaDbUser(testingProperties
				.getProperty(HiveIndexerPropertiesHelper.KEY_JENA_DB_USER));

		HiveMetadataIndexerInitializer initializer = new HiveMetadataIndexerInitializerImpl(
				jenaHiveConfiguration);

		OntModel ontModel = initializer.initializeBareOntologyModel();
		Assert.assertNotNull("null ontModel returned");
		ontModel.close();

	}
}