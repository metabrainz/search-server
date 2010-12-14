package org.musicbrainz.search.replication;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.AnnotationIndex;
import org.musicbrainz.search.index.ArtistIndex;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.IndexOptions;
import org.musicbrainz.search.index.LabelIndex;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.RecordingIndex;
import org.musicbrainz.search.index.ReleaseGroupIndex;
import org.musicbrainz.search.index.ReleaseIndex;
import org.musicbrainz.search.index.TagIndex;
import org.musicbrainz.search.index.ThreadedIndexWriter;
import org.musicbrainz.search.index.WorkIndex;
import org.musicbrainz.search.replication.packet.ReplicationChange;
import org.musicbrainz.search.replication.packet.ReplicationPacket;

public class LiveDataFeedIndexUpdater {

	private Map<String, EntityDependencyTree> dependencyTrees;
	private final Logger LOGGER = Logger.getLogger(LiveDataFeedIndexUpdater.class.getName());
	
	private Connection mainDbConn;
	private LiveDataFeedIndexOptions options;
	private HashMap<Integer,ReplicationPacket> remotePacketCache = new HashMap<Integer,ReplicationPacket>();
	
	
	public LiveDataFeedIndexUpdater(LiveDataFeedIndexOptions options) {
		
		LOGGER.setUseParentHandlers(false);
	    Handler conHdlr = new ConsoleHandler();
	    conHdlr.setFormatter(new Formatter() {
	      @Override
	      public String format(LogRecord record) {
	        return new Date(record.getMillis()).toString() + "\t" + record.getLevel() + "\t" + record.getMessage() + "\n";
	      }
	    });
	    LOGGER.addHandler(conHdlr);
	    Level logLevel = options.isVerbose() ? Level.FINE : Level.INFO;
	    LOGGER.setLevel(logLevel);
	    conHdlr.setLevel(logLevel);
	    
	    this.options = options;
	    dependencyTrees = initDependencyTrees();
	}
	
	public void start() throws SQLException, IOException {
		
        // Connect to main database
        mainDbConn = options.getMainDatabaseConnection();
    
        StopWatch clock = new StopWatch();

        // MusicBrainz data indexing
        DatabaseIndex[] indices = {
                new ArtistIndex(mainDbConn),
                new ReleaseIndex(mainDbConn),
                new ReleaseGroupIndex(mainDbConn),
                new RecordingIndex(mainDbConn),
                new LabelIndex(mainDbConn),
                new WorkIndex(mainDbConn),
                new AnnotationIndex(mainDbConn),
                new TagIndex(mainDbConn),
        };

        for (DatabaseIndex index : indices) {

            // Check if this index should be built
            if (!options.buildIndex(index.getName())) {
            	LOGGER.info("");
            	LOGGER.info("Skipping index: " + index.getName());
                continue;
            }

            clock.start();
            LOGGER.info("");
            LOGGER.info("Started updating index: " + index.getName());
            
            updateDatabaseIndex(index, options);
            
            clock.stop();
            LOGGER.info("Finished updating index: " + index.getName() + " in " + Float.toString(clock.getTime()/1000) + " seconds");
            clock.reset();
        }

	}
	
