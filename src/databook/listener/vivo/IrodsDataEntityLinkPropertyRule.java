package databook.listener.vivo;

import static databook.persistence.rule.rdf.RDFAbstractPropertyRule.PROP_MODEL;
import static databook.utils.ModelUtils.ACCESS_HISTORY_URI;
import static databook.utils.ModelUtils.bracket;
import static databook.utils.ModelUtils.databookStatement;
import static databook.utils.ModelUtils.extractId;

import java.util.Collection;
import java.util.Collections;

import databook.local.model.RDFDatabase.Format;
import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.rdf.RDFCollectionPropertyRule;
import databook.persistence.rule.rdf.ruleset.Access;
import databook.persistence.rule.rdf.ruleset.DataEntityLink;

public class IrodsDataEntityLinkPropertyRule extends RDFCollectionPropertyRule<Access, DataEntityLink>{

	IrodsDataEntityLinkPropertyRule() {
		super(true);
	}
	@Override
	public void create(Access e, String prop, Collection<DataEntityLink> o, PersistenceContext context) {
		for(DataEntityLink ol : o) {
			context.union(ol.getDataEntity(), extractId(ACCESS_HISTORY_URI.toString()),Collections.EMPTY_LIST, Collections.singleton(e));
			//context.getRdfTrans().add(databookStatement(bracket(ol.getDataEntity().getUri()),
			//	bracket(ACCESS_HISTORY_URI),
			//	bracket(e.getUri())), 
			//	Format.N3, PROP_MODEL.getPropModelURI(prop));
		}
		super.create(e, prop, o, context);
	}

}
