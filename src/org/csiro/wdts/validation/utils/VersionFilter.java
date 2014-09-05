/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.validation.utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
/**
 *
 * @author spenserk
 */
public class VersionFilter extends XMLFilterImpl
{
  String versionString;
  boolean readData;

  public VersionFilter()
  {
    this.versionString = "";
    this.readData = false;
  }

  public VersionFilter(XMLReader parent)
  {
    super(parent);
    this.versionString = "";
    this.readData = false;
  }


  /**
   * Filter the Namespace URI for start-element events.
   */
    @Override
  public void
  startElement (String uri, String localName, String qName,
	Attributes atts)
  throws SAXException
  {
    if ( localName.equals("version") && uri.equals("http://www.bom.gov.au/std/water/xml/wdtf0.3") ) {
      this.readData = true;
    }
    super.startElement(uri, localName, qName, atts);
  }


    @Override
  public void characters(char[] ch, int start, int length) {
      if(this.readData == true) {
          this.versionString = new String(ch, start, length);
      }
  }

  /**
   * Filter the Namespace URI for end-element events.
   */
    @Override
  public void
  endElement (String uri, String localName, String qName)
  throws SAXException
  {
    if ( localName.equals("version") && uri.equals("http://www.bom.gov.au/std/water/xml/wdtf0.3") ) {
      this.readData = false;

    }
    super.endElement(uri, localName, qName);
  }

}
