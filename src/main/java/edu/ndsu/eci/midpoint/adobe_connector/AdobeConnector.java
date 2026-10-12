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

import java.util.List;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.AttributesAccessor;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

import edu.ndsu.eci.midpoint.adobe_connector.api.UpdateUser;
import edu.ndsu.eci.midpoint.adobe_connector.api.User;

@ConnectorClass(displayNameKey = "campusgroups.connector.display", configurationClass = AdobeConfiguration.class)
public class AdobeConnector implements PoolableConnector, TestOp, SchemaOp, SearchOp<Filter>, CreateOp, UpdateOp, DeleteOp {

  private static final String FIRST_NAME = "firstname";
  private static final String LAST_NAME = "lastname";
  private static final String COUNTRY = "country";
  private static final String ACTIVE = "active";
  private static final String TYPE = "type";
  private static final String EMAIL = "email";


  private AdobeConfiguration config;
  private AdobeConnection connection;
  
  @Override
  public Configuration getConfiguration() {
    return config;
  }

  @Override
  public void init(Configuration cfg) {
    this.config = (AdobeConfiguration) cfg;
    connection = new AdobeConnection(config);
  }

  @Override
  public void dispose() {
    // no-op
  }

  @Override
  public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
    connection.deleteUser(uid.getName());
  }

  @Override
  public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
    AttributesAccessor accessor = new AttributesAccessor(replaceAttributes);
    
    UpdateUser user = new UpdateUser();
    
    Uid newUid = uid;
    boolean userChanged = false;
    
    if (accessor.getName() != null) {
      user.setUsername(accessor.getName().getNameValue());
      newUid = new Uid(user.getUsername());
      userChanged = true;
    }

    if (accessor.hasAttribute(EMAIL)) {
     user.setEmail(accessor.findString(EMAIL));
     userChanged = true;
    }
    
    if (accessor.hasAttribute(FIRST_NAME)) {
      user.setFirstName(accessor.findString(FIRST_NAME));
      userChanged = true;
    }

    if (accessor.hasAttribute(LAST_NAME)) {
      user.setLastName(accessor.findString(LAST_NAME));
      userChanged = true;
    }
    

    
    Boolean active = null;
    if (accessor.hasAttribute(ACTIVE)) {
      active = accessor.findBoolean(ACTIVE);
    }
    
    if (!userChanged) {
      user = null;
    }
    
    connection.updateUser(uid.getUidValue(), user, active);
    
    return newUid;
  }

  @Override
  public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
    AttributesAccessor accessor = new AttributesAccessor(createAttributes);
    connection.createUser(accessor.getName().getNameValue(), accessor.findString(EMAIL), accessor.findString(FIRST_NAME), accessor.findString(LAST_NAME), accessor.findString(COUNTRY));
    
    return new Uid(accessor.getName().getNameValue());
  }

  @Override
  public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
    return new FilterTranslator<Filter>() {
      public List<Filter> translate(Filter filter) {
          return CollectionUtil.newList(filter);
      }
    };
  }

  @Override
  public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
    if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
      User user = connection.getUser(((Uid)((EqualsFilter)query).getAttribute()).getUidValue());
      if (user == null) {
        return;
      }
      
      if (config.getFederatedOnly() && !user.isFederated()) {
        return;
      }
      
      handler.handle(convertUserToConnectorObject(user));
      
      return;
    }

    // get all of the users;
    for (User user : connection.getUsers()) {
      if (config.getFederatedOnly() && !user.isFederated()) {
        continue;
      }
      handler.handle(convertUserToConnectorObject(user));
    }
  }

  @Override
  public Schema schema() {
    ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
    objectClassBuilder.setType("adobeAccount");
    
    AttributeInfoBuilder uidAib = new AttributeInfoBuilder(Uid.NAME);
    uidAib.setNativeName("username");
    uidAib.setRequired(true);
    objectClassBuilder.addAttributeInfo(uidAib.build());
    
    AttributeInfoBuilder nameAib = new AttributeInfoBuilder(Name.NAME);
    nameAib.setNativeName("username");
    nameAib.setRequired(true);
    objectClassBuilder.addAttributeInfo(nameAib.build());
    
    AttributeInfoBuilder emailAib = new AttributeInfoBuilder(EMAIL);
    emailAib.setNativeName("email");
    emailAib.setRequired(true);
    objectClassBuilder.addAttributeInfo(emailAib.build());
    
    AttributeInfoBuilder firstNameAib = new AttributeInfoBuilder(FIRST_NAME);
    firstNameAib.setNativeName("firstname");
    firstNameAib.setRequired(true);
    objectClassBuilder.addAttributeInfo(firstNameAib.build());
    
    AttributeInfoBuilder lastNameAib = new AttributeInfoBuilder(LAST_NAME);
    lastNameAib.setNativeName("lastname");
    lastNameAib.setRequired(true);
    objectClassBuilder.addAttributeInfo(lastNameAib.build());
    
    AttributeInfoBuilder countryAib = new AttributeInfoBuilder(COUNTRY);
    countryAib.setNativeName("country");
    countryAib.setUpdateable(false);
    countryAib.setRequired(true);
    objectClassBuilder.addAttributeInfo(countryAib.build());
    
    AttributeInfoBuilder statusAib = new AttributeInfoBuilder(ACTIVE);
    statusAib.setNativeName("status");
    statusAib.setType(Boolean.class);
    objectClassBuilder.addAttributeInfo(statusAib.build());
    
    AttributeInfoBuilder typeAib = new AttributeInfoBuilder(TYPE);
    typeAib.setNativeName("type");
    typeAib.setCreateable(false);
    typeAib.setUpdateable(false);
    objectClassBuilder.addAttributeInfo(typeAib.build());
    
    SchemaBuilder schemaBuilder = new SchemaBuilder(AdobeConnector.class);
    schemaBuilder.defineObjectClass(objectClassBuilder.build());
    return schemaBuilder.build();
  }

  @Override
  public void test() {
    connection.getToken();
  }

  @Override
  public void checkAlive() {
    connection.getToken();
  }
  
  private ConnectorObject convertUserToConnectorObject(User user) {
    return new ConnectorObjectBuilder()
        .setUid(user.getUsername())
        .setName(user.getUsername())
        .addAttribute(EMAIL, user.getEmail())
        .addAttribute(FIRST_NAME, user.getFirstName())
        .addAttribute(LAST_NAME, user.getLastName())
        .addAttribute(COUNTRY, user.getCountry())
        .addAttribute(TYPE, user.getType())
        .addAttribute(ACTIVE, user.isActive(config.getActiveGroupName()))
        .build();
  }
}
