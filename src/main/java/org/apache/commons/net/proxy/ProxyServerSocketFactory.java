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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

public class ProxyServerSocketFactory extends ServerSocketFactory {
	private ProxyConnector connector;
	
	public ProxyServerSocketFactory(String proxyUrl) throws MalformedURLException {
		this.connector = new ProxyConnector();
		connector.setProxyUrl(proxyUrl);
	}
	
	public ProxyServerSocketFactory(ProxyConnector connector) {
		this.connector = connector;
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		return connector.createServerSocket(port);
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		return connector.createServerSocket(port, backlog);
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
		return connector.createServerSocket(port, backlog, ifAddress);
	}

}
