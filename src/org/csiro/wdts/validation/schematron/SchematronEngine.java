/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.schematron;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
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
public class SchematronEngine {

    String compiledRulePath;
    FileInputStream fis;
    //XSLT Engine vars
    Transformer validationTransformer;
    //validationTranformation filters
    XMLFilter validationFilter1, validationFilter2, validationFilter3, svrlFilter;
    private ApplicationContext applicationContext;

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public SchematronEngine(String compiledRules) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException, TransformerConfigurationException {
        File compiledFile = new File(compiledRules);
        this.fis = new FileInputStream(compiledFile);

        this.setupValidationEngine(fis);
    }

    public SchematronEngine(File compiledFile) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException, TransformerConfigurationException {

        this.fis = new FileInputStream(compiledFile);
        this.setupValidationEngine(fis);
    }
    public SchematronEngine(ApplicationContext appContext, File compiledFile) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException, TransformerConfigurationException {
        this.setApplicationContext(appContext);
        this.fis = new FileInputStream(compiledFile);
        this.setupValidationEngine(fis);
    }
    public SchematronEngine(ApplicationContext appContext, InputStream inputStream) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException, TransformerConfigurationException {
        this.setApplicationContext(appContext);
		this.setupValidationEngine(inputStream);

    }
    public void cleanup() throws IOException {
        this.cleanupFis();

        validationTransformer = null;
        validationFilter1 = null;
        validationFilter2 = null;
        validationFilter3 = null;
        svrlFilter = null;

    }

    public void cleanupFis() throws IOException {
        if(fis != null) {
            fis.close();
            fis = null;
        }

    }

    //validation engine does the work of running compiled xsl with xml
    private void setupValidationEngine(InputStream compiledSch) throws IOException, SAXException, ParserConfigurationException, TransformerConfigurationException {
        // Read the arguments
        String textOutputStylesheet = "format/format-svrl-output-to-text.xsl";
        String svrlFilterStylesheet = "format/filter-svrl.xsl";
        //String htmlOutputStylesheet = "format/format-svrl-output-to-html.xsl";
        //File stylesheet2 = new File(htmlOutputStylesheet);

        Resource textOutputResource = this.applicationContext.getResource(textOutputStylesheet);
        File stylesheet1 = null, svrlFilterFile = null;
        try {
            stylesheet1 = textOutputResource.getFile();
        } catch (IOException ioe) {
            throw(new IOException());
        }
        Resource svrlFilterResource = this.applicationContext.getResource(svrlFilterStylesheet);

        try{
            svrlFilterFile = svrlFilterResource.getFile();
        } catch (IOException ioe) {
            IOException internal_ioe = new IOException();

            throw(new IOException());
        }


        //xml transformer property
        System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
        //System.setProperty("javax.xml.transform.TransformerFactory",
        //                   "com.saxonica.SchemaAwareTransformerFactory");


        XMLReader reader;
        SAXParser parser;
        SAXTransformerFactory stf;

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        parser = spf.newSAXParser();
        reader = parser.getXMLReader();

        stf = (SAXTransformerFactory) TransformerFactory.newInstance();
        try {
            // Create the filters
            validationFilter1 = stf.newXMLFilter(new StreamSource(compiledSch));

            validationFilter2 = stf.newXMLFilter(
                    new StreamSource(stylesheet1));

            validationFilter3 = stf.newXMLFilter(
                    new StreamSource(stylesheet1));

            svrlFilter = stf.newXMLFilter(
                    new StreamSource(svrlFilterFile));

        } catch (TransformerConfigurationException ex) {
            throw(ex);
        }
        //validationFilter3 = stf.newXMLFilter(
        //        new StreamSource(stylesheet2));

        // Wire the output of the reader to filter1 (see Note #3)
        // and the output of filter1 to filter2
        validationFilter1.setParent(reader);
        svrlFilter.setParent(validationFilter1);

        validationFilter2.setParent(svrlFilter); //text output
        //validationFilter3.setParent(validationFilter1); //html output


        validationTransformer = stf.newTransformer();
        System.gc();

    }

