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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.RetryableException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;

import com.google.gson.Gson;

import edu.ndsu.eci.midpoint.adobe_connector.api.AuthResponse;
import edu.ndsu.eci.midpoint.adobe_connector.api.CreateFederated;
import edu.ndsu.eci.midpoint.adobe_connector.api.GroupList;
import edu.ndsu.eci.midpoint.adobe_connector.api.RemoveFromOrg;
import edu.ndsu.eci.midpoint.adobe_connector.api.RemoveGroup;
import edu.ndsu.eci.midpoint.adobe_connector.api.UpdateUser;
import edu.ndsu.eci.midpoint.adobe_connector.api.UpdateWrapper;
import edu.ndsu.eci.midpoint.adobe_connector.api.User;
import edu.ndsu.eci.midpoint.adobe_connector.api.UserInfo;
import edu.ndsu.eci.midpoint.adobe_connector.api.UserResponse;
import edu.ndsu.eci.midpoint.adobe_connector.api.UsersResponse;
import edu.ndsu.eci.midpoint.adobe_connector.api.ActionResponse;
import edu.ndsu.eci.midpoint.adobe_connector.api.ActionWrapper;
import edu.ndsu.eci.midpoint.adobe_connector.api.AddGroup;

public class AdobeConnection {

  private static final int RETRY_COUNT = 5;
  
  private AdobeConfiguration config;
  private AuthResponse auth;
  private Gson gson = new Gson();
  private CloseableHttpClient http = HttpClients.createDefault();
  
  public AdobeConnection(AdobeConfiguration config) {
    this.config = config;
  }
  
  private void login() {
    HttpPost post = new HttpPost("https://ims-na1.adobelogin.com/ims/token/v3");
    List<NameValuePair> pairs = new ArrayList<>(); 
    pairs.add(new BasicNameValuePair("client_id", config.getClientId()));
    pairs.add(new BasicNameValuePair("client_secret", config.getClientSecret()));
    pairs.add(new BasicNameValuePair("grant_type" , "client_credentials"));
    pairs.add(new BasicNameValuePair("scope" , "openid,user_management_sdk,AdobeID"));
    post.setEntity(new UrlEncodedFormEntity(pairs));
    CloseableHttpResponse resp = null;
    String json;
    try {
      resp = http.execute(post);
      HttpEntity entity = resp.getEntity();
      json = EntityUtils.toString(entity);
      EntityUtils.consume(entity);
      resp.close();
      if (resp.getCode() != 200) {
        throw new ConnectionFailedException(json);
      }
    } catch (IOException | ParseException e) {
      throw new ConnectionFailedException(e);
    } finally {
      closeQuietly(resp);
    }

    auth = gson.fromJson(json, AuthResponse.class);
    auth.calculateValid();
  }
  
  /**
   * Get the auth token. Handles refreshes
   * @return token that is valid for at least 10 minutes
   */
  public synchronized String getToken() {
    if (auth != null && auth.isValid()) {
      return auth.getToken();
    }
    login();
    
    return auth.getToken();
  }
  
  
  private void closeQuietly(CloseableHttpResponse resp) {
    if (resp == null) {
      return;
    }
    try {
      resp.close();
    } catch (IOException e) {
      // ignore this on purpose
    }
  }
  
  public String retryablePost(Object obj, int retryCount) {
    HttpPost post = new HttpPost("https://usermanagement.adobe.io/v2/usermanagement/action/" + config.getOrgId());
    post.addHeader("Authorization", "Bearer " + getToken());
    post.addHeader("x-api-key", config.getClientId());
    StringEntity entity = new StringEntity(gson.toJson(obj), ContentType.APPLICATION_JSON);
    post.setEntity(entity);
    CloseableHttpResponse resp = null;
    String body;
    try {
      resp = http.execute(post);
      HttpEntity rEntity = resp.getEntity();
      body = EntityUtils.toString(rEntity);
      EntityUtils.consume(rEntity);
      resp.close();
    } catch (IOException | ParseException e) {
      throw new ConnectorIOException(e);
    } finally {
      closeQuietly(resp);
    }

    if (resp.getCode() != 200) {
      if (resp.getCode() == 429) {
        if (retryCount > RETRY_COUNT) {
          throw RetryableException.wrap("Too many 429s", new RuntimeException("Too many 429s"));
        }
        try {
          Header retry = resp.getFirstHeader("Retry-After");
          //https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html#actionThrottle
          int sleep = Integer.valueOf(retry.getValue()) * 1_000;
          Thread.sleep(sleep);
          return retryablePost(obj, retryCount++);
        } catch (InterruptedException e) {
         // ignored
        }
      }
      throw new ConnectorException("Error: " + resp.getCode() + " while getting users");
    }
    
    return body;
  }
  
