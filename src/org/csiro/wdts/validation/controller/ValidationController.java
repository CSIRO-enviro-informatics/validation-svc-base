/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.csiro.wdts.validation.ValidationInputException;
import org.csiro.wdts.validation.ValidationResult;
import org.csiro.wdts.validation.input.ValidationFormInput;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.csiro.wdts.validation.schematron.SchematronEngine;
import org.csiro.wdts.validation.service.XMLValidationWithResolutionService;
import org.csiro.wdts.validation.utils.SvrlParser;
import org.csiro.wdts.validation.utils.URLChecker;
import org.csiro.wdts.validation.utils.UnicodeBOMInputStream;
import org.csiro.wdts.validation.utils.VersionParser;

/** 
 *
 * @author yu021
 *
 * Combines calls to both XML Validation and Schematron Validation services
 * 
 */
public class ValidationController extends SimpleFormController {
    protected XMLValidationWithResolutionService xmlValidationWithResolutionService;
    public  String debug;
    public String validationType;
    public String validationVersion;
    public int loglevel; //0 = everything, 1 = minor logging , 2 = major logs, 3 = significant logs

    public void setLoglevel(int loglevel) {
        this.loglevel = loglevel;
    }

    public int getLoglevel() {
        return this.loglevel;
    }


    public XMLValidationWithResolutionService getXmlValidationWithResolutionService() {
        return xmlValidationWithResolutionService;
    }

    public void setXmlValidationWithResolutionService(XMLValidationWithResolutionService xmlValidationWithResolutionService) {
        this.xmlValidationWithResolutionService = xmlValidationWithResolutionService;
    }

    public ValidationController() {
       this.debug = "";
       this.validationType = "";
       this.validationVersion = "";
       loglevel = 2;
    }

    public void outprintln(String message) {
        this.outprint(message + "\n");
    }
    public void errprintln(String message) {
        this.errprint(message + "\n");
    }
    public void outprint(String message) {
        System.out.println(message);
    }
    public void errprint(String message) {
        System.err.println(message);
    }

    public void logOutput(String message, int level) {
        if(this.loglevel <= level) {
            String outmsg = "in Validation (" + this.getDateTime() + "): " +message;

            this.outprintln(outmsg);
            debug += outmsg + "\n";

        }
    }
    public void logError(String message, int level) {
        if(this.loglevel <= level) {
            String errmsg = "in Validation (" + this.getDateTime() + "): " +message;

            this.errprintln(errmsg);
            debug += errmsg + "\n";
        }
    }

    public String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public InputStream getCleanInputStream(InputStream is) throws NullPointerException, IOException {
        UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(is);

        //System.out.println("detected BOM: " + ubis.getBOM());
        this.logOutput("detected BOM: " + ubis.getBOM(), 2);

        ubis.skipBOM();

        //ubis.close();
        //fis.close();

        return ubis;
    }


    

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
//    protected ModelAndView onSubmit(HttpServletRequest request,HttpServletResponse response, Object command, BindException errors)     throws Exception {
        ModelAndView mv;
        ValidationResult vr = new ValidationResult(); //container object to populate info to return to view
        ValidationFormInput bean = (ValidationFormInput) command;
        InputStream fileInputStream = null;

        debug = "";
        chooseOutputMethod(bean);
        mv = new ModelAndView(getSuccessView());//init mv with the right view

        try {
            //handles:
            //- populating various info into bean
            //- checking of valid xml input
            //- checking of valid version input
            //- catalog selection (which determines the version selection...)
            this.performPreProcessingSteps(bean, vr);
        } catch (ValidationInputException vie) {
            vr.setValidXmlData(false);
            vr.setXmlDataErrorMsg(vie.getMessage());

            System.err.println("Caught ValidationInputException: " + vie.getMessage());
            //get the model view info required and return control
            mv = vr.getModelAndView(mv);
            return mv;
        }

        vr.setValidXmlData(true);


        //handle structural validation
        this.performValidationSteps(bean, vr);


        //return model and view object for presentation
        mv = vr.getModelAndView(mv);

