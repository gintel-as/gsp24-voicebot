# Gintel summer project - Voice Bot
This repository contains progress and results accomplished during the **Voice Bot project** of Gintel's student-internship 2024.

# Overview
The task at hand during this project has been creating a chat-bot application, utilizing different cloud services from industry-leading providers in speech-to-text, text-to-speech, AI-chat completions and language-translation. The providers we have gained experience with through testing different services are Azure, OpenAI, Google and AWS. We took advantage of different services from the different providers due to differences in service-structure, functionality, level of documentation and insights in performence pre-development. 

The development timeline was started by familiarizing ourselves with and planning the project's structure and technologies. We then implemented and tested Azure's Speech and OpenAI services through a web-client interface, before expanding our services to OpenAI's chat-bot (through Azure's framework), Google's Speech-to-Text and Text-to-Speech, and AWS's Text-to-Speech. As a product of our project development and testing we lastly created a performance analysis for the different services, which we hope can be of use for Gintel in future business.

# Deployment

### Technologies used:
- JRE 1.8
- JDK 22
- Maven 3.9.7
- Apache Tomcat 9.0.41

### Prerequisites:
- Cloud resources:
  - Azure Speech
  - Azure OpenAI
  - OpenAI API
  - Google cloud Text-to-Speech API
  - Google cloud Speech-to-Text API
  - AWS Transcribe
  - AWS Polly
- Updated API-keys in **web.properties** file in /conf/ folder (following the layout of included web.properties.schema file) (NB. Needs to be copied into conf-folder of local tomcat repository after every update)
- Configured Google Cloud credentials for local development environment (guide: https://cloud.google.com/docs/authentication/provide-credentials-adc#local-dev)
- Configured AWS credentials for local user. Done by downloading and setting up AWS CLI and configure credentials and config in local .aws folder to this format:

  - **Credentials:** <br />[default]<br />aws_access_key_id = access_key<br />aws_secret_access_key = secret_access<br /><br />[your_user_name]<br />aws_access_key_id = access_key<br />aws_secret_access_key = secret_access<br />

  - **Config:** <br />[default]<br />region = us-east-1<br />output = text<br /><br />[profile you_user_name]<br />region = us-east-1<br />output = text<br />


### Step 1:
Build with: mvn clean package

### Step 2:
Deploy web.war file from web/target/ to tomcat server

### Step 3:
Access web-client at http://localhost:8080/web/

### Comments:
- Speech-to-text service-provider is chosen **before building the application** with the variable "sttChosenProvider" in \core\src\main\java\com\gintel\cognitiveservices\service\CognitiveServices.java
- Text-to-speech and AI-engine providers is chosen through dropdowns in the web-client after deployment
# Intended use of the Web Application
The web application serves as a platform for users to interact with a virtual assistant through both text (“Text-To-Speech service”) and voice (“VoiceBot service”). 
Users can access the Text-To-Speech service by navigating to http://localhost:8080/web/ (see steps) and clicking on the designated "Text to Speech" button. This service allows users to input text to be answered by AI, with the AI answer being displayed as both audio and text. For now, this service utilizes only Azure’s cognitive services, but the “ttsChosenProvider” is added in \core\src\main\java\com\gintel\cognitiveservices\service\CognitiveServices.java is intended to facilitate hardcoding the switch between different providers.
The VoiceBot service can use both Azure and Google speech-to-text, which can be chosen before building the application with the variable “sttChosenProvider” in \core\src\main\java\com\gintel\cognitiveservices\service\CognitiveServices.java. During the session, one can switch between text-to-speech and openAI providers using the two dropdown menus. Azure tts uses a multilingual voice, whilst Google and AWS tts language must be chosen in the selected output language dropdown menu to ensure proper pronunciation of answer.
During the session, one can interrupt the audio of the chatbot answer by using the “Interrupt AI”-button”, which automatically stops the AI’s audio playback and resumes recording from microphone. Use the buttons “Stop streaming” and “Start streaming” to pause and initiate recording, respectively.