	/**
     * Update an index built from database using Data Replication packets
     * 
     * @param options
     * @throws IOException 
     * @throws SQLException 
     */
    private void updateDatabaseIndex(DatabaseIndex index, LiveDataFeedIndexOptions options) throws IOException, SQLException
    {
    	
    	String path = options.getIndexesDir() + index.getFilename();
        IndexWriter indexWriter = new ThreadedIndexWriter(FSDirectory.open(new File(path)),
                index.getAnalyzer(),
                false,
                Runtime.getRuntime().availableProcessors(),
                10,
                IndexWriter.MaxFieldLength.LIMITED);
        indexWriter.setMaxBufferedDocs(IndexOptions.MAX_BUFFERED_DOCS);
        indexWriter.setMergeFactor(IndexOptions.MERGE_FACTOR);
    	
        IndexReader reader = indexWriter.getReader();
    	IndexSearcher searcher = new IndexSearcher(reader);
    	
		Term term = new Term(MetaIndexField.META.getName(), MetaIndexField.META_VALUE);
		TermQuery query = new TermQuery(term);
    	TopDocs hits = searcher.search(query, 10);

    	if (hits.scoreDocs.length == 0) {
            throw new IllegalArgumentException("No matches in the index for the given Term.");
    	} else if (hits.scoreDocs.length > 1) {
            throw new IllegalArgumentException("Given Term matches more than 1 document in the index.");
    	} else {
            int docId = hits.scoreDocs[0].doc;

            // retrieve the old document
            MbDocument doc = new MbDocument(searcher.doc(docId));
            int lastReplicationSequence = Integer.parseInt(doc.get(MetaIndexField.REPLICATION_SEQUENCE));
            int lastSchemaSequence = Integer.parseInt(doc.get(MetaIndexField.SCHEMA_SEQUENCE));
            String tmpStr = doc.get(MetaIndexField.LAST_CHANGE_SEQUENCE);
            Integer lastChangeSequence = (tmpStr != null && !tmpStr.isEmpty()) ? Integer.parseInt(tmpStr) : null;

            LOGGER.info("Current index properties: schema_sequence=" + lastSchemaSequence +
            		", replication_sequence=" + lastReplicationSequence + 
            		", change_sequence=" + (lastChangeSequence != null ? lastChangeSequence : "") );
            
            Set<Integer> deletedIds = new HashSet<Integer>();
            Set<Integer> insertedOrUpdatedIds = new HashSet<Integer>();
            
            // For debug purpose: force replication and change sequences
            //lastReplicationSequence = 500;
            //lastChangeSequence = 1;
            
            // Load and process each replication packet released since
            ReplicationPacket lastPacket = null;
            ReplicationPacket packet;
            while ( (packet = getCachedReplicationPacket(lastReplicationSequence+1)) != null ) {
            	           	
            	LOGGER.info("Loading packet #" + packet.getReplicationSequence());
            	
            	if (lastSchemaSequence != packet.getSchemaSequence()) {
            		LOGGER.info("Aborting, new packet is for a different SCHEMA sequence");
            		break;
            	}
            	
            	lastPacket = packet;
            	processReplicationPacket(packet, lastChangeSequence, dependencyTrees.get(index.getName()), insertedOrUpdatedIds, deletedIds);
            	
            	lastReplicationSequence++;
            }

            // Now try to get last changes straight from the database (should only work on master database)
            if (lastPacket != null && lastChangeSequence != null) {
            	lastChangeSequence = Math.max(lastChangeSequence, lastPacket.getMaxChangeId());
            } else if (lastPacket != null) {
            	lastChangeSequence = lastPacket.getMaxChangeId();
            }
            
            if (lastChangeSequence != null) {
            	packet = ReplicationPacket.loadFromDatabase(mainDbConn, lastChangeSequence);
            	if (packet != null) {
            		LOGGER.info("Loading pending changes from database since change #" + lastChangeSequence);
        			processReplicationPacket(packet, lastChangeSequence, dependencyTrees.get(index.getName()), insertedOrUpdatedIds, deletedIds);
        			lastPacket = packet;
            	}
            	
            }
            
            // We're done parsing all replication packets and analyzing impacted entities
            Date indexingDate = new Date();
            
            // Delete obsolete documents
        	for(Integer id : deletedIds) {
        		LOGGER.fine("Deleting " + index.getName() + " #" + id.toString());
        		term = new Term(index.getIdentifierField().getName(), id.toString());
        		query = new TermQuery(term);
        		indexWriter.deleteDocuments(query);
        	}
        	
        	// Index new (or udpated) ones
        	if (!insertedOrUpdatedIds.isEmpty()) {
        		index.init(indexWriter, true);	
        	
	        	for(Integer id : insertedOrUpdatedIds) {
	        		LOGGER.fine("Reindexing " + index.getName() + " #" + id.toString());
	        		term = new Term(index.getIdentifierField().getName(), id.toString());
	        		query = new TermQuery(term);
	        		indexWriter.deleteDocuments(query);
	        		index.indexData(indexWriter, id, id);
	        	}
	        	index.destroy();
        	}
        	
        	// Only update the index if we've been able to load at least one packet
        	if (lastPacket != null) {
        		index.updateMetaInformation(indexWriter, 
        				lastPacket.getSchemaSequence(), 
        				lastPacket.getReplicationSequence(),
        				lastPacket.getMaxChangeId(),
        				indexingDate);
        		indexWriter.commit();
        		// TODO: index don't need to optimized on each update, it's way too resource intensive
        		// => disabled for now, need to be done on a regular basis that should determined
        		// indexWriter.optimize();
            
	            // Check to we have as much Lucune documents as Database rows
	            int dbRows = index.getNoOfRows(Integer.MAX_VALUE);
	            reader = reader.reopen();
	            LOGGER.info(dbRows + " rows in database, " + (reader.maxDoc()-1) + " lucene documents");
        	} else {
        		LOGGER.info("No changes found");
        	}
	            
            indexWriter.close();
    	}
    	
    }
    
