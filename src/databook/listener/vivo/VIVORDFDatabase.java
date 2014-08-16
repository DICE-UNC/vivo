package databook.listener.vivo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import databook.edsl.googql.Dquery;
import databook.edsl.googql.actionHead;
import databook.local.model.RDFDatabase;
import databook.local.model.RDFDatabaseException;
import databook.local.model.RDFServiceWrapper;
import databook.utils.ModelUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

public class VIVORDFDatabase implements RDFDatabase {
	RDFServiceWrapper rdfs;
	public edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService rdfService;


	public VIVORDFDatabase(RDFServiceWrapper rdfService) {

		this.rdfService = (edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService) rdfService.getRdfServiceImpl();
	}

	private static final Log log = LogFactory.getLog(VIVORDFDatabase.class);

	public class VIVORDFDatabaseTransaction implements RDFDatabaseTransaction {

		ChangeSet cs;
		List<InputStream> iss = new ArrayList<InputStream>();

		public void start() throws RDFDatabaseException {
			try {
				cs = rdfService.manufactureChangeSet();
			} catch (Exception e) {
				throw new RDFDatabaseException(e);
			}
		}

		public void commit() throws RDFDatabaseException {
			try {
				rdfService.changeSetUpdate(cs);
			} catch (RDFServiceException e) {
				throw new RDFDatabaseException(e);
			} finally {
				for (InputStream is : iss) {
					closeIfNotNull(is); // not necessary
				}
				iss.clear();
			}

		}

		public void abort() {
			for (InputStream is : iss) {
				closeIfNotNull(is); // not necessary
			}
			iss.clear();

		}

		public void add(String n3StrAdd, Format format, String model) {
			log.info("add rdf '" + n3StrAdd + "'");
			InputStream isAdd = new ByteArrayInputStream(n3StrAdd.getBytes());
			cs.addAddition(isAdd, translate(format), model);
		}

		public void remove(String n3StrRem, Format format, String model) {
			InputStream isRem = new ByteArrayInputStream(n3StrRem.getBytes());
			cs.addRemoval(isRem, translate(format), model);
		}

		
		private void closeIfNotNull(InputStream... iss) {
			for (InputStream is : iss) {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						log.error(e.getLocalizedMessage());
					}
				}
			}
		}

	}

	@Override
	public RDFDatabaseTransaction newTransaction() {
		return new VIVORDFDatabaseTransaction();
	}

	@Override
	public String getUniqueValue(String subject, String property) {
		String valueOld = null;
		try {
			List<String[]> ret = (List<String[]>) selectQuery().node(new URI(subject)).follow(property).sel().end().run();
			if(!ret.isEmpty()) {
				valueOld = ret.get(0)[0];
			}
		} catch (URISyntaxException e) {
			log.error("error", e);
		}
		return valueOld;
	}

	@Override
	public List<String> getValues(String subject, String property) {
		List<String> valueOld = null;
		try {
			List<String[]> ret = (List<String[]>) selectQuery().node(new URI(subject)).follow(property).sel().end().run();
			valueOld = new ArrayList<String>();
			for(String[] ss : ret) {
				valueOld.add(ss[0]);
			}
		} catch (URISyntaxException e) {
			log.error("error", e);
		}
		return valueOld;
	}


	@Override
	public InputStream describe(String subject, Format format) throws RDFDatabaseException {
		try {
			return rdfService.sparqlDescribeQuery(subject, translate(format));
		} catch (RDFServiceException e) {
			throw new RDFDatabaseException(e);
		}
	}
	
	private ModelSerializationFormat translate(Format f) {
		switch (f) {
		case N3:
			return ModelSerializationFormat.N3;
		case RDF_XML:
			return ModelSerializationFormat.RDFXML;
		default:
			return null;
		}
	}

	@Override
	public boolean exists(String subject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getEntityTypeUri(String subject) {
		try {
			return ((List<String[]>)selectQuery().node(new URI(subject)).follow(ModelUtils.IS_A).uri().end().run()).get(0)[0];
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public actionHead<Dquery<Object, Object>, Dquery<Object, Object>, Object, Object> selectQuery() {
		return databook.edsl.googql.Utils.prog().use(rdfs);
	}
	
}
