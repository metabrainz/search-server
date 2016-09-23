FROM jetty:9.3.10

MAINTAINER Robert Kaye <rob@metabrainz.org>

RUN apt-get update && apt-get install -y --no-install-recommends psmisc

WORKDIR $JETTY_HOME

# Install the war file
RUN curl -o $JETTY_HOME/webapps/ROOT.war ftp://ftp.eu.metabrainz.org/pub/musicbrainz/search/servlet/searchserver.war 

# Install dumb-init
RUN wget -O /usr/local/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.1.3/dumb-init_1.1.3_amd64
RUN chmod +x /usr/local/bin/dumb-init

# Install the watcher script
ADD watch-for-reload.sh /

# Setup the data volumes/dirs
VOLUME ["/home/search/data"]
RUN ln -s /home/search/data /home/search/indexdata

ENTRYPOINT ["/usr/local/bin/dumb-init", "--"]
CMD ["/watch-for-reload.sh", "/home/search/data/RELOAD", "/docker-entrypoint.sh", "java","-jar","/usr/local/jetty/start.jar"]
