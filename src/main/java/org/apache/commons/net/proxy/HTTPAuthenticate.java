/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPAuthenticate implements ProxyAuthenticate {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());

	final String US_ASCII = "US-ASCII";
	final String UTF_8 = "UTF-8";
	private static final String ERROR_PREFIX = "HTTP proxy authenticate: ";

	@Override
	public void authenticate(Socket socket,
			String hostname, int port, Credentials credentials)
			throws IOException {

		String proxyUser = credentials.getUserName();
		String proxyPass = credentials.getPassword();

		// The CRLF sequence.
		byte[] CRLF = "\r\n".getBytes(UTF_8);
		// The connect command line.
		String connect = "CONNECT " + hostname + ":" + port + " HTTP/1.1";
		String hostHeader = "Host: " + hostname + ":" + port;
		// A connection status flag.
		boolean connected = false;
		// The socket for the connection with the proxy.
		InputStream in = null;
		OutputStream out = null;
		// FTPConnection routine.
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
			// Send the CONNECT request.
			out.write(connect.getBytes(UTF_8));
			out.write(CRLF);
			out.write(hostHeader.getBytes(UTF_8));
			out.write(CRLF);
			log.trace(">>>>>");
			log.trace(connect);
			log.trace(hostHeader);
			// Auth headers
			if (proxyUser != null && proxyPass != null) {
				String tmp = proxyUser + ":" + proxyPass;
				byte[] base64password = Base64.encodeBase64(tmp.getBytes(US_ASCII));
				String header = "Proxy-Authorization: Basic " + new String(base64password, US_ASCII);
				out.write(header.getBytes(UTF_8));
				out.write(CRLF);
				log.trace(header);
			}
			out.write(CRLF);
			log.trace("<<<<<");
			// Get the proxy response.
			ArrayList<String> responseLines = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			for (String line = reader.readLine(); line != null
					&& line.length() > 0; line = reader.readLine()) {
				responseLines.add(line);
				log.trace(line);
			}
			// Parse the response.
			int size = responseLines.size();
			if (size < 1) {
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
			String code = null;
			String response = responseLines.get(0);
			if (response.startsWith("HTTP/") && response.length() >= 12) {
				code = response.substring(9, 12);
			} else {
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
			if (!"200".equals(code)) {
				StringBuffer msg = new StringBuffer();
				msg.append(ERROR_PREFIX + "connection failed\r\n");
				msg.append("Response received from the proxy:\r\n");
				for (int i = 0; i < size; i++) {
					String line = responseLines.get(i);
					msg.append(line);
					msg.append("\r\n");
				}
				throw new IOException(msg.toString());
			}
			connected = true;
		} catch (IOException e) {
			throw e;
		} finally {
			if (!connected) {
				if (out != null) {
					try {
						out.close();
					} catch (Throwable t) {
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (Throwable t) {
					}
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (Throwable t) {
					}
				}
			}
		}
	}

}
