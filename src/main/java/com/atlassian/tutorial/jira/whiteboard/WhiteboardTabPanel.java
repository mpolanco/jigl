package com.atlassian.tutorial.jira.whiteboard;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;

public class WhiteboardTabPanel extends AbstractIssueTabPanel implements
		IssueTabPanel {
	private static final Logger log = LoggerFactory
			.getLogger(WhiteboardTabPanel.class);

	public List getActions(Issue issue, User remoteUser) {
		List<GenericMessageAction> messages = new ArrayList<GenericMessageAction>();

		try {
			CustomField bpLinkField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Launchpad Blueprint Link");
			
			String bpLink = issue.getCustomFieldValue(bpLinkField).toString();

			String url = "http://api.launchpad.net/devel/trove/+spec/replication";

			URL obj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

			boolean redirect = false;

			// normally, 3xx is redirect
			int status = conn.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
						|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}

			System.err.println("Response Code ... " + status);

			if (redirect) {

				// get redirect url from "location" header field
				String newUrl = conn.getHeaderField("Location");

				// get the cookie if need, for login
				String cookies = conn.getHeaderField("Set-Cookie");

				// open the new connnection again
				conn = (HttpURLConnection) new URL(newUrl).openConnection();
				//conn.setRequestProperty("Cookie", cookies);

				System.err.println("Redirect to URL : " + newUrl);

			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String inputLine;
			StringBuffer html = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				html.append(inputLine);
			}
			in.close();

			System.err.println("URL Content... \n" + html.toString());
			
			JSONTokener tokener = new JSONTokener( html.toString() );
		    JSONObject finalResult = new JSONObject( tokener );
		    messages.add(new GenericMessageAction((String) finalResult.get("whiteboard")));
		    
			System.err.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return messages;
	}

	public boolean showPanel(Issue issue, User remoteUser) {
		return true;
	}
}
