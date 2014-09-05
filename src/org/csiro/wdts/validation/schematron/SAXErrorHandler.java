/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.schematron;

import org.csiro.wdts.validation.service.*;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author yu021
 */
class SAXErrorHandler extends DefaultHandler {

    StringBuffer parseError;
    int errorCount = 0;

    public SAXErrorHandler() {
        errorCount = 0;
        parseError = new StringBuffer();

    }

    public void error(SAXParseException ex) {
        parseError.append("\n");
        parseError.append("ERROR: ");

        parseError.append(ex.getMessage());
        parseError.append("\n");

        errorCount++;
    }

    public void fatalError(SAXParseException ex) {
        parseError.append("\n");
        parseError.append("FATAL: ");

        parseError.append(ex.getMessage());
        parseError.append("\n");
        parseError.append(ex.fillInStackTrace());
        errorCount++;
    }

    public void warning(SAXParseException ex) {
        parseError.append("\n");
        parseError.append("WARN: ");
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
