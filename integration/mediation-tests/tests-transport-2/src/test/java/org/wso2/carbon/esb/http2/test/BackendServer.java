/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.esb.http2.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.NativeLibraryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;

public class BackendServer {

    static Logger log = LoggerFactory.getLogger(BackendServer.class);
    HttpWsConnectorFactory httpWsConnectorFactory = new DefaultHttpWsConnectorFactory();
    private ServerConnector serverConnector;

    public BackendServer() {

    }

    public void startServer() {

        ListenerConfiguration listenerConfiguration = initListenerConfiguration();

        this.serverConnector =
                httpWsConnectorFactory.createServerConnector(new ServerBootstrapConfiguration(new HashMap<>()),
                        listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new DefaultHttpConnectorListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
        }
    }

    public void startSSLServer() throws URISyntaxException {

        ListenerConfiguration listenerConfiguration=initSSLListenerConfiguration();
        this.serverConnector =
                httpWsConnectorFactory.createServerConnector(new ServerBootstrapConfiguration(new HashMap<>()),
                        listenerConfiguration);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHttpConnectorListener(new DefaultHttpConnectorListener());
        try {
            serverConnectorFuture.sync();
        } catch (InterruptedException e) {
        }
    }

    public void stop() {

        serverConnector.stop();
        try {
            httpWsConnectorFactory.shutdown();
        } catch (InterruptedException e) {
        }
    }

    private ListenerConfiguration initListenerConfiguration() {

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        listenerConfiguration.setPort(8080);
        listenerConfiguration.setHost("localhost");
        listenerConfiguration.setVersion("2.0");
        return listenerConfiguration;
    }

    private ListenerConfiguration initSSLListenerConfiguration() throws URISyntaxException {
//        System.load(String.valueOf(Paths.get(System.getProperty("java.library.path")+"/netty-tcnative-boringssl-static-2.0.47.Final" +
//                ".jar")));
//        System.load("/home/thuva/Downloads/netty-tcnative-boringssl-static-2.0.47.Final.jar");
//        System.loadLibrary("io.netty.netty-tcnative-boringssl-static");
//        log.info(System.getProperty("java.library.path"));

        ListenerConfiguration listenerConfiguration = initListenerConfiguration();
        listenerConfiguration.setScheme("https");
        listenerConfiguration.setKeyStoreFile(Paths.get(getClass().getResource("/").toURI()).getParent() + "/test-classes/keystores/products/wso2carbon.jks");
        listenerConfiguration.setKeyStorePass("wso2carbon");
        listenerConfiguration.setValidateCertEnabled(false);
        return listenerConfiguration;
    }

    private class DefaultHttpConnectorListener implements HttpConnectorListener {


        public void onMessage(HttpCarbonMessage httpCarbonMessage) {
            log.info("Message received");
            ByteBuf RESPONSE_BYTES =
                    Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(httpCarbonMessage.getHttpVersion(),
                            CharsetUtil.UTF_8));
            HttpCarbonMessage response = new HttpCarbonResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK));
            response.setHeaders(httpCarbonMessage.getHeaders());
            response.setHttpStatusCode(200);
            response.setKeepAlive(false);
            response.setHeader("content-length", httpCarbonMessage.getHttpVersion().length());
            response.addHttpContent(new DefaultLastHttpContent(RESPONSE_BYTES));
            try {
                httpCarbonMessage.respond(response);
            } catch (ServerConnectorException e) {
                log.error("Cannot respond to message",e);
            }
        }

        public void onError(Throwable throwable) {

            System.out.println("Error");
        }
    }
}
