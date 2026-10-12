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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public class ActionWrapper {
  private String user;
  
  private String requestID = UUID.randomUUID().toString();
  
  @SerializedName("do")
  private List<Object> doList =  new ArrayList<>();

  public ActionWrapper() {
    
  }
  
  public ActionWrapper(String user, Object firstDo) {
    this.user = user;
    doList.add(firstDo);
  }
  
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public List<Object> getDoList() {
    return doList;
  }

  public void setDoList(List<Object> doList) {
    this.doList = doList;
  }

  public void addDoList(Object obj) {
    doList.add(obj);
  }
  
  public String getRequestID() {
    return requestID;
  }

  public void setRequestID(String requestID) {
    this.requestID = requestID;
  }  

}
