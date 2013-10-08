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
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.remote.FtpComponent;
import org.apache.camel.component.file.remote.FtpConfiguration;
import org.apache.camel.component.file.remote.FtpEndpoint;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP Component
 */
public class FtpComponentExt extends FtpComponent {

    public FtpComponentExt() {
    }

    public FtpComponentExt(CamelContext context) {
        super(context);
    }

    @Override
    protected GenericFileEndpoint<FTPFile> buildFileEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        String baseUri = getBaseUri(uri);

        // lets make sure we create a new configuration as each endpoint can customize its own version
        // must pass on baseUri to the configuration (see above)
        FtpConfiguration config = createConfig(baseUri);

        FtpEndpoint<FTPFile> answer = new FtpEndpointExt<FTPFile>(uri, this, config);
        extractAndSetFtpClientConfigParameters(parameters, answer);
        extractAndSetFtpClientParameters(parameters, answer);

        return answer;
    }

    protected FtpConfiguration createConfig(String baseUri) throws Exception {
    	return new FtpConfigurationExt(new URI(baseUri));
    }

}
