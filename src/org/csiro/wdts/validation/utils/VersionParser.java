/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.utils;

import java.io.FileReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author spenserk
 */
public class VersionParser extends DefaultHandler {

    String versionString = null;
    StringBuffer buf = null;
    boolean readData = false;
    int numberOfVersionElements = 0;

    public boolean isReadData() {
        return readData;
    }

    public void setReadData(boolean readData) {
        this.readData = readData;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public static void main(String args[])
            throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        VersionParser handler = new VersionParser();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        
        // Parse each file provided on the
        // command line.
        for (int i = 0; i < args.length; i++) {
            FileReader r = new FileReader(args[i]);
            xr.parse(new InputSource(r));
        }
    }

    public VersionParser() {
        super();
        this.versionString = "";
        this.readData = false;
        this.numberOfVersionElements = 0;
        this.buf = null;
    }


    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////
    @Override
    public void startDocument() {
       
    }

    @Override
    public void endDocument() {

    }

    public void startElement(String uri, String name, String qName, Attributes atts) {

        if (name.equals("version") &&
            (
                uri.equals("http://www.bom.gov.au/std/water/xml/wdtf0.3") ||
                uri.equals("http://www.bom.gov.au/std/water/xml/wdtf/1.0")
            )
           )
        {
            this.readData = true;
            //writeContent();
        }
    }

    public void endElement(String uri, String name, String qName) {

        if (name.equals("version") &&
            (
                uri.equals("http://www.bom.gov.au/std/water/xml/wdtf0.3") ||
                uri.equals("http://www.bom.gov.au/std/water/xml/wdtf/1.0")
            )
           )
        {
            //writeContent();
            this.readData = false; //reset flag
            this.numberOfVersionElements++;

            //get the string from the buffer
            this.versionString = this.buf.toString();
            this.buf = null;
        }

    }

    public void characters(char ch[], int start, int length) {
       
        if (this.readData == true) {
            String content = new String(ch, start, length);
            if(!content.trim().equals("")) {
                if(this.buf == null) {
                    this.buf = new StringBuffer(content);
                }
                else {
                    this.buf.append(content);
                }

            }
        }
    }

}
