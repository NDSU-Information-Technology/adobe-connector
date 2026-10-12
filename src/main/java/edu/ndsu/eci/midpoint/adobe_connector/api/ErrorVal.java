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

/**
 * error is reserved
 */
public class ErrorVal {
  private int index;
  private int step;
  private String message;
  private String errorCode;
  private String user;
  public int getIndex() {
    return index;
  }
  public void setIndex(int index) {
    this.index = index;
  }
  public int getStep() {
    return step;
  }
  public void setStep(int step) {
    this.step = step;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public String getErrorCode() {
    return errorCode;
  }
  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }


}
