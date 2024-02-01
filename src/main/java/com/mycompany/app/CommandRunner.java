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

  public CommandRunner (HttpRequest req) {
    request = req;
  }

  public String execCommand() {

    String[] validForwardCommand = {"bash",  "-c", "echo Success with forward"};
    String[] invalidForwardCommand = {"bash",  "-c", "echo 'Failed (25)'"};
    String[] validRotateCommand = {"bash",  "-c", "echo Success with rotation"};

    try {
      System.out.println("exec");
      System.out.println(request.getUri());
      String[] commands = request.getUri().toString().split("[? ]");
      if (commands.length >=2) {

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

        Process compareImageProcess = Runtime.getRuntime().exec(execString);

        System.out.println("wait...");
        if (!compareImageProcess.waitFor(1, TimeUnit.MINUTES)) {
          System.out.println("wait failed");
          return "FAILED waitFor failure";
        }

        try (var stdout = new BufferedReader(new InputStreamReader(compareImageProcess.getInputStream()))) {
          var s = stdout.readLine();
          if (s == null) {
            try (var stderr = new BufferedReader(new InputStreamReader(compareImageProcess.getErrorStream()))) {
              s = stderr.readLine();
              System.out.println("Got err" + s);
              return String.format("<HTML> <P>%s</P> </HTML>", s);
            }

          }
          System.out.println("Got " + s);
          return String.format("<HTML> <P>%s</P> </HTML>", s);
        }
      }

      return String.format("<HTML> <P> Invalid... %s</P> </HTML>", request.getUri().toString());

    } catch (IOException | InterruptedException e) {
      System.out.println("exception");
      return "FAILED " + e.getMessage();
    }
  }
}