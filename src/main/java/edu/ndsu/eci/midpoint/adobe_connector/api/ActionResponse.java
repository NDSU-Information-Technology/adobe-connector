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

public class ActionResponse {

  private int completed;
  private int notCompleted;
  private String result;
  
  private List<ErrorVal> errors;

  public int getCompleted() {
    return completed;
  }

  public void setCompleted(int completed) {
    this.completed = completed;
  }

  public int getNotCompleted() {
    return notCompleted;
  }

  public void setNotCompleted(int notCompleted) {
    this.notCompleted = notCompleted;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public List<ErrorVal> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorVal> errors) {
    this.errors = errors;
  }
  
  public boolean isSuccess() {
    return "success".equals(result);
  }
  
  public boolean isUserIdNotExist() {
    if (errors.isEmpty()) {
      return false;
    }
    
    for (ErrorVal val : errors) {
      if ("error.user.nonexistent".equals(val.getErrorCode()) || "error.user.not_found".equals(val.getErrorCode())) {
        return true;
      }
    }
    return false;
  }
}
