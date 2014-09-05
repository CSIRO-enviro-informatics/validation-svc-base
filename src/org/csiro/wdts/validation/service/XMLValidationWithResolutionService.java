/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.util.XMLCatalogResolver;
import org.csiro.wdts.validation.context.AppContext;
//import org.custommonkey.xmlunit.Validator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author yu021
 */
public class XMLValidationWithResolutionService implements ApplicationContextAware {
    private ApplicationContext applicationContext;


    //validation trace variables
    private boolean xmlParseError;
    private boolean validationResult;
    private String validationDebug;
    private String validationOutput;
    private String validationErrors;
    //XMLReader parser;
    SAXParser parser;
    //SAXErrorHandler defaultHandler;
    CustomSAXHandler customHandler;
    private String SCHEMA =
            "http://apache.org/xml/features/validation/schema";
    private String SCHEMA_FULL_CHECKING =
            "http://apache.org/xml/features/validation/schema-full-checking";
    private String WARN_ON_DUPLICATE_ATTDEF = "http://apache.org/xml/features/validation/warn-on-duplicate-attdef";
    private String LOAD_EXTERNAL_DTD =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    private String VALIDATION = "http://xml.org/sax/features/validation";

    public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}


    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    public XMLValidationWithResolutionService() throws SAXException {
        //init variables
        validationResult = false;
        validationDebug = "";
        validationOutput = "";
        validationErrors = "";
        xmlParseError = false;

        //setup parser
        parser = new SAXParser();
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        //parser.setFeature("http://xml.org/sax/features/use-entity-resolver", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

        //parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);

        //parser.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);


        //parser.setFeature("http://apache.org/xml/features/validation/warn-on-duplicate-attdef", true);
        //parser.setFeature("http://apache.org/xml/features/validation/warn-on-undeclared-elemdef", true);


        // load schema\dtd
        parser.setFeature(LOAD_EXTERNAL_DTD, true);
        // turn on validation
        parser.setFeature(VALIDATION, true);
        parser.setFeature(SCHEMA, true);
        parser.setFeature(SCHEMA_FULL_CHECKING, true);
        //parser.setFeature(WARN_ON_DUPLICATE_ATTDEF, true);

        //SAXErrorHandler handler = new SAXErrorHandler();
        customHandler = new CustomSAXHandler();
        parser.setErrorHandler(customHandler);
    }

    public void setValidationDebug(String validationDebug) {
        this.validationDebug = validationDebug;
    }

    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void setValidationOutput(String validationOutput) {
        this.validationOutput = validationOutput;
    }

    public void setValidationResult(boolean validationResult) {
        this.validationResult = validationResult;
    }

    public String getValidationDebug() {
        return validationDebug;
    }

    public String getValidationErrors() {
        return validationErrors;
    }

    public String getValidationOutput() {
        return validationOutput;
    }

    public boolean isValidationResult() {
        return validationResult;
    }

    public boolean isXmlParseError() {
        return xmlParseError;
    }

    

    public void resetValidationVariables() {
        setValidationResult(false);
        setValidationDebug("");
        setValidationErrors("");
        setValidationOutput("");
    }

    public boolean validateXML(String xmltext, String[] catalogs) {
        InputSource input = new InputSource(new StringReader(xmltext));
        return doValidation(input, catalogs);
    }

    public boolean validateXML(String xmltext) {
        InputSource input = new InputSource(new StringReader(xmltext));

        //hardcode default catalogs file - assume localcatalog.xml
        //String[] arrCatalogFiles= new String[1];
        //arrCatalogFiles[0] = "catalogs/localcatalog.xml";

        return doValidation(input, null);
    }

    public boolean validateXML(byte[] bytes, String[] catalogs) {
        InputSource input = new InputSource(new ByteArrayInputStream(bytes));

        return doValidation(input, catalogs);
    }

    public boolean validateXML(File file, String[] catalogs) throws FileNotFoundException {
        InputSource input = new InputSource(new FileReader(file));

        return doValidation(input, catalogs);
    }

    public boolean validateXML(InputStream stream, String[] catalogs) throws FileNotFoundException {
        InputSource input = new InputSource(new InputStreamReader(stream));

        return doValidation(input, catalogs);
    }

    private boolean doValidation(InputSource input, String[] catalogs) {
        StringBuffer sb = new StringBuffer();
        String line = "";

        resetValidationVariables();

        try {

            customHandler = new CustomSAXHandler();
            parser.setErrorHandler(customHandler);


            if (catalogs == null) {
                
                ApplicationObjectSupport aos = new ApplicationObjectSupport() {};

                /* ApplicationContext ctx = aos.getApplicationContext();
                if(ctx == null) {
                    this.validationDebug += "\nctx context is null\n";
                }
                */

                ApplicationContext ctx = AppContext.getApplicationContext();
                if(ctx == null) {
                    this.validationDebug += "\nctx2 context is null\n";
                }
                else {
                    this.setApplicationContext(ctx);
                }
                
                
                this.validationDebug += "\nWorking dir: " + System.getProperty("user.dir");
                if(this.getApplicationContext() == null) {
                    this.validationDebug += "\napp context is null\n";
                }
                this.validationDebug += "\nApp context: " + this.getApplicationContext().getDisplayName();


                String debug = "";
                
                //ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
                String[] temp = new String[1];
                String fname = "localcatalog.xml";
                
                Resource r = this.getApplicationContext().getResource(fname);
                debug = debug + "filename: " + fname + "; ";
                debug = debug + "resource: " + r.exists() + "\n";

                

                if (r.exists() == true) {
                    //catalogs[i] = checkResource.getFilename();
                    //catalogs[i] = "file:///" + reverseSlashes( checkResource.getFile().toString() );
                    temp[0] = r.getFile().toString().replace(" ", "%20");
                    debug = debug + "setting resource : " + temp[0] + "\n";
                }

                catalogs = temp;
                this.validationDebug += debug;
            }
            //parsing the file with validation turned on no content handler needed
                XMLCatalogResolver catalogResolver = new XMLCatalogResolver(catalogs, true);
                catalogResolver.setPreferPublic(false);

                parser.setEntityResolver(catalogResolver);
            



            parser.parse(input);
        //Validator.validate(parser.);
        } catch (SAXParseException spe) {
            sb.append("threw SAXParseException! bad XML input");
            sb.append("\n");
            sb.append("\n");
            //sb.append("trace: " + se.getMessage());
            //sb.append("\n");
            //sb.append("\n");
            sb.append(this.customHandler.getMessages());
            sb.append("\n");
            sb.append("LOG: " + this.customHandler.getNumberOfErrors() + " error(s)");
            validationDebug += sb.toString();

        } catch (SAXException se) {
            sb.append("threw SAXException!");
            sb.append("\n");
            sb.append("\n");
            //sb.append("trace: " + se.getMessage());
            //sb.append("\n");
            //sb.append("\n");
            sb.append(this.customHandler.getMessages());
            sb.append("\n");
            sb.append("LOG: " + this.customHandler.getNumberOfErrors() + " error(s)");
            validationDebug += sb.toString();
            xmlParseError = true;
        } catch (Exception ioe) {
            sb.append("threw  Exception!");
            sb.append("\n");
            sb.append("\n");
            sb.append("trace: " + ioe.getMessage());
            sb.append("\n");
            validationDebug += sb.toString();
        }

        System.out.println("finish validation function... returning");
        String errorBuffer = this.customHandler.getMessages();

        validationDebug += errorBuffer;

        this.validationErrors = this.formatErrors(errorBuffer);

        if ((this.customHandler.getNumberOfErrors() <= 0) && (!xmlParseError)) {
            return true;
        }

        return false;
    }

    private String formatErrors(String errors) {
        String formatted = "";

        String arrBuf[] = errors.split("\n");
        String curr = "";

        int errorCount = 0;
        for (int i = 0; i < arrBuf.length; i++) {
            curr = arrBuf[i];

            if (curr.contains("ERROR: ")) {
                curr = curr.replace("ERROR: ", "(Error) ");
                formatted += curr + "\n";
            } else if (curr.contains("FATAL: ")) {
                curr = curr.replace("FATAL: ", "(Fatal error) ");
                formatted += curr + "\n";
            } else if (curr.contains("WARNING: ")) {
                curr = curr.replace("WARNING: ", "(Warning) ");
                formatted += curr + "\n";
            }
        }

        return formatted;
    }

}


