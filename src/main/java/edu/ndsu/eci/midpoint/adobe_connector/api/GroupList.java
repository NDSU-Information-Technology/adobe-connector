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

import com.google.gson.annotations.SerializedName;

public class GroupList {
  @SerializedName("group")
  private List<String> groups;
  
  public GroupList(List<String> groups) {
    this.groups = groups;
  }
  
  public GroupList(String group) {
    groups = new ArrayList<>();
    groups.add(group);
  }
  
}
