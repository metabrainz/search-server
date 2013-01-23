package org.musicbrainz.search.update;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.jdom.JDOMException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.AnnotationIndex;
import org.musicbrainz.search.index.ArtistIndex;
import org.musicbrainz.search.index.CommonTables;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.ReplicationInformation;
import org.musicbrainz.search.index.LabelIndex;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.RecordingIndex;
import org.musicbrainz.search.index.ReleaseGroupIndex;
import org.musicbrainz.search.index.ReleaseIndex;
import org.musicbrainz.search.index.TagIndex;
import org.musicbrainz.search.index.ThreadedIndexWriter;
import org.musicbrainz.search.index.WorkIndex;
import org.musicbrainz.search.replication.ReplicationPacket;
import org.musicbrainz.search.replication.ReplicationPacketIterator;
import org.musicbrainz.search.update.dependencies.DatabaseIndexDependencies;

public class LiveDataFeedIndexUpdater {

	private final Logger LOGGER = Logger.getLogger(LiveDataFeedIndexUpdater.class.getName());
	
	private Connection mainDbConn;
	
	private LiveDataFeedIndexUpdaterOptions options;
	private List<DatabaseIndex> indexes;
	private Map<DatabaseIndex,IndexWriter> indexWriters =  new HashMap<DatabaseIndex,IndexWriter>();
	private Map<DatabaseIndex,ReplicationInformation> indexReplicationInfos = new HashMap<DatabaseIndex,ReplicationInformation>();
	private Map<DatabaseIndex,ChangesAnalyzer> indexChangesAnalyzers = new HashMap<DatabaseIndex,ChangesAnalyzer>();
	
	public LiveDataFeedIndexUpdater(LiveDataFeedIndexUpdaterOptions options) {
		
		this.options = options;
		LOGGER.setUseParentHandlers(false);
	    Handler conHdlr = new ConsoleHandler();
	    conHdlr.setFormatter(new Formatter() {
	      @Override
	      public String format(LogRecord record) {
	        return new Date(record.getMillis()).toString() + "\t" + record.getLevel() + "\t" + record.getMessage() + "\n";
	      }
	    });
	    LOGGER.addHandler(conHdlr);
	    Level logLevel = this.options.isVerbose() ? Level.FINE : Level.INFO;
	    LOGGER.setLevel(logLevel);
	    conHdlr.setLevel(logLevel);
	    
	}
	
