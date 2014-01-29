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

(`GERRIT_USER = username`)

Where "username" is the username set within the User's openstack account.

Installation
--------------
The plugin can be installed by uploading the Plugin.jar file (located at this project's root directory) to the add-ons management screen (`Administration > Add Ons > Manage add-ons > Upload add-on`) within Jira. For the plugin to work properly and retrieve comments from a Gerrit review, the Jira instance must have access to the GERRIT_USER environment variable and ssh keys.

Contributing
--------------
To develop for Atlassian Jira plugins the user must have installed and configured the Atlassian SDK. Further information is available here:

[Atlassian Setup Guide](https://developer.atlassian.com/display/DOCS/Set+up+the+Atlassian+Plugin+SDK+and+Build+a+Project)

If the aforementioned is complete, the user can contribute to this project by cloning the root directory and using standard Atlassian commands.
The following topics are consideration for future iterations:
- Updating a custom field and issue status based on the Gerrit Status retrieved from the Gerrit tab
- Smoother transition between tabs (like when one clicks on the Comments tab)

Testing
--------------
Basic tests have been added for the Whiteboard tab. Future tests include:
- Whiteboard Content Tests
- General Gerrit Tab Tests
- Gerrit Tab Content Tests

