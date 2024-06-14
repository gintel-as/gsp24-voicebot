package com.microsoft.cognitiveservices.speech.samples.console;

import java.io.*;
import java.net.*;

public class LoopMain {

   public static String getHTML(String urlToRead) throws Exception {
      StringBuilder result = new StringBuilder();
      URL url = new URI(urlToRead).toURL();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      try (BufferedReader reader = new BufferedReader(
                  new InputStreamReader(conn.getInputStream()))) {
          for (String line; (line = reader.readLine()) != null; ) {
              result.append(line);
          }
      }
      return result.toString();
   }

   public static void main(String[] args) throws Exception
   {
    while (true){
        String str = getHTML("http://localhost:8080/web/example/stt");
        String s = str.substring(str.indexOf("text")+7);
        String p = s.substring(0, s.indexOf("detectedLanguage")-3);
        System.out.println(p);
        getHTML("http://localhost:8080/web/example/v1?text="+p.replace(" ", "%20"));
        System.out.println(str);
        if (str.contains("Stop") || str.contains("stop")){
            break;
        }
    }
   }
}
