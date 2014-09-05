/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package org.csiro.wdts.validation.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.resolver.Resolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 *
 * @author yu021
 */
public class XMLValidationService {


	XMLReader parser;
    SAXErrorHandler defaultHandler;
    
    private String SCHEMA =
            "http://apache.org/xml/features/validation/schema";
    private String SCHEMA_FULL_CHECKING =
            "http://apache.org/xml/features/validation/schema-full-checking";
    private String WARN_ON_DUPLICATE_ATTDEF =
            "http://apache.org/xml/features/validation/warn-on-duplicate-attdef";
    private String LOAD_EXTERNAL_DTD =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private String VALIDATION =  "http://xml.org/sax/features/validation";
    
    public XMLValidationService() throws SAXException {
        //Name of the parser to use to create the xmlReader.
        String parserName = "org.apache.xerces.parsers.SAXParser";

        parser = XMLReaderFactory.createXMLReader(parserName);

        // load schema\dtd
        parser.setFeature(LOAD_EXTERNAL_DTD, true);
        // turn on validation
        parser.setFeature(VALIDATION, true);
        parser.setFeature(SCHEMA, true);
        parser.setFeature(SCHEMA_FULL_CHECKING, true);
        parser.setFeature(WARN_ON_DUPLICATE_ATTDEF, true);

        defaultHandler = new SAXErrorHandler();
        
    }

    public void validateFile(InputSource source, DefaultHandler handler)
            throws SAXException, IOException {

        // make schema resolver so the parser knows where to find the schema
        Resolver resolver = new Resolver();

        parser.setEntityResolver((EntityResolver) resolver);
        parser.setErrorHandler(handler);

        //parsing the file with validation turned on no content handler needed
        parser.parse(source);

    }


    public String validateXML(String xmltext){
        StringBuffer sb = new StringBuffer();
        InputSource input = new InputSource(new StringReader(xmltext));
        try {
            // make schema resolver so the parser knows where to find the schema
            //EntityResolver resolver = new EntityResolver();

            parser.setEntityResolver(this.defaultHandler);
            parser.setErrorHandler(this.defaultHandler);

            //parsing the file with validation turned on no content handler needed
            parser.parse(input);
        } catch (SAXException se) {
            sb.append("threw SAXException!");
            sb.append("\n");
            sb.append("\n");
            //sb.append("trace: " + se.getMessage());
            //sb.append("\n");
            //sb.append("\n");
            sb.append(this.defaultHandler.getMessages());
            sb.append("\n");
            sb.append("LOG: " + this.defaultHandler.getNumberOfErrors() + " error(s)");


            return sb.toString();

        } catch (IOException ioe) {
            sb.append("threw IO Exception!");
            sb.append("\n");
            sb.append("\n");
            sb.append("trace: " + ioe.getMessage());
            sb.append("\n");
            return sb.toString();

        }
        
        return "Success! No structural errors found! ";
    }

    public String validateXMLByteStream(byte[] bytes) throws FileNotFoundException{
        StringBuffer sb = new StringBuffer();
        InputSource input = new InputSource(new ByteArrayInputStream(bytes));
        try {
            // make schema resolver so the parser knows where to find the schema
            //EntityResolver resolver = new EntityResolver();

            parser.setEntityResolver(this.defaultHandler);
            parser.setErrorHandler(this.defaultHandler);

            //parsing the file with validation turned on no content handler needed
            parser.parse(input);
        } catch (SAXException se) {
            sb.append("threw SAXException!");
            sb.append("\n");
            sb.append("\n");
            //sb.append("trace: " + se.getMessage());
            //sb.append("\n");
            //sb.append("\n");
            sb.append(this.defaultHandler.getMessages());
            sb.append("\n");
            sb.append("LOG: " + this.defaultHandler.getNumberOfErrors() + " error(s)");


            return sb.toString();

        } catch (IOException ioe) {
            sb.append("threw IO Exception!");
            sb.append("\n");
            sb.append("\n");
            sb.append("trace: " + ioe.getMessage());
            sb.append("\n");
            return sb.toString();

        }

        return "Success! No structural errors found! ";
    }

}
