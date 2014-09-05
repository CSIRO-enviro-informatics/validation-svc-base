/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.csiro.wdts.validation.input;


import org.springframework.web.multipart.MultipartFile;



/**
 *
 * @author yu021
 */
public class ValidationFormInput {
    //private byte[] file;
    private MultipartFile  file;
    private String[] catalogs;
    private String filename;
    private String version;
    private String validationType;
    private String inputMethod;
    private String xmltext;
    private String outputMethod;

    public String getOutputMethod() {
        return outputMethod;
    }

    public void setOutputMethod(String outputMethod) {
        this.outputMethod = outputMethod;
    }


    public String getXmltext() {
        return xmltext;
    }

    public void setXmltext(String xmltext) {
        this.xmltext = xmltext;
    }

    public boolean isXmltextEmpty(){
        return (this.xmltext.length() == 0);
    }

    public String getInputMethod() {
        return inputMethod;
    }

    public void setInputMethod(String inputMethod) {
        this.inputMethod = inputMethod;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String[] getCatalogs() {
        return catalogs;
    }

    public void setCatalogs(String[] catalogs) {
        this.catalogs = catalogs;
    }

    /*public void setFile(byte[] file) {
        this.file = file;
    }

    public byte[] getFile() {
        return file;
    }

    // for checking upload.filesize.cap
    public int getFilesize() {
        return file.length;
    }*/
    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public MultipartFile getFile() {
        return file;
    }

    // for checking upload.filesize.cap
    public long getFilesize() {
        return file.getSize();
    }

    // checking Upload file's content's WDTF version
   /* public String getContentWDTFVersion (byte[] b) {
        Element root;
        String namespaceShort=null;
        String namespaceLong=null;
        String sWDTFversion=null;
        SAXBuilder builder = new SAXBuilder();
        Stack  stack = new Stack();
        String result="error";
        String result2="error";

        ByteArrayInputStream input1 = new ByteArrayInputStream(b);
        try {
            Document doc = builder.build(input1);
            root = doc.getRootElement();
            doc.
        } catch (JDOMException e) {
            System.out.println(e.getMessage());
            return "error1";
        } catch (IOException e) {
            System.out.println(e);
            return "error2";
        }

        result="error3";
        namespaceShort = root.getAttributeValue("xmlns");
        namespaceLong = root.getAttributeValue("xmlns:wdtf");
        if (namespaceShort != null) {
            sWDTFversion = namespaceShort;
            result2 = "s:" + namespaceShort;
        } else if (namespaceLong != null) {
            sWDTFversion = namespaceLong;
            result2 = "l:" + namespaceLong;
        }

        if (sWDTFversion != null ) {
            StringTokenizer st = new StringTokenizer(sWDTFversion, "/");
            // use stack to pick the last token
            while (st.hasMoreTokens()) {
                stack.push(st.nextElement());
            }
            while(!stack.empty()) {
                result = stack.pop().toString();
            }
        }

        return result2;
    }*/

}
