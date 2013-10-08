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
package org.apache.camel.component.file.remote.ext;


import org.apache.camel.component.file.remote.FtpEndpoint;
import org.apache.camel.component.file.remote.RemoteFileComponent;
import org.apache.camel.component.file.remote.RemoteFileConfiguration;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.proxy.ProxySocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FTP endpoint. Read data through a proxy.
 * 
 * Old camel component ftp sintax url without proxy:
 * <pre>
 * from("ftp://{{remoteurl}}/")
 * .log("read file from ftp server");
 * </pre>
 * New camel compoent ftpext sintax with proxy:
 * <pre>
 * // PropertiesComponent have property
 * // proxyUrl=http://user:password@proxy.domain.lan/
 * // or
 * // proxyUrl=http://DOMAIN\\user:password@proxy.domain.lan/
 * // or
 * // proxyUrl=ftp://user:password@proxy.domain.lan/
 * // or
 * // proxyUrl=socks://user:password@proxy.domain.lan/
 * // or
 * // proxyUrl=socks4://user:password@proxy.domain.lan/
 * 
 * from("ftpext://{{remoteurl}}/?ftpClient.proxyUrl={{proxyUrl}}")
 * .log("read file from ftp server ");
 * </pre>
 */
public class FtpEndpointExt<T extends FTPFile> extends FtpEndpoint<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpEndpointExt.class);

	public FtpEndpointExt() {
        super();
    }

    public FtpEndpointExt(String uri, RemoteFileComponent<FTPFile> component, RemoteFileConfiguration configuration) {
        super(uri, component, configuration);
    }

	@Override
	protected FTPClient createFtpClient() throws Exception {
		FTPClient client = super.createFtpClient();
		String proxyUrl = null;
		FtpConfigurationExt configurationExt = null;

		if(configuration instanceof FtpConfigurationExt) {
			configurationExt = (FtpConfigurationExt) configuration;
			proxyUrl = configurationExt.getProxyUrl();
		}

		if(proxyUrl == null && ftpClientParameters != null) {
			// Read ftpClient.proxyUrl property from uri.
			// Since commons-net 3.3, SoketClient (FtpClien) add proxy property.
			// Change old ftpClient.proxy to ftpClient.proxyUrl 
			proxyUrl = (String) ftpClientParameters.get("proxyUrl");
		}

		if(proxyUrl != null) {
			LOGGER.trace("ftp use proxy {} change config to passive mode", proxyUrl);
			getConfiguration().setPassiveMode(true);
			client.setSocketFactory(new ProxySocketFactory(proxyUrl));
		}
		return client;
	}
}
