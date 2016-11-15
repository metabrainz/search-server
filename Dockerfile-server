FROM jetty:9.3.10

MAINTAINER Robert Kaye <rob@metabrainz.org>

ENV SEARCH_HOME=/home/search
ENV INDEXES_VERSION=1
ENV RSYNC_PASSWORD=search

RUN apt-get update && apt-get install -y --no-install-recommends \
        bsdmainutils \
        psmisc \
        python \
        rsync

# Install consul template 
COPY ./docker/prod/environment /etc/consul_template_env.sh

COPY ./docker/prod/docker-helpers/install_consul_template.sh \
     ./docker/prod/docker-helpers/install_runit.sh \
     /usr/local/bin/
RUN chmod 755 /usr/local/bin/install_consul_template.sh /usr/local/bin/install_runit.sh && \
    sync && \
    install_consul_template.sh && \
    rm -f \
        /usr/local/bin/install_consul_template.sh \
        /usr/local/bin/install_runit.sh

WORKDIR $JETTY_HOME

# Install the war file
RUN curl -o $JETTY_HOME/webapps/ROOT.war ftp://data.musicbrainz.org/pub/musicbrainz/search/servlet/searchserver.war 

# Setup the data volumes/dirs
VOLUME ["/home/search/indexes"]

COPY ./docker/prod/server/server.service /etc/sv/server/run
RUN chmod 755 /etc/sv/server/run && \
    ln -sf /etc/sv/server /etc/service/

# Configuration
COPY ./docker/prod/server/consul-template.conf /etc/consul-template.conf
COPY ./docker/prod/server/consul_server.ini.ctmpl /code/consul_server.ini.ctmpl

ENTRYPOINT ["/usr/local/bin/runsvinit"]
