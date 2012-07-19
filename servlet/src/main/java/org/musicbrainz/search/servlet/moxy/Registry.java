package org.musicbrainz.search.servlet.moxy;

import org.musicbrainz.mmd2.Relation;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;

@XmlRegistry
public class Registry {
    private static final String ARTIST      = "artist";
    private static final String RELEASE     = "release";
    private static final String RECORDING   = "recording";
    private static final String URL         = "url";

    @XmlElementDecl(name=ARTIST)
    public JAXBElement<Relation> createArtist(Relation relation) {
        return new JAXBElement<Relation>(new QName(ARTIST), Relation.class, relation);
    }

    @XmlElementDecl(name=RELEASE, substitutionHeadName=ARTIST)
    public JAXBElement<Relation> createRelease(Relation relation) {
        return new JAXBElement<Relation>(new QName(RELEASE), Relation.class, relation);
    }

    @XmlElementDecl(name=RECORDING, substitutionHeadName=ARTIST)
    public JAXBElement<Relation> createRecording(Relation relation) {
        return new JAXBElement<Relation>(new QName(RECORDING), Relation.class, relation);
    }

    @XmlElementDecl(name=URL, substitutionHeadName=ARTIST)
    public JAXBElement<Relation> createUrl(Relation relation) {
        return new JAXBElement<Relation>(new QName(URL), Relation.class, relation);
    }

}