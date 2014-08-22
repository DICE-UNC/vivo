package databook.listener;

import static databook.utils.ConnectionUtils.adminAccount;
import static databook.utils.ConnectionUtils.irodsFs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;

import databook.config.IrodsConfig;
import databook.listener.vivo.SimpleRDFServiceWrapper;
import databook.listener.vivo.VIVOIndexer;
import databook.listener.vivo.VIVORDFDatabase;
import databook.local.model.RDFDatabase;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.UploadedFileHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

public class ModelUpdateListener implements ServletContextListener {

	private ExecutorService execService;
	static final Log log = LogFactory.getLog(ModelUpdateListener.class);
	public static final String AMQP_HOST = "localhost";
	public static final String AMQP_QUEUE = "metaQueue2";
	private RDFServiceFactory rdfServiceFactory;
	private RDFService rdfService;
	public RDFDatabase database;
	private FileStorage fileStorage;
	private ServletContext servletContext;
	private WebappDaoFactory webAppDaoFactory;
	private UploadedFileHelper uploadedFileHelper;
	public static VIVOIndexer vivoIndex;
	public static ModelUpdateListener instance = null;
	public static ModelUpdater modelUpdater;
	public static Scheduler scheduler;
	

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		// initialize context vars
		servletContext = sce.getServletContext();
		Object o = servletContext.getAttribute(FileStorageSetup.ATTRIBUTE_NAME);
		fileStorage = (FileStorage) o;
		webAppDaoFactory = (WebappDaoFactory) servletContext.getAttribute("webappDaoFactory");
		uploadedFileHelper = new UploadedFileHelper(fileStorage, webAppDaoFactory, servletContext);
		execService = Executors.newSingleThreadExecutor();
		execService.submit(new Runnable() {
			@Override
			public void run() {
				log.info("Databook plugin started");
				rdfServiceFactory = RDFServiceUtils
						.getRDFServiceFactory(sce.getServletContext());
				rdfService = rdfServiceFactory.getRDFService();
				database = new VIVORDFDatabase(new SimpleRDFServiceWrapper(rdfService));
				vivoIndex = new VIVOIndexer(database);
				modelUpdater = new ModelUpdater();
				modelUpdater.regIndexer(vivoIndex);
				scheduler = new SimpleScheduler();
				vivoIndex.setScheduler(scheduler);

				AMQPClient.receiveMessage(AMQP_HOST, AMQP_QUEUE,
						modelUpdater);
			}
		});
		instance = this;
	}
	

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

	public static void logFully(InputStream ris) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(ris));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				log.info(line);
			}
		} catch (IOException e1) {
			log.error(e1.getLocalizedMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e1) {
				log.error(e1.getLocalizedMessage());
			}
		}
	}
	
	public void updateImage(String entityUri, String thumbImageObjPath, String mainImageObjPath) throws IOException {
		log.info("Try to set images on '" + entityUri + "': objPath=" + thumbImageObjPath + ", " + mainImageObjPath);

		FileNames fileNames = getFileNames();
		
		try {
		  // all thumbnails/previews have the "image/jpeg" MIME type
		  IRODSAccount acc = IRODSAccount
					.instance(
							IrodsConfig.getString("irods.host"), IrodsConfig.getInt("irods.port"), IrodsConfig.getString("irods.user"), IrodsConfig.getString("irods.password"), IrodsConfig.getString("irods.home"), IrodsConfig.getString("irods.zone"), IrodsConfig.getString("irods.defaultResource")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$;
		  IRODSFileFactory fileFactory = irodsFs.getIRODSFileFactory(acc);
		  IRODSFileInputStream fis = fileFactory.instanceIRODSFileInputStream(mainImageObjPath);
		  FileInfo mainInfo = uploadedFileHelper.createFile(fileNames.name, "image/jpeg", fis);
		  fis.close();
		  
		  IRODSFileInputStream fisThumb = fileFactory.instanceIRODSFileInputStream(thumbImageObjPath);
		  FileInfo thumbInfo = uploadedFileHelper.createFile(fileNames.thumbName, "image/jpeg", fisThumb);
		  fis.close();
		  
		  irodsFs.close(acc);
		
		  IndividualDao individualDao = webAppDaoFactory.getIndividualDao();
		  // DataPropertyStatementDao dataPropertyStatementDao = webAppDaoFactory.getDataPropertyStatementDao();
		  ObjectPropertyStatementDao objectPropertyStatementDao = webAppDaoFactory.getObjectPropertyStatementDao();

		  Individual entity = individualDao.getIndividualByURI(entityUri);
		  if (entity == null) {
			  throw new NullPointerException("No entity found for URI '"
					  + entityUri + "'.");
		  }

		  // Add the thumbnail file to the main image file.
		  objectPropertyStatementDao
				  .insertNewObjectPropertyStatement(new ObjectPropertyStatementImpl(
						  mainInfo.getUri(), VitroVocabulary.FS_THUMBNAIL_IMAGE,
						  thumbInfo.getUri()));

		  // Add the main image file to the entity.
		  entity.setMainImageUri(mainInfo.getUri());
		  individualDao.updateIndividual(entity);

		  log.info("Set images on '" + entity.getURI() + "': main=" + mainInfo
				  + ", thumb=" + thumbInfo);

		} catch(Exception e) {
		  throw new Error(e);
		}
	}
	
	public static class FileNames {
		public String nameTmp;
		public String name;
		public String thumbName;
		public String thumbNameTmp;
		public FileNames(String tmpName, String thumbName, String name, String id) {
			this.nameTmp = tmpName;
			this.thumbName = thumbName;
			this.name = name;
			this.thumbNameTmp = id;
		}
	}
	
	// util methods
	private static long lastAccessTime;
	private static int lastAccessTimeCount;
	public static synchronized FileNames getFileNames() {
		long currTime = System.currentTimeMillis();
		if(currTime > lastAccessTime) {
			lastAccessTime = currTime;
			lastAccessTimeCount = 1;
		} else {
			lastAccessTimeCount++;
		}
		String idBase = lastAccessTime + "_" + (lastAccessTimeCount - 1);
		return new FileNames("/tmp/databook.tmp" + idBase, "databook.thumb"+idBase, "databook."+idBase, "databook_"+idBase);
		
	}
	
	public static void waitAndLog(Log log, Process p) {
	try{
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

		    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			String s;
		    // read the output from the command
		    System.out.println("Here is the standard output of the command:\n");
		    while ((s = stdInput.readLine()) != null) {
		        log.info(s);
		    }
		    
		    // read any errors from the attempted command
		    System.out.println("Here is the standard error of the command (if any):\n");
		    while ((s = stdError.readLine()) != null) {
		        log.error(s);
		    }
		    p.waitFor();

	} catch(IOException e) {
		throw new Error(e);
	}	catch(InterruptedException e) {
		throw new Error(e);
			}
	}

}
