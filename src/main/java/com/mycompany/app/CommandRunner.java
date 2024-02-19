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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mycompany.sockets.pojos.HttpRequest;

/**
 * Run an external command based on a received HttpRequest.
 */
public class CommandRunner {

  private final HttpRequest request;

  public CommandRunner (HttpRequest request) {
    this.request = request;
  }

  /**
   * Execute the external command based on the received HttpRequest.
   *
   * @return a response String to send back (will be sent via the socket)
   */
  public String execCommand() {
    String responseBody;

    final List<String> commandList = new ArrayList<>();
    commandList.add("python3");

    try {
      // Expecting a command URL like  https://127.0.0.1:8080/testOne?forward=100
      // Split this on question mark and equals to isolate the part like "forward=100"
      String[] commands = request.getUri().toString().split("[?=]");
      if (commands.length == 3) {
        String command = commands[1];
        String commandProcess = null;
        String commandParameter = null;

        if (command.equals("forward")) {
          commandProcess = "forward.py";
        } else if (command.equals("rotate")) {
          commandProcess = "rotate.py";
        }

        if (commands[2].matches("^ *-?\\d+ *$")) {
          commandParameter = commands[2];
        }

        if (commandProcess != null && commandParameter != null) {
          commandList.add(commandProcess);
          commandList.add(commandParameter);
          System.out.println(commandList);
          Process runExternalProcess = Runtime.getRuntime().exec(commandList.toArray(new String[0]));
          if (!runExternalProcess.waitFor(1, TimeUnit.MINUTES)) {
            responseBody = String.format("Timeout... %s", request.getUri().toString());
          } else {
            // Get the stdout of the process - return the first line
            try (var stdout = new BufferedReader(new InputStreamReader(runExternalProcess.getInputStream()))) {
              responseBody = stdout.readLine();
            }
          }
        } else {
          responseBody = String.format("Error: %s", request.getUri().toString());
        }
      } else {
        responseBody = String.format("Error: %s", request.getUri().toString());
      }
    } catch (IOException | InterruptedException e) {
      responseBody = String.format("Exception... %s:%s", e.getMessage(), request.getUri().toString());
    }

    System.out.println(responseBody);
    return String.format("<HTML> <P>%s</P> </HTML>", responseBody);
  }
}