    private ReplicationPacket getCachedReplicationPacket(Integer packetNo) throws IOException {
    	
    	if (remotePacketCache.containsKey(packetNo)) {
    		return remotePacketCache.get(packetNo);
    	} else {
    		ReplicationPacket packet = ReplicationPacket.loadFromRepository(packetNo, options.getRepositoryPath());
    		remotePacketCache.put(packetNo, packet);
    		return packet;
    	}
    	
    }
    
	private void processReplicationPacket(ReplicationPacket packet, Integer lastChangeSequence, EntityDependencyTree dependencyTree, Set<Integer> insertedOrUpdatedIds, Set<Integer> deletedIds) 
    throws SQLException 
    {
    	
    	Map<String, Set<Integer>> changedTables = new HashMap<String, Set<Integer>>();
    	
    	// Initialize deleted and inserted maps of 
    	for (String tableName : dependencyTree.getTables()) {
    		if (tableName.equals(dependencyTree.getRootTableName())) {
    			continue;
    		}
    		changedTables.put(tableName, new HashSet<Integer>());
    	}
    	
    	// Process changes in replication packet
    	for (ReplicationChange change : packet.getChanges()) {

    		if (lastChangeSequence != null && change.getId() <= lastChangeSequence ) {
    			LOGGER.finer("Skipping change #" + change.getId() + " because it has already been applied");
    			continue;
    		}
    		
    		if (!dependencyTree.getTables().contains(change.getTableName())) {
    			LOGGER.finest("Skipping change #" + change.getId() + " on unrelated table " + change.getTableName().toUpperCase());
    			continue;
    		}
    		
    		LinkedTable lt = dependencyTree.getDependency(change.getTableName());
    		
    		LOGGER.finer("Analyzing change #" + change.getId() + " on table " + change.getTableName().toUpperCase());
    		switch (change.getOperation()) {
    			case INSERT:
        			{
        				Map<String,String> values = change.getNewValues();
        				if (lt.isHead()) {
        					insertedOrUpdatedIds.add( Integer.parseInt(values.get("id")) );
        				} else {
        					changedTables.get(change.getTableName()).add( 
        							Integer.parseInt(values.get(lt.getSourceJoinField()) )); 
        				}
        			}
        			break;
    			case UPDATE:
        			{
        				Map<String,String> oldValues = change.getOldValues();
        				Map<String,String> newValues = change.getNewValues();
        				if (lt.isHead()) {
        					// TODO: fix hardcoding of "id"?
        					insertedOrUpdatedIds.add( Integer.parseInt(newValues.get("id")) );
        				} else {
        					
        					// By default, skip this change unless no fields used for indexing 
        					// have been declared
        					boolean skipChange = lt.getFieldsUsedForIndexing().isEmpty() ? false : true;
        					
        					// Now check that at least one of the used fields has been changed
        					for (String usedField : lt.getFieldsUsedForIndexing()) {
        						if (change.getChangedFields().contains(usedField)) {
        							skipChange = false;
        							break;
        						}
        					}
        					
        					if (skipChange) {
        						LOGGER.finer("Skipping change #" + change.getId() + " on table " + change.getTableName().toUpperCase()
        								+ " because none of the fields used for indexing has been changed");
        					} else {
	        					changedTables.get(change.getTableName()).add( 
	        							Integer.parseInt(newValues.get(lt.getSourceJoinField()) ));
	        					changedTables.get(change.getTableName()).add( 
	        							Integer.parseInt(oldValues.get(lt.getSourceJoinField()) ));
        					}
        				}
        			}
    				break;
    			case DELETE:
        			{
        				Map<String,String> values = change.getOldValues();
        				if (lt.isHead()) {
        					deletedIds.add( Integer.parseInt(values.get("id")) );
        				} else {
        					changedTables.get(change.getTableName()).add( 
        							Integer.parseInt(values.get(lt.getSourceJoinField()) )); 
        				}
        			}
    				break;
    		}
    		
    	}
    	
    	// Now determine the ids of our entity
    	for (String tableName : changedTables.keySet()) {
    		
    		Set<Integer> tmpIds = changedTables.get(tableName);
    		if (tmpIds.isEmpty()) {
    			continue;
    		}
    		LinkedTable lt = dependencyTree.getDependency(tableName);
    		String sql = lt.generateSQL(tmpIds);
    		
    		LOGGER.finer("Resolution of affected ids for table " + tableName + ": " + sql);
    		
    		Statement st = mainDbConn.createStatement();
    		ResultSet rs = st.executeQuery(sql);
    		
    		while (rs.next()) {
    			insertedOrUpdatedIds.add(rs.getInt(1));
    		}
    		
    	}
    	
    }
    
