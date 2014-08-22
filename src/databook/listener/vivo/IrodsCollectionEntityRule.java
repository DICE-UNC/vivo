package databook.listener.vivo;

import java.util.List;

import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.rdf.RDFEntityRule;
import databook.persistence.rule.rdf.ruleset.Collection;
import databook.persistence.rule.rdf.ruleset.StorageLocation;
import databook.persistence.rule.rdf.ruleset.StorageLocationRequest;

public class IrodsCollectionEntityRule extends RDFEntityRule<databook.persistence.rule.rdf.ruleset.Collection> {
	
	public IrodsCollectionEntityRule() {
		super();
	}

	@Override
	public void create(Collection e, PersistenceContext context) {
		List<StorageLocation> storageLocation = e.getStorageLocation();
		List<StorageLocationRequest> storageLocationRequest = e.getStorageLocationRequest();
		// do not maintain storage location for now
		/*for(StorageLocationRequest lr : storageLocationRequest) {
			switch(lr) {
			case IRODS:
				storageLocation.add(StorageLocation.IRODS);
				break;
			case TRIPLE_STORE:
				storageLocation.add(StorageLocation.TRIPLE_STORE);
				break;
			}
		}*/
		for(StorageLocationRequest lr : storageLocationRequest) {
			switch(lr) {
			case IRODS:
				IRODSFileFactory fileFactory = null;
				try {
					fileFactory = context.getIrodsFs()
							.getIRODSFileFactory(context.getIrodsAccount());
					IRODSFile f = fileFactory.instanceIRODSFile(e.getTitle());
					f.mkdir();
				} catch (Exception exc) {
					throw new Error(exc);
				} finally {
					try {
						context.getIrodsFs().close(context.getIrodsAccount());
					} catch (Exception exc) {
						throw new Error(exc);
					}
				}
				//storageLocation.add(StorageLocation.IRODS);
				break;
			case TRIPLE_STORE:
				super.create(e, context);
				//storageLocation.add(StorageLocation.TRIPLE_STORE);
				break;
			default:
				throw new RuntimeException("unsupported storage location request "+lr);
			}
			
		}	
			
		if(storageLocationRequest.isEmpty()) { // default to triple store
				super.create(e, context);
			}
	}

	@Override
	public void delete(Collection e, PersistenceContext context) {
		List<StorageLocationRequest> storageLocation = e.getStorageLocationRequest();
		for(StorageLocationRequest l: storageLocation) {
			switch(l) {
			case IRODS:
				IRODSFileFactory fileFactory = null;
				try {
					fileFactory = context.getIrodsFs()
							.getIRODSFileFactory(context.getIrodsAccount());
					IRODSFile f = fileFactory.instanceIRODSFile(e.getTitle());
					f.delete();
				} catch (Exception exc) {
					throw new Error(exc);
				} finally {
					try {
						context.getIrodsFs().close(context.getIrodsAccount());
					} catch (Exception exc) {
						throw new Error(exc);
					}
				}
				break;
			case TRIPLE_STORE:
				super.delete(e, context);
				break;
			default:
				throw new RuntimeException("unsupported storage location "+l);
			}
		}
	}
	
}
