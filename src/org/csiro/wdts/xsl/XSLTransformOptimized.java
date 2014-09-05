/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.xsl;


import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XSLTransformOptimized {

    StreamSource styleSheet;

    public XSLTransformOptimized() {
        System.setProperty("javax.xml.transform.TransformerFactory",
                   "org.apache.xalan.processor.TransformerFactoryImpl");

    }

    public XSLTransformOptimized(StreamSource styleSheet) {
        this.styleSheet = styleSheet;
    }
    

    public StreamSource getStylesheet() {
        return styleSheet;
    }

    public void setStylesheet(StreamSource stylesheet) {
        this.styleSheet = stylesheet;
    }

    public String transform(StreamSource streamSourceInput) throws Exception {

        StringWriter out = new StringWriter();

        javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance( );
        javax.xml.transform.Transformer trans = transFact.newTransformer(styleSheet);

        trans.transform(streamSourceInput, new StreamResult(out));
        

        return out.toString();
    }

    public String transform(StreamSource streamSourceInput, String param, Object value) throws Exception {

        StringWriter out = new StringWriter();

        javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance( );
        javax.xml.transform.Transformer trans = transFact.newTransformer(styleSheet);
        trans.setParameter(param, value);

        trans.transform(streamSourceInput, new StreamResult(out));

        return out.toString();
    }



} 