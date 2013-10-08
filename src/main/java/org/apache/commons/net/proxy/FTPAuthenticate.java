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
import java.net.Socket;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPAuthenticate implements ProxyAuthenticate {
	private static final String ERROR_PREFIX = "HTTP proxy authenticate: ";

	@Override
	public void authenticate(Socket socket, String hostname, int port, Credentials credentials) 
			throws IOException {

		FTPClient ftpClient = new LocalFTPClient(socket);

		String proxyUser = credentials.getUserName();
		String proxyPass = credentials.getPassword();
		
		// Welcome message.
		int reply;		
		try {
			reply = ftpClient.getReply();
		} catch (IOException e) {
			throw new IOException(ERROR_PREFIX + "Invalid proxy response", e);
		}
		// Does this reply mean "ok"?
		if (reply != FTPReply.SERVICE_READY) {
			// Mmmmm... it seems no!
			throw new IOException(ERROR_PREFIX + "Invalid proxy response");
		}
		if (proxyUser != null && !proxyUser.isEmpty()) {
			// Usefull flags.
			boolean passwordRequired;
			// Send the user and read the reply.
			try {
				reply = ftpClient.sendCommand("USER " + proxyUser);
			} catch (IOException e) {
				throw new IOException(ERROR_PREFIX + "Invalid proxy response", e);
			}
			switch (reply) {
			case FTPReply.USER_LOGGED_IN:
				// Password isn't required.
				passwordRequired = false;
				break;
			case FTPReply.NEED_PASSWORD:
				// Password is required.
				passwordRequired = true;
				break;
			default:
				// User validation failed.
				throw new IOException(ERROR_PREFIX + "Proxy authentication failed");
			}
			// Password.
			if (passwordRequired) {
				// Send the password.
				try {
					reply = ftpClient.sendCommand("PASS " + proxyPass);
				} catch (IOException e) {
					throw new IOException(ERROR_PREFIX + "Invalid proxy response");
				}
				if (reply != FTPReply.USER_LOGGED_IN) {
					// Authentication failed.
					throw new IOException(ERROR_PREFIX + "Proxy authentication failed");
				}
			}
			ftpClient.sendCommand("SITE " + hostname + ":" + port);
		} else {
			ftpClient.sendCommand("OPEN " + hostname + ":" + port);
		}
	}
	
	class LocalFTPClient extends FTPClient {

		public LocalFTPClient(Socket socket) throws IOException {
			this._socket_ = socket;
			_connectAction_();
		}
	}
}
