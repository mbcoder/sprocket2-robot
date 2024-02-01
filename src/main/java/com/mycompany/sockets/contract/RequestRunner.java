package com.mycompany.sockets.contract;

import com.mycompany.sockets.pojos.HttpRequest;
import com.mycompany.sockets.pojos.HttpResponse;

public interface RequestRunner {
    HttpResponse run(HttpRequest request);
}
