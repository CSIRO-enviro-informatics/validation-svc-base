/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.validation;

import java.io.File;
import java.io.InputStream;
import org.springframework.web.servlet.ModelAndView;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yu021
 */
public class ValidationResult {
    String inputMethod;
    String contentFilename;
    String  validationVersion;
    String validationType;
    String xmltext;
    String catalogs;
    String contentWDTFVersion;

    String xmlSource;

    String contentValidationReport;
    int numContentValidationMessages;
    int numContentValidationErrors;
    Map<String,List<String>> messages;
    String structureValidationOutput;
    String structureValidationErrors;

    long filesizecap;
    String filesizecapInDecimalFormat;
    long filesize;
    String filesizeInDecimalFormat;

    boolean structureValidationResult;

    boolean isValidWDTFVersion;
    boolean isTextInput;
    boolean isFileSelected;
    boolean withinUploadFilesizeCap;
    boolean contentValidationResult;
    boolean schematronError;


    //input validation stuff
    String xmlDataErrorMsg;
    boolean validXmlData;

    File contentValidationFile;

    InputStream validationResultStream;


    public ValidationResult() {
        inputMethod = null;
        isTextInput = false;
        isFileSelected = false;
        withinUploadFilesizeCap = false;
        contentFilename = null;
        validationVersion = null;
        validationType = null;
        xmltext = null;
        catalogs = null;
        contentWDTFVersion = null;
        isValidWDTFVersion = false;
        this.validXmlData = false;
        this.xmlDataErrorMsg = null;

        this.messages = null;

        numContentValidationMessages = 0;
        numContentValidationErrors = 0;

        xmlSource = null;

        contentValidationResult = false;
        schematronError = false;
        contentValidationReport = null;

        structureValidationOutput = null;
        structureValidationErrors = null;
        structureValidationResult = false;
        contentValidationFile = null;

        filesizecapInDecimalFormat=null;
        filesizeInDecimalFormat=null;
        validationResultStream=null;
    }

    public ModelAndView getModelAndView(ModelAndView mv) {
        //ModelAndView mv = new ModelAndView();

        //process strings
        mv.addObject("inputMethod",this.inputMethod);
        mv.addObject("contentFilename",this.contentFilename);
        mv.addObject("validationVersion",this.validationVersion);
        mv.addObject("validationType",this.validationType);
        mv.addObject("catalogs",this.catalogs);
        mv.addObject("contentWDTFVersion",this.contentWDTFVersion);
        mv.addObject("contentValidationReport",this.contentValidationReport);
        mv.addObject("structureValidationOutput",this.structureValidationOutput);
        mv.addObject("structureValidationErrors",this.structureValidationErrors);
        mv.addObject("xmlDataErrorMsg",this.xmlDataErrorMsg);


        //process booleans
        //mv.addObject("isValidWDTFVersion",this.isValidWDTFVersion);
        //mv.addObject("isTextInput",this.isTextInput);
        //mv.addObject("isFileSelected",this.isFileSelected);
        //mv.addObject("withinUploadFilesizeCap",this.withinUploadFilesizeCap);
        mv.addObject("structureValidationResult",this.structureValidationResult);
        mv.addObject("contentValidationResult",this.contentValidationResult);
        mv.addObject("schematronError",this.schematronError);
        mv.addObject("isValidXmlData",this.validXmlData);

        //process other
        //mv.addObject("filesizecap",this.filesizecap);
        //mv.addObject("filesizecapInDecimalFormat",this.filesizecapInDecimalFormat);
        mv.addObject("filesize",this.filesize);
        mv.addObject("filesizeInDecimalFormat",this.filesizeInDecimalFormat);

        mv.addObject("contentValidationFile",this.contentValidationFile);

        mv.addObject("validationResultStream",this.validationResultStream);
        mv.addObject("numContentValidationMessages",this.numContentValidationMessages);
        mv.addObject("numContentValidationErrors",this.numContentValidationErrors);

        mv.addObject("messages",this.messages);
        //mv.addObject("",this.);

        return mv;
    }

