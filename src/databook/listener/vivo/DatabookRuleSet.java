
package databook.listener.vivo;


import static databook.utils.ModelUtils.DATABOOK_MODEL_URI;
import static databook.utils.ModelUtils.IS_A;
import static databook.utils.ModelUtils.bracket;
import static databook.utils.ModelUtils.databookStatement;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import databook.local.model.RDFDatabase.Format;
import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.RuleRegistry;
import databook.persistence.rule.TranscientPropertyRule;
import databook.persistence.rule.rdf.RDFAbstractCollectionPropertyRule;
import databook.persistence.rule.rdf.RDFCollectionPropertyRule;
import databook.persistence.rule.rdf.RDFEntityRule;
import databook.persistence.rule.rdf.RDFNumberPropertyRule;
import databook.persistence.rule.rdf.RDFObjectPropertyRule;
import databook.persistence.rule.rdf.RDFStringPropertyRule;
import databook.persistence.rule.rdf.RDFTimePropertyRule;
import databook.persistence.rule.rdf.StringStringMapping;
import databook.persistence.rule.rdf.ruleset.AVU;
import databook.persistence.rule.rdf.ruleset.Access;
import databook.persistence.rule.rdf.ruleset.AccessPermission;
import databook.persistence.rule.rdf.ruleset.Collection;
import databook.persistence.rule.rdf.ruleset.DataEntity;
import databook.persistence.rule.rdf.ruleset.DataEntityLink;
import databook.persistence.rule.rdf.ruleset.DataObject;
import databook.persistence.rule.rdf.ruleset.Identifier;
import databook.persistence.rule.rdf.ruleset.IndividualObject;
import databook.persistence.rule.rdf.ruleset.Post;
import databook.persistence.rule.rdf.ruleset.RDFEntity;
import databook.persistence.rule.rdf.ruleset.Replica;
import databook.persistence.rule.rdf.ruleset.Session;
import databook.persistence.rule.rdf.ruleset.StorageLocation;
import databook.persistence.rule.rdf.ruleset.StorageLocationRequest;
import databook.persistence.rule.rdf.ruleset.User;
import databook.persistence.rule.rdf.ruleset.UserLink;
import databook.utils.ModelUtils;

public class DatabookRuleSet extends RuleRegistry {

