package databook.listener.vivo;

import static databook.utils.ModelUtils.ATTRIBUTE_URI;
import static databook.utils.ModelUtils.IS_A;
import static databook.utils.ModelUtils.UNIT_URI;
import static databook.utils.ModelUtils.VALUE_URI;
import static databook.utils.ModelUtils.bracket;
import static databook.utils.ModelUtils.databookResourceNoBracket;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Collection;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import databook.edsl.googql.action;
import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.rdf.RDFCollectionPropertyRule;
import databook.persistence.rule.rdf.StringObjectMapping;
import databook.persistence.rule.rdf.ruleset.AVU;
import databook.persistence.rule.rdf.ruleset.DataEntity;

public class IrodsMetadataPropertyRule extends RDFCollectionPropertyRule<DataEntity, AVU> {

	private static Logger log = Logger.getLogger(IrodsMetadataPropertyRule.class);
	
	public IrodsMetadataPropertyRule(StringObjectMapping<AVU> som) {
		super(som, true);
	}

	public IrodsMetadataPropertyRule() {
		super(new StringObjectMapping<AVU>(){
			private final Logger log = Logger.getLogger(this.getClass());
			private String encode(String s) {
				try {
					return URLEncoder.encode(s, "utf8");
				} catch (UnsupportedEncodingException e) {
					log.error("", e);
					return null;
				}
			}

			@Override
			public AVU stringToObject(String val) {
				return null;
			}

			@Override
			public String objectToString(AVU obj) {
				return encode(obj.getAttribute())+"+"+encode(obj.getValue())+"+"+encode(obj.getUnit());
			}

			@Override
			public action query(action a) {
				return a.follow(ATTRIBUTE_URI).sel().back()
						.follow(VALUE_URI).sel().back()
						.follow(UNIT_URI).sel().back()
						.sel().back()
						.follow(IS_A).sel();
			}

			@Override
			public AVU sparqlQueryResultStringToObject(String[] val) {
				AVU avu = new AVU();
				avu.setAttribute(val[0]);
				avu.setValue(val[1]);
				avu.setUnit(val[2]);
				try {
					avu.setUri(new URI(val[3]));
					avu.setTypeUri(new URI(val[4]));
				} catch (URISyntaxException e) {
					log.error("", e);
				}
				
				return avu;
			}

			@Override
			public String objectToRdfString(AVU obj) {
				return bracket(obj.getUri());
				//return databookAnonymousResource(ATTRIBUTE_URI, obj.getAttribute(),
				//	VALUE_URI, obj.getValue(),
				//  UNIT_URI, obj.getUnit());
			}

			@Override
			public AVU msgStringToObject(String val) {
				return null;
			}

			@Override
			public String objectToMsgString(AVU obj) {
				return null;
			}}, true);
	}
	public static int count = 0;
	public static long countTime = 0;
	public static synchronized URI genUniqueUri() {
		long time = System.currentTimeMillis(); 
		if(time != countTime) {
			count = 0;
		} else {
			count ++;
		}
		ByteBuffer buf = ByteBuffer.allocate(12);
		buf.putLong(time);
		buf.putInt(count);
		buf.flip();
		URI uri = null;
		try {
			uri = new URI(databookResourceNoBracket("avu"+new String(Base64.encodeBase64(buf.array()))));
		} catch (URISyntaxException e) {
			log.error("error", e);
		}
		return uri;
	}

	@Override
	public void create(DataEntity e0, String prop, Collection<AVU> e1, PersistenceContext context) {
		// irods does not generate ids for avu, fill them in
		for(AVU avu : e1) {
			URI uri = context.lookupURIByDataEntityAndAVU(e0, avu);
			if(uri != null) {
				throw new RuntimeException("duplicate avu");
			}
			avu.setUri(genUniqueUri());
		}
		super.create(e0, prop, e1, context);
	}

	@Override
	public void delete(DataEntity e0, String prop, Collection<AVU> e1, PersistenceContext context) {
		if(e1 == null) {
			e1 = context.getPropertyObjects(e0, prop, som);
		}
		for(AVU avu : e1) {
			if(avu.getUri() == null) {
				avu.setUri(context.lookupURIByDataEntityAndAVU(e0, avu));
			}
		}
		super.delete(e0, prop, e1, context);
	}


}
