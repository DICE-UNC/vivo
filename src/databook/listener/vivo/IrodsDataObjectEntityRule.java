package databook.listener.vivo;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.rdf.RDFEntityRule;
import databook.persistence.rule.rdf.ruleset.DataObject;
import databook.persistence.rule.rdf.ruleset.StorageLocation;
import databook.persistence.rule.rdf.ruleset.StorageLocationRequest;

public class IrodsDataObjectEntityRule extends RDFEntityRule<databook.persistence.rule.rdf.ruleset.DataObject> {
	
	public IrodsDataObjectEntityRule() {
		super();
	}

	@Override
	public void create(DataObject e, PersistenceContext context) {
		List<StorageLocation> storageLocation = e.getStorageLocation();
		List<StorageLocationRequest> storageLocationRequest = e.getStorageLocationRequest();
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
				try {
					InputStream inputStream = e.getTempInputStream(); 
					IRODSFileFactory fileFactory = context.getIrodsFs().getIRODSFileFactory(context.getIrodsAccount());
					OutputStream fos = fileFactory.instanceIRODSFileOutputStream(e.getTitle());
					byte[] buffer = new byte[8192];
					int howMany;
					while (-1 != (howMany = inputStream.read(buffer))) {
						fos.write(buffer, 0, howMany);
					}
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
		if(storageLocationRequest.isEmpty()) {
			super.create(e, context);
		}
	}

	@Override
	public void delete(DataObject e, PersistenceContext context) {
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
		if(storageLocation.isEmpty()) {
			super.delete(e, context);
		}
	}
	
}
