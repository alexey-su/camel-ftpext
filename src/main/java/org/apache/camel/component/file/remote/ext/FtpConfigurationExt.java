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

import java.net.URI;

import org.apache.camel.component.file.remote.FtpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpConfigurationExt extends FtpConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(FtpConfigurationExt.class);

	private String proxyUrl;

	public FtpConfigurationExt() {
		super();
	}

	public FtpConfigurationExt(URI uri) {
		super(uri);
		setProtocol("ftp");
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public void setProxy(String proxyUrl) {
		this.proxyUrl = proxyUrl;
		LOGGER.trace("set proxyUrl {}", proxyUrl);

		if(proxyUrl != null && !proxyUrl.isEmpty()) {
			setPassiveMode(true);
		}
	}
}
