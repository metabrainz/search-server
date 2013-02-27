package org.musicbrainz.search.servlet;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.HashSet;

/**
 * Convert any chars in the data that are within EscapeString to the ' character. This is because these characters
 * cannot be output in Xml in any form, however it is valid for them to be output in json so we do not want to remove them from
 * source.
 *
 * All illegal chars are mapped to ' because this the most common scenerio is when user uses invalid ' character, this
 * happens rarely but when it does prevent the Xml being returned so this is a pragmatic approach to allow the Xml to be returned
 * without error.
 *
 */
public class EscapingXMLStreamWriter implements XMLStreamWriter
{

    private final XMLStreamWriter writer;
    public static final char substitute = '\'';
    private static final HashSet<Character> illegalChars;

    static {
        final String escapeString = "\u0000\u0001\u0002\u0003\u0004\u0005" +
                "\u0006\u0007\u0008\u000B\u000C\u000E\u000F\u0010\u0011\u0012" +
                "\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001A\u001B\u001C" +
                "\u001D\u001E\u001F\uFFFE\uFFFF";

        illegalChars = new HashSet<Character>();
        for (int i = 0; i < escapeString.length(); i++) {
            illegalChars.add(escapeString.charAt(i));
        }
    }

    public EscapingXMLStreamWriter(XMLStreamWriter writer) {

        if (null == writer) {
            throw new IllegalArgumentException("null");
        } else {
            this.writer = writer;
        }
    }

    private boolean isIllegal(char c) {
        return illegalChars.contains(c);
    }

    /**
     * Substitutes all illegal characters in the given string by the value of
     * {@link EscapingXMLStreamWriter#substitute}. If no illegal characters
     * were found, no copy is made and the given string is returned.
     *
     * @param string
     * @return
     */
    private String escapeCharacters(String string) {

        char[] copy = null;
        boolean copied = false;
        for (int i = 0; i < string.length(); i++) {
            if (isIllegal(string.charAt(i))) {
                if (!copied) {
                    copy = string.toCharArray();
                    copied = true;
                }
                copy[i] = substitute;
            }
        }
        return copied ? new String(copy) : string;
    }

    public void writeStartElement(String s) throws XMLStreamException {
        writer.writeStartElement(s);
    }

    public void writeStartElement(String s, String s1) throws XMLStreamException {
        writer.writeStartElement(s, s1);
    }

    public void writeStartElement(String s, String s1, String s2)
            throws XMLStreamException {
        writer.writeStartElement(s, s1, s2);
    }

    public void writeEmptyElement(String s, String s1) throws XMLStreamException {
        writer.writeEmptyElement(s, s1);
    }

    public void writeEmptyElement(String s, String s1, String s2)
            throws XMLStreamException {
        writer.writeEmptyElement(s, s1, s2);
    }

    public void writeEmptyElement(String s) throws XMLStreamException {
        writer.writeEmptyElement(s);
    }

    public void writeEndElement() throws XMLStreamException {
        writer.writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        writer.writeEndDocument();
    }

    public void close() throws XMLStreamException {
        writer.close();
    }

    public void flush() throws XMLStreamException {
        writer.flush();
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writer.writeAttribute(localName, value);
    }

    public void writeAttribute(String prefix, String namespaceUri, String localName, String value)
            throws XMLStreamException {
        writer.writeAttribute(prefix, namespaceUri, localName, value);
    }

    public void writeAttribute(String namespaceUri, String localName, String value)
            throws XMLStreamException {
        writer.writeAttribute(namespaceUri, localName, value);
    }

    public void writeNamespace(String s, String s1) throws XMLStreamException {
        writer.writeNamespace(s, s1);
    }

    public void writeDefaultNamespace(String s) throws XMLStreamException {
        writer.writeDefaultNamespace(s);
    }

    public void writeComment(String s) throws XMLStreamException {
        writer.writeComment(s);
    }

    public void writeProcessingInstruction(String s) throws XMLStreamException {
        writer.writeProcessingInstruction(s);
    }

    public void writeProcessingInstruction(String s, String s1)
            throws XMLStreamException {
        writer.writeProcessingInstruction(s, s1);
    }

    public void writeCData(String s) throws XMLStreamException {
        writer.writeCData(escapeCharacters(s));
    }

    public void writeDTD(String s) throws XMLStreamException {
        writer.writeDTD(s);
    }

    public void writeEntityRef(String s) throws XMLStreamException {
        writer.writeEntityRef(s);
    }

    public void writeStartDocument() throws XMLStreamException {
        writer.writeStartDocument();
    }

    public void writeStartDocument(String s) throws XMLStreamException {
        writer.writeStartDocument(s);
    }

    public void writeStartDocument(String s, String s1)
            throws XMLStreamException {
        writer.writeStartDocument(s, s1);
    }

    public void writeCharacters(String s) throws XMLStreamException {
        writer.writeCharacters(escapeCharacters(s));
    }

    public void writeCharacters(char[] chars, int start, int len)
            throws XMLStreamException
    {
        writer.writeCharacters(escapeCharacters(new String(chars, start, len)));
    }

    public String getPrefix(String s) throws XMLStreamException {
        return writer.getPrefix(s);
    }

    public void setPrefix(String s, String s1) throws XMLStreamException {
        writer.setPrefix(s, s1);
    }

    public void setDefaultNamespace(String s) throws XMLStreamException {
        writer.setDefaultNamespace(s);
    }

    public void setNamespaceContext(NamespaceContext namespaceContext)
            throws XMLStreamException {
        writer.setNamespaceContext(namespaceContext);
    }

    public NamespaceContext getNamespaceContext() {
        return writer.getNamespaceContext();
    }

    public Object getProperty(String s) throws IllegalArgumentException {
        return writer.getProperty(s);
    }
}