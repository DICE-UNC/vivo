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

import static databook.utils.ConnectionUtils.adminAccount;
import static databook.utils.ConnectionUtils.irodsFs;
import databook.listener.vivo.*;
import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.rdf.ruleset.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.irods.jargon.core.connection.IRODSAccount;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.DirectRedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

/**
 * This servlet will convert a request to an EditSubmission, find the
 * EditConfiguration associated with the request, use ProcessRdfForm to process
 * these to a set of RDF additions and retractions, the apply these to the
 * models.
 */
public class NewDataObjFormFileUploadController extends FreemarkerHttpServlet {

	private Log log = LogFactory
			.getLog(NewDataObjFormFileUploadController.class);

	/** Limit file size to 10 megabytes. */
	public static final int MAXIMUM_FILE_SIZE = 10 * 1024 * 1024;

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return SimplePermission.DO_FRONT_END_EDITING.ACTIONS;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		log.info("^^^ !!! *** file upload *** !!! ^^^");

		String filename = null;
		String collName = null;
		String dataType = null;
		InputStream filecontent = null;
		String subjectUri = null;
		if ((collName = vreq.getParameter("coll")) != null) {
			subjectUri = vreq.getParameter("subjectUri");
			dataType = vreq.getParameter("dataType");
		} else {
			try {
				List<FileItem> items = new ServletFileUpload(
						new DiskFileItemFactory()).parseRequest(vreq);
				for (FileItem item : items) {
					if (item.isFormField()) {
						String fieldname = item.getFieldName();
						String fieldvalue = item.getString();
						log.info(fieldname + " = " + fieldvalue);
						if (fieldname.equals("subjectUri")) {
							subjectUri = fieldvalue;
						} else if (fieldname.equals("dataType")) {
							dataType = fieldvalue;
						} else if (fieldname.equals("coll")) {
							collName = fieldvalue;
						}
					} else {
						String fieldname = item.getFieldName();
						filename = FilenameUtils.getName(item.getName());
						filecontent = item.getInputStream();
						log.info(fieldname + " = (file) " + filename);
					}
				}
			} catch (Exception e) {
				throw new Error(e);
			}
		}

		log.info("subject uri = " + subjectUri);
		int from = subjectUri.indexOf('#') + 1;
		int to = subjectUri.length() - 10; // remove the timestamp
		String collectionName = subjectUri.substring(from, to);
		log.info("collection name = " + collectionName);
		IRODSAccount acc = null;
		DatabookRuleSet rule = new DatabookRuleSet();
		
		try {
			acc = adminAccount();
			PersistenceContext pc = new PersistenceContext(null, null, rule, irodsFs, acc);
			if (dataType.equals("Data Object")) {
				DataObject entity = new DataObject();
				entity.setLabel(collectionName + "/" + collName);
				entity.setTempInputStream(filecontent);
				entity.setStorageLocationRequest(Collections.singletonList(StorageLocationRequest.IRODS));
				rule.lookupRule(entity).create(entity, null);

			} else if (dataType.equals("Collection")) {
				Collection entity = new Collection();
				entity.setLabel(collectionName + "/" + collName);
				entity.setStorageLocationRequest(Collections.singletonList(StorageLocationRequest.IRODS));
				rule.lookupRule(entity).create(entity, null);
			} else {
				throw new Error("data type error");
			}
		} catch (Exception e) {
			throw new Error(e);
		} finally {
			if (filecontent != null) {
				try {
					filecontent.close();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
			if (acc != null) {
				try {
					irodsFs.close(acc);
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		}

		try {
			return new DirectRedirectResponseValues(
					new URI(null, null, null, -1, "/vivo/individual", "uri="
							+ subjectUri, null).toString());
		} catch (URISyntaxException e) {
			throw new Error(e);
		}
	}

}
