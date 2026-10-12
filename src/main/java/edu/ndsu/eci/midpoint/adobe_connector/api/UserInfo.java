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

public class UserInfo {

  /** can only be set on create */
  private String country;
  
  /** email / username */
  private String email;
  
  /** first name */
  @SerializedName("firstname")
  private String firstName;
  
  /** last name */
  @SerializedName("lastname")
  private String lastName;
  
  /** option to make this an update too*/
  private String option = "updateIfAlreadyExists";
  
  public UserInfo() {
    
  }
  
  public UserInfo(String email, String firstName, String lastName, String country) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.country = country;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
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

  public String getOption() {
    return option;
  }

  public void setOption(String option) {
    this.option = option;
  }
  
  
}