        return mv;
    } // end onSubmit


    public void performValidationSteps(ValidationFormInput bean, ValidationResult vr)
            throws IOException {
        
        this.performStructuralValidation(bean, vr);

        this.performContentValidation(bean, vr);

    }

    public boolean performStructuralValidation(ValidationFormInput bean, ValidationResult vr) throws IOException {
        boolean structuralValidationResult = false;
        InputStream fileInputStream = null;

        if(bean.getInputMethod().equals("file")) {
            this.logOutput("Preparing to perform structure validation via file", 2);
            fileInputStream = bean.getFile().getInputStream();
            InputStream cleanedInputStream = this.getCleanInputStream(fileInputStream);
            structuralValidationResult = this.handleStructuralValidation(cleanedInputStream, bean.getCatalogs(), vr);
            fileInputStream.close();
            fileInputStream = null;
            cleanedInputStream.close();
            cleanedInputStream = null;
        }
        else  {
            structuralValidationResult = this.handleStructuralValidation(bean.getXmltext(), bean.getCatalogs(), vr);
        }

        return structuralValidationResult;
    }

    public boolean performContentValidation(ValidationFormInput bean, ValidationResult vr) throws IOException {
        String ruleLocation = "";
        InputStream fileInputStream = null;
        //give the right rules file to process internally according to version being validated
        ruleLocation = getRuleLocation(bean);

        boolean contentValidationResult = false;
        if (bean.getInputMethod().equals("file")) {
            fileInputStream = bean.getFile().getInputStream();
            InputStream cleanedInputStream = this.getCleanInputStream(fileInputStream);
            contentValidationResult = this.handleContentValidation(vr, cleanedInputStream, ruleLocation);

            fileInputStream.close();
            fileInputStream = null;
            cleanedInputStream.close();
            cleanedInputStream = null;
        } else {
            contentValidationResult = this.handleContentValidation(vr, bean.getXmltext(), ruleLocation);
        }

        return contentValidationResult;
    }
    

    public boolean performPreProcessingSteps(ValidationFormInput bean, ValidationResult vr) throws IOException, ValidationInputException {
        boolean step1, step2, step3;

        step1 =  this.checkIsValidXmlInput(bean, vr);
        //actually resolve catalogs to point to right one
        bean.setCatalogs(this.resolveCatalogPaths(bean.getCatalogs()));

        step2 = this.checkValidInputVersion(bean, vr);

        
        return (step1 && step2);
    }

    public boolean checkIsValidXmlInput(ValidationFormInput bean, ValidationResult vr) throws ValidationInputException {
        vr.setInputMethod(bean.getInputMethod());
        // make either file is slected or the text is input
        if (bean.getInputMethod().contains("text")) {
            boolean checkResultTextInput = checkIsTextInput(bean);
            vr.setIsTextInput(checkResultTextInput);


        } else { // file input
            vr.setIsFileSelected(checkIsFileSelected(bean));
        }

        String catalogString = "";
        catalogString = getCatalogString(bean);
        vr.setCatalogs(catalogString);

        this.validationVersion = bean.getVersion();
        this.validationType = bean.getValidationType();

        vr.setContentFilename(bean.getFilename());
        vr.setValidationVersion(validationVersion);
        vr.setValidationType(validationType);
        vr.setWithinUploadFilesizeCap(checkUploadFileSizeLimit(vr, bean));


        this.logOutput("Checking if the input data is processable", 2);

        boolean validXmlData = true;
        String xmlDataError = "";

        //if input validation fails return control
        if ((vr.getInputMethod().contains("text") && !vr.isIsTextInput())) {
            validXmlData = false;
            xmlDataError = "No text has been input. Please input text to validate.";
        }
        else if (vr.getInputMethod().contains("file") && !vr.isIsFileSelected())  {
            validXmlData = false;
            xmlDataError = "No file has been selected. Please select file to validate.";

        }
        else if(!vr.isWithinUploadFilesizeCap()) {
            validXmlData = false;
            xmlDataError = "Upload filesize exceeds " + vr.getFilesizecapInDecimalFormat() + " bytes limit. Please try another file within that limit.";
        }


        if(!validXmlData) {
            this.logOutput("Input data is not processable! Returning control...", 2);
            vr.setValidXmlData(validXmlData);
            vr.setXmlDataErrorMsg(xmlDataError);
            //get the model view info required and return control
            //mv = vr.getModelAndView(mv);
            //mv.addObject("debug",this.debug);
        }

        
        return validXmlData;
    }

    public boolean checkValidInputVersion(ValidationFormInput bean, ValidationResult vr) throws IOException, ValidationInputException  {

        // check if file content's version matches with that of chosen validation version
        boolean validVersion = false;

        //if either sax exception or invalid version - treat as an input data validation error
        validVersion = true;

        return validVersion;
        
    }


    protected boolean checkIsFileSelected(ValidationFormInput bean) {
        /* Make sure file is selected! */
        if (bean.getFilename().length() != 0) {
            //mv.addObject("isFileSelected", Boolean.TRUE);
            return true;
        } else {
            return false;
        }
    }
    protected boolean checkIsTextInput(ValidationFormInput bean) {
        /* Make Text is input! */
        if (bean.isXmltextEmpty()) {
            //mv.addObject("isTextInput", Boolean.FALSE);
            return false;
        //return mv;
        } else {
            //mv.addObject("isTextInput", Boolean.TRUE);
            return true;
        }
    }

    protected boolean checkUploadFileSizeLimit(ValidationResult vr, ValidationFormInput bean) {
        // checking upload.filesize.cap
        Properties p = (Properties) this.getApplicationContext().getBean("serverConfig");
        long filesizecap = Long.parseLong(p.getProperty("upload.filesize.cap"));
        vr.setFilesizecap(filesizecap);
        long filesize = bean.getFilesize();
        vr.setFilesize(filesize);

        if (filesize > filesizecap) {
            return false;
        } else {
            return true;
        }
    }

    

    public void chooseOutputMethod(ValidationFormInput bean) {
        if (bean.getOutputMethod().contains("html")) {
            setSuccessView("resultView");
        } else if (bean.getOutputMethod().contains("xml")) {
            setSuccessView("xmlView");
        } else {
            setSuccessView("xmlView");
        }
    }

    protected String getCatalogString(ValidationFormInput bean) {
        String catalogString = null;
        String[] catalogs = bean.getCatalogs();
        for (int i = 0; i < catalogs.length; i++) {
            if (i == 0) {
                catalogString = catalogs[i];
            } else {
                catalogString = catalogString + ", " + catalogs[i];
            }
        }
        return catalogString;
    }

    public String getRuleLocation(ValidationFormInput bean) {
        String ruleLocation = null;

        return ruleLocation;
    }

    protected String[] resolveCatalogPaths(String catalogs[]) throws IOException {
        //resolve file paths
        //Resource r = rl.getResource(fname);
        String fname = catalogs[0];
        Resource r = this.getApplicationContext().getResource(fname);
        debug = debug + "filename: " + fname + "; ";
        debug = debug + "resource: " + r.exists() + "\n";

        Resource checkResource;
        for (int i = 0; i < catalogs.length; i++) {
            checkResource = this.getApplicationContext().getResource(catalogs[i]);
            debug = debug + "filename: " + catalogs[i] + "\n";
            debug = debug + "resource exists: " + checkResource.exists() + "\n";

            if (checkResource.exists() == true) {
                //catalogs[i] = checkResource.getFilename();
                //catalogs[i] = "file:///" + reverseSlashes( checkResource.getFile().toString() );
                catalogs[i] = checkResource.getFile().toString().replace(" ", "%20");

                debug = debug + "setting resource " + i + ": " + catalogs[i] + "\n";
            }
        }

        return catalogs;

    }

    public boolean handleStructuralValidation(String strXml, String[] catalogs, ValidationResult vr) throws FileNotFoundException, IOException {
        InputStream is = null;
        InputStream cleanedInputStream = null;
        try {
            is = new ByteArrayInputStream(strXml.getBytes("UTF-8"));
            cleanedInputStream = this.getCleanInputStream(is);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        boolean result = handleStructuralValidation(is, catalogs, vr);
        if(is != null) {
            is.close();
            is = null;
    }

        if(cleanedInputStream != null) {
            cleanedInputStream.close();
            cleanedInputStream = null;
        }
        return result;
    }

    public boolean handleStructuralValidation(InputStream stream, String[] catalogs, ValidationResult vr) throws FileNotFoundException {
        boolean validationResult;
        String validationDebug = "", validationErrors, validationOutput = "";

        validationErrors = null;
        validationResult = xmlValidationWithResolutionService.validateXML(stream, catalogs);
        validationDebug = xmlValidationWithResolutionService.getValidationDebug();
        validationOutput = xmlValidationWithResolutionService.getValidationOutput();

        debug += "Validation result: " + validationResult + "\n";
        debug += "Validation debug: " + validationDebug + "\n";
        debug += "Validation output: " + validationOutput + "\n\n";

        if (validationResult == false) {
            validationErrors = xmlValidationWithResolutionService.getValidationErrors();
        }

        vr.setStructureValidationOutput(validationOutput);
        vr.setStructureValidationErrors(validationErrors);
        vr.setStructureValidationResult(validationResult);

        return validationResult;
    }

    /* Perform Content Validation via Schematron */
    public boolean handleContentValidation(ValidationResult vr, String strXml, String ruleLocation) throws IOException {
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(strXml.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        boolean result = handleContentValidation(vr, is, ruleLocation);

        if(is != null) {
            is.close();
            is = null;
    }

        return result;
    }

    public boolean handleContentValidation(ValidationResult vr, InputStream stream, String ruleLocation) throws IOException {
        //check if dependant service URLs reachable
        URLChecker urlchecker = new URLChecker();
        //default properties for hosts...
        String host1 = "http://localhost:8080/VocabLookup";
        String host2 = "http://localhost:8080/vocab-service/client/";

        //get info from properties file
        Properties p = (Properties) this.getApplicationContext().getBean("serverConfig");

        host1 = p.getProperty("vocablookup.location");
        host2 = p.getProperty("vocab-service.location") + "/client";

        boolean connectable = (urlchecker.checkUrl(host1) && urlchecker.checkUrl(host2));
        //boolean connectable = (urlchecker.checkUrl(host1));
        boolean didSchematronInit = false;

        String schematronError = "";


        if (!connectable) {
            schematronError += "Schematron could not init: dependant services not available\n";

            vr.setContentValidationResult(false);
            vr.setSchematronError(true);
            vr.setContentValidationReport(schematronError);
        } else { //all dependable services and rules are available
            File compiledRulesFile;
            SchematronEngine schEngine;
            boolean schValidationSuccess = false;
            File resultFile = null, svrlFile = null;
            Map<String,List<String>> messages = null;

            try {
                //get compiled rules file
                Resource rulesResource = this.getApplicationContext().getResource(ruleLocation);
                compiledRulesFile = rulesResource.getFile();
                schEngine = new SchematronEngine(this.getApplicationContext(), compiledRulesFile);

                //the following controls which output gets generated
                svrlFile = schEngine.validateSch2svrl(stream);
                //resultFile = schEngine.validateSch2text(stream);

                /*
                 * Commenting previous XSLT based parsing of SVRL
                resultFile = schEngine.svrl2text(svrlFile);

                if (resultFile == null) {
                    schematronError += "Schematron validation failed: result was null\n";
                    schValidationSuccess = false;

                } else {
                    schValidationSuccess = true;
                }
                 */

                debug = debug + org.apache.commons.io.FileUtils.readFileToString(svrlFile);
                messages = this.parseSvrlMessages(svrlFile);
                schValidationSuccess = true;


                schEngine.cleanup();
                schEngine = null;
            } catch (SAXException ex) {
                schematronError += "Schematron validation failed: SAX Exception";
                schValidationSuccess = false;
            } catch (ParserConfigurationException ex) {
                schematronError += "Schematron validation failed: ParserConfig Exception";
                schValidationSuccess = false;
            } catch (TransformerConfigurationException ex) {
                schematronError += "Schematron validation failed: TransformerConfig Exception";
                schValidationSuccess = false;
            } catch (TransformerException ex) {
                schematronError += "Schematron validation failed: Transformer Exception " + ex.getMessageAndLocation();
                schValidationSuccess = false;

            } catch (FileNotFoundException ex) {
                schematronError += "Schematron validation failed: FileNotFound Exception";
                schValidationSuccess = false;
            }

            if(messages == null) {
               schematronError += "Schematron validation failed: message list was null\n";
               schValidationSuccess = false;
            }
            else {
               schValidationSuccess = true;
            }

            //report any errors from the call
            if (schValidationSuccess == false) {
                //mv.addObject("contentValidationResult", false);
                //mv.addObject("schematronError", true);
                //mv.addObject("contentValidationReport", schematronError);
                vr.setContentValidationResult(false);
                vr.setSchematronError(true);
                vr.setContentValidationReport(schematronError);
            } else {
                //validation went ok - so get the reports
                /*debug = debug + schematronValidation.getSvrlReport();

                debug = debug + "\n";
                debug = debug + "------------------------------------\n";
                debug = debug + "\n";

                debug = debug + schematronValidation.getTextReport();

                debug = debug + "\n";
                debug = debug + "------------------------------------\n";
                debug = debug + "\n";

                debug = debug + schematronValidation.getHtmlReport();

                debug = debug + "\n";
                debug = debug + "------------------------------------\n";
                debug = debug + "\n";
                 */

                /**
                 * Commenting out the XSLT based processing procedures


                boolean isContentValid = false;
                //SchematronValidation schematronValidation = new SchematronValidation();
                //isContentValid = schematronValidation.interpretReport(svrlFile);

                if(resultFile.length() > 0)
                    isContentValid = false;
                else
                    isContentValid = true;

                debug = debug + isContentValid;

                //mv.addObject("contentValidationResult", isContentValid);
                //mv.addObject("contentValidationDebug", debug);
                //mv.addObject("schematronError", false);
                //mv.addObject("contentValidationReport", schematronValidation.getTextReport());


                vr.setContentValidationResult(isContentValid);
                vr.setSchematronError(false);
                //vr.setContentValidationReport(schematronValidation.getTextReport());
                //FileUtils fileutils = new FileUtils();

                if(resultFile.length() > 500000) { //limit the size of string to return to view
                    vr.setContentValidationFile(resultFile);
                }else {
                    vr.setContentValidationFile(resultFile);
                    //vr.setContentValidationReport(fileutils.readFile(resultFile));
                }
                */

                /* Process messages */
                vr.setMessages(messages);
                vr.setNumContentValidationMessages(this.countItemsInMessagesIndex(messages));
                vr.setNumContentValidationErrors(this.countErrorsInMessagesIndex(messages));

                if(vr.getNumContentValidationErrors() <= 0) {
                    vr.setContentValidationResult(true);
                    vr.setSchematronError(false);
                }
                else {
                    vr.setContentValidationResult(false);
                }

                return true;
            }
        }

        return false;
    }

    protected int countItemsInMessagesIndex(Map<String,List<String>> messages) {
        int count = 0;

        for(String key : messages.keySet()) {
            for(String item : messages.get(key)) {
                if(item != null) {
                    count++;
                }
            }
        }

        return count;
    }

    protected int countErrorsInMessagesIndex(Map<String,List<String>> messages) {
        int count = 0;

        for(String key : messages.keySet()) {

            if(key.toLowerCase().equals("error") || key.toLowerCase().equals("fatal")) {
                for(String item : messages.get(key)) {
                    if(item != null) {
                        count++;
                    }
                }
            }

        }

        return count;
    }


    protected Map<String,List<String>> parseSvrlMessages(File f) throws SAXException, IOException {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        SvrlParser handler = new SvrlParser();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        xr.parse(new InputSource(new FileReader(f)));

        //get the right input source

        return handler.getMessages();
    }

    protected boolean handleDependantSvcCheck() {
        //check if dependant service URLs reachable
        URLChecker urlchecker = new URLChecker();
        //default properties for hosts...
        String host1 = "http://localhost:8080/VocabLookup";
        String host2 = "http://localhost:8080/vocab-service/client/";

        //get info from properties file
        Properties p = (Properties) this.getApplicationContext().getBean("serverConfig");

        host1 = p.getProperty("vocablookup.location");
        host2 = p.getProperty("vocab-service.location") + "/client";

        boolean connectable = (urlchecker.checkUrl(host1) && urlchecker.checkUrl(host2));

        return connectable;
    }

    protected InputStream getContentRulesStream() {
        PipedInputStream pis = null;

        return pis;
    }

    protected SchematronEngine initialiseSchematronEngine(String ruleLocation) throws IOException, FileNotFoundException, SAXException, ParserConfigurationException, TransformerConfigurationException {
        FileInputStream compiledRulesStream;

        SchematronEngine schEngine;
        Resource rulesResource;//get compiled rules file
        rulesResource = this.getApplicationContext().getResource(ruleLocation);
        compiledRulesStream = new FileInputStream(rulesResource.getFile());

        schEngine = new SchematronEngine(this.getApplicationContext(), compiledRulesStream);
        return schEngine;
    }


    protected boolean handleContentValidation_piped(ValidationResult vr, InputStream stream, String ruleLocation) throws IOException {
        boolean didSchematronInit = false;
        String schematronError = "";

        PipedInputStream  svrlInPipe = null, svrlResultPipe = null, textResultPipe = null;

        //check if dependant service URLs reachable
        boolean connectable = this.handleDependantSvcCheck();

        if (!connectable) {
            schematronError += "Schematron could not init: dependant vocabulary service is unavailable\n";

            vr.setContentValidationResult(false);
            vr.setSchematronError(true);
            vr.setContentValidationReport(schematronError);
        } else { //all dependable services and rules are available
            SchematronEngine schEngine;
            boolean schValidationSuccess = false;
            
            try {
                schEngine = this.initialiseSchematronEngine(ruleLocation);
                
                //the following controls which output gets generated
                svrlResultPipe = schEngine.validateSch2svrl_piped(stream);
                textResultPipe = schEngine.svrl2text_piped(svrlResultPipe);

                if (textResultPipe == null) {
                    schematronError += "Schematron validation failed: result was null\n";
                    schValidationSuccess = false;
                } else {
                    schValidationSuccess = true;
                }
            } catch (SAXException ex) {
                schematronError += "Schematron validation failed: SAX Exception";
                schValidationSuccess = false;
            } catch (ParserConfigurationException ex) {
                schematronError += "Schematron validation failed: ParserConfig Exception";
                schValidationSuccess = false;
            } catch (TransformerConfigurationException ex) {
                schematronError += "Schematron validation failed: TransformerConfig Exception";
                schValidationSuccess = false;
            } catch (TransformerException ex) {
                schematronError += "Schematron validation failed: Transformer Exception " + ex.getMessageAndLocation();
                schValidationSuccess = false;

            } catch (FileNotFoundException ex) {
                schematronError += "Schematron validation failed: FileNotFound Exception";
                schValidationSuccess = false;
            }

            //report any errors from the call
            if (schValidationSuccess == false) {
                vr.setContentValidationResult(false);
                vr.setSchematronError(true);
                vr.setContentValidationReport(schematronError);
            } else {
                boolean isContentValid = false;

                if(false) // check content
                    isContentValid = false;
                else
                    isContentValid = true;

                debug = debug + isContentValid;

                //read the content into stream/string
                vr.setContentValidationResult(isContentValid);
                vr.setSchematronError(false);
                
                vr.setValidationResultStream(textResultPipe);
                
                return true;
            }
        }

        return false;
    }

    public String outputStreamToString(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        //print the resulting
        BufferedInputStream bis = new BufferedInputStream(in);
        int c;
        while((c = bis.read()) > 0) {
            char cc = (char) c;
            sb.append(cc);
            //System.out.print(cc);
        }

        return sb.toString();

    }

       /*
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
            throws ServletException {
        // to actually be able to convert Multipart instance to byte[]
        // we have to register a custom editor
        //binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
        //binder.registerCustomEditor(File.class, new FileEditor());
    // now Spring knows how to handle multipart object and convert them
    }
*/
    protected String reverseSlashes(String str) {
        String result = "";

        result = str.replace("\\", "/");

        return result;
    }


    // Process all files and directories under dir
    public String visitAllDirsAndFiles(File dir) {
        String result = "";

        if (dir.getName().endsWith(".xsd")) {
            result = dir.getPath();
            result = result + "\n";
        }

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                result = result + visitAllDirsAndFiles(new File(dir, children[i]));

            }
        }

        return result;
    }
}