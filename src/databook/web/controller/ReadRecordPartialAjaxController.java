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

import static databook.utils.ModelUtils.databookResource;
import static databook.utils.ModelUtils.databookResourceNoBracket;
import static databook.utils.ModelUtils.rdfsResource;
import static databook.utils.ModelUtils.validateUri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.ProcessRdfFormController;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;


/**
 * This servlet will convert a request to an EditSubmission, 
 * find the EditConfiguration associated with the request, 
 * use ProcessRdfForm to process these to a set of RDF additions and retractions,
 * the apply these to the models. 
 */
public class ReadRecordPartialAjaxController extends FreemarkerHttpServlet{
	
    private Log log = LogFactory.getLog(ReadRecordPartialAjaxController.class);

    	
//    @Override
//	protected Actions requiredActions(VitroRequest vreq) {
//    		return SimplePermission.DO_FRONT_END_EDITING.ACTIONS;
//	}

	@Override 
	protected ResponseValues processRequest(VitroRequest vreq) {

		String objectUri = null, subjectUri = null, predicateUri = null;

		objectUri = vreq.getParameter("objectUri");
		subjectUri = vreq.getParameter("subjectUri");
		predicateUri = vreq.getParameter("predicateUri");

		if(subjectUri == null)
			throw new Error("No subjectUri found");
		if(predicateUri == null)
			throw new Error("No predicateUri found");
		if(objectUri != null) {
		  validateUri(objectUri);
		}
		validateUri(subjectUri);
		validateUri(predicateUri);

		//WebappDaoFactory webAppDaoFactory = vreq.getWebappDaoFactory();
		//IndividualDao individualDao = webAppDaoFactory.getIndividualDao();
		//Individual subject = individualDao.getIndividualByURI(subjectUri);

		RDFServiceFactory rdfServiceFactory = RDFServiceUtils.getRDFServiceFactory(getServletContext());
		RDFService rdfService = rdfServiceFactory.getRDFService();
		Dataset ds = new RDFServiceDataset(rdfService);
		Map<String, Object> values = new HashMap<String, Object>();

		List<Object> posts = new ArrayList<Object>();
		
		if(predicateUri.equals(databookResourceNoBracket("discussion"))) {
			  values.put("template", "propStatement-discussion.ftl");
			  QueryExecution qexec = null;
			  try {
			      String valuesOfProperty = 
				      "SELECT ?object ?title ?description ?created ?owner ?ownerLabel \n" +
				      "WHERE { <" + subjectUri + "> <" + predicateUri + "> ?object \n" +
				      "         OPTIONAL { ?object " + databookResource("title") + " ?title } \n" +
				      "         OPTIONAL { ?object " + databookResource("description") + " ?description } \n" +
				      "         OPTIONAL { ?object " + databookResource("created") + " ?created } \n" +
				      "         OPTIONAL { ?object " + databookResource("owner") + " ?owner } \n" +
				      "         OPTIONAL { ?object " + databookResource("owner") + " ?owner . ?owner " + rdfsResource("label") + " ?ownerLabel } \n" +
				      "} ORDER BY DESC ( ?created ) ?object ";
				      log.info("query = "+valuesOfProperty);
			      Query query = QueryFactory.create(valuesOfProperty);
			      qexec = QueryExecutionFactory.create(query, ds);
			      ResultSet rs = qexec.execSelect();
			      while (rs.hasNext()) {
				      QuerySolution s = rs.next();
				      Map<String, Object> post = new HashMap<String, Object>();
				      
				      Literal title = s.getLiteral("?title");
				      Literal description = s.getLiteral("?description");
				      Resource owner = s.getResource("?owner");
				      Literal created = s.getLiteral("?created");
				      Literal ownerLabel = s.getLiteral("?ownerLabel");
				      Resource object = s.getResource("?object");
				      
				      // freemarker treats null as undefined
				      post.put("ownerLabel", ownerLabel==null?null:ownerLabel.getValue());
				      post.put("title", title==null?null:title.getValue());
				      post.put("description", description==null?null:description.getValue());
				      post.put("owner", owner==null?null:owner.getURI());
				      post.put("created", created==null?null:created.getValue());
				      post.put("object", object);
				      
				      posts.add(post);
				      
			      }
		      } finally {
			  if(qexec!=null) qexec.close();
		      }
     	
			  
			  values.put("statements", posts);
			  values.put("statements_size", posts.size());
		  } else if(predicateUri.equals(databookResourceNoBracket("hasPart"))) {
			  values.put("template", "propStatement-hasPart.ftl");
			  QueryExecution qexec = null;
			  try {
			      String valuesOfProperty = 
				      "SELECT ?object ?type ?label ?owner ?ownerLabel \n" +
				      "WHERE { <" + subjectUri + "> <" + predicateUri + "> ?object . \n" +
				      "         ?object " + rdfsResource("label") + " ?label . \n" +
				      "         ?object <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> ?typeUri . \n" +
				      "         ?typeUri " + rdfsResource("label") + " ?type . \n"+
				      "         OPTIONAL { ?object " + databookResource("owner") + " ?owner } \n" +
				      "         OPTIONAL { ?object " + databookResource("owner") + " ?owner . ?owner " + rdfsResource("label") + " ?ownerLabel } \n" +
				      "} ORDER BY ASC ( ?label ) ?object ";
				      log.info("query = "+valuesOfProperty);
			      Query query = QueryFactory.create(valuesOfProperty);
			      qexec = QueryExecutionFactory.create(query, ds);
			      ResultSet rs = qexec.execSelect();
			      while (rs.hasNext()) {
				      QuerySolution s = rs.next();
				      Map<String, Object> post = new HashMap<String, Object>();
				      
				      Literal type = s.getLiteral("?type");
				      Literal label = s.getLiteral("?label");
				      Resource owner = s.getResource("?owner");
				      Literal ownerLabel = s.getLiteral("?ownerLabel");
				      Resource object = s.getResource("?object");
				      
				      // freemarker treats null as undefined
				      post.put("ownerLabel", ownerLabel==null?null:ownerLabel.getValue());
				      post.put("type", type==null?null:type.getValue());
				      post.put("label", label==null?null:label.getValue());
				      post.put("owner", owner==null?null:owner.getURI());
				      post.put("object", object);
				      
				      posts.add(post);
				      
			      }
		      } finally {
			  if(qexec!=null) qexec.close();
		      }
     	
		      values.put("statements", posts);
		      values.put("statements_size", posts.size());
		  } else if(predicateUri.equals(databookResourceNoBracket("metadata"))) {
			  values.put("template", "propStatement-avu.ftl");
			  QueryExecution qexec = null;
			  try {
			      String valuesOfProperty = 
				      "SELECT ?object ?attrib ?value ?unit \n" +
				      "WHERE { <" + subjectUri + "> <" + predicateUri + "> ?object . \n" +
				      "         ?object " + databookResource("attribute") + " ?attrib . \n" +
				      "         ?object " + databookResource("value") + " ?value . \n" +
				      "         ?object " + databookResource("unit") + " ?unit . \n" +
				      "} ORDER BY ASC ( ?attrib ) ASC ( ?value ) ASC ( ?unit ) ?object ";
				      log.info("query = "+valuesOfProperty);
			      Query query = QueryFactory.create(valuesOfProperty);
			      qexec = QueryExecutionFactory.create(query, ds);
			      ResultSet rs = qexec.execSelect();
			      while (rs.hasNext()) {
				      QuerySolution s = rs.next();
				      Map<String, Object> post = new HashMap<String, Object>();
				      
				      Literal attrib = s.getLiteral("?attrib");
				      Literal value = s.getLiteral("?value");
				      Literal unit = s.getLiteral("?unit");
				      Resource object = s.getResource("?object");
				      
				      // freemarker treats null as undefined
				      post.put("attribute", attrib==null?null:attrib.getValue());
				      post.put("value", value==null?null:value.getValue());
				      post.put("unit", unit==null?null:unit.getValue());
				      post.put("object", object);
				      
				      posts.add(post);
				      
			      }
		      } finally {
			  if(qexec!=null) qexec.close();
		      }
     	
		      values.put("statements", posts);
		      values.put("statements_size", posts.size());
		  } else {
		    //unsupported predicateUri
		  }

		
		values.put("predicateUri", predicateUri);
		values.put("localName", predicateUri.substring(predicateUri.indexOf('#')+1));
		return new TemplateResponseValues("readRecordPartialAjaxRet.ftl", values);
	}




