/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.service;

import java.io.File;
import java.io.FileReader;
import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author yu021
 */
public class SchematronValidation {

    
    public boolean interpretReport(File fileReport) {
        boolean result = false;

        try {
            SVRLHandler handler = new SVRLHandler();
            SAXErrorHandler errHandler = new SAXErrorHandler();
            InputSource is = new InputSource(new FileReader(fileReport));

            SAXParser parser = new SAXParser();
            parser.setContentHandler(handler);
            parser.setErrorHandler(errHandler);
            parser.parse(is);

            //is it valid?
            result = handler.isValid();
        } catch (Exception ex) {
            System.out.println("EXCEPTION: " + ex);
        }



        return result;
    }

    /**
     * Return the text that a node contains. This routine:<ul>
     * <li>Ignores comments and processing instructions.
     * <li>Concatenates TEXT nodes, CDATA nodes, and the results of
     *     recursively processing EntityRef nodes.
     * <li>Ignores any element nodes in the sublist.
     *     (Other possible options are to recurse into element
     *      sublists or throw an exception.)
     * </ul>
     * @param    node  a  DOM node
     * @return   a String representing its contents
     */
    public String getTextFromTag(Node node) {
        StringBuffer result = new StringBuffer();
        if (!node.hasChildNodes()) {
            return "";
        }

        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.TEXT_NODE) {
                result.append(subnode.getNodeValue());
            } else if (subnode.getNodeType() ==
                    Node.CDATA_SECTION_NODE) {
                result.append(subnode.getNodeValue());
            } else if (subnode.getNodeType() ==
                    Node.ENTITY_REFERENCE_NODE) {
                // Recurse into the subtree for text
                // (and ignore comments)
                result.append(getTextFromTag(subnode));

            }
        }
        return result.toString();
    }

}
