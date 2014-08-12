package databook.listener.vivo;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import databook.listener.Indexer;
import databook.listener.ModelUpdateListener;
import databook.listener.Scheduler;
import databook.local.model.RDFDatabase;
import databook.local.model.RDFDatabase.Format;
import databook.local.model.RDFDatabase.RDFDatabaseTransaction;
import databook.local.model.RDFDatabaseException;
import databook.persistence.rule.EntityRule;
import databook.persistence.rule.PersistenceContext;
import databook.persistence.rule.rdf.ruleset.DataEntity;
import databook.persistence.rule.rdf.ruleset.Message;
import databook.persistence.rule.rdf.ruleset.Messages;

public class VIVOIndexer implements Indexer {
	// private final ModelUpdateListener modelUpdateListener;
	
	static final Logger log = Logger.getLogger("VIVOIndexer");
	private RDFDatabase database;
	DatabookRuleSet rs = new DatabookRuleSet();
	PersistenceContext context = new PersistenceContext(database, null, rs, null, null);
	Scheduler scheduler;

	public VIVOIndexer(RDFDatabase database) {
		this.database = database;
		this.context = new PersistenceContext(database, null, rs, null, null);
	}
	
	public PersistenceContext getPersistenceContext() {
		return context;
	}


	/*
	 * message format: first line command:
	 * 
	 * add del move addMeta delMeta modMeta
	 */
	public void messages(Messages ms) throws RDFDatabaseException {
		RDFDatabaseTransaction trans = database.newTransaction();
		PersistenceContext context = this.getPersistenceContext();
		context.setRdfTrans(trans);
		
		trans.start();
		
		boolean commit = true;
		loop:
		for(Message m: ms.getMessages()){
			List<DataEntity> parts = m.getHasPart();
			DataEntity dataEntity = parts.get(0);
			
			String op = m.getOperation();
			EntityRule<DataEntity> r = rs.lookupRule(dataEntity);
			if(op.equals("create")) {
				r.create(dataEntity, context);
			} else if(op.equals("delete")) {
				r.delete(dataEntity, context);
			} else if(op.equals("modify")) {
				DataEntity dataEntity2 = parts.get(1);
				r.modify(dataEntity, dataEntity2, context);
			} else if(op.equals("union")) {
				DataEntity dataEntity2 = parts.get(1);
				r.union(dataEntity, dataEntity2, context);
			} else if(op.equals("diff")) {
				DataEntity dataEntity2 = parts.get(1);
				r.diff(dataEntity, dataEntity2, context);
			} else if(op.equals("describe")) {
				InputStream ris = database.describe(dataEntity.getUri().toString(), Format.N3);
				ModelUpdateListener.logFully(ris);
			} else {
				log.log(java.util.logging.Level.SEVERE, "Unsupported command '" + op + "'");
				trans.abort();
				commit = false;
				break loop;
			}
		}
		if(commit) {
			trans.commit();
		}
		
		for (Runnable pp : context.getPostProcList()) {
			pp.run();
		}

	}

	@Override
	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
		
	}

}
