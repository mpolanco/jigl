Copyright 2014 Rackspace Hosting

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Gerrit-Launchpad Jira Plugin
--------------

Information
--------------
This plugin adds the following tabs to the view issue screen:

Whiteboard - displays the whiteboard in its current status as it would be displayed on a Launchpad blueprint

Gerrit - displays the comments posted on a Gerrit review for a given commit id

Pre-Requesites
--------------
In order for this plugin to fully work, the user must have/complete the following:

1. An Openstack account configured with the user's public ssh key (this is to access comments for the Gerrit review tab as currently the only available means of accessing these is via an SSH API). 

2. The following Custom Fields must be added to the Jira environment under Issue workflow screens:
	- Gerrit ID
	- Launchpad Blueprint URL
	
	To have these fields added, please contact your Jira Administrator.

3. The following environment variable must be added to the environment such that it is accessible to the running Jira instance:

	`GERRIT_USER = username`

	Where "username" is the username set within the User's openstack account.

Installation
--------------
The plugin can be installed by uploading the Plugin.jar file (located at this project's root directory) to the add-ons management screen (`Administration > Add Ons > Manage add-ons > Upload add-on`) within Jira. For the plugin to work properly and retrieve comments from a Gerrit review, the Jira instance must have access to the GERRIT_USER environment variable and ssh keys.

Contributing
--------------
To develop for Atlassian Jira plugins the user must have installed and configured the Atlassian SDK. Further information is available here:

[Atlassian Setup Guide](https://developer.atlassian.com/display/DOCS/Set+up+the+Atlassian+Plugin+SDK+and+Build+a+Project)

If the aforementioned is complete, the user can contribute to this project by cloning the root directory and using standard Atlassian commands.
The following topics are in consideration for future iterations:
- Updating a custom field and issue status based on the Gerrit Status retrieved from the Gerrit tab
- Smoother transition between tabs (like when one clicks on the Comments tab)

Testing
--------------
Basic tests have been added for the Whiteboard tab. Future tests include:
- Whiteboard Content Tests
- General Gerrit Tab Tests
- Gerrit Tab Content Tests

To run the test suite the user must have the Atlassian SDK installed so that they can then run `atlas-unit-test` in the root of this project.
