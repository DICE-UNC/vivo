/*
Copyright (c) 2012, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
 * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package databook.web.controller;

import static databook.utils.ModelUtils.DATABOOK_MODEL_URI;
import static databook.utils.ModelUtils.databookResource;
import static databook.utils.ModelUtils.databookResourceNoBracket;
import static databook.utils.ModelUtils.databookStatement;
import static databook.utils.ModelUtils.validateUri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import databook.local.model.RDFDatabase.Format;
import databook.local.model.RDFDatabase.RDFDatabaseTransaction;
import databook.local.model.RDFDatabaseException;
import databook.listener.vivo.*;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

/**
 * This servlet will convert a request to an EditSubmission, find the
 * EditConfiguration associated with the request, use ProcessRdfForm to process
 * these to a set of RDF additions and retractions, the apply these to the
 * models.
 */
public class DeleteRecordPartialAjaxController extends FreemarkerHttpServlet {

	private Log log = LogFactory
			.getLog(DeleteRecordPartialAjaxController.class);

	/** Limit file size to 10 megabytes. */
	public static final int MAXIMUM_FILE_SIZE = 10 * 1024 * 1024;

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return SimplePermission.DO_FRONT_END_EDITING.ACTIONS;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		log.info("^^^ !!! *** file upload *** !!! ^^^");

		String objectUri = null, subjectUri = null, predicateUri = null;

		objectUri = vreq.getParameter("objectUri");
		subjectUri = vreq.getParameter("subjectUri");
		predicateUri = vreq.getParameter("predicateUri");

		if (subjectUri == null)
			throw new Error("No subjectUri found");
		if (predicateUri == null)
			throw new Error("No predicateUri found");
		if (objectUri == null)
			throw new Error("No objectUri found");
		validateUri(subjectUri);
		validateUri(predicateUri);
		validateUri(objectUri);

		WebappDaoFactory webAppDaoFactory = vreq.getWebappDaoFactory();
		IndividualDao individualDao = webAppDaoFactory.getIndividualDao();
		Individual subject = individualDao.getIndividualByURI(subjectUri);

		RDFServiceFactory rdfServiceFactory = RDFServiceUtils
				.getRDFServiceFactory(getServletContext());
		RDFService rdfService = rdfServiceFactory.getRDFService();
		VIVORDFDatabase database = new VIVORDFDatabase(new SimpleRDFServiceWrapper(rdfService));
		try {
			RDFDatabaseTransaction trans = database.newTransaction();
			trans.start();
			if (predicateUri.equals(databookResourceNoBracket("discussion"))) {
				trans.remove(
						databookStatement("<" + objectUri + ">", "a",
								databookResource("post")), Format.N3,
						DATABOOK_MODEL_URI);
			} else if (predicateUri
					.equals(databookResourceNoBracket("likedBy"))
					|| predicateUri
							.equals(databookResourceNoBracket("dislikedBy"))) {

			} else if (predicateUri
					.equals(databookResourceNoBracket("metadata"))) {
				trans.remove(
						databookStatement("<" + objectUri + ">", "a",
								databookResource("avu")), Format.N3,
						DATABOOK_MODEL_URI);
			}
			trans.remove(
					databookStatement("<" + subjectUri + ">", "<"
							+ predicateUri + ">", "<" + objectUri + ">"),
					Format.N3, DATABOOK_MODEL_URI);

			trans.commit();

		} catch (RDFDatabaseException e) {
			throw new Error(e);
		}
		Map<String, Object> values = new HashMap<String, Object>();

		List<ObjectPropertyStatement> objPropStmtList = subject
				.getObjectPropertyStatements(predicateUri);
		values.put("predicateUri", predicateUri);
		values.put("localName",
				predicateUri.substring(predicateUri.indexOf('#') + 1));
		values.put("statements", objPropStmtList);
		values.put("statements_size", objPropStmtList.size());
		return new TemplateResponseValues("deleteRecordPartialAjaxRet.ftl",
				values);
	}

}
