/**
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import com.mycompany.sockets.Server;
import com.mycompany.sockets.pojos.HttpResponse;

import static com.mycompany.sockets.contract.HttpMethod.GET;

public class App extends Application {

    private MapView mapView;

    private Server myServer;

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

        // Note: it is not best practice to store API keys in source code.
        // An API key is required to enable access to services, web maps, and web scenes hosted in ArcGIS Online.
        // If you haven't already, go to your developer dashboard to get your API key.
        // Please refer to https://developers.arcgis.com/java/get-started/ for more information
        String yourApiKey = "YOUR_API_KEY";
        yourApiKey = System.getenv("API_KEY");
        System.out.println(yourApiKey);
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();
        stackPane.getChildren().add(mapView);

        // create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);

        // display the map by setting the map on the map view
        mapView.setMap(map);


      try {
        System.out.println("Start server");
        myServer = new Server(8080);
        myServer.addRoute(GET, "/testOne",
            (req) -> new HttpResponse.Builder()
                .setStatusCode(200)
                .addHeader("Content-Type", "text/html")
                .setEntity(new CommandRunner(req))
                .build());
        executor.execute(() -> {
          try {
            System.out.println("Called start");
            myServer.start();
            System.out.println("Start exited");
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      } catch (Throwable e) {
        System.out.println("Server failed: " + e.getMessage());
      }
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
      if (myServer != null) {
        System.out.println("Stop the server");
        myServer.stop();
      }

        if (mapView != null) {
            mapView.dispose();
        }

        executor.shutdown(); // Disable new tasks from being submitted
        try {
          // Wait a while for existing tasks to terminate
          if (!executor.awaitTermination(6, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!executor.awaitTermination(6, TimeUnit.SECONDS))
              System.err.println("Pool did not terminate");
          }
        } catch (InterruptedException ex) {
          // (Re-)Cancel if current thread also interrupted
          executor.shutdownNow();
          // Preserve interrupt status
          Thread.currentThread().interrupt();
        }
    }
}


