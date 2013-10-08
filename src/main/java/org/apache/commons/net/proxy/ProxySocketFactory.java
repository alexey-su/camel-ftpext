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
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

/**
 * Proxy Soket Factory.
 * Foe example:
 * <pre>
 * String proxyUrl = "http://user:password@proxy.domain.lan/";
 * SocketFactory socketFactory = new ProxySocketFactory(proxyUrl);
 * FTPClient client = new FTPClient();
 * client.setSocketFactory(socketFactory);
 * client.connect(remotehost, remoteport);
 * </pre>
 * 
 * @author Alexey Sushko
 */
public class ProxySocketFactory extends SocketFactory {
	private ProxyConnector connector;
	
	public ProxySocketFactory() {
	}
	
	public ProxySocketFactory(String proxyUrl) throws MalformedURLException {
		this.connector = new ProxyConnector(proxyUrl);
	}
	
	public void setProxyUrl(String proxyUrl) throws MalformedURLException {
		this.connector = new ProxyConnector(proxyUrl);		
	}

	@Override
	public Socket createSocket() throws IOException {
		return connector.createSocket();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {		
		return connector.createSocket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return connector.createSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
		return connector.createSocket(host, port, localHost, localPort);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return connector.createSocket(address, port, localAddress, localPort);
	}
}
