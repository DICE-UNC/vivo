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

package databook.web.serving;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;

import org.irods.jargon.core.connection.*;
import org.irods.jargon.core.connection.auth.*;
import org.irods.jargon.core.pub.io.*;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.exception.*;

import static databook.utils.ConnectionUtils.*;

/**
 * <p>
 * Handles a request to serve an uploaded file from the file storage system.
 * </p>
 * <p>
 * The path of the request should be the "alias URL" of the desired file. We
 * need to:
 * <ul>
 * <li>Use the alias URL to find the URI of the file bytestream object.</li>
 * <li>Find the file surrogate object to get the MIME type of the file, and
 * confirm the filename.</li>
 * <li>Set the MIME type on the output stream and serve the bytes.</li>
 * </ul>
 * </p>
 * <p>
 * If the request is superficially correct, but no such file can be found,
 * return a 404. If there is a break in the data structures within the model or
 * the file system, return a 500.
 * </p>
 */
public class IRODSFileServingServlet extends VitroHttpServlet {
	/** If we can't locate the requested image, use this one instead. */
	private static final String PATH_MISSING_LINK_IMAGE = "/images/missingLink.png";

	private static final Log log = LogFactory.getLog(IRODSFileServingServlet.class);

	/**
	 * Get a reference to the File Storage system.
	 */
	@Override
	public void init() throws ServletException {
	}

	@Override
	protected void doGet(HttpServletRequest rawRequest,
			HttpServletResponse response) throws ServletException, IOException {
		VitroRequest request = new VitroRequest(rawRequest);

		// Use the alias URL to get the URI of the bytestream object.
		String path = request.getParameter("path");
		log.debug("Path is '" + path + "'");

		/*
		 * Get the mime type and an InputStream from the file. If we can't, use
		 * the dummy image file instead.
		 */
		String mimeType = "binary/octet-stream"; // use generic mime type
		InputStream fis = null;
		IRODSAccount acc = null;
		IRODSFileFactory fileFactory = null;
		try {
		  acc = adminAccount();
		  fileFactory = irodsFs.getIRODSFileFactory(acc);
		  fis = fileFactory.instanceIRODSFileInputStream(path);
		} catch (Exception e) {
			String referer = request.getHeader("referer");
			log.warn("Failed to serve the file at '" + path + "' -- " + e.getMessage());
			fis = openMissingLinkImage(request);
			mimeType = "image/png";
		}

		/*
		 * Everything is ready. Set the status and the content type, and send
		 * the image bytes.
		 */
		response.setStatus(SC_OK);

		if (mimeType != null) {
			response.setContentType(mimeType);
		}
		
		response.setHeader("Content-Disposition", "attachment; filename=\""+path+"\"");

		ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
			byte[] buffer = new byte[8192];
			int howMany;
			while (-1 != (howMany = fis.read(buffer))) {
				out.write(buffer, 0, howMany);
			}
		} finally {
			if (fis != null)
			  try {
			    fis.close();
			  } catch (Exception e) {
				  throw new Error(e);
			}
			if(acc!=null) {
			  try {
			      irodsFs.close(acc);
			  } catch (Exception e) {
				  throw new Error(e);
			  }
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		}
	}

	/** Any suprises when opening the image? Use this one instead. */
	private InputStream openMissingLinkImage(VitroRequest vreq)
			throws FileNotFoundException {
		InputStream stream = vreq.getSession().getServletContext()
				.getResourceAsStream(PATH_MISSING_LINK_IMAGE);
		if (stream == null) {
			throw new FileNotFoundException("No image file at '"
					+ PATH_MISSING_LINK_IMAGE + "'");
		}
		return stream;
	}

	/**
	 * A POST request is treated the same as a GET request.
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * There was a problem serving the file bytestream.
	 */
	private static class FileServingException extends Exception {
		public FileServingException(String message) {
			super(message);
		}

		public FileServingException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
