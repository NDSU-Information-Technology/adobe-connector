/*
 * Copyright (c) 2026 North Dakota State University
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
package edu.ndsu.eci.midpoint.adobe_connector;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class AdobeConfiguration extends AbstractConfiguration {
  private static final Log LOG = Log.getLog(AdobeConfiguration.class);
  
  private String clientId;
  private String clientSecret;
  private String orgId;
  private String activeGroupName;
  private Boolean federatedOnly = false;
  
  @Override
  public void validate() {
    if (clientId == null) {
      LOG.error("clientId can't be null");
      throw new ConfigurationException("clientId can't be null");
    }
    
    if (clientSecret == null) {
      LOG.error("clientSecret can't be null");
      throw new ConfigurationException("clientSecret can't be null");
    }

    if (orgId == null) {
      LOG.error("orgId can't be null");
      throw new ConfigurationException("orgId can't be null");
    }
    
    if (activeGroupName == null) {
      LOG.error("Active Group Name can't be null");
      throw new ConfigurationException("Active Group Name can't be null");
    }
    
  }

  @ConfigurationProperty
  public Boolean getFederatedOnly() {
    return federatedOnly;
  }

  public void setFederatedOnly(Boolean federatedOnly) {
    this.federatedOnly = federatedOnly;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getActiveGroupName() {
    return activeGroupName;
  }

  public void setActiveGroupName(String activeGroupName) {
    this.activeGroupName = activeGroupName;
  }

}
