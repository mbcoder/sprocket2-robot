/**
 * Copyright 2024 Esri
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mycompany.app;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import com.mycompany.sockets.Server;
import com.mycompany.sockets.pojos.HttpResponse;

import static com.mycompany.sockets.contract.HttpMethod.GET;

public class App extends Application {



  private final HBox hBox = new HBox();

  private Server connectionServer;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) {

    // set the title and size of the stage and show it
    stage.setTitle("My Map App");
    stage.setWidth(800);
    stage.setHeight(700);
    stage.show();

    // create a JavaFX scene with a stack pane as the root node and add it to the scene
    StackPane stackPane = new StackPane();
    Scene scene = new Scene(stackPane);
    stage.setScene(scene);

    startServer(true);

    //Button nonSslServer = new Button("Not SSL");
    //nonSslServer.setOnAction((e) -> startServer(false));

    //Button sslServer = new Button("SSL");
    //sslServer.setOnAction((e) -> startServer(true));

    //hBox.getChildren().addAll(nonSslServer, sslServer);
    //stackPane.getChildren().add(hBox);

  }

  /**
   * Start a connection server listening on port 8080.
   *
   * @param usingSsl true for SSL connection using the local keystore.jks.
   */
  private void startServer(boolean usingSsl) {
    // Prevent multiple servers being started

    hBox.setDisable(true);
    try {
      connectionServer = new Server(8080, usingSsl);
      connectionServer.addRoute(GET, "/testOne",
          (req) -> new HttpResponse.Builder()
              .setStatusCode(200)
              .addHeader("Content-Type", "text/html")
              .setEntity(new CommandRunner(req))
              .build());
      executor.execute(() -> {
        try {
          connectionServer.start();
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
      });
    } catch (Throwable e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Stops and releases all resources used in application.
   */
  @Override
  public void stop() {
    if (connectionServer != null) {
      connectionServer.stop();
    }


    // Ensure threads are stopped to allow app to exit
    executor.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!executor.awaitTermination(5, TimeUnit.SECONDS))
          System.err.println("Thread Executor did not terminate");
      }
    } catch (InterruptedException ex) {
      // (Re-)Cancel if current thread also interrupted
      executor.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
