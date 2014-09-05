/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.schematron;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

/**
 * Purpose is to load Schematron Rules and compile it.
 * Compiled schematron rules are then used on input xml streams
 * 
 * @author yu021
 */
public class CompileSchematron {

    String rulePath;
    String expandInclusionsXsl = "iso-schematron-xslt2-local/iso_dsdl_include.xsl";
    String expandAbstractXsl = "iso-schematron-xslt2-local/iso_abstract_expand.xsl";
    String compileSchemaXsl = "iso-schematron-xslt2-local/iso_svrl_for_xslt2.xsl";
    String svrl2htmlXsl = "format/format-svrl-output-to-html.xsl";
    String svrl2textXsl = "format/format-svrl-output-to-text.xsl";
    String systemId;
    public File compiledSch;

    //XSLT Engine vars
    Transformer compileSchTransformer;
    SAXParser parser;
    XMLReader reader;
    SAXTransformerFactory stf;

    //compileTransformation filters
    XMLFilter compileFilter1,compileFilter2,compileFilter3;


    public CompileSchematron() throws SAXException, ParserConfigurationException, FileNotFoundException, IOException, TransformerConfigurationException {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();

        stf =  (SAXTransformerFactory) TransformerFactory.newInstance();

        this.setupCompileEngine();

    }

    public CompileSchematron(String rules) throws SAXException, ParserConfigurationException, FileNotFoundException, IOException, TransformerConfigurationException {
        this.rulePath = rules;

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();

        stf =  (SAXTransformerFactory) TransformerFactory.newInstance();

        this.setupCompileEngine();

        FileInputStream fis = new FileInputStream(rules);
        this.compileSch(fis);

    }

   

    //assume that the schematron rules read previously and converted to inputstream
    private void setupCompileEngine() throws TransformerConfigurationException {
        File stylesheet1 = new File(expandInclusionsXsl);
        File stylesheet2 = new File(expandAbstractXsl);
        File stylesheet3 = new File(compileSchemaXsl);
   
        //setup XSLT Engine
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        
        
            // Create the filters
            compileFilter1 = stf.newXMLFilter(new StreamSource(stylesheet1));
            compileFilter2 = stf.newXMLFilter(new StreamSource(stylesheet2));
            compileFilter3 = stf.newXMLFilter(new StreamSource(stylesheet3));

            // Wire the output of the reader to filter1 
            // and the output of filter1 to filter2
            compileFilter1.setParent(reader);
            compileFilter2.setParent(compileFilter1);
            compileFilter3.setParent(compileFilter2);

            compileSchTransformer = stf.newTransformer();
        
    }

    public void compileSch(InputStream schRules) {
        // Set up the input stream
        BufferedInputStream bis = new BufferedInputStream(schRules);
        InputSource input = new InputSource(bis);

        try{
            // Set up the output stream
            //CharArrayWriter caw = new CharArrayWriter();
            //FileWriter fw = new FileWriter("compiled-sch.xsl");

            //File outputfile = new File("compiled-sch.xsl");
            //create temp file to process xsl file
            this.compiledSch = File.createTempFile("wdts-compiledSch-", ".tmp");
            this.compiledSch.deleteOnExit();
            
            FileOutputStream fos = new FileOutputStream(this.compiledSch);

            //StreamResult result = new StreamResult(caw);
            //StreamResult result = new StreamResult(System.out);
            //StreamResult result = new StreamResult(fw);
            StreamResult result = new StreamResult(fos);
            //StreamResult result = new StreamResult(caw);


            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            SAXSource transformSource = new SAXSource(compileFilter3, input);
            compileSchTransformer.transform(transformSource, result);

        } catch (IOException ex) {
            Logger.getLogger(CompileSchematron.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CompileSchematron.class.getName()).log(Level.SEVERE, null, ex);
        }  

    }

    public void compileSch(InputStream schRules, File outputfile) {
        // Set up the input stream
        BufferedInputStream bis = new BufferedInputStream(schRules);
        InputSource input = new InputSource(bis);

        try{
            // Set up the output stream
            //CharArrayWriter caw = new CharArrayWriter();
            //FileWriter fw = new FileWriter("compiled-sch.xsl");

            //File outputfile = new File("compiled-sch.xsl");
            //create temp file to process xsl file
            this.compiledSch = outputfile;

            FileOutputStream fos = new FileOutputStream(this.compiledSch);

            //StreamResult result = new StreamResult(caw);
            //StreamResult result = new StreamResult(System.out);
            //StreamResult result = new StreamResult(fw);
            StreamResult result = new StreamResult(fos);
            //StreamResult result = new StreamResult(caw);


            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            SAXSource transformSource = new SAXSource(compileFilter3, input);
            compileSchTransformer.transform(transformSource, result);

        } catch (IOException ex) {
            Logger.getLogger(CompileSchematron.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(CompileSchematron.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
