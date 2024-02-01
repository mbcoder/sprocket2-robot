package com.mycompany.sockets;

import com.mycompany.sockets.contract.HttpMethod;
import com.mycompany.sockets.contract.RequestRunner;
import com.mycompany.sockets.http.HttpHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Simple Server: accepts HTTP connections and responds using
 * the Java net socket library.
 *  - Blocking approach ( 1 request per thread )
 *  - Non-blocking ( Investigate Java NIO - new IO, Netty uses this? )
 *
 *
 * Adapted from examples in Robert Finn's article: https://rjlfinn.medium.com/creating-a-http-server-in-java-9b6af7f9b3cd
 * and code from Robert Finn's associated GitHub repo:https://github.com/rjlfinn/java-http-server
 */
public class Server {

    private final Map<String, RequestRunner> routes;
    private final ServerSocket socket;
    private final ExecutorService threadPool;
    private HttpHandler handler;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    public Server(int port) throws IOException {
        routes = new HashMap<>();
        threadPool = Executors.newFixedThreadPool(100);
        socket = new ServerSocket(port);
    }

    public void start() throws IOException {
        handler = new HttpHandler(routes);

        while (keepRunning.get()) {
          try {
            Socket clientConnection = socket.accept();
            handleConnection(clientConnection);
          } catch (SocketTimeoutException | SocketException t) {
            // Ignore timeout
          }
        }
        threadPool.shutdownNow();
    }

    public void stop() {
      keepRunning.set(false);
      try {
        socket.close();
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }

    /*
     * Capture each Request / Response lifecycle in a thread
     * executed on the threadPool.
     */
    private void handleConnection(Socket clientConnection) {
        Runnable httpRequestRunner = () -> {
            try {
                handler.handleConnection(clientConnection.getInputStream(), clientConnection.getOutputStream());
            } catch (IOException ignored) {
            }
        };
        threadPool.execute(httpRequestRunner);
    }

    public void addRoute(final HttpMethod opCode, final String route, final RequestRunner runner) {
        routes.put(opCode.name().concat(route), runner);
    }
}
