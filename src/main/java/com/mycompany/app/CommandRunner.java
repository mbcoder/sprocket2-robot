/*
 COPYRIGHT 1995-2024 ESRI

 TRADE SECRETS: ESRI PROPRIETARY AND CONFIDENTIAL
 Unpublished material - all rights reserved under the
 Copyright Laws of the United States.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */

package com.mycompany.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import com.mycompany.sockets.pojos.HttpRequest;

public class CommandRunner {

  private final HttpRequest request;

  public CommandRunner (HttpRequest request) {
    this.request = request;
  }

  public String execCommand() {

    final String[] validForwardCommand = {"bash",  "-c", "echo Success with forward"};
    final String[] invalidForwardCommand = {"bash",  "-c", "echo 'Failed (25)'"};
    final String[] validRotateCommand = {"bash",  "-c", "echo Success with rotation"};

    try {
      // Expecting a command URL like  https://127.0.0.1:8080/testOne?forward=100
      // Split this on question mark to isolate the part like "forward=100"
      // If the command string has 100000 then fail the command to test failure paths
      String[] commands = request.getUri().toString().split("\\?");
      if (commands.length >= 2) {
        String command = commands[1];
        String [] execString;
        if (command.contains("forward")) {
          if (command.contains("100000")) {
            execString = invalidForwardCommand;
          } else {
            execString = validForwardCommand;
          }
        } else {
          execString = validRotateCommand;
        }

        Process runExternalProcess = Runtime.getRuntime().exec(execString);

        if (!runExternalProcess.waitFor(1, TimeUnit.MINUTES)) {
          return "FAILED external process waitFor problem";
        }

        // Get the stdout of the process
        try (var stdout = new BufferedReader(new InputStreamReader(runExternalProcess.getInputStream()))) {
          var line = stdout.readLine();
          return String.format("<HTML> <P>%s</P> </HTML>", line);
        }
      }

      return String.format("<HTML> <P> Invalid... %s</P> </HTML>", request.getUri().toString());
    } catch (IOException | InterruptedException e) {
      System.out.println("exception");
      return "FAILED " + e.getMessage();
    }
  }
}