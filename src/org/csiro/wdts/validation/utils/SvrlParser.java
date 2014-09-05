/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.utils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author spenserk
 */
public class SvrlParser extends DefaultHandler {

    StringBuffer errMsgBuf = null;
    StringBuffer diagMsgBuf = null;
    HashMap<String, List<String>> messages;
    boolean inTextTag, inSuccessfulReport, inFailedAssert, inDiagnosticReference;
    String messageType = null;

    public HashMap<String, List<String>> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, List<String>> messages) {
        this.messages = messages;
    }
    

    public static void main(String args[])
            throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        SvrlParser handler = new SvrlParser();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        
        // Parse each file provided on the
        // command line.
        for (int i = 0; i < args.length; i++) {
            FileReader r = new FileReader(args[i]);
            xr.parse(new InputSource(r));
        }
    }

    public SvrlParser() {
        super();
        this.inTextTag = false;
        this.inDiagnosticReference = false;
        this.inFailedAssert = false;
        this.inSuccessfulReport = false;
        this.messageType = null;
        this.errMsgBuf = null;
        this.diagMsgBuf = null;
        this.messages = new HashMap<String,List<String>>();
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

        if (name.equals("successful-report"))
        {
            this.inSuccessfulReport = true;
            this.errMsgBuf = null;
            this.diagMsgBuf = null;

        }
        else if (name.equals("failed-assert")) {
            this.inFailedAssert = true;
            this.errMsgBuf = null;
            this.diagMsgBuf = null;
        }
        else if (name.equals("diagnostic-reference")) {
            this.inDiagnosticReference = true;            
        }
        else if (name.equals("text") && (this.inSuccessfulReport || this.inFailedAssert)) {
            this.inTextTag = true;
        }


        if(this.inSuccessfulReport || this.inFailedAssert) {
            String flag = this.getFlagValue(atts);
            if(flag != null) {
                this.messageType = flag;
            }
        }
    }
    
    private String getFlagValue(Attributes atts) {
        if(atts ==null || atts.getLength() <= 0) {
            return null;
        }

        String value = null;
        
        value = atts.getValue("flag");

        return value;
    }

    public void endElement(String uri, String name, String qName) {

        if(name.equals("successful-report") ||name.equals("failed-assert") ) {
            //add the data collected to the hashmap
            if(this.messageType == null) {
                this.messageType = "INFO";
            }
            else {
                this.messageType = this.messageType.toUpperCase();
            }

            if(this.errMsgBuf != null) {
                String combinedMessage = null;
                List<String> typedListOfMessages = this.messages.get(this.messageType);
                if(typedListOfMessages == null) {
                    typedListOfMessages = new ArrayList<String>();
                }

                if(this.diagMsgBuf != null) {
                    combinedMessage = this.errMsgBuf.toString().replaceAll("\\s+|\n", " ") + "\n" + this.diagMsgBuf.toString();
                }
                else {
                    combinedMessage = this.errMsgBuf.toString().replaceAll("\\s+|\n", " ");
                }
                //typedListOfMessages.add(this.buf.toString().replaceAll("\\s+|\n", " "));
                typedListOfMessages.add(combinedMessage);

                //put back list to hashmap
                this.messages.put(this.messageType, typedListOfMessages);
            }

            this.errMsgBuf = null;
            this.diagMsgBuf = null;
        }

        //reset the right stuff...
        if (name.equals("successful-report"))
        {
            this.inSuccessfulReport = false;
        }
        else if (name.equals("failed-assert")) {
            this.inFailedAssert = false;
        }
        else if (name.equals("diagnostic-reference")) {
            this.inDiagnosticReference = false;
        }
        else if (name.equals("text")) {
            this.inTextTag = false;
        }

    }

    public void characters(char ch[], int start, int length) {
       
        if (this.inTextTag || this.inDiagnosticReference) {
            String content = new String(ch, start, length);
            if(!content.trim().equals("")) {
                
                if(this.inDiagnosticReference) {
                    if(this.diagMsgBuf == null) {
                        this.diagMsgBuf = new StringBuffer(content);
                    }
                    else {
                        this.diagMsgBuf.append(content);
                    }
                    
                }
                else {
                    if(this.errMsgBuf == null) {
                        this.errMsgBuf = new StringBuffer(content);
                    }
                    else {
                        this.errMsgBuf.append(content);
                    }
                    
                }


            }
        }
    }

}
