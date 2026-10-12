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
package edu.ndsu.eci.midpoint.adobe_connector.api;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
  @SerializedName("token_type")
  private String type;
  @SerializedName("access_token")
  private String token;
  @SerializedName("expires_in")
  private int expires;
  
  private long validUntil;
  
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getToken() {
    return token;
  }
  public void setToken(String token) {
    this.token = token;
  }
  public int getExpires() {
    return expires;
  }
  public void setExpires(int expires) {
    this.expires = expires;
    calculateValid();
  }
  
  public boolean isValid() {
    return System.currentTimeMillis() < validUntil - 600_000;
  }
  
  /**
   * Calculate valid until value. GSON doesn't call setter, so it is being not called there.
   */
  public void calculateValid() {
    validUntil = System.currentTimeMillis() + (expires * 1_000);    
  }
}
