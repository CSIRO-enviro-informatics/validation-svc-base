package org.csiro.wdts.validation.utils;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class URLChecker {

    public boolean checkUrl(String urlString)  {
        boolean urlChecksout = false;


        try {
            URL url = new URL(urlString);
            URLConnection connection;

            connection = url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(5000);
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection =
                        (HttpURLConnection) connection;
                httpConnection.setRequestMethod("HEAD");
                httpConnection.connect();
                int response =
                        httpConnection.getResponseCode();
                System.out.println("[" + response + "]" +
                        urlString);
                String location =
                        httpConnection.getHeaderField("Location");
                if (location != null) {
                    System.out.println(
                            "Location: " + location);
                }
                System.out.println();



                if (response > 300) {
                    urlChecksout = false;
                } else {
                    urlChecksout = true;
                }
            }
        }  catch (java.net.SocketTimeoutException tex) {
            urlChecksout = false;
            System.out.println("[timeout]");


        } catch (java.net.ConnectException cex) {
            Logger.getLogger(URLChecker.class.getName()).log(Level.SEVERE, urlString , cex);
            urlChecksout = false;


        } catch (java.net.BindException bex) {
            urlChecksout = false;
            System.out.println("[bindexception] " + bex);

        }

        catch (MalformedURLException ex) {
            Logger.getLogger(URLChecker.class.getName()).log(Level.SEVERE, urlString, ex);
            urlChecksout = false;
        }   catch (IOException ex) {
            Logger.getLogger(URLChecker.class.getName()).log(Level.SEVERE, urlString, ex);
            urlChecksout = false;
        }  
        
        return urlChecksout;
    }
}