    public File validateSch2svrl(InputStream xmlData) {
        File svrlOutput = null;

        try {
            // Set up the output stream
            svrlOutput = File.createTempFile("wdts-result", ".svrl");
            svrlOutput.deleteOnExit();

            BufferedOutputStream bos;
            bos = new BufferedOutputStream(new FileOutputStream(svrlOutput));

            //StreamResult result = new StreamResult(System.out);
            StreamResult result = new StreamResult(bos);

            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            // Set up the input stream
            BufferedInputStream bis = new BufferedInputStream(xmlData);
            InputSource input = new InputSource(bis);

            SAXSource transformSource = new SAXSource(svrlFilter, input);
            validationTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
            validationTransformer.transform(transformSource, result);


        } catch (IOException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return svrlOutput;

    }



    public File validateSch2svrl(StringReader xmlData) {
        File svrlOutput = null;

        try {
            // Set up the output stream
            svrlOutput = File.createTempFile("wdts-result", ".svrl");

            svrlOutput.deleteOnExit();

            BufferedOutputStream bos;
            bos = new BufferedOutputStream(new FileOutputStream(svrlOutput));

            //StreamResult result = new StreamResult(System.out);
            StreamResult result = new StreamResult(bos);

            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            // Set up the input stream
            BufferedReader bis = new BufferedReader(xmlData);
            InputSource input = new InputSource(bis);

            SAXSource transformSource = new SAXSource(svrlFilter, input);
            validationTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
            validationTransformer.transform(transformSource, result);


        } catch (IOException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return svrlOutput;

    }

    public File validateSch2svrl(FileReader xmlData) {
        File svrlOutput = null;

        try {
            // Set up the output stream
            svrlOutput = File.createTempFile("wdts-result", ".svrl");
            svrlOutput.deleteOnExit();

            BufferedOutputStream bos;
            bos = new BufferedOutputStream(new FileOutputStream(svrlOutput));

            //StreamResult result = new StreamResult(System.out);
            StreamResult result = new StreamResult(bos);

            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            // Set up the input stream
            BufferedReader bis = new BufferedReader(xmlData);
            InputSource input = new InputSource(bis);

            SAXSource transformSource = new SAXSource(svrlFilter, input);
            validationTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
            validationTransformer.transform(transformSource, result);


        } catch (IOException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return svrlOutput;

    }

    public File validateSch2text(InputStream xmlData) {
        File textOutput = null;

        try {
            // Set up the output stream
            //File f = new File("result.svrl");
            textOutput = File.createTempFile("wdts-result", ".txt");
            textOutput.deleteOnExit();

            //FileWriter fw = new FileWriter(f);

            BufferedOutputStream bos;
            bos = new BufferedOutputStream(new FileOutputStream(textOutput));

            //StreamResult result = new StreamResult(caw);
            //StreamResult result = new StreamResult(System.out);
            StreamResult result = new StreamResult(bos);
            //StreamResult result = new StreamResult(caw);


            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            // Set up the input stream
            BufferedInputStream bis = new BufferedInputStream(xmlData);
            InputSource input = new InputSource(bis);

            SAXSource transformSource = new SAXSource(validationFilter2, input);
            validationTransformer.setOutputProperty(OutputKeys.METHOD, "text");
            validationTransformer.transform(transformSource, result);


            bis.close();
            bos.close();
            System.gc();

        } catch (IOException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return textOutput;
    }


    public File validateSch2text(StringReader xmlData) throws TransformerException, FileNotFoundException, IOException {
        File textOutput = null;

        // Set up the output stream
        //File f = new File("result.svrl");
        textOutput = File.createTempFile("wdts-result", ".txt");
        textOutput.deleteOnExit();

        //FileWriter fw = new FileWriter(f);

        BufferedOutputStream bos;
        bos = new BufferedOutputStream(new FileOutputStream(textOutput));

        //StreamResult result = new StreamResult(caw);
        //StreamResult result = new StreamResult(System.out);
        StreamResult result = new StreamResult(bos);
        //StreamResult result = new StreamResult(caw);


        // Set up the transformer to process the SAX events
        // generated by the last filter in the chain
        // Set up the input stream
        BufferedReader bis = new BufferedReader(xmlData);
        InputSource input = new InputSource(bis);

        SAXSource transformSource = new SAXSource(validationFilter2, input);
        validationTransformer.setOutputProperty(OutputKeys.METHOD, "text");
        validationTransformer.transform(transformSource, result);

        System.gc();


        return textOutput;
    }


    public File svrl2text(File svrl) throws TransformerException, FileNotFoundException, IOException {
        File textOutput = null;

        // Set up the output stream
        //File f = new File("result.svrl");
        textOutput = File.createTempFile("wdts-result", ".txt");
        textOutput.deleteOnExit();

        //FileWriter fw = new FileWriter(f);

        BufferedOutputStream bos;
        bos = new BufferedOutputStream(new FileOutputStream(textOutput));

        //StreamResult result = new StreamResult(caw);
        //StreamResult result = new StreamResult(System.out);
        StreamResult result = new StreamResult(bos);
        //StreamResult result = new StreamResult(caw);


        // Set up the transformer to process the SAX events
        // generated by the last filter in the chain
        // Set up the input stream
        BufferedReader bis = new BufferedReader(new FileReader(svrl));
        InputSource input = new InputSource(bis);

        SAXSource transformSource = new SAXSource(validationFilter3, input);
        validationTransformer.setOutputProperty(OutputKeys.METHOD, "text");
        validationTransformer.transform(transformSource, result);

        System.gc();


        return textOutput;

    }

    public PipedInputStream validateSch2svrl_piped(InputStream xmlData) throws IOException {

        PipedInputStream resultStream = null;
        PipedOutputStream svrlOutput = null;

        try {

            //read xmlData and parse, configure outPipe for result, connect outpipe to resultStreamPipe
            BufferedInputStream bis = new BufferedInputStream(xmlData);
            InputSource input = new InputSource(bis);

            svrlOutput = new PipedOutputStream();
            resultStream = new PipedInputStream(svrlOutput);

            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            // Set up the input stream
            StreamResult result = new StreamResult(svrlOutput);

            SAXSource transformSource = new SAXSource(svrlFilter, input);
            validationTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
            validationTransformer.transform(transformSource, result);

        } catch (TransformerException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultStream;

    }
    public PipedInputStream validateSch2text_piped(InputStream xmlData) throws IOException {
        PipedOutputStream textOutput = null;
        PipedInputStream inPipe = null;

        try {
            //read xmlData and parse, configure outPipe for result, connect outpipe to resultStreamPipe
            BufferedInputStream bis = new BufferedInputStream(xmlData);
            InputSource input = new InputSource(bis);

            textOutput = new PipedOutputStream();
            inPipe = new PipedInputStream(textOutput);

            // Set up the transformer to process the SAX events
            // generated by the last filter in the chain
            // Set up the input stream
            StreamResult result = new StreamResult(textOutput);

            SAXSource transformSource = new SAXSource(validationFilter2, input);
            validationTransformer.setOutputProperty(OutputKeys.METHOD, "text");
            validationTransformer.transform(transformSource, result);

            System.gc();
        } catch (TransformerException ex) {
            Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return inPipe;
    }
    public PipedInputStream svrl2text_piped(InputStream svrl) throws TransformerException, FileNotFoundException, IOException {
        PipedInputStream textStream  = null;


        //read xmlData and parse, configure outPipe for result, connect outpipe to resultStreamPipe
        BufferedInputStream bis = new BufferedInputStream(svrl);
        InputSource input = new InputSource(bis);

        PipedOutputStream outputStream = new PipedOutputStream();
        textStream = new PipedInputStream();
        textStream.connect(outputStream);

        // Set up the transformer to process the SAX events
        // generated by the last filter in the chain
        // Set up the input stream
        StreamResult result = new StreamResult(outputStream);

        SAXSource transformSource = new SAXSource(validationFilter3, input);
        validationTransformer.setOutputProperty(OutputKeys.METHOD, "text");
        validationTransformer.transform(transformSource, result);

        System.gc();


        return textStream;
    }
    /* public void validateSch2html(InputStream xmlData) {
    File htmlOutput = null;
    try {
    // Set up the output stream
    htmlOutput = File.createTempFile("wdts-result", ".html");
    htmlOutput.deleteOnExit();

    BufferedOutputStream bos;
    bos = new BufferedOutputStream(new FileOutputStream(htmlOutput));

    //StreamResult result = new StreamResult(System.out);
    StreamResult result = new StreamResult(bos);

    // Set up the transformer to process the SAX events
    // generated by the last filter in the chain
    // Set up the input stream
    BufferedInputStream bis = new BufferedInputStream(xmlData);
    InputSource input = new InputSource(bis);

    SAXSource transformSource = new SAXSource(validationFilter3, input);
    validationTransformer.transform(transformSource, result);

    System.gc();
    } catch (IOException ex) {
    Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TransformerException ex) {
    Logger.getLogger(SchematronEngine.class.getName()).log(Level.SEVERE, null, ex);
    }

    } */
}
