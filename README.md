# gsp24-voicebot

Gintel summer project - Voice Bot

# Overview

This example shows a typical maven multi-module project, consisting of:
 - core : core module, which typically will contain your service-definining interfaces, common types, utils
       etc.
 - web : a web module which you optionally can use to test stuff, e.g. to invoke your text-to-speech 
       service. Test a simple echo endpoint by deploying ./web/target/web.war in Tomcat and accessing 
       http://localhost:8080/web/example/v1?text=test

Build with: 
  mvn clean package

Define modules as needed (e.g. 1 for your Azure text-to-speech implementation, Azure speech-to-text, 
chat-bot etc) in the root/ parent pom.xml; and create subfolders referencing this parent artifact
(see e.g. ./core/pom.xml).