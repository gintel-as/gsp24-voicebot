package com.microsoft.cognitiveservices.speech.samples.console;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class LoopMain {

   public static String getHTML(String urlToRead) throws Exception {
      StringBuilder result = new StringBuilder();
      URL url = new URI(urlToRead).toURL();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      try (BufferedReader reader = new BufferedReader(
                  new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
          for (String line; (line = reader.readLine()) != null; ) {
              result.append(line);
          }
      }
      System.out.println(result);
      return result.toString();
   }

   public static void main(String[] args) throws Exception
   {
    while (true){
        System.out.println("Speak now ------");
        String voiceInput = getHTML("http://localhost:8080/web/rest/example/stt");
        String s = voiceInput.substring(voiceInput.indexOf("text")+7);
        String p = s.substring(0, s.indexOf("detectedLanguage")-3);
        System.out.println(p);
        if (voiceInput.contains("Stop") || voiceInput.contains("stop")){
            break;
        }

        String botInput = getHTML("http://localhost:8080/web/rest/example/openai?text="+p.replace(" ", "%20"));
        String a = botInput.substring(botInput.indexOf("response")+11);
        String b = a.substring(0, a.indexOf("input")-3);
        b = b.replaceAll("\\\\n", "");
        b = b.replaceAll("\\\\", "");
        System.out.println(b);

        String ubrukeligString = getHTML("http://localhost:8080/web/rest/example/v1?text="+b.replace(" ", "%20"));
        System.out.println(botInput);
    }
   }
}