	public DatabookRuleSet() {
		
		TranscientPropertyRule t2 = new TranscientPropertyRule();
		Class class1 = java.util.Collection.class;

		registerRule(RDFEntity.class, new RDFEntityRule<RDFEntity>());
		registerRule(RDFEntity.class, "additionalProperties", Object.class, t2);
		
		registerRule(Identifier.class, new RDFEntityRule<Identifier>());
		registerRule(Identifier.class, "identifierType", String.class, new RDFStringPropertyRule<Identifier>());
		registerRule(Identifier.class, "identifierValue", String.class, new RDFStringPropertyRule<Identifier>());
		
		registerRule(DataEntity.class, new RDFEntityRule<DataEntity>());
		registerRule(DataEntity.class, "uri", java.net.URI.class, t2);
		registerRule(DataEntity.class, "typeUri", java.net.URI.class, t2);
		registerRule(DataEntity.class, "label", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "id", Identifier.class, new RDFObjectPropertyRule<DataEntity, Identifier>(false));
		registerRule(DataEntity.class, "type", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "hasPart", (Class<java.util.Collection<DataEntity>>)class1, new RDFCollectionPropertyRule<DataEntity, DataEntity>(false));
		registerRule(DataEntity.class, "related", class1, new RDFCollectionPropertyRule<DataEntity, DataEntity>(false));
		registerRule(DataEntity.class, "partOf", class1, new RDFCollectionPropertyRule<DataEntity, DataEntity>(true));
		registerRule(DataEntity.class, "description", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "title", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "created", Date.class, new RDFTimePropertyRule<DataEntity>());
		registerRule(DataEntity.class, "submitted", Date.class, new RDFTimePropertyRule<DataEntity>());
		registerRule(DataEntity.class, "owner", User.class, new RDFObjectPropertyRule<DataEntity, User>(true));
		registerRule(DataEntity.class, "contributor", (Class<java.util.Collection<User>>)class1, new RDFCollectionPropertyRule<DataEntity, User>(false));
		registerRule(DataEntity.class, "discussion", (Class<java.util.Collection<Post>>)class1, new RDFCollectionPropertyRule<DataEntity, Post>(false));
		registerRule(DataEntity.class, "likedBy", (Class<java.util.Collection<User>>)class1, new RDFCollectionPropertyRule<DataEntity, User>(false));
		registerRule(DataEntity.class, "dislikedBy", (Class<java.util.Collection<User>>)class1, new RDFCollectionPropertyRule<DataEntity, User>(false));
		registerRule(DataEntity.class, "metadata", (Class<java.util.Collection<AVU>>)class1, new IrodsMetadataPropertyRule());
		registerRule(DataEntity.class, "accessHistory", (Class<java.util.Collection<Access>>)class1, new RDFCollectionPropertyRule<DataEntity, Access>(false));
		registerRule(DataEntity.class, "accessPermission", (Class<java.util.Collection<AccessPermission>>)class1, new RDFCollectionPropertyRule<DataEntity, AccessPermission>(true));
		registerRule(DataEntity.class, "storageLocation", (Class<java.util.Collection<StorageLocation>>)class1, t2);
		registerRule(DataEntity.class, "storageLocationRequest", (Class<java.util.Collection<StorageLocationRequest>>)class1, t2);
		registerRule(DataEntity.class, "tempInputStream", InputStream.class, t2);
		registerRule(DataEntity.class, "previewPath", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "previewThumbPath", String.class, new RDFStringPropertyRule<DataEntity>());

		registerRule(IndividualObject.class, new RDFEntityRule<IndividualObject>());
		registerRule(IndividualObject.class, "replaces", DataEntity.class, new RDFObjectPropertyRule<IndividualObject, DataEntity>(false));
		registerRule(IndividualObject.class, "replacedBy", DataEntity.class, new RDFObjectPropertyRule<IndividualObject, DataEntity>(false));
		registerRule(IndividualObject.class, "hasVersion", String.class, new RDFStringPropertyRule<IndividualObject>());
		registerRule(IndividualObject.class, "dataSize", Double.class, new RDFNumberPropertyRule<IndividualObject>());

		registerRule(User.class, new RDFEntityRule<User>() {
			public void create(User e, PersistenceContext context) {
				super.create(e, context);
				context.getRdfTrans().add(databookStatement(bracket(e.getUri()), bracket(ModelUtils.LABEL_URI), ModelUtils.databookString(e.getUri().toString())),
						Format.N3, ModelUtils.LABEL_MODEL_URI);
			}
		});
		registerRule(User.class, "contributeTo", class1, new RDFCollectionPropertyRule<User, DataEntity>(false));
		registerRule(User.class, "own", class1, new RDFCollectionPropertyRule<User, DataEntity>(false));
		registerRule(User.class, "like", class1, new RDFCollectionPropertyRule<User, DataEntity>(false));
		registerRule(User.class, "dislike", class1, new RDFCollectionPropertyRule<User, DataEntity>(false));

		registerRule(Access.class, new RDFEntityRule<Access>());
		registerRule(Access.class, "session", (Class<java.util.Collection<Session>>)class1, new RDFCollectionPropertyRule<Access, Session>(false));
		registerRule(Access.class, "finished", Date.class, new RDFTimePropertyRule<Access>());
		registerRule(Access.class, "linkingDataEntity", (Class<java.util.Collection<DataEntityLink>>)class1, new IrodsDataEntityLinkPropertyRule());
		registerRule(Access.class, "linkingUser", (Class<java.util.Collection<UserLink>>)class1, new RDFCollectionPropertyRule<Access, UserLink>(true));

		registerRule(Session.class, new RDFEntityRule<Session>());
		registerRule(Session.class, "sessionPart", (Class<java.util.Collection<Access>>)class1, new RDFCollectionPropertyRule<Session, Access>(false));

		registerRule(AccessPermission.class, new RDFEntityRule<AccessPermission>());
		registerRule(AccessPermission.class, "permission", String.class, new RDFStringPropertyRule<AccessPermission>());
		registerRule(AccessPermission.class, "linkingDataEntity", (Class<java.util.Collection<DataEntityLink>>)class1, new RDFCollectionPropertyRule<AccessPermission, DataEntityLink>(true));
		registerRule(AccessPermission.class, "linkingUser", (Class<java.util.Collection<UserLink>>)class1, new RDFCollectionPropertyRule<AccessPermission, UserLink>(true));

		registerRule(databook.persistence.rule.rdf.ruleset.Collection.class, new IrodsCollectionEntityRule());

		registerRule(AVU.class, new RDFEntityRule<AVU>()); // TODO change rule
		registerRule(AVU.class, "attribute", String.class, new RDFStringPropertyRule<AVU>());
		registerRule(AVU.class, "value", String.class, new RDFStringPropertyRule<AVU>());
		registerRule(AVU.class, "unit", String.class, new RDFStringPropertyRule<AVU>());
		
		registerRule(Collection.class, new IrodsCollectionEntityRule(){
			public void create(Collection e, PersistenceContext context) {
				super.create(e, context);
				try {
					// uri is available only when it comes from irods
					// we also need to avoid double labeling 	
					if(!e.getStorageLocationRequest().contains(StorageLocationRequest.IRODS) && ((List<String[]>)context.getRdfDb().selectQuery().node(new URI("file://"+e.getUri())).follow(ModelUtils.LABEL_URI).uri().end().run()).size() == 1) {
						URI uri = e.getUri();
						String uristr = uri.toString();
						String path = uristr.substring(0, uristr.indexOf('@'));
						context.getRdfTrans().add(databookStatement(bracket(e.getUri()), bracket(ModelUtils.LABEL_URI), ModelUtils.databookString(path)),
								Format.N3, ModelUtils.LABEL_MODEL_URI);
					}
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		registerRule(DataObject.class, new IrodsDataObjectEntityRule());
		registerRule(DataObject.class, "replica", (Class<java.util.Collection<Replica>>)class1, new RDFCollectionPropertyRule<DataObject, Replica>(true));

        registerRule(DataEntityLink.class, new RDFEntityRule<DataEntityLink>());
        registerRule(DataEntityLink.class, "dataEntity", DataEntity.class, new RDFObjectPropertyRule<DataEntityLink, DataEntity>(false));
        registerRule(DataEntityLink.class, "dataEntityRole", String.class, new RDFStringPropertyRule<DataEntityLink>());

		registerRule(UserLink.class, new RDFEntityRule<UserLink>());
		registerRule(UserLink.class, "user", User.class, new RDFObjectPropertyRule<UserLink, User>(true));
		registerRule(UserLink.class, "userRole", String.class, new RDFStringPropertyRule<UserLink>());
}


}
