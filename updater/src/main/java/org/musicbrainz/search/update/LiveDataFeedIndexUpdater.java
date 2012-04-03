package org.musicbrainz.search.update;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.lang.time.StopWatch;
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

//	private Map<String, DatabaseIndexDependencies> dependencyTrees;
	private final Logger LOGGER = Logger.getLogger(LiveDataFeedIndexUpdater.class.getName());
	
	private Connection mainDbConn;
	
	public LiveDataFeedIndexUpdater(LiveDataFeedIndexUpdaterOptions options) {
		
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
	    
//	    dependencyTrees = initDependencyTrees();
	}
	
	public void update() throws SQLException, IOException {
		
		LiveDataFeedIndexUpdaterOptions options = LiveDataFeedIndexUpdaterOptions.getInstance();
		
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

        List<String> indexesToBeBuilt = new ArrayList<String>();
        for (DatabaseIndex index : indices) {
            // Check if this index should be built
            if (options.buildIndex(index.getName())) {
                indexesToBeBuilt.add(index.getName());
            }
        }
        // Create temporary tables used by multiple indexes
        CommonTables commonTables = new CommonTables(mainDbConn, indexesToBeBuilt);
        commonTables.createTemporaryTables(true);

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
            
            try {
				updateDatabaseIndex(index, options);
			} catch (DatabaseSchemaChangedException e) {
				LOGGER.severe("DatabaseSchemaChangedException: " +  e.getMessage());
			}
            
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
	 * @throws DatabaseSchemaChangedException 
     */
    private void updateDatabaseIndex(DatabaseIndex index, LiveDataFeedIndexUpdaterOptions options) throws IOException, SQLException, DatabaseSchemaChangedException
    {

        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,index.getAnalyzer());

    	String path = options.getIndexesDir() + index.getFilename();
        IndexWriter indexWriter = new ThreadedIndexWriter(FSDirectory.open(new File(path)),
                writerConfig,
                Runtime.getRuntime().availableProcessors(),
                10);

        IndexReader reader = indexWriter.getReader();     
        ReplicationInformation replicationInfo = index.readReplicationInformationFromIndex(reader);
        DatabaseIndexDependencies dependencies = new DatabaseIndexDependencies(index.getName());
        try {
			dependencies.loadFromConfigFile( getClass().getResourceAsStream("/dependencies.xml") );
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Integer lastChangeSequence = replicationInfo.changeSequence;

        LOGGER.info("Current index properties: schema_sequence=" + replicationInfo.schemaSequence +
        		", replication_sequence=" + replicationInfo.replicationSequence + 
        		", change_sequence=" + (replicationInfo.changeSequence != null ? replicationInfo.changeSequence : "") );
        
        // Load and process each replication packet released since
        ChangesAnalyzer changesAnalyzer = new ChangesAnalyzer(index, dependencies);
        
        boolean packetsFound = false;
        ReplicationPacketIterator itPacket = new ReplicationPacketIterator(replicationInfo);
        while (itPacket.hasNext()) {
        	
        	packetsFound = true;
        	ReplicationPacket packet = itPacket.next();           	
        	LOGGER.info("Loading packet #" + packet.getReplicationSequence());
        	
        	if (replicationInfo.schemaSequence != packet.getSchemaSequence()) {
        		LOGGER.info("Aborting, new packet is for a different SCHEMA sequence");
        		throw new DatabaseSchemaChangedException(replicationInfo.schemaSequence, packet.getSchemaSequence());
        	}
        	
        	changesAnalyzer.analyze(packet, lastChangeSequence);
        	
        	lastChangeSequence = packet.getMaxChangeId();
        }

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
    	
    	// Only update the index if we've been able to load at least one packet
    	if (packetsFound) {
    		index.updateMetaInformation(indexWriter, itPacket.getCurrentReplicationPosition());
    		indexWriter.commit();
    		// TODO: index don't need to be optimized on each update, it's way too resource intensive
    		// => disabled for now, need to be done on a regular basis that should determined
    		// indexWriter.optimize();
        
            // Check to we have as much Lucene documents as Database rows
            int dbRows = index.getNoOfRows(Integer.MAX_VALUE);
            reader = reader.reopen();
            LOGGER.info(dbRows + " rows in database, " + (reader.maxDoc()-1) + " lucene documents");
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
        updater.update();
	}
    
}
