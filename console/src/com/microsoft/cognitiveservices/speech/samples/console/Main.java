package com.microsoft.cognitiveservices.speech.samples.console;

import java.util.Scanner;

//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//

@SuppressWarnings("resource") // scanner
public class Main {

    public static void main(String[] args) {
        String prompt = "Your choice (0: Stop): ";

        System.out.println(" 1. Speech synthesis to audio and SRT files.");
        System.out.println(" 2. Speech synthesis to text.");
        System.out.println(" 3. Speech synthesis from wav file to text.");

        System.out.print(prompt);

        try {
            String x;
            do {
                x = new Scanner(System.in).nextLine();
                System.out.println("");
                switch (x.toLowerCase()) {
                case "1":
                    TextFileToAudio.textToAudio();
                    break;
                case "2":
                    AudioToText.audioToText(args);
                    break;
                case "3":
                    WavAudioToText.audioToText(args);
                }
                System.out.println("\nExecution done. " + prompt);
            } while (!x.equals("0"));

            System.out.println("Finishing demo.");
            System.exit(0);
        } catch (Exception ex) {
            System.out.println("Unexpected " + ex.toString());
            System.exit(1);
        }
    }
}
