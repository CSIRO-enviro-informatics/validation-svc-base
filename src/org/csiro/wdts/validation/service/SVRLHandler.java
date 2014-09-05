/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.validation.service;

/**
 *
 * @author yu021
 */

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.parsers.SAXParser;

public class SVRLHandler extends DefaultHandler
{
      int tagCount = 0;
      int failedAsserts = 0;
      

    @Override
      public void startElement(String uri, String localName,
         String rawName, Attributes attributes)
      {
            if (rawName.equals("svrl:schematron-output")) {
               tagCount++;
            }
            else if(rawName.equals("svrl:failed-assert")) {
                failedAsserts++;
            }

            System.out.println("Elem: " + rawName);
      }

    @Override
      public void endDocument()
      {
            System.out.println("There are " + tagCount +
                " <schematron-output> elements.");
      }

      public boolean isValid() {
          if(failedAsserts > 0)
              return false;

          return true;
      }

     
}