	public void init() throws SQLException, IOException {
		
        // Connect to main database
        mainDbConn = this.options.getMainDatabaseConnection();
    
        // MusicBrainz data indexing
        indexes = new ArrayList<DatabaseIndex>() {{
            add(new ArtistIndex(mainDbConn));
            add(new ReleaseIndex(mainDbConn));
            add(new ReleaseGroupIndex(mainDbConn));
            add(new RecordingIndex(mainDbConn));
            add(new LabelIndex(mainDbConn));
            add(new WorkIndex(mainDbConn));
            add(new AnnotationIndex(mainDbConn));
            add(new TagIndex(mainDbConn));
        }};

        // Remove from the indexes list indexes that should not be handled in this run
        Iterator<DatabaseIndex> it = indexes.iterator();
        while (it.hasNext()) {
        	DatabaseIndex index = it.next();
            if (!options.buildIndex(index.getName())) {
            	it.remove();
            }
		}
        
        // Step 1: Initialize index writers and load replication info of each index 
        for (DatabaseIndex index : indexes) {

            // Initialize index writer
            IndexWriter indexWriter = createWriterForIndex(index, options);
            indexWriters.put(index, indexWriter);
            
            // Load replication information
            IndexReader indexReader = IndexReader.open(indexWriter, true);
            ReplicationInformation replicationInfo = index.readReplicationInformationFromIndex(indexReader);
            indexReplicationInfos.put(index, replicationInfo);
            
            // Initialize the changes analyzer
            DatabaseIndexDependencies dependencies = new DatabaseIndexDependencies(index.getName());
            try {
    			dependencies.loadFromConfigFile( getClass().getResourceAsStream("/dependencies.xml") );
    		} catch (JDOMException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            ChangesAnalyzer changesAnalyzer = new ChangesAnalyzer(index, dependencies);
            indexChangesAnalyzers.put(index, changesAnalyzer);
        }
	}
	
	public void destroy() throws CorruptIndexException, IOException, SQLException {
        for(DatabaseIndex index : indexes) {
        	indexWriters.get(index).close();
        }
        mainDbConn.close();
	}
	
	public void update() throws SQLException, IOException {
		
        StopWatch clock = new StopWatch();
        
        if (indexes.size() == 0) {
        	LOGGER.info("No selected indexes to update, aborting");
        	return;
        }
               
        // Step 1: Load all replication packets and analyze changes to determine what need to be reindexed
        List<ReplicationInformation> list = new ArrayList<ReplicationInformation>(indexReplicationInfos.values());
        Collections.sort(list);
        ReplicationInformation earliestReplicationInformation = list.get(0);
        
        Integer lastChangeSequence = earliestReplicationInformation.changeSequence;
        ReplicationPacketIterator itPacket = new ReplicationPacketIterator(earliestReplicationInformation);
        while (itPacket.hasNext()) {
        	
        	ReplicationPacket packet = itPacket.next();           	
        	LOGGER.info("Loading packet #" + packet.getReplicationSequence());
        	
        	for (DatabaseIndex index : indexes) {

        		// SchemaSequence of packet should match the packet's one 
            	if (indexReplicationInfos.get(index).schemaSequence != packet.getSchemaSequence()) {
            		LOGGER.fine("Ignoring packet #" + packet.getReplicationSequence() + " for index " + index.getName() + ": SCHEMA sequence mismatches");
            	// Check if this packet hasn't already been processed for this index (by comparing replication information)
            	} else if (indexReplicationInfos.get(index).compareTo(itPacket.getCurrentReplicationPosition()) > 0) {
            		LOGGER.fine("Ignoring packet #" + packet.getReplicationSequence() + " for index " + index.getName() + ": already indexed");
        		// Otherwise process the packet to find changes
            	} else {
            		LOGGER.fine("Analyzing packet #" + packet.getReplicationSequence() + " for index " + index.getName());
            		try {
            			indexChangesAnalyzers.get(index).analyze(packet, lastChangeSequence);
            		} catch (InvalidReplicationChangeException e) {
            			LOGGER.severe("Aborting: " + e.getMessage());
            			return;
            		}
            	}
        	}
        	
        	lastChangeSequence = packet.getMaxChangeId();
        }

        // Step 2: Create temporary tables, used by multiple indexes, if changes has been found
        boolean changesExist = false;
        for(ChangesAnalyzer changesAnalyzer : indexChangesAnalyzers.values()) {
        	if (changesAnalyzer.hasChanges()) {
        		changesExist = true;
        		break;
        	}
        }
        if (changesExist) {
            List<String> indexesToBeBuilt = new ArrayList<String>();
            for(DatabaseIndex index : indexes) {
            	indexesToBeBuilt.add(index.getName());
            }
	        CommonTables commonTables = new CommonTables(mainDbConn, indexesToBeBuilt);
	        commonTables.createTemporaryTables(true);
        } 
        
        // Step 3: Update the existing indexes from found changes
        for (DatabaseIndex index : indexes) {

        	clock.start();
            LOGGER.info("Started updating index: " + index.getName());
            
            IndexWriter indexWriter = indexWriters.get(index);
            ReplicationInformation replicationInfo = indexReplicationInfos.get(index);
        	ChangesAnalyzer changesAnalyzer = indexChangesAnalyzers.get(index);
        	
            try {
				updateDatabaseIndex(index, indexWriter, replicationInfo, changesAnalyzer, itPacket.getCurrentReplicationPosition());
			} catch (DatabaseSchemaChangedException e) {
				LOGGER.severe("DatabaseSchemaChangedException: " +  e.getMessage());
			}
            
            changesAnalyzer.reset();
            
            clock.stop();
            LOGGER.fine("Finished updating index: " + index.getName() + " in " + Float.toString(clock.getTime()/1000) + " seconds");
            clock.reset();
        }

	}
	
	private IndexWriter createWriterForIndex(DatabaseIndex index, LiveDataFeedIndexUpdaterOptions options) throws IOException {
	       IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, index.getAnalyzer());

	       String path = options.getIndexesDir() + index.getFilename();
	       IndexWriter indexWriter = new ThreadedIndexWriter(FSDirectory.open(new File(path)),
	                writerConfig,
	                Runtime.getRuntime().availableProcessors(),
	                10);
	       
	       return indexWriter;
	}
	