    public Map<String, List<String>> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, List<String>> messages) {
        this.messages = messages;
    }


    public String getXmlDataErrorMsg() {
        return xmlDataErrorMsg;
    }

    public void setXmlDataErrorMsg(String xmlDataErrorMsg) {
        this.xmlDataErrorMsg = xmlDataErrorMsg;
    }

    public String getFilesizeInDecimalFormat() {
        return filesizeInDecimalFormat;
    }

    public void setFilesizeInDecimalFormat(String filesizeInDecimalFormat) {
        this.filesizeInDecimalFormat = filesizeInDecimalFormat;
    }

    public String getFilesizecapInDecimalFormat() {
        return filesizecapInDecimalFormat;
    }

    public void setFilesizecapInDecimalFormat(String filesizecapInDecimalFormat) {
        this.filesizecapInDecimalFormat = filesizecapInDecimalFormat;
    }

    public int getNumContentValidationErrors() {
        return numContentValidationErrors;
    }

    public void setNumContentValidationErrors(int numContentValidationErrors) {
        this.numContentValidationErrors = numContentValidationErrors;
    }

    public int getNumContentValidationMessages() {
        return numContentValidationMessages;
    }

    public void setNumContentValidationMessages(int numContentValidationMessages) {
        this.numContentValidationMessages = numContentValidationMessages;
    }




    public boolean isValidXmlData() {
        return validXmlData;
    }

    public void setValidXmlData(boolean validXmlData) {
        this.validXmlData = validXmlData;
    }

    public File getContentValidationFile() {
        return contentValidationFile;
    }

    public void setContentValidationFile(File contentValidationFile) {
        this.contentValidationFile = contentValidationFile;
    }

    public long getFilesizecap() {
        return filesizecap;
    }

    public void setFilesizecap(long filesizecap) {
        this.filesizecap = filesizecap;
        DecimalFormat df = new DecimalFormat("##,###,###");
        this.filesizecapInDecimalFormat = df.format(filesizecap);
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
        DecimalFormat df = new DecimalFormat("###,###,###");
        this.filesizeInDecimalFormat = df.format(filesize);
    }

    public String getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(String catalogs) {
        this.catalogs = catalogs;
    }

    public String getContentFilename() {
        return contentFilename;
    }

    public void setContentFilename(String contentFilename) {
        this.contentFilename = contentFilename;
    }

    public String getContentValidationReport() {
        return contentValidationReport;
    }

    public void setContentValidationReport(String contentValidationReport) {
        this.contentValidationReport = contentValidationReport;
    }

    public boolean isContentValidationResult() {
        return contentValidationResult;
    }

    public void setContentValidationResult(boolean contentValidationResult) {
        this.contentValidationResult = contentValidationResult;
    }

    public String getContentWDTFVersion() {
        return contentWDTFVersion;
    }

    public void setContentWDTFVersion(String contentWDTFVersion) {
        this.contentWDTFVersion = contentWDTFVersion;
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(String inputMethod) {
        this.inputMethod = inputMethod;
    }

    public boolean isIsFileSelected() {
        return isFileSelected;
    }

    public void setIsFileSelected(boolean isFileSelected) {
        this.isFileSelected = isFileSelected;
    }

    public boolean isIsTextInput() {
        return isTextInput;
    }

    public void setIsTextInput(boolean isTextInput) {
        this.isTextInput = isTextInput;
    }

    public boolean isIsValidWDTFVersion() {
        return isValidWDTFVersion;
    }

    public void setIsValidWDTFVersion(boolean isValidWDTFVersion) {
        this.isValidWDTFVersion = isValidWDTFVersion;
    }

    public boolean isSchematronError() {
        return schematronError;
    }

    public void setSchematronError(boolean schematronError) {
        this.schematronError = schematronError;
    }

    public String getStructureValidationErrors() {
        return structureValidationErrors;
    }

    public void setStructureValidationErrors(String structureValidationErrors) {
        this.structureValidationErrors = structureValidationErrors;
    }

    public String getStructureValidationOutput() {
        return structureValidationOutput;
    }

    public void setStructureValidationOutput(String structureValidationOutput) {
        this.structureValidationOutput = structureValidationOutput;
    }

    public boolean getStructureValidationResult() {
        return structureValidationResult;
    }

    public void setStructureValidationResult(boolean structureValidationResult) {
        this.structureValidationResult = structureValidationResult;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getValidationVersion() {
        return validationVersion;
    }

    public void setValidationVersion(String validationVersion) {
        this.validationVersion = validationVersion;
    }

    public boolean isWithinUploadFilesizeCap() {
        return withinUploadFilesizeCap;
    }

    public void setWithinUploadFilesizeCap(boolean withinUploadFilesizeCap) {
        this.withinUploadFilesizeCap = withinUploadFilesizeCap;
    }

    public String getXmlSource() {
        return xmlSource;
    }

    public void setXmlSource(String xmlSource) {
        this.xmlSource = xmlSource;
    }

    public String getXmltext() {
        return xmltext;
    }

    public void setXmltext(String xmltext) {
        this.xmltext = xmltext;
    }

    public InputStream getValidationResultStream() {
        return validationResultStream;
    }

    public void setValidationResultStream(InputStream validationResultStream) {
        this.validationResultStream = validationResultStream;
    }


}
