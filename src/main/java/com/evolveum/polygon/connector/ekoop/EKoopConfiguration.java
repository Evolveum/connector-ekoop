/*
 * Copyright (c) 2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.polygon.connector.ekoop;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import java.net.MalformedURLException;
import java.net.URL;

import static org.identityconnectors.common.StringUtil.isBlank;

/**
 * E-Koop Connector configuration.
 *
 * @author gpalos
 */
public class EKoopConfiguration extends AbstractConfiguration {

    private static final Log LOG = Log.getLog(EKoopConfiguration.class);

    /**
     * The E-Koop SOAP endpoint.
     */
    private String endpoint;

    /**
     * If true, accept all HTTPS certificates, default false - only certificates in {midpoint.home}\keystore.jceks, @see https://wiki.evolveum.com/display/midPoint/Keystore+Configuration.
     */
    private Boolean trustingAllCertificates = false;

    /**
     * If true, use symulated ekoop system in memory over @com.evolveum.polygon.connector.RealServiceImpl as mockup @see com.evolveum.polygon.connector.DummyServiceImpl.
     */
    private Boolean useMockup = false;


    @Override
    public void validate() {
        if (isBlank(endpoint))
            throw new ConfigurationException("endpoint is empty");

        try {
            new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Malformed endpoint: " + endpoint, e);
        }
    }

    @ConfigurationProperty(displayMessageKey = "ekoop.config.endpoint",
            helpMessageKey = "ekoop.config.endpoint.help")
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @ConfigurationProperty(displayMessageKey = "ekoop.config.trustingAllCertificates",
            helpMessageKey = "ekoop.config.trustingAllCertificates.help")
    public Boolean getTrustingAllCertificates() {
        return trustingAllCertificates;
    }

    public void setTrustingAllCertificates(Boolean trustingAllCertificates) {
        this.trustingAllCertificates = trustingAllCertificates;
    }

    @ConfigurationProperty(displayMessageKey = "ekoop.config.useMockup",
            helpMessageKey = "ekoop.config.useMockup.help")
    public Boolean getUseMockup() {
        return useMockup;
    }

    public void setUseMockup(Boolean useMockup) {
        this.useMockup = useMockup;
    }

}