	//In case of back button confusion
	//Currently returning an error message: 
	//Later TODO: Per Brian Caruso's instructions, replicate
	//the logic in the original datapropertyBackButtonProblems.jsp
	private ResponseValues doProcessBackButton(EditConfigurationVTwo configuration,
			MultiValueEditSubmission submission, VitroRequest vreq) {
		  
		//The bulk of the processing should probably/already sits in ProcessRdfForm so that should remain there
		//The issue is what then to do with the actual redirect? What do we redirect to?
		HashMap<String,Object> map = new HashMap<String,Object>();
   	 	map.put("errorMessage", "Back button confusion has occurred");
		ResponseValues values = new TemplateResponseValues("error-message.ftl", map);        
		return values;
	}

	//Check for "back button" confusion specifically for data property editing although need to check if this applies to object property editing?
	//TODO: Check if only applicable to data property editing
	private boolean checkForBackButtonConfusion(EditConfigurationVTwo editConfig, VitroRequest vreq, Model model) {
		//back button confusion limited to data property
		if(EditConfigurationUtils.isObjectProperty(editConfig.getPredicateUri(), vreq)) {
			return false;
		}
		
		WebappDaoFactory wdf = vreq.getWebappDaoFactory();
		 if ( ! editConfig.isDataPropertyUpdate())
	            return false;
	        
        Integer dpropHash = editConfig.getDatapropKey();
        DataPropertyStatement dps = 
            RdfLiteralHash.getPropertyStmtByHash(editConfig.getSubjectUri(), 
                    editConfig.getPredicateUri(), dpropHash, model);
        if (dps != null)
            return false;
        
        DataProperty dp = wdf.getDataPropertyDao().getDataPropertyByURI(
                editConfig.getPredicateUri());
        if (dp != null) {
            if (dp.getDisplayLimit() == 1 /* || dp.isFunctional() */)
                return false;
            else
                return true;
        }
        return false;

	}
	
