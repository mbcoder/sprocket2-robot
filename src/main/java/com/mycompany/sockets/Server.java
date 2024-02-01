package com.mycompany.sockets;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.mycompany.sockets.contract.HttpMethod;
import com.mycompany.sockets.contract.RequestRunner;
import com.mycompany.sockets.http.HttpHandler;

/*
 * Simple Server: accepts HTTP connections and responds using
 * the Java net socket library.
 *  - Blocking approach ( 1 request per thread )
 *  - Non-blocking ( Investigate Java NIO - new IO, Netty uses this? )
 *
 *
 * Adapted from examples in Robert Finn's article: https://rjlfinn.medium
 * .com/creating-a-http-server-in-java-9b6af7f9b3cd
 * and code from Robert Finn's associated GitHub repo:https://github.com/rjlfinn/java-http-server
 *
 * Secure socket ideas taken from: https://stackoverflow.com/questions/2308479/simple-java-https-server
 */
public class Server {

  private final Map<String, RequestRunner> routes;
  private ServerSocket socket;
  private final ExecutorService threadPool;
  private HttpHandler handler;
  private final AtomicBoolean keepRunning = new AtomicBoolean(true);

  public Server(int port, boolean usingSsl) {
    routes = new HashMap<>();
    threadPool = Executors.newFixedThreadPool(100);

    var keyStorePath = Path.of("./keystore.jks");
    char[] keyStorePassword = "pass_for_self_signed_cert".toCharArray();

    // Bind the socket to the given port and address

    try {
      if (usingSsl) {
        socket = getSslContext(keyStorePath, keyStorePassword)
            .getServerSocketFactory()
            .createServerSocket(port, 0, new InetSocketAddress("0.0.0.0", port).getAddress());

      } else {
        socket = new ServerSocket(port);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  private static SSLContext getSslContext(Path keyStorePath, char[] keyStorePass)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException,
      UnrecoverableKeyException, KeyManagementException {

    var keyStore = KeyStore.getInstance("JKS");
    keyStore.load(new FileInputStream(keyStorePath.toFile()), keyStorePass);

    var keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
    keyManagerFactory.init(keyStore, keyStorePass);

    var sslContext = SSLContext.getInstance("TLS");
    // Null means using default implementations for TrustManager and SecureRandom
    sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
    return sslContext;
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
      System.err.println(e.getMessage());
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
