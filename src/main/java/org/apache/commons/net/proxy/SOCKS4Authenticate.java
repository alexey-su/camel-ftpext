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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class SOCKS4Authenticate implements ProxyAuthenticate {
	final String UTF_8 = "UTF-8";
	private static final String ERROR_PREFIX = "SOCKS4 proxy authenticate: ";

	@Override
	public void authenticate(Socket socket,
			String hostname, int port, Credentials credentials)
			throws IOException {

		String proxyUser = credentials.getUserName();
		
		// Socks 4 or 4a?
		boolean socks4a = false;
		byte[] address;
		try {
			address = InetAddress.getByName(hostname).getAddress();
		} catch (Exception e) {
			// Cannot resolve host, switch to version 4a.
			socks4a = true;
			address = new byte[] { 0x00, 0x00, 0x00, 0x01 };
		}
		// A connection status flag.
		boolean connected = false;
		// The socket for the connection with the proxy.
		InputStream in = null;
		OutputStream out = null;
		// FTPConnection routine.
		try {			
			in = socket.getInputStream();
			out = socket.getOutputStream();
			// Send the request.
			// Version 4.
			out.write(0x04);
			// CONNECT method.
			out.write(0x01);
			// Remote port number.
			out.write(port >> 8);
			out.write(port);
			// Remote host address.
			out.write(address);
			// The user.
			if (proxyUser != null) {
				out.write(proxyUser.getBytes(UTF_8));
			}
			// End of user.
			out.write(0x00);
			// Version 4a?
			if (socks4a) {
				out.write(hostname.getBytes(UTF_8));
				out.write(0x00);
			}
			// Get and parse the response.
			int aux = read(in);
			if (aux != 0x00) {
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
			aux = read(in);
			switch (aux) {
			case 0x5a:
				in.skip(6);
				connected = true;
				break;
			case 0x5b:
				throw new IOException(ERROR_PREFIX + "connection refused/failed");
			case 0x5c:
				throw new IOException(ERROR_PREFIX + "cannot validate the user");
			case 0x5d:
				throw new IOException(ERROR_PREFIX + "invalid user");
			default:
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (!connected) {
				if (out != null) {
					try {
						out.close();
					} catch (Throwable t) {
						;
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (Throwable t) {
						;
					}
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (Throwable t) {
						;
					}
				}
			}
		}
	}

	private int read(InputStream in) throws IOException {
		int aux = in.read();
		if (aux < 0) {
			throw new IOException(ERROR_PREFIX + "connection closed by the proxy");
		}
		return aux;
	}
}
