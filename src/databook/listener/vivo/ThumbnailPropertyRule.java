package databook.listener.vivo;

import static databook.utils.ModelUtils.ATTR_PREVIEW;
import static databook.utils.ModelUtils.ATTR_THUMB_PREVIEW;
import static databook.utils.ModelUtils.DATABOOK_MODEL_URI;
import static databook.utils.ModelUtils.bracket;
import static databook.utils.ModelUtils.databookResource;
import static databook.utils.ModelUtils.databookResourceNoBracket;
import static databook.utils.ModelUtils.databookStatement;
import static databook.utils.ModelUtils.databookString;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import databook.listener.ModelUpdateListener;
import databook.local.model.RDFDatabase.Format;
import databook.local.model.RDFDatabase.RDFDatabaseTransaction;
import databook.persistence.rule.ObjectPropertyRule;
import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.rdf.ruleset.RDFEntity;

public class ThumbnailPropertyRule<T extends RDFEntity> implements
		ObjectPropertyRule<T, String, PersistenceContext> {

	static final Log log = LogFactory.getLog(ThumbnailPropertyRule.class);

	public ThumbnailPropertyRule() {
		super();
	}

	@Override
	public void create(final T e, String prop, String o,
			PersistenceContext context) {
		if (o != null) {
			URI uri = e.getUri();
			RDFDatabaseTransaction trans = context.getRdfTrans();
			trans.add(
					databookStatement(bracket(uri), databookResource(prop),
							databookString(o)), Format.N3, DATABOOK_MODEL_URI);
			updateImage(e, prop, o, context);
		}
	}

	public void updateImage(final T e, String prop, String o,
			PersistenceContext context) {
		if (prop.equals(ATTR_THUMB_PREVIEW) || prop.equals(ATTR_PREVIEW)) {
			// these two are pseudo metadata in the sense that they are used for
			// presentation purposes only
			log.info("calling metadata specific handler for '" + prop + "'");
			final String previewPath;
			final String previewThumbPath;
			if (prop.equals(ATTR_PREVIEW)) {
				previewPath = o;
				// get the preview thumb path
				previewThumbPath = context.getRdfDb().getUniqueValue(e.getUri()
						.toString(),
						databookResourceNoBracket(ATTR_THUMB_PREVIEW));
			} else /* if (propertyName.equals(ATTR_THUMB_PREVIEW)) */{
				previewThumbPath = o;
				// get the preview path
				previewPath = context.getRdfDb().getUniqueValue(e.getUri()
						.toString(), databookResourceNoBracket(ATTR_PREVIEW));
			}
			if (previewPath != null && previewThumbPath != null) {
				Runnable postProc = new Runnable() {
					public void run() {
						try {
							ModelUpdateListener.instance.updateImage(e.getUri()
									.toString(), previewThumbPath, previewPath);
						} catch (IOException e) {
							log.error("Error updating preview '"
									+ e.getMessage() + "'");
						}
					}
				};
				context.appendToPostProcList(postProc);
			}
		}
	}

	@Override
	public void delete(T e, String prop, String o, PersistenceContext context) {
		if (o != null) {
			URI uri = e.getUri();
			RDFDatabaseTransaction trans = context.getRdfTrans();
			trans.remove(
					databookStatement(bracket(uri), databookResource(prop),
							bracket(e.getUri())), Format.N3, DATABOOK_MODEL_URI);
		}
	}

	@Override
	public void modify(T e, String prop, String o0, String o1,
			PersistenceContext context) {
		delete(e, prop, o0, context);
		create(e, prop, o1, context);
	}

	@Override
	public void union(T e, String prop, String o0, String o1,
			PersistenceContext context) {
		modify(e, prop, o0, o1, context);
	}

	@Override
	public void diff(T e, String prop, String o0, String o1,
			PersistenceContext context) {
		if (o1 != null) {
			delete(e, prop, o0, context);
		}
	}

}
