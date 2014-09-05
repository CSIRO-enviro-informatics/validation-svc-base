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
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.trans.CompilerInfo;

/**
 * This class represents a XSL Transformation for the
 * SOAP Web Service Request.
 *
 * @author Siegfried Bolz (http://blog.jdevelop.eu)
 *
 */
public class XSLTransform {

    /**
     * The Configuration object
     */
    private Configuration config;

    /**
     * The StreamSource wrapping the XSLT stylesheet
     */
    private StreamSource styleSheet;

    /**
     * Constructs a new transformer with the given XSLT stylesheet.
     *
     * @param streamSource - The style sheet as a <code>StreamSource</code>
     */
    public XSLTransform(StreamSource streamSource) {
        this(streamSource, new Configuration());
       System.setProperty("javax.xml.transform.TransformerFactory",
                   "net.sf.saxon.TransformerFactoryImpl");



    }

    /**
     * Constructs a new transformer with the given XSLT stylesheet
     * and the given <code>Configuration</code>.
     *
     * @param streamSource - The style sheet as a <code>StreamSource</code>
     * @param configuration - The configuration object
     */
    public XSLTransform(StreamSource streamSource, Configuration configuration) {
        this.styleSheet = streamSource;
        this.config = configuration;

       System.setProperty("javax.xml.transform.TransformerFactory",
                   "net.sf.saxon.TransformerFactoryImpl");
    }

    /**
     * Transforms the given XML file wrapped in a <code>StreamSource</code>
     * and returns an XML file in form of a <code>String</code>. This <code>String</code>
     * can be than wrapped into a <code>java.io.StringWriter</code> to get a
     * <code>java.io.Writer</code> or into a <code>java.io.StringReader</code> to get a
     * <code>java.io.Reader</code>.
     *
     * @param streamSourceInput
     * @return An XML document in form of a <code>String</code>
     * @throws Exception
     */
    public String transform(StreamSource streamSourceInput) throws Exception {


        StringWriter out = new StringWriter();

        Transformer transformer = newTransformer(styleSheet);

        transformer.transform(streamSourceInput, new StreamResult(out));
        

        return out.toString();
    }

    /**
     * Transforms the given XML file wrapped in a <code>StreamSource</code>
     * and returns an XML file in form of a <code>String</code>. This <code>String</code>
     * can be than wrapped into a <code>java.io.StringWriter</code> to get a
     * <code>java.io.Writer</code> or into a <code>java.io.StringReader</code> to get a
     * <code>java.io.Reader</code>.
     *
     * @param streamSourceInput
     * @return An XML document in form of a <code>String</code>
     * @throws Exception
     */
    public String transform(StreamSource streamSourceInput, String param, Object value) throws Exception {

        StringWriter out = new StringWriter();

        Transformer transformer = newTransformer(styleSheet);
        transformer.setParameter(param, value);

        transformer.transform(streamSourceInput, new StreamResult(out));


        return out.toString();
    }

    /**
     * Returns a new <code>Transformer</code> object for the given XSLT
     * file.
     *
     * @param source
     * @return A <code>Transformer</code>
     * @throws TransformerConfigurationException
     */
    protected Transformer newTransformer(Source source) throws TransformerConfigurationException {
        Templates templates = newTemplates(source);
        return templates.newTransformer();
    }

    /**
     * Creates a new XSLT stylesheet template from the given
     * XSLT source file
     *
     * @param source
     * @return A <code>Templates</code>
     * @throws TransformerConfigurationException
     */
    protected Templates newTemplates(Source source) throws TransformerConfigurationException {
        CompilerInfo info = new CompilerInfo();
        info.setURIResolver(config.getURIResolver());
        info.setErrorListener(config.getErrorListener());
        //info.setCompileWithTracing(config.isCompileWithTracing());

        return PreparedStylesheet.compile(source, config, info);
    }

    /**
     * A simple use case for testing.
     *
     * XSLT: mapping.xslt
     * Input XML: custom_request.xml
     * Output XML: printed to System.out
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // in this directory are all files located
        String directory = "xmldata\\";

        // this is the XSLT-mapping-file
        String XSLTfile = "mapping.xslt";

        // create a transformer and initialize it with the XSLT
        XSLTransform transformer = new XSLTransform(new StreamSource(directory + XSLTfile));

        // execute the transformation and show the produced XML in System.out
        System.out.println(transformer.transform(new StreamSource(directory + "custom_request.xml")));
    }

} // .EOF