    /**
     * Load table dependencies declaration from an XML configuration file
     * @return
     */
    
    private Map<String, EntityDependencyTree> initDependencyTrees() {
    	
    	HashMap<String, EntityDependencyTree> map = new HashMap<String, EntityDependencyTree>();
    	
    	org.jdom.Document document;
    	
    	SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build( getClass().getResourceAsStream("/dependencies.xml") );
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return map;
		}

		// Define the filter that allows to get the beginning of the path
		// (i.e. the deepest <table> element)
		Filter filter = new Filter() {
			public boolean matches(Object ob) {
				if (!(ob instanceof Element)) { return false; }
				Element element = (Element) ob;
				if ("table".equals(element.getName()) && element.getChildren().isEmpty()) {
					return true;
				} 
				return false;
			}
		};

		// Iterate over each group
		Element root = document.getRootElement();
		List groups = root.getChildren("index");
		
		for (Iterator it = groups.iterator(); it.hasNext();) {
			
			Element group = (Element) it.next();
			EntityDependencyTree dependencyTree = new EntityDependencyTree();
			String indexName = group.getAttributeValue("name");		
			map.put(indexName, dependencyTree);
					
			// Iterate over each path for the group 
			for (Iterator it2 = group.getDescendants(filter); it2.hasNext();) {

				Element currElement = (Element) it2.next();
				
				LinkedTable firstLinkedtable = null;
				LinkedTable lastLinkedTable = null;
				Element lastJoin = null;
				
				while ( "table".equals(currElement.getName()) ) {
					
					LinkedTable newlt = new LinkedTable(currElement.getAttributeValue("name"));
					Attribute usedFields = currElement.getAttribute("used_fields"); 
					if (usedFields != null && !usedFields.getValue().isEmpty()) {
						newlt.setFieldsUsedForIndexing(usedFields.getValue().replace(" ", "").split(","));	
					}
					
					if (lastLinkedTable == null) {
						firstLinkedtable = newlt;
					} else {
						
						// targetJoinKey -> src_field and sourceJoinKey -> target_field
						// because we're reading it the other way
						String targetJoinKey = lastJoin.getAttributeValue("src_field");
						String sourceJoinKey = lastJoin.getAttributeValue("target_field");
						
						lastLinkedTable.setTargetTable(newlt, targetJoinKey, sourceJoinKey);
					}
					lastLinkedTable = newlt;
					lastJoin = currElement.getParentElement();
					currElement = lastJoin.getParentElement();
				}
				
				// Finally declare the whole path
				dependencyTree.addDependency(firstLinkedtable);
			}
			
			// Output of our dependencies for debug purpose
			LOGGER.fine("Building tree dependency for " + indexName + "...");
			for (LinkedTable lt : dependencyTree.dependencies.values()) {
				if (lt.isHead()) {
					LOGGER.finest(lt.getTableName());
					continue;
				}
				StringBuilder sb = new StringBuilder();
				sb.append(indexName + " ==> ");
				while (lt.getTargetTable() != null) {
					sb.append(lt.getTableName() + " (" + lt.getSourceJoinField() + ")");
					sb.append(" -> " + lt.getTargetTable().getTableName() + " (" + lt.getTargetJoinField() + ")");
					lt = lt.getTargetTable();
					if (lt.getTargetTable() != null) sb.append(" -> ");
				}
				LOGGER.finest(sb.toString());
			}
			
		}
    	
    	return map;
    }

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, SQLException {
		
		LiveDataFeedIndexOptions options = new LiveDataFeedIndexOptions();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("Couldn't parse command line parameters");
            parser.printUsage(System.out);
            System.exit(1);
        }

        // On request, print command line usage
        if (options.isHelp()) {
            parser.printUsage(System.out);
            System.exit(1);
        }
        
        // At least one index should have been selected 
        ArrayList<String> selectedIndexes = options.selectedIndexes();
        if (selectedIndexes.size() == 0 
              || (selectedIndexes.size() == 1 && selectedIndexes.contains(""))) { 
            System.out.println("No indexes selected. Exiting.");
            System.exit(1);
        }
        
        LiveDataFeedIndexUpdater updater = new LiveDataFeedIndexUpdater(options);
        updater.start();
	}
    
}
