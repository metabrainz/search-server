**:warning: Note: This repository has been replaced with [MB Solr](https://github.com/metabrainz/mb-solr) since June 2018, see the [blog post](https://blog.metabrainz.org/2018/06/15/musicbrainz-search-overhaul/).**

----

Search Server Build and Deploy Instructions
===========================================

This Search Server provides the indexed search functions for MusicBrainz.

Requirements
------------

This server can be run on linux/osx/windows, but these instructions assume 
that you will be running linux and will be using the tomcat7 server to host
the servlet.

It also assumes you have created a user called search with a home folder of `/home/search`.

To run this search server you will also need to have a MusicBrainz database
with the core data set loaded. Please see 

    https://github.com/metabrainz/musicbrainz-server/blob/master/INSTALL

for details on how to setup at least a "database only install". This 
document assumes that you have completed the setup of this server.

You will also need these tools:

- Java complete with JDK 1.6, not just the JRE
- Apache Tomcat 7
- Maven 2

In a recent Ubuntu, you can install these with this command:

    apt-get install openjdk-6-jdk maven2 tomcat7

Alternatively if you're not on Ubuntu, install the required tools from the following links

- Java complete with JDK 1.6, not just the JRE ( http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html )
- Apache Tomcat 6    ( http://tomcat.apache.org )
- Maven version 2    ( http://maven.apache.org/download.html )

Setup Java Home directory in your profile (not needed on Ubuntu):

    export JAVA_HOME="/usr/bin/java"


Check out and build server code
-------------------------------

Check out the **mmd-schema** and **search-server** repositories using Git as follows:

    git clone https://github.com/metabrainz/mmd-schema.git
    git clone https://github.com/metabrainz/search-server.git

Build and install the model:

    cd mmd-schema/brainz-mmd2-jaxb
    mvn install

By default the search server home will be at: `/home/search` and the indexes will live in `/home/search/indexdata`.
But the index location can be overridden by editing

    ... /webapps/ROOT/WEB-INF/web.xml

so that the index dir is correct:

    <param-value>/home/search/indexdata</param-value>

Then build the indexer and the search application as follows:

    cd search-server
    mvn package

This step will download any required java components and then compile and test the whole server.


Deploy the Search Server Code
----------------------------------

Unjar the search server webapp into the `ROOT` directory under `webapps` for your tomcat installation. If you installed
Tomcat using `apt` this will be

    /var/lib/tomcat7/webapps

If you installed tomcat manually the webapps directory is directly within the folder tomcat has been installed to.

Automatic deployment of search apps didn't work so well in practice, so it is recommended that you
stop tomcat before upgrading the search application.

On Ubuntu this will work:

    sudo /etc/init.d/tomcat7 stop
    cd /var/lib/tomcat6/webapps
    rm -rf ROOT/*
    cd ROOT
    jar -xf <your lucene java src dir>/servlet/target/searchserver.war
    sudo /etc/init.d/tomcat7 start


Build Indexes
-------------

Copy the Index building code

    cp /home/search/searchserver/index/target/index-2.0-SNAPSHOT-jar-with-dependencies.jar /home/search

Download the latest freedb dump (in tar.bz2 format) from:

    http://www.freedb.org/en/download__database.10.html

The file you download should be a complete dump and should look like this:

    freedb-complete-20090901.tar.bz2

Place the downloaded file into `/home/search`.

Now build indexes with these commands:

    cd /home/search
    java -Xmx512M -jar index-2.0-SNAPSHOT-jar-with-dependencies.jar --indexes-dir /home/search/indexdata --freedb-dump /home/search/<freedb tar.bz2 file>

This will build all of the indexes using upto 512mb of memory using all of the defaults for connecting
to the database servers. If your database isn't on the same server and named according to the
defaults you will need to give the command line more options. To see the options execute this:

    java -Xmx512M -jar index-2.0-SNAPSHOT-jar-with-dependencies.jar --help

Usually you will want to build the freedb index at a different time to the other indexes so you can also
build it separately.
If you want to build just the freedb_index, you can specify `--indexes freedb`.

Building the search indexes will take some time -- even on a fast machine it will still take an hour.
Once indexes are built, ensure that your tomcat instance has the permissions to access your data.
In Ubuntu:

    chown -R tomcat7:tomcat7 /home/search


Update Indexes using the Live Data Feed
---------------------------------------

It's possible to update search indexes using replication packets, the same way your slave MusicBrainz database is updated.

You need to edit the updateindex.sh and adapt the `SETTINGS` section to fit your setup:

	vi /home/search/searchserver/updater/updateindex.cfg

Once you're done, you can launch the indexes updating:
	/home/search/searchserver/updater/updateindex.sh

You can run this script hourly using cron.

Advanced options:

* You can get more verbose log by using the `--verbose` parameter:

```
/home/search/searchserver/updater/updateindex.sh --verbose
```

* The default settings allow you update most of the indexes excepted recording ones. 
If you want to choose specifically the indexes to be updated, either update the `INDEXES` variable in `updateindex.cfg`,
or comment the `INDEXES` variable in `updateindex.cfg` and run the script using the `--indexes` args:

```
/home/search/searchserver/updater/updateindex.sh --indexes artist,label
```

* A lock file is created to ensure you're not running concurrently the script.
You can provide the lock file by setting the `LOCK_FILE` env variable. This can be useful if you can to update concurrently different indexes:

```
LOCK_FILE=/tmp/lock_updating_release /home/search/searchserver/updater/updateindex.sh --indexes release
LOCK_FILE=/tmp/lock_updating_label /home/search/searchserver/updater/updateindex.sh --indexes label
```

Tuning Tomcat
-------------

Ubuntu enables the Java security manager by default, this will prevent the search server working.
For Ubuntu, set `TOMCAT6_SECURITY` in `/etc/default/tomcat7` to no:

    # Use the Java security manager? (yes/no)
    TOMCAT6_SECURITY=no

Next, configure Tomcat with the max heap memory you want to use for Search Server. This is 
very important because by default it will only use a maximum of 32MB, adding
the following to your profile sets up Tomcat with 512MB. This is the optimum amount to use if you can afford it, do not
allocate more than this because search can use the unallocated memory to cache the indexes.

Also configure Tomcat to run with file encoding UTF-8, otherwise it will use the default which 
will vary from platform to platform. To accomplish both, set the `JAVA_OPTS` like this:

    export JAVA_OPTS="-Xms512M -Xmx512M -Dfile.encoding=UTF-8"

In Ubuntu, you can change `JAVA_OPTS` in `/etc/default/tomcat7` -- you do not need to set an environment var.

Next, specify that Tomcat expects URIEncodings in UTF-8 format so they are decoded correctly.
In `server.xml` (in Ubuntu that's in `/etc/tomcat7/server.xml`) add the URIEncoding parameter to this
line:

    <Connector port="8080" protocol="HTTP/1.1"connectionTimeout="20000" redirectPort="8443">

So it nows looks like:

    <Connector port="8080" protocol="HTTP/1.1"connectionTimeout="20000" redirectPort="8443" URIEncoding="UTF-8">

By default Tomcat runs on port 8080. To switch it to port 80, edit the Connector line in `server.xml` again:

    <Connector port="80" protocol="HTTP/1.1"connectionTimeout="20000" redirectPort="8443" URIEncoding="UTF-8">

Finally, to enable a threadpool so that Tomcat can use multiple processors/cores, uncomment/add this line
in `server.xml`:

    <Executor name="tomcatThreadPool" namePrefix="catalina-exec-" 
              maxThreads="150" minSpareThreads="4" minThreads="8"/>

and add the executor attribute to the Connector:

    <Connector port="80" protocol="HTTP/1.1"connectionTimeout="20000" redirectPort="8443" 
               URIEncoding="UTF-8" executor="tomcatThreadPool">

We opted to create one thread per available core our setup, which in this case is 8.

Now start (or restart) Tomcat to have the latest settings take effect.


Testing the Search Server
-------------------------

You can now lookup search for resources with a url like:

    http://localhost:80/ws/2/artist/?query=fred

For compatibility with Webservice version 1 use:

    http://localhost:80/ws/1/artist/?query=fred

You can also get the results in JSON format using a url like:

    http://localhost:80/ws/2/artist/?query=fred&fmt=json


Server administration
---------------------

If you have built new indexes you can inform the search server of the new indexes without restarting it using the init
command. Setting init to mmap will memory map the indexes (this now the default),

    http://localhost:8080/?init=mmap

Or you can revert to the older filesystem based format using 

    http://localhost:8080/?init=nfio

You can enable the rate limiter with

    http://localhost:8080/?rate=true

or disable it with

    http://localhost:8080/?rate=false

The reload command is intended for use when an existing index has been updated rather than replaced, this is not currently used
    http://localhost:8080/?reload=true

All the above commands can only be performed on the local search machine otherwise a 403 error will be returned.

The number of queries done against any index since the servlet was started can be obtained using the count parameter
and index name

    http://localhost:8080/?count=artist


Troubleshooting
---------------

The search url is a mapping you can also use the underlying url directly as follows:

     http://localhost:8080/?type=artist&query=fred


Development
------------

For development environments there is a much simpler deployment possible

    mvn install  (because jetty will uses the installed index.jar, not work directly from the code like the servlet code)
    cd search-server/servlet
    mvn jetty:run

This will start the Jetty servlet container (instead of Tomcat), then just use the underlying url on port 8080

     http://localhost:8080/?type=artist&query=fred