	/**
     * Update an index built from database using Data Replication packets
     * 
     * @param options
     * @throws IOException 
     * @throws SQLException 
	 * @throws DatabaseSchemaChangedException 
     */
    private void updateDatabaseIndex(DatabaseIndex index, IndexWriter indexWriter, ReplicationInformation currentReplicationInfo, ChangesAnalyzer changesAnalyzer, ReplicationInformation newReplicationInfo) throws IOException, SQLException, DatabaseSchemaChangedException
    {

        LOGGER.info("Current index properties: schema_sequence=" + currentReplicationInfo.schemaSequence +
        		", replication_sequence=" + currentReplicationInfo.replicationSequence + 
        		", change_sequence=" + (currentReplicationInfo.changeSequence != null ? currentReplicationInfo.changeSequence : "") );
        
        // We're done parsing all replication packets and analyzing impacted entities
		Term term = new Term(MetaIndexField.META.getName(), MetaIndexField.META_VALUE);
		TermQuery query = new TermQuery(term);
        
        // Delete obsolete documents
    	for(Integer id : changesAnalyzer.getDeletedIds()) {
    		LOGGER.fine("Deleting " + index.getName() + " #" + id.toString());
    		term = new Term(index.getIdentifierField().getName(), id.toString());
    		query = new TermQuery(term);
    		indexWriter.deleteDocuments(query);
    	}
    	
    	// Index new (or udpated) ones
    	if (!changesAnalyzer.getInsertedOrUpdatedIds().isEmpty()) {
    		index.init(indexWriter, true);	
    	
        	for(Integer id : changesAnalyzer.getInsertedOrUpdatedIds()) {
        		LOGGER.fine("Reindexing " + index.getName() + " #" + id.toString());
        		term = new Term(index.getIdentifierField().getName(), id.toString());
        		query = new TermQuery(term);
        		indexWriter.deleteDocuments(query);
        		index.indexData(indexWriter, id, id);
        	}
        	index.destroy();
    	}
    	
    	// Only update the index if we've processed some database changes
    	if (currentReplicationInfo.compareTo(newReplicationInfo) != 0) {
    		
    		index.updateMetaInformation(indexWriter, newReplicationInfo);
    		indexWriter.commit();
    		// TODO: index don't need to be optimized on each update, it's way too resource intensive
    		// => disabled for now, need to be done on a regular basis that should determined
    		// indexWriter.optimize();
        
            // Check to we have as much Lucene documents as Database rows
            int dbRows = index.getNoOfRows(Integer.MAX_VALUE);
            IndexReader indexReader = IndexReader.open(indexWriter, true);
            LOGGER.info(dbRows + " rows in database, " + (indexReader.maxDoc()-1) + " lucene documents");
    	} else {
    		LOGGER.info("No changes found");
    	}
            
        indexWriter.close();
    	
    }
    
	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, SQLException {
		
		LiveDataFeedIndexUpdaterOptions options = LiveDataFeedIndexUpdaterOptions.getInstance();
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
        updater.init();
        updater.update();
        updater.destroy();
	}
    
}
