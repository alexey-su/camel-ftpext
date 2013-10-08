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
import java.net.Socket;

public class SOCKS5Authenticate implements ProxyAuthenticate {
	final String UTF_8 = "UTF-8";
	private static final String ERROR_PREFIX = "SOCKS5 proxy authenticate: ";

	@Override
	public void authenticate(Socket socket,
			String hostname, int port, Credentials credentials)
			throws IOException {
		
		String proxyUser = credentials.getUserName();
		String proxyPass = credentials.getPassword();
		
		// Authentication flag
		boolean authentication = proxyUser != null && proxyPass != null;
		// A connection status flag.
		boolean connected = false;
		// The socket for the connection with the proxy.
		InputStream in = null;
		OutputStream out = null;
		// FTPConnection routine.
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
			int aux;
			// Version 5.
			out.write(0x05);
			// Authentication?
			if (authentication) {
				// Authentication with username/password.
				out.write(0x01);
				out.write(0x02);
			} else {
				// No authentication.
				out.write(0x01);
				out.write(0x00);
			}
			// Get the response.
			aux = read(in);
			if (aux != 0x05) {
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
			aux = read(in);
			if (authentication) {
				if (aux != 0x02) {
					throw new IOException(
							ERROR_PREFIX + "proxy doesn't support "
									+ "username/password authentication method");
				}
				// Authentication with username/password.
				byte[] user = proxyUser.getBytes(UTF_8);
				byte[] pass = proxyPass.getBytes(UTF_8);
				int userLength = user.length;
				int passLength = pass.length;
				// Check sizes.
				if (userLength > 0xff) {
					throw new IOException(ERROR_PREFIX + "username too long");
				}
				if (passLength > 0xff) {
					throw new IOException(ERROR_PREFIX + "password too long");
				}
				// Version 1.
				out.write(0x01);
				// Username.
				out.write(userLength);
				out.write(user);
				// Password.
				out.write(passLength);
				out.write(pass);
				// Check the response.
				aux = read(in);
				if (aux != 0x01) {
					throw new IOException(ERROR_PREFIX + "invalid proxy response");
				}
				aux = read(in);
				if (aux != 0x00) {
					throw new IOException(ERROR_PREFIX + "authentication failed");
				}
			} else {
				if (aux != 0x00) {
					throw new IOException(ERROR_PREFIX + "proxy requires authentication");
				}
			}
			// FTPConnection request.
			// Version 5.
			out.write(0x05);
			// CONNECT method
			out.write(0x01);
			// Reserved.
			out.write(0x00);
			// Address type -> domain.
			out.write(0x03);
			// Domain.
			byte[] domain = hostname.getBytes(UTF_8);
			if (domain.length > 0xff) {
				throw new IOException(ERROR_PREFIX + "domain name too long");
			}
			out.write(domain.length);
			out.write(domain);
			// Port number.
			out.write(port >> 8);
			out.write(port);

			// FTPConnection response
			// Version?
			aux = read(in);
			if (aux != 0x05) {
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
			// Status?
			aux = read(in);
			switch (aux) {
			case 0x00:
				// Connected!
				break;
			case 0x01:
				throw new IOException(ERROR_PREFIX + "general failure");
			case 0x02:
				throw new IOException(ERROR_PREFIX + "connection not allowed by ruleset");
			case 0x03:
				throw new IOException(ERROR_PREFIX + "network unreachable");
			case 0x04:
				throw new IOException(ERROR_PREFIX + "host unreachable");
			case 0x05:
				throw new IOException(ERROR_PREFIX + "connection refused by destination host");
			case 0x06:
				throw new IOException(ERROR_PREFIX + "TTL expired");
			case 0x07:
				throw new IOException(ERROR_PREFIX + "command not supported / protocol error");
			case 0x08:
				throw new IOException(ERROR_PREFIX + "address type not supported");
			default:
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
			// Reserved.
			in.skip(1);
			// Address type.
			aux = read(in);
			if (aux == 0x01) {
				// IPv4.
				in.skip(4);
			} else if (aux == 0x03) {
				// Domain name.
				aux = read(in);
				in.skip(aux);
			} else if (aux == 0x04) {
				// IPv6.
				in.skip(16);
			} else {
				throw new IOException(ERROR_PREFIX + "invalid proxy response");
			}
			// Port number.
			in.skip(2);
			// Well done!
			connected = true;
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
