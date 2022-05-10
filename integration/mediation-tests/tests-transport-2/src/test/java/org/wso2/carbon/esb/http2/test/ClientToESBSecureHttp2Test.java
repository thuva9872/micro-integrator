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

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.IOException;

public class ClientToESBSecureHttp2Test extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
    }

    @Test(description = "Test the secure HTTP/2 (HTTPS) request-response between the client and ESB")
    public void testSecureHttp2RequestClientToESB() throws IOException {

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url("https://localhost:5100/noBackendCall").get().build();
        Response response = client.newCall(request).execute();
        Assert.assertEquals(response.protocol(), Protocol.HTTP_2);
    }

    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {

        super.cleanup();
    }
}
