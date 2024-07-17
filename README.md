# gsp24-voicebot

Gintel summer project - Voice Bot

# Overview

To-do:
- Write overview

# Deployment

### Technologies used:
- JRE 1.8
- JDK 22
- Maven 3.9.7
- Apache Tomcat 9.0.41

### Prerequisites:
- Updated API-keys in **web.properties** file in /conf/ folder (following the layout of included web.properties.schema file) (NB. Needs to be copied into conf-folder of local tomcat repository after every update)
- Configured Google Cloud credentials for local development environment (guide: https://cloud.google.com/docs/authentication/provide-credentials-adc#local-dev)
- Configured AWS credentials for local user. Done by downloading and setting up AWS CLI and configure credentials and config in local .aws folder to this format:

**Credentials:**
[default]
aws_access_key_id = access_key
aws_secret_access_key = secret_access


[your_user_name]
aws_access_key_id = access_key
aws_secret_access_key = secret_access

**Config:**

[default]
region = us-east-1
output = text

[profile you_user_name]
region = us-east-1
output = text


### Step 1:
Build with: mvn clean package

### Step 2:
Deploy web.war file from web/target/ to tomcat server

### Step 3:
Access web-client at http://localhost:8080/web/

### Comments:
- Speech-to-text service-provider is chosen **before building the application** with the variable "sttChosenProvider" in \core\src\main\java\com\gintel\cognitiveservices\service\CognitiveServices.java
- Text-to-speech and AI-engine providers is chosen through dropdowns in the web-client after deployment

