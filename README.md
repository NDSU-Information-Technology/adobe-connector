<!--
Copyright (c) 2026 North Dakota State University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
# Adobe connector for ConnID and midPoint

## Overview

Connector for Adobe using ConnId. This is provided as-is.

Copyright 2026 North Dakota State University and released under the Apache License, Version 2.0.

Used by NDSU to manage Adobe. For support see a midPoint support organization like Unicon. Pull requests welcome.

Deployed on midPoint 4.10. 

## Assumptions

This is setup to operate in the way required by NDSU. If that doesn't work for your institution, pull requests to add functionality are welcome.

Group management is done by Grouper, and not this connector.

However, a group to track "active" users is created. See documentation below for further details.

Only manages federated user types. 

Adobe has significant rate limiting. See documentation below for more on that.

# Setup

Needs to be setup in the Adobe developer console. It needs access to the User Management API.

## Configuration

<table>
  <tr>
    <th>Key</th>
    <th>Value</th>
  </tr>
  <tr>
    <td>clientId</td>
    <td>Client ID from OAuth</td>
  </tr>
  <tr>
    <td>clientSecret</td>
    <td>Client secret from OAuth</td>
  </tr>
  <tr>
    <td>orgId</td>
    <td>In the URL when looking at the admin console. Include the <strong>@AdobeOrg</strong> in this value</td>
  </tr>
  <tr>
    <td>activeGroupName</td>
    <td>Name of the group in Adobe to keep track of who is "active".</td>
  </tr>
  <tr>
    <td>federatedOnly</td>
    <td>Boolean, if true search will only return federated accounts. Can help with duplicates while cleaning up.</td>
  </tr>
</table>

## Schema

User type **adobeAccount**

<table>
  <tr>
    <th>Attribute</th>
    <th>Type</th>
    <th>Comment</th>
  </tr>
  <tr>
    <td>firstname</td>
    <td>String</td>
    <td>First name. Required.</td>
  </tr>
  <tr>
    <td>lastname</td>
    <td>String</td>
    <td>Last name. Required.</td>
  </tr>
  <tr>
    <td>country</td>
    <td>String</td>
    <td>Country code, Required, Not Updateable</td>
  </tr>
  <tr>
    <td>username</td>
    <td>String</td>
    <td>__UID__ and __NAME__ likely domain scoped and same as email. This can be updated as usernames change. Required</td>
  </tr>
  <tr>
    <td>email</td>
    <td>String</td>
    <td>Email, Required</td>
  </tr>
  <tr>
    <td>status</td>
    <td>Boolean</td>
    <td>If the account should be considered active or not.</td>
  </tr>
  <tr>
    <td>type</td>
    <td>String</td>
    <td>Type of account. When creating will only be federatedID, when reading it can be any valid value. Read only.</td>
  </tr>  
</table>

## Administrative Status

Adobe doesn't have a real method of disabling users. So to support a delayed delete to prevent early deletion of data, an "Active" group is used. Users that have a **status** of **true** will be put in the active group defined in configuration. They will be removed when **false**. You still need to perform access control in your IdP/OP in order to stop the user from being able to log into Adobe. This just gives midPoint something to look at to "disable" the user and time out the delayed delete.

## Federated Users only

This only creates and updates federated users. You should be running an IdP/OP for Adobe to give your users SSO and manage everything that way. If you need to support enterpriseIds or adobeIds, this connector is not for you. The **federatedOnly** configuration option is to limit the user searches to just federatedIds. This is helpful if you have a bunch of duplicated accounts between federatedIds and adobeIds and you need time to clean it up.

If your correlation will differentiate between federated and Adobe IDs, you don't need to flip that boolean.

It is also suggested that you disable the creation of Adobe IDs, otherwise they likely will keep showing up and causing trouble.

## Rate limiting

Adobe has a rate limit of 20 changes per minute. This is not very high. This connector should handle everything correctly to work around that limit and the 429's it throws. However, it is recommended to do shadow caching in order to reach out to the service as little as possible, as it will slow all of your other reconciles down considerably if the user has to be looked up in Adobe. It is generally good advice to use shadow caching for all remote resources, but especially so for a slow one like this. NDSU runs a reconcile once a week and has shadow caching set to 15 days to ensure that other tasks don't reach out to Adobe.

Actual changes to Adobe are also slow. You may want to set up this connector using [Provision propagation](https://docs.evolveum.com/midpoint/features/current/provisioning-propagation/) to delay provisioning to Adobe. This means that large changes coming from a policy group in Grouper won't slow down the Grouper Live Sync task, and would be performed by another task. This is what NDSU is doing. 