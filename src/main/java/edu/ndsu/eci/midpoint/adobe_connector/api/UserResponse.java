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

public class UserResponse {
  private String result;
  
  private String message;
  
  private User user;

  /**
   * The status of the request. One of success or an error key: { "success", "error", "error.apikey.invalid", "error.user.email.invalid", "error.api.user.not.parent.org", "error.organization.invalid_id" }
   * @return result string
   */
  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  /**
   * An error message, returned only if initial validation of the request fails. It is not populated when a 200 status is returned.
   * @return message, null if 200
   */
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
  
  
}
