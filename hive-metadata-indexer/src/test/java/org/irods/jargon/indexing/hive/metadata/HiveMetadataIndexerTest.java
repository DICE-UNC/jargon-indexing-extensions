package org.irods.jargon.indexing.hive.metadata;

import java.net.URI;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.MetaDataAndDomainData.MetadataDomain;
import org.irods.jargon.core.utils.IRODSUriUtils;
import org.irods.jargon.hive.external.indexer.modelservice.IrodsJenaModelUpdater;
import org.irods.jargon.hive.external.utils.JargonHiveConfigurationHelper;
import org.irods.jargon.hive.external.utils.JenaHiveConfiguration;
import org.irods.jargon.hive.external.utils.JenaModelManager;
import org.irods.jargon.hive.irods.HiveVocabularyEntry;
import org.irods.jargon.hive.irods.IRODSHiveService;
import org.irods.jargon.hive.irods.IRODSHiveServiceImpl;
import org.irods.jargon.indexing.wrapper.IndexingConstants.actionsEnum;
import org.irods.jargon.indexing.wrapper.event.MetadataEvent;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class HiveMetadataIndexerTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
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
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testOnMetadataAdd() throws Exception {

		String testCollection = "testOnMetadataAdd";
		String testVocabTerm = testCollection;
		String testURI = "http://a.vocabulary#term";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSHiveService irodsHiveService = new IRODSHiveServiceImpl(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount);

		String targetDb = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ "/" + testCollection);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ testCollection);

		IRODSFile vocabCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsCollection);
		vocabCollection.mkdirs();

		HiveVocabularyEntry entry = new HiveVocabularyEntry();
		entry.setComment("comment");
		entry.setDomainObjectUniqueName(targetIrodsCollection);
		entry.setMetadataDomain(MetadataDomain.COLLECTION);
		entry.setPreferredLabel(testVocabTerm);
		entry.setTermURI(testURI);
		entry.setVocabularyName("agrovoc");

		irodsHiveService.saveOrUpdateVocabularyTerm(entry);
		JenaHiveConfiguration jenaHiveConfiguration = JargonHiveConfigurationHelper
				.buildJenaHiveConfigurationFromProperties(testingProperties);

		// sub in a temp database
		StringBuilder sb = new StringBuilder();
		sb.append(testingProperties
				.getProperty(JargonHiveConfigurationHelper.INDEXER_DB_URI_PREFIX));
		sb.append(targetDb);
		sb.append("data");
		sb.append(testingProperties
				.getProperty(JargonHiveConfigurationHelper.INDEXER_DB_URI_SUFFIX));
		jenaHiveConfiguration.setJenaDbUri(sb.toString());

		HiveMetadataIndexer hiveMetadataIndexer = new HiveMetadataIndexer();
		hiveMetadataIndexer.setIndexerAccount(irodsAccount);
		hiveMetadataIndexer.setIrodsFileSystem(irodsFileSystem);
		hiveMetadataIndexer.setJenaHiveConfiguration(jenaHiveConfiguration);
		JenaModelManager jenaModelManager = new JenaModelManager();
		OntModel ontModel = jenaModelManager
				.buildJenaDatabaseModel(jenaHiveConfiguration);
		IrodsJenaModelUpdater modelUpdater = new IrodsJenaModelUpdater(
				irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				ontModel, jenaHiveConfiguration);

		hiveMetadataIndexer.setOntModel(ontModel);
		hiveMetadataIndexer.setModelUpdater(modelUpdater);

		AvuData avuData = AvuData
				.instance(
						"http://a.vocabulary#term",
						"vocabulary=agrovoc|preferredLabel=testOnMetadataAdd|comment=comment",
						"iRODSUserTagging:HIVE:VocabularyTerm");

		MetadataEvent metadataEvent = new MetadataEvent();
		metadataEvent.setActionsEnum(actionsEnum.ADD);
		metadataEvent.setAvuData(avuData);
		metadataEvent.setIrodsAbsolutePath(targetIrodsCollection);
		metadataEvent.setObjectType(ObjectType.COLLECTION);

		hiveMetadataIndexer.onMetadataAdd(metadataEvent);

		URI irodsUri = IRODSUriUtils
				.buildURIForAnAccountWithNoUserInformationIncluded(
						irodsAccount, targetIrodsCollection);

		// read back the resource

		Resource irodsCollResc = ontModel.getResource(irodsUri.toString());
		Assert.assertNotNull("no resc found in model", irodsCollResc);

		ontModel.write(System.out);
		Property correspondingConceptProp = ontModel
				.getProperty("http://www.irods.org/ontologies/2013/2/iRODS.owl#"
						+ "correspondingConcept");

		StmtIterator iter = irodsCollResc.listProperties();

		while (iter.hasNext()) {
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>> iter next:"
					+ iter.nextStatement());
		}

		iter = ontModel.listStatements();

		while (iter.hasNext()) {
			System.out
					.println(">>>>>>>>>>>>>>>>>>>>>>>> whole model iter next:"
							+ iter.nextStatement());
		}

	}

}
