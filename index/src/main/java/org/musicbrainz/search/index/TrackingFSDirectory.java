package org.musicbrainz.search.index;

import org.apache.lucene.store.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TrackingFSDirectory extends SimpleFSDirectory {

    private Set<String> openOutputs = new HashSet<String>();
    private Set<String> openInputs  = new HashSet<String>();

    public TrackingFSDirectory(File path) throws IOException
    {
        super(path);
    }

    synchronized public int getFileDescriptorCount()
    {
        return openOutputs.size() + openInputs.size();
    }

    synchronized private void report(String msg)
    {
        System.out.println("openfilecount:"+msg+":fs count:"+getFileDescriptorCount());
    }

    synchronized public IndexInput openInput(String name) throws IOException
    {
        return openInput(name, BufferedIndexInput.BUFFER_SIZE);
    }

    synchronized public IndexInput openInput(String name, int bufferSize) throws IOException
    {
        openInputs.add(name);
        report("Open Input:"+name);
        return new TrackingFSIndexInput(name, bufferSize);
    }


    synchronized public IndexOutput createOutput(String name) throws IOException
    {
        openOutputs.add(name);
        report("Open Output:"+name);
        File file = new File(getFile(),name);
        if(file.exists() && !file.delete())
        {
            throw new IOException("Cannot Overwrite:"+file);
        }
        return new TrackingFSIndexOutput(name);
    }

    protected class TrackingFSIndexInput extends SimpleFSIndexInput
    {
        String name;

        public TrackingFSIndexInput( String name, int bufferSize) throws IOException
        {
            super(new File(getFile(),name),bufferSize,getReadChunkSize());
            this.name=name;
        }

        boolean cloned = false;

        public Object clone()
        {
            TrackingFSIndexInput clone = (TrackingFSIndexInput)super.clone();
            clone.cloned = true;
            return clone;
        }

        public void close() throws IOException
        {
            super.close();
            if(!cloned)
            {
                synchronized(TrackingFSDirectory.this)
                {
                    openInputs.remove(name);
                }
            }
            report("Close Input:" + name);
        }


    }

    protected class TrackingFSIndexOutput extends SimpleFSIndexOutput
    {

        String name;

        public TrackingFSIndexOutput( String name) throws IOException
        {
            super(new File(getFile(),name));
            this.name=name;
        }

        public void close() throws IOException
        {
            super.close();
            synchronized(TrackingFSDirectory.this)
            {
                openOutputs.remove(name);
            }
            report("Close Output:"+name);
        }

    }
}
