/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.webbitserver.handler;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.webbitserver.WebServers.createWebServer;
import static org.webbitserver.testutil.HttpClient.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;

public class RangeTest {
    private final WebServer webServer = createWebServer(59504);

    private final String content = "Very short string for which there is no real point in compressing, but we're going to do it anyway.";

    @After
    public void die() throws IOException, InterruptedException {
        webServer.stop().join();
    }

    @Test
    public void rangeRequestReturnsPartialContent() throws IOException {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
                    throws Exception {
                response.content(content).end();
            }
        }).start();
        HttpURLConnection urlConnection = (HttpURLConnection) specifyRange(httpGet(webServer, "/"), "0-9");
        String result = contents(urlConnection);
        assertEquals(206, urlConnection.getResponseCode());
        assertEquals("0-9/99", urlConnection.getHeaderField("content-range"));
        assertEquals(10, urlConnection.getContentLength());
        assertEquals(content.substring(0, 10), result);
    }

    @Test
    public void rangeRequestPlaysWellWithCompressedResponses() throws Exception {
        webServer.add(new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control)
                    throws Exception {
                response.content(content).end();
            }
        }).start();
        HttpURLConnection urlConnection = (HttpURLConnection) supportCompressedResponse(specifyRange(
                httpGet(webServer, "/"), "0-10"));

        String result = decompressContents(urlConnection);

        assertEquals(content.substring(0, 11), result);
    }

    public URLConnection specifyRange(URLConnection urlConnection, String byteRangeSet) {
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
        httpConnection.addRequestProperty("Range", "bytes=" + byteRangeSet);
        return httpConnection;
    }

    public URLConnection supportCompressedResponse(URLConnection urlConnection) {
        HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
        httpConnection.addRequestProperty("Accept-Encoding", "gzip");
        return httpConnection;
    }

}