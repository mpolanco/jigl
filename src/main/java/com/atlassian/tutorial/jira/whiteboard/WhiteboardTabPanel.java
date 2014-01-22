package com.atlassian.tutorial.jira.whiteboard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;

public class WhiteboardTabPanel extends AbstractIssueTabPanel implements
		IssueTabPanel {
	
	private final String BASE_LAUNCHPAD_API_URL = "https://api.launchpad.net/";
	private final String API_VERSION = "devel";	// currently devel level of the api is the only means of accessing the whiteboard data
	private final String BASE_LAUNCHPAD_BLUEPRINT_HOST = "blueprints.launchpad.net";

	public List<IssueAction> getActions(Issue issue, User remoteUser) {
		List<IssueAction> messages = new ArrayList<IssueAction>();

		try 
		{
			CustomField bpLinkField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Launchpad Blueprint Link");
			
			String bpLink = issue.getCustomFieldValue(bpLinkField).toString();
			
			String url = BASE_LAUNCHPAD_API_URL + API_VERSION + bpLink.substring((bpLink.lastIndexOf(BASE_LAUNCHPAD_BLUEPRINT_HOST) + BASE_LAUNCHPAD_BLUEPRINT_HOST.length()));

			// TODO: make api url more robust
			
			URL obj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

			boolean redirect = false;

			// normally, 3xx is redirect
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) 
			{
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
						|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}

			if (redirect) 
			{
				// get redirect url from "location" header field
				String newUrl = conn.getHeaderField("Location");

				// open the new connnection again
				conn = (HttpURLConnection) new URL(newUrl).openConnection();
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String inputLine;
			StringBuffer json = new StringBuffer();

			while ((inputLine = in.readLine()) != null) 
			{
				json.append(inputLine);
			}
			in.close();
			
			JSONTokener tokener = new JSONTokener( json.toString() );
		    JSONObject finalResult = new JSONObject( tokener );
		    messages.add(new GenericMessageAction(finalResult.getString("whiteboard")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return messages;
	}

	public boolean showPanel(Issue issue, User remoteUser) {
		return true;
	}
}
