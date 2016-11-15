FROM airdock/oracle-jdk:jdk-8u74

MAINTAINER Robert Kaye <rob@metabrainz.org>

ENV SEARCH_HOME=/home/search
ENV INDEXES_VERSION=1

RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
	python \
        rsync \
        runit

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

# Install search indexer stuff
WORKDIR /home/search

RUN mkdir -p /home/search/rsync /home/search/bin
COPY indexer/rsyncd.conf /etc
COPY indexer/rsync.secrets /etc
RUN chmod og-rw /etc/rsync.secrets

# Install the war file
RUN curl -o /home/search/index.jar ftp://data.musicbrainz.org/pub/musicbrainz/search/index/index.jar

# Install the scripts 
COPY ./docker/prod/indexer/build-indexes.sh /home/search/bin
COPY ./docker/prod/indexer/indexer.service /etc/sv/indexer/run
RUN chmod 755 /etc/sv/indexer/run && \
    ln -sf /etc/sv/indexer /etc/service/

# Configuration
COPY ./docker/prod/indexer/consul-template.conf /etc/consul-template.conf
COPY ./docker/prod/indexer/consul_indexer_config.ini.ctmpl /code/consul_indexer_config.ini.ctmpl

ENTRYPOINT ["/usr/local/bin/runsvinit"]
