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

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class User {

  private String email;
  @SerializedName("firstname")
  private String firstName;
  @SerializedName("lastname")
  private String lastName;
  private String country;
  private String type;
  private List<String> groups;
  private String username;
  
  
  
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  public String getCountry() {
    return country;
  }
  public void setCountry(String country) {
    this.country = country;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public List<String> getGroups() {
    return groups;
  }
  public void setGroups(List<String> groups) {
    this.groups = groups;
  }
  public Boolean isActive(String activeGroupName) {
    if (groups == null) {
      return false;
    }
    for (String group : groups) {
      if (activeGroupName.equals(group)) {
        return true;
      }
    }
    
    return false;
  }
  
  public boolean isFederated() {
    return "federatedID".equals(type);
  }
  
}
