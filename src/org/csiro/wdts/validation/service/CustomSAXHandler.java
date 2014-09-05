/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.validation.service;

import java.io.IOException;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author yu021
 */
public class CustomSAXHandler extends DefaultHandler {
    CatalogResolver resolver = new CatalogResolver();
    
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        System.out.println("Resolve entity");
        return resolver.resolveEntity(publicId, systemId);
    }

    StringBuffer parseError;
    int errorCount = 0;

    public CustomSAXHandler() {
        errorCount = 0;
        parseError = new StringBuffer();

    }

    @Override
    public void error(SAXParseException ex) {
        System.out.println("SAX error "+ ex.getMessage());
        parseError.append("\n");
        parseError.append("ERROR: ");

        parseError.append("in line " + ex.getLineNumber()     + " ");

        parseError.append(ex.getMessage());
        parseError.append("\n");

        errorCount++;
    }

    @Override
    public void fatalError(SAXParseException ex) throws SAXException {
        System.out.println("SAX fatal error:" + ex.getMessage());
        parseError.append("\n");
        parseError.append("FATAL: ");

        parseError.append(ex.getMessage());
        parseError.append("\n");
        //parseError.append(ex.fillInStackTrace());
        errorCount++;
        SAXException se = new SAXException();
        throw se;

    }

    @Override
    public void warning(SAXParseException ex) {
        System.out.println("SAX warning error "+ ex.getMessage());
        parseError.append("\n");
        parseError.append("WARNING: ");
        parseError.append(ex.getMessage());
        errorCount++;
    }

    public String getMessages() {
        return parseError.toString();
    }

    public int getNumberOfErrors() {
        return errorCount;
    }
}
