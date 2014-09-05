/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.validation.schematron;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import org.csiro.wdts.xsl.XSLTransform;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.csiro.wdts.validation.service.SVRLHandler;
import org.csiro.wdts.validation.utils.FileUtils;

/**
 *
 * @author yu021
 */
public class SchematronValidator {
    String expandInclusionsXsl = "iso-schematron-xslt2/iso_dsdl_include.xsl";
    String expandAbstractXsl = "iso-schematron-xslt2/iso_abstract_expand.xsl";
    String compileSchemaXsl = "iso-schematron-xslt2/iso_svrl_for_xslt2.xsl";
    String svrl2htmlXsl= "format/format-svrl-output-to-html.xsl";
    String svrl2textXsl= "format/format-svrl-output-to-text.xsl";
    String systemId;

    String svrlReport = null;
    String textReport = null;
    String htmlReport = null;

    private ApplicationContext applicationContext;

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getHtmlReport() {
        return htmlReport;
    }

    public String getSvrlReport() {
        return svrlReport;
    }

    public String getTextReport() {
        return textReport;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}


    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private String loadFileResource(String path) {
        Resource r;
        String fileContents = null;

        r = applicationContext.getResource(path);
        try {
            fileContents = file2str(r.getFile());
            System.out.println("File " + path + " exists? " + r.exists());
            //System.out.println("File contents of " + path + " is: " + fileContents);
        } catch (IOException ex) {
            System.err.println("Exception caught");
        }

        return fileContents;
    }


    public String file2str(File f) {
        String contents;
        FileUtils fileUtils = new FileUtils();
        try {
            contents = fileUtils.readFile(f);
        } catch (FileNotFoundException ex) {
            contents = null;
        }

        return contents;
    }

    public String validate(String xmlContent, String schRules) {
        String result = "";

        result = validate(xmlContent, schRules, "#ALL");

        return result;
    }

    public String validate(String xmlContent, String schRules, String phase) {
        FileUtils fileUtils = new FileUtils();

        String result = "", stage1 = "", stage2 = "", stage3 = "";
        String textOutput = "", htmlOutput = "";
        String currResource = "";
        

        
        // expand inclusions in=schRules out=stage1-file xsl=expandInclusion
        currResource = loadFileResource(expandInclusionsXsl);
        if (currResource == null) {
            System.err.println("Resource is NULL: " + expandInclusionsXsl);
        } else {
            stage1 = xslTransformOperation(currResource, schRules, "phase", phase);
            //System.out.println("Stage 1 xsl:\n" + stage1 + "\n\n");
        }

        currResource = null;
        
        
        // expand abstract patterns in=stage1-file out=stage2-file xsl=expandAbstract
        currResource = loadFileResource(expandAbstractXsl);
        if (currResource == null) {
            System.err.println("Resource is NULL: " + expandAbstractXsl);
        } else {
            stage2 = xslTransformOperation(currResource, stage1, "phase", phase);
            //System.out.println("Stage 2 xsl:\n" + stage2 + "\n\n");
        }

        stage1 = null;

        // expand compile in=stage2-file out=stage3-file xsl=compile xsl=compileSchema
        currResource = loadFileResource(compileSchemaXsl);
        if (currResource == null) {
            System.err.println("Resource is NULL: " + compileSchemaXsl);
        } else {
            stage3 = xslTransformOperation(currResource, stage2,  "phase", phase);
        }

        stage2 = null;

        // do validation in=xmlInput out=result.svrl xsl=stage3-file
        result = xslTransformOperation(stage3, xmlContent);
        //System.out.println("SVRL:\n" + result + "\n\n");

        this.svrlReport = result;

        stage3 = null;

	    //format to html xsl="format/format-svrl-output-to-html.xsl" in=result.svrl
/*        currResource = loadFileResource(svrl2htmlXsl);
        if (currResource == null) {
            System.err.println("Resource is NULL: " + svrl2htmlXsl);
        } else {
            htmlOutput = xslTransformOperation(currResource, this.svrlReport);

            this.htmlReport = htmlOutput;
        }

        htmlOutput = null;
*/
        //format to text xsl="format/format-svrl-output-to-text.xsl" in=result.svrl
        currResource = loadFileResource(svrl2textXsl);
        if (currResource == null) {
            System.err.println("Resource is NULL: " + svrl2textXsl);
        } else {
            textOutput = xslTransformOperation(currResource, result);

            this.textReport = textOutput;
        }

        textOutput = null;

        return result;
    }

      //returns number of failed asserts
      public boolean hasFailedAsserts() {
        boolean result = false;

        //count the number of failed asserts

        return result;
    }


    public String xslTransformOperation(String xsl, String xmlContent) {
        String result = "";


        result = xslTransformOperation(xsl, xmlContent, null, null);
        
        return result;
    }
    
    public String xslTransformOperation(String xsl, String xmlContent, String param, Object paramVal)  {
        
        
        
         StreamSource xslSource = new StreamSource(new StringReader(xsl));
        StreamSource xmlSource = new StreamSource(new StringReader(xmlContent));
        String result = "";


        xslSource.setSystemId(systemId);
        xmlSource.setSystemId(systemId);


         XSLTransform xslt = new XSLTransform(xslSource);
        
        System.out.println("\nSystem id--: " + xslSource.getSystemId());

        try {
            if(param == null) {
                result = xslt.transform(xmlSource);
            }
            else {
                result = xslt.transform(xmlSource, param, paramVal);
            }
        } catch (Exception ex) {
            result = "failed operation: " + ex.getMessage();
        }

         
        /*XMLInputFactory factory = XMLInputFactory.newInstance();

        //get Reader connected to XML input from somewhere..
        Reader readerXSL = new StringReader(xsl);
        Reader readerXML = new StringReader(xmlContent);

        XMLStreamWriter streamWriter=null;
        streamWriter = new XMLStreamWriter();


        Result resultWriter = null;

        try {

            XMLStreamReader streamReaderXSL =
                factory.createXMLStreamReader(readerXSL);
            XMLStreamReader streamReaderXML =
                factory.createXMLStreamReader(readerXML);


                    //define the Source object for the stylesheet
        Source XSL=new StAXSource(streamReaderXSL);

        //define the Source object for the XML document
        Source XML=new StAXSource(streamReaderXML);

        //define the Result object
        resultWriter =new StAXResult(streamWriter);


        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
*/


        




        //throw new UnsupportedOperationException("Not implemented yet.");
        return result;
    }

}