  /**
   * Deletes a user from the organization
   * Will handle running the same user twice no problem
   * @param user username to delete.
   */
  public void deleteUser(String user) {
    ActionWrapper wrapper = new ActionWrapper();
    wrapper.setUser(user);
    wrapper.addDoList(new RemoveFromOrg());
    List<ActionWrapper> list = new ArrayList<>();
    list.add(wrapper);
    System.out.println(gson.toJson(list));

    String response = retryablePost(list, 0);
    System.out.println(response);
    handleActionResponse(response);
  }
  
  public void createUser(String username, String email, String firstName, String lastName, String country) {
    UserInfo info = new UserInfo(email, firstName, lastName, country);
    ActionWrapper wrapper = new ActionWrapper(username, new CreateFederated(info));
    wrapper.addDoList(new AddGroup(new GroupList(config.getActiveGroupName())));
    List<ActionWrapper> list = new ArrayList<>();
    list.add(wrapper);
    String response = retryablePost(list, 0);
    handleActionResponse(response);
  }
 
  public void updateUser(String userId, UpdateUser user, Boolean active) {
    ActionWrapper wrapper = new ActionWrapper();
    wrapper.setUser(userId);
    if (user != null) {
      wrapper.addDoList(new UpdateWrapper(user));
    }
    
    if (active != null && active) {
      wrapper.addDoList(new AddGroup(new GroupList(config.getActiveGroupName())));
    } else if (active != null && !active) {
      wrapper.addDoList(new RemoveGroup(new GroupList(config.getActiveGroupName())));
    }
    List<ActionWrapper> list = new ArrayList<>();
    list.add(wrapper);
    String response = retryablePost(list, 0);

    handleActionResponse(response);
  }

  /**
   * @param response
   */
  private void handleActionResponse(String response) {
    ActionResponse ar = gson.fromJson(response, ActionResponse.class);
    if (ar.isSuccess()) {
      return;
    }
    
    if (ar.isUserIdNotExist()) {
      throw new UnknownUidException(response);
    }
    
    throw new ConnectorException(response);
  }
  
  public String retryableGet(String url, int retryCount) {
    HttpGet get = new HttpGet(url);
    get.addHeader("Authorization", "Bearer " + getToken());
    get.addHeader("x-api-key", config.getClientId());
    CloseableHttpResponse resp = null;
    String body;
    try {
      resp = http.execute(get);
      HttpEntity rEntity = resp.getEntity();
      body = EntityUtils.toString(rEntity);
      EntityUtils.consume(rEntity);
      resp.close();
    } catch (IOException | ParseException e) {
      throw new ConnectorIOException(e);
    } finally {
      closeQuietly(resp);
    }

    if (resp.getCode() != 200) {
      if (resp.getCode() == 429) {
        if (retryCount > RETRY_COUNT) {
          throw RetryableException.wrap("Too many 429s", new RuntimeException("Too many 429s"));
        }
        try {
          Header retry = resp.getFirstHeader("Retry-After");
          //https://adobe-apiplatform.github.io/umapi-documentation/en/api/ActionsRef.html#actionThrottle
          int sleep = Integer.valueOf(retry.getValue()) * 1_000;
          Thread.sleep(sleep);
          return retryableGet(url, retryCount++);
        } catch (InterruptedException e) {
         // ignored
        }
      }
      if (resp.getCode() == 404) {
        throw new UnknownUidException(body);
      }
      
      throw new ConnectorException("Error: " + resp.getCode() + " while getting users");
    }
    
    return body;
  }
  
  public User getUser(String user) {
    String body = retryableGet("https://usermanagement.adobe.io/v2/usermanagement/organizations/" + config.getOrgId() + "/users/" + user, 0);
    UserResponse resp = gson.fromJson(body, UserResponse.class);
    return resp.getUser();
  }
  
  public List<User> getUsers() {
    List<User> users = new ArrayList<>();
    int page = 0;
    UsersResponse resp = null;
    do {
      String body = retryableGet("https://usermanagement.adobe.io/v2/usermanagement/users/" + config.getOrgId() + "/" + page, 0);
      resp = gson.fromJson(body, UsersResponse.class);
      users.addAll(resp.getUsers());
      page++;
    } while (!resp.isLastPage());
    return users;
  }
}
