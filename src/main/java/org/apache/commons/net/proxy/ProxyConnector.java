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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProxyConnector {
    protected final transient Logger log = LoggerFactory.getLogger(getClass());

    /** The socket's ServerSocket Factory. */
    protected ServerSocketFactory _serverSocketFactory_;

	/**
	 * The proxy type.
	 */
	protected String proxySchema;

	/**
	 * The proxy host name.
	 */
	protected String proxyHost;

	/**
	 * The proxy port.
	 */
	protected int proxyPort;

	/**
	 * The proxy authentication.
	 */
	protected Credentials credentials;

	protected boolean haveProxy;

	ProxyAuthenticate authenticate;

	public ProxyConnector() {
		_serverSocketFactory_ = ServerSocketFactory.getDefault();
	}

	public ProxyConnector(String proxyUrl) throws MalformedURLException {
		_serverSocketFactory_ = ServerSocketFactory.getDefault();
		parseProxyUrl(proxyUrl);
	}

	public void setProxyUrl(String proxyUrl) throws MalformedURLException {
		parseProxyUrl(proxyUrl);
	}

	private void parseProxyUrl(String proxyUrl) throws MalformedURLException {
		if(proxyUrl != null && !proxyUrl.isEmpty()) {
    		URL url = new URL(proxyUrl);

    		proxySchema = url.getProtocol();
    		proxyHost = url.getHost();
    		proxyPort = url.getPort();
    		String authority = url.getUserInfo();
    		String proxyUser = null;
    		String proxyPass = null;

    		if(authority == null)
    			authority = url.getAuthority();

    		if(authority != null && !authority.isEmpty()) {
    			int p = authority.indexOf('@');

    			if(p > 0)
    				authority = authority.substring(0, p);

    			p = authority.indexOf(':');
    			proxyUser = p < 0 ? authority : authority.substring(0, p);
    			proxyPass = p < 0 ? "" : authority.substring(p + 1);
    		}
    		credentials = new Credentials(proxyUser, proxyPass);
		}
		else {
			initDefault();
		}
		authenticate = createAuthenticate();
	}

	private void initDefault() {
		proxySchema = null;
		proxyHost = null;
		proxyPort = -1;
		credentials = null;
		haveProxy = false;
		authenticate = null;
	}

	private ProxyAuthenticate createAuthenticate() {
		ProxyAuthenticate authenticate = null;

		if(proxyHost != null && !proxyHost.isEmpty()) {
			if(proxySchema.startsWith("http")) {
				authenticate = new HTTPAuthenticate();
			}
			else if(proxySchema.startsWith("ftp")) {
				authenticate = new FTPAuthenticate();
			}
			else if(proxySchema.startsWith("socks4")) {
				authenticate = new SOCKS4Authenticate();
			}
			else if(proxySchema.startsWith("socks")) {
				authenticate = new SOCKS5Authenticate();
			}

			haveProxy = authenticate != null;
		}
		if(authenticate == null) {
			authenticate = new DirectAuthenticate();
			haveProxy = false;
		}

		return authenticate;
	}

	public boolean isHaveProxy() {
		return true;
	}

	public void setHaveProxy(boolean haveProxy) {
		this.haveProxy = haveProxy;
	}

	//
	// ServerSocket
	//

	public ServerSocket createServerSocket(int port) throws IOException {
		ServerSocket socket = _serverSocketFactory_.createServerSocket(port);
		return socket;
	}

	public ServerSocket createServerSocket(int port, int backlog) throws IOException {
		ServerSocket socket = _serverSocketFactory_.createServerSocket(backlog, backlog);
		return socket;
	}

	public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
		ServerSocket socket = _serverSocketFactory_.createServerSocket(port, backlog, ifAddress);
		return socket;
	}

	//
	// Socket
	//

	public Socket createSocket() throws IOException, UnknownHostException {
		return new ProxySocket();
	}

	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return new ProxySocket(host, port);
	}

	public Socket createSocket(InetAddress host, int port) throws IOException {
		return new ProxySocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
		return new ProxySocket(host, port, localHost, localPort);
	}

	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return new ProxySocket(address, port, localAddress, localPort);
	}

	public class ProxySocket extends Socket {
		public ProxySocket() {
			super();
		}

		public ProxySocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
			super(address, port, localAddr, localPort);
		}

		public ProxySocket(InetAddress address, int port) throws IOException {
			super(address, port);
		}

		public ProxySocket(Proxy proxy) {
			super(proxy);
		}

		public ProxySocket(SocketImpl impl) throws SocketException {
			super(impl);
		}

		public ProxySocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
			super(host, port, localAddr, localPort);
		}

		public ProxySocket(String host, int port) throws UnknownHostException, IOException {
			super(host, port);
		}

		@Override
		public void connect(SocketAddress endpoint, int timeout) throws IOException {
			if(authenticate != null && haveProxy) {
				if (endpoint == null)
					throw new IllegalArgumentException("connect: The address can't be null");

				if (timeout < 0)
					throw new IllegalArgumentException("connect: timeout can't be negative");

				if (isClosed())
					throw new SocketException("Socket is closed");

				if (!(endpoint instanceof InetSocketAddress))
					throw new IllegalArgumentException("Unsupported address type");

				InetSocketAddress epoint = (InetSocketAddress) endpoint;
				int port = epoint.getPort();
				String host = epoint.getHostName();

				if(host == null)
					host = epoint.getAddress().getHostAddress();

				log.trace("ProxySocket connect to {} with handshake", host);

				InetSocketAddress proxyEpoint = new InetSocketAddress(proxyHost, proxyPort);
				super.connect(proxyEpoint, timeout);

				handshake(host, port);
			}
			else {
				log.trace("ProxySocket connect to {}", ((InetSocketAddress) endpoint).getHostName());
				super.connect(endpoint, timeout);
			}
		}

		private void handshake(String host, int port) throws IOException {
			authenticate.authenticate(this, host, port, credentials);

			if (isClosed())
				throw new SocketException("Socket is closed");
		}

		@Override
		public synchronized void close() throws IOException {
			super.close();
		}

	}
}
