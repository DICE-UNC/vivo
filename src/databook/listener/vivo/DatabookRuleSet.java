
package databook.listener.vivo;


import java.io.InputStream;
import java.net.URI;
import java.util.Date;

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
import databook.persistence.rule.rdf.ruleset.User;
import databook.persistence.rule.rdf.ruleset.UserLink;

public class DatabookRuleSet extends RuleRegistry {

	public DatabookRuleSet() {
		
		TranscientPropertyRule<RDFEntity, Object, PersistenceContext> t2 = new TranscientPropertyRule<RDFEntity, Object, PersistenceContext>();

		registerRule(RDFEntity.class, new RDFEntityRule<RDFEntity>());
		registerRule(RDFEntity.class, "additionalProperties", Object.class, t2);
		
		registerRule(Identifier.class, new RDFEntityRule<Identifier>());
		registerRule(Identifier.class, "identifierType", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(Identifier.class, "identifierValue", String.class, new RDFStringPropertyRule<DataEntity>());
		
		registerRule(DataEntity.class, new RDFEntityRule<DataEntity>());
		registerRule(DataEntity.class, "uri", java.net.URI.class, t2);
		registerRule(DataEntity.class, "typeUri", java.net.URI.class, t2);
		registerRule(DataEntity.class, "label", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "id", Identifier.class, new RDFObjectPropertyRule<DataEntity, Identifier>(false));
		registerRule(DataEntity.class, "type", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "hasPart", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, DataEntity>(false));
		registerRule(DataEntity.class, "related", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, DataEntity>(false));
		registerRule(DataEntity.class, "partOf", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, DataEntity>(false));
		registerRule(DataEntity.class, "description", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "title", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "created", Date.class, new RDFTimePropertyRule<DataEntity>());
		registerRule(DataEntity.class, "submitted", Date.class, new RDFTimePropertyRule<DataEntity>());
		registerRule(DataEntity.class, "owner", User.class, new RDFObjectPropertyRule<DataEntity, User>(false));
		registerRule(DataEntity.class, "contributor", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, User>(false));
		registerRule(DataEntity.class, "discussion", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, Post>(false));
		registerRule(DataEntity.class, "likedBy", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, User>(false));
		registerRule(DataEntity.class, "dislikedBy", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, User>(false));
		registerRule(DataEntity.class, "metadata", java.util.Collection.class, new IrodsMetadataPropertyRule());
		registerRule(DataEntity.class, "accessHistory", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, Access>(false));
		registerRule(DataEntity.class, "accessPermission", java.util.Collection.class, new RDFCollectionPropertyRule<DataEntity, AccessPermission>(true));
		registerRule(DataEntity.class, "discussion", Post.class, new RDFCollectionPropertyRule<DataEntity, Post>(true));
		registerRule(DataEntity.class, "storageLocation", java.util.Collection.class, new RDFAbstractCollectionPropertyRule<DataEntity, String>(new StringStringMapping(), false));
		registerRule(DataEntity.class, "storageLocationRequest", java.util.Collection.class, t2);
		registerRule(DataEntity.class, "tempInputStream", InputStream.class, t2);
		registerRule(DataEntity.class, "previewPath", String.class, new RDFStringPropertyRule<DataEntity>());
		registerRule(DataEntity.class, "previewThumbPath", String.class, new RDFStringPropertyRule<DataEntity>());

		registerRule(IndividualObject.class, new RDFEntityRule<IndividualDataObject>());
		registerRule(IndividualObject.class, "replaces", DataEntity.class, new RDFObjectPropertyRule<IndividualDataObject, DataEntity>(false));
		registerRule(IndividualObject.class, "replacedBy", DataEntity.class, new RDFObjectPropertyRule<IndividualDataObject, DataEntity>(false));
		registerRule(IndividualObject.class, "hasVersion", String.class, new RDFStringPropertyRule<IndividualDataObject>());
		registerRule(IndividualObject.class, "dataSize", Double.class, new RDFNumberPropertyRule<IndividualDataObject>());

		registerRule(User.class, new RDFEntityRule<User>());
		registerRule(User.class, "contributeTo", java.util.Collection.class, new RDFCollectionPropertyRule<User, DataEntity>(false));
		registerRule(User.class, "own", java.util.Collection.class, new RDFCollectionPropertyRule<User, DataEntity>(false));
		registerRule(User.class, "like", java.util.Collection.class, new RDFCollectionPropertyRule<User, DataEntity>(false));
		registerRule(User.class, "dislike", java.util.Collection.class, new RDFCollectionPropertyRule<User, DataEntity>(false));

		registerRule(Access.class, new RDFEntityRule<Access>());
		registerRule(Access.class, "session", java.util.Collection.class, new RDFCollectionPropertyRule<Access, Session>(false));
		registerRule(Access.class, "finished", Date.class, new RDFTimePropertyRule<Access>());
		registerRule(Access.class, "linkingDataEntity", java.util.Collection.class, new RDFCollectionPropertyRule<Access, DataEntityLink>(true));
		registerRule(Access.class, "linkingUser", java.util.Collection.class, new RDFCollectionPropertyRule<Access, UserLink>(true));

		registerRule(Session.class, new RDFEntityRule<Session>());
		registerRule(Session.class, "sessionPart", java.util.Collection.class, new RDFCollectionPropertyRule<Session, Access>(false));

		registerRule(AccessPermission.class, new RDFEntityRule<AccessPermission>());
		registerRule(AccessPermission.class, "permission", String.class, new RDFStringPropertyRule<AccessPermission>());
		registerRule(AccessPermission.class, "linkingDataEntity", java.util.Collection.class, new RDFCollectionPropertyRule<AccessPermission, DataEntityLink>(true));
		registerRule(AccessPermission.class, "linkingUser", java.util.Collection.class, new RDFCollectionPropertyRule<AccessPermission, UserLink>(true));

		registerRule(databook.persistence.rule.rdf.ruleset.Collection.class, new IrodsCollectionEntityRule());

		registerRule(AVU.class, new RDFEntityRule<AVU>()); // TODO change rule
		registerRule(AVU.class, "attribute", String.class, new RDFStringPropertyRule<AVU>());
		registerRule(AVU.class, "value", String.class, new RDFStringPropertyRule<AVU>());
		registerRule(AVU.class, "unit", String.class, new RDFStringPropertyRule<AVU>());

		registerRule(DataObject.class, new IrodsDataObjectEntityRule());
		registerRule(DataObject.class, "replica", java.util.Collection.class, new RDFCollectionPropertyRule<DataObject, Replica>(true));

                registerRule(DataEntityLink.class, new RDFEntityRule<DataEntityLink>());
                registerRule(DataEntityLink.class, "dataEntity", DataEntity.class, new RDFObjectPropertyRule<DataEntityLink, DataEntity>(false));
                registerRule(DataEntityLink.class, "dataEntityRole", String.class, new RDFStringPropertyRule<DataEntityLink>());

		registerRule(UserLink.class, new RDFEntityRule<UserLink>());
		registerRule(UserLink.class, "user", User.class, new RDFObjectPropertyRule<UserLink, User>(true));
		registerRule(UserLink.class, "userRole", String.class, new RDFStringPropertyRule<UserLink>());
}


}
