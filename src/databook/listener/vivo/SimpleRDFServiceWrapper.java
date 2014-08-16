package databook.listener.vivo;

import java.io.InputStream;

import databook.local.model.RDFServiceWrapper;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;

public final class SimpleRDFServiceWrapper implements RDFServiceWrapper {
	public RDFService rdfService;
	
	public SimpleRDFServiceWrapper(RDFService rdfService) {
		this.rdfService = rdfService;
	}
	
	@Override
	public InputStream sparqlSelectQuery(String query) throws Exception {
		// currently only support CSV from this method
		// use getRdfServiceImpl to get other formats
		return rdfService.sparqlSelectQuery(query, edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ResultFormat.CSV);
	}

	@Override
	public Object getRdfServiceImpl() {
		return rdfService;
	}
}