	private ResponseValues doValidationErrors(VitroRequest vreq,
			EditConfigurationVTwo editConfiguration, MultiValueEditSubmission submission) {
		
		Map<String, String> errors = submission.getValidationErrors();
		
		if(errors != null && !errors.isEmpty()){
			String form = editConfiguration.getFormUrl();
			vreq.setAttribute("formUrl", form);
			vreq.setAttribute("view", vreq.getParameter("view"));	
			//Need to ensure that edit key is set so that the correct
			//edit configuration and edit submission are retrieved
			//This can also be set as a parameter instead
			String formUrl = editConfiguration.getFormUrl();
			formUrl += "&editKey=" + editConfiguration.getEditKey();
	        return new RedirectResponseValues(formUrl);
		}
		return null; //no errors		
	}
	
	//Move to EditN3Utils but keep make new uris here
	public static class Utilities {
		
		private static Log log = LogFactory.getLog(ProcessRdfFormController.class);
	    
		public static String assertionsType = "assertions";
		public static String retractionsType = "retractions";
		
		public static boolean isDataProperty(EditConfigurationVTwo configuration, VitroRequest vreq) {
			return EditConfigurationUtils.isDataProperty(configuration.getPredicateUri(), vreq);
		}
		
		public static boolean isObjectProperty(EditConfigurationVTwo configuration, VitroRequest vreq) {
			
			return EditConfigurationUtils.isObjectProperty(configuration.getPredicateUri(), vreq);
		}
		
	    public static List<String> makeListCopy(List<String> list) {
	    	List<String> copyOfN3 = new ArrayList<String>();
            for( String str : list){
                copyOfN3.add(str);
            }
            return copyOfN3;
	    }
	     
	     //TODO: Check if this would be correct with multiple values and uris being passed back
	     //First, need to order by uris in original and new values probably and 
	     //for literals, order by? Alphabetical or numeric value? Hard to say
	     public static boolean hasFieldChanged(String fieldName,
	             EditConfigurationVTwo editConfig, MultiValueEditSubmission submission) {
	         List<String> orgValue = editConfig.getUrisInScope().get(fieldName);
	         List<String> newValue = submission.getUrisFromForm().get(fieldName);
	         //Sort both just in case
	         if(orgValue != null) {
	        	 Collections.sort(orgValue);
	         }
	         if(newValue != null) {
	        	 Collections.sort(newValue);
	         }
	         if (orgValue != null && newValue != null) {
	             if (orgValue.equals(newValue))
	                 return false;
	             else
	                 return true;
	         }

	         List<Literal> orgLit = editConfig.getLiteralsInScope().get(fieldName);
	         List<Literal> newLit = submission.getLiteralsFromForm().get(fieldName);
	         //TODO: Sort ? Need custom comparator
	         //Collections.sort(orgLit);
	         //Collections.sort(newLit);
	         //for(Literal l: orgLit)
	         //boolean fieldChanged = !EditLiteral.equalLiterals(orgLit, newLit);
	         //TODO:Check if below acts as expected
	         boolean fieldChanged = !orgLit.equals(newLit);
	         if(!fieldChanged) {
	        	 int orgLen = orgLit.size();
	        	 int newLen = newLit.size();
	        	 if(orgLen != newLen) {
	        		 fieldChanged = true;
	        	 } else {
	        		 int i;
	        		 for(i = 0; i < orgLen; i++) {
	        			 if(!EditLiteral.equalLiterals(orgLit.get(i), newLit.get(i))) {
	        				 fieldChanged = true;
	        				 break;
	        			 }
	        		 }
	        	 }
	         }
	         log.debug("field " + fieldName + " "
	                 + (fieldChanged ? "did Change" : "did NOT change"));
	         return fieldChanged;
	     }
	     		
		//Get predicate local anchor
		public static String getPredicateLocalName(EditConfigurationVTwo editConfig) {
			String predicateLocalName = null;
	        if( editConfig != null ){
                String predicateUri = editConfig.getPredicateUri();            
                if( predicateUri != null ){
                    try{
                        Property prop = ResourceFactory.createProperty(predicateUri);
                        predicateLocalName = prop.getLocalName();
                    
                    }catch (com.hp.hpl.jena.shared.InvalidPropertyURIException e){                  
                        log.debug("could not convert predicateUri into a valid URI",e);
                    }                               
                }
	        }
	        return predicateLocalName;
		}
		//Get URL pattern for return
		public static String getPostEditUrlPattern(VitroRequest vreq, EditConfigurationVTwo editConfig) {
			String cancel = vreq.getParameter("cancel");
	        String urlPattern = null;

            String urlPatternToReturnTo = null;
            String urlPatternToCancelTo = null;
            if (editConfig != null) {
                urlPatternToReturnTo = editConfig.getUrlPatternToReturnTo();
                urlPatternToCancelTo = vreq.getParameter("url");
            }
            // If a different cancel return path has been designated, use it. Otherwise, use the regular return path.
            if (cancel != null && cancel.equals("true") && !StringUtils.isEmpty(urlPatternToCancelTo)) {
                urlPattern = urlPatternToCancelTo;
            } else if (!StringUtils.isEmpty(urlPatternToReturnTo)) {
                urlPattern = urlPatternToReturnTo;       
            } else {
                urlPattern = "/individual";         
            }
            return urlPattern;
		}
		
		//Get resource to redirect to
		public static String getResourceToRedirect(VitroRequest vreq, EditConfigurationVTwo editConfig, String entityToReturnTo ) {
			String resourceToRedirectTo = null;
			if( editConfig != null ){

                if( editConfig.getEntityToReturnTo() != null && editConfig.getEntityToReturnTo().startsWith("?") ){             
                    resourceToRedirectTo = entityToReturnTo;            
                }else{            
                    resourceToRedirectTo = editConfig.getEntityToReturnTo();
                }
                
                //if there is no entity to return to it is likely a cancel
                if( resourceToRedirectTo == null || resourceToRedirectTo.length() == 0 )
                    resourceToRedirectTo = editConfig.getSubjectUri();                
            }
			return resourceToRedirectTo;
		}
		

			
	}
}
