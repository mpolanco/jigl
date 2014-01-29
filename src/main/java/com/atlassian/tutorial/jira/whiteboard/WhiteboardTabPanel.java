package com.atlassian.tutorial.jira.whiteboard;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;

/**
 * @author marc7279
 * WhiteboardTabPanel creates the "Whiteboard" tab in Jira. For a given Launchpad
 * blueprint URL, the tab will retrieve and display the related whiteboard.
 */
public class WhiteboardTabPanel extends AbstractIssueTabPanel implements
		IssueTabPanel {
	
	private final String BASE_LAUNCHPAD_API_URL = "https://api.launchpad.net/";
	private final String API_VERSION = "devel";	// currently devel level of the api is the only means of accessing the whiteboard data
	private final String BASE_LAUNCHPAD_BLUEPRINT_HOST = "blueprints.launchpad.net";
	private final String URL_REGEX = "\\b(https?|http)://blueprints.launchpad.net/[-a-zA-Z0-9+&@#%?=~_|!:,.;]*/[+]spec/[-a-zA-Z0-9+&@#%=~_|]*[-a-zA-Z0-9+&@#/%=~_|]";
	private final String WHITEBOARD = "whiteboard";
	private final String LAUNCHPAD_URL_FIELD_NAME = "Launchpad Blueprint URL";

	/* (non-Javadoc)
	 * @see com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel#getActions(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
	 */
	@Override
	public List<IssueAction> getActions(Issue issue, User remoteUser) {
		List<IssueAction> messages = new ArrayList<IssueAction>();

		//retrieve and validate url from custom field
		CustomField bpLinkField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(LAUNCHPAD_URL_FIELD_NAME);
		
		if(bpLinkField == null)
		{
			messages.add(new GenericMessageAction("\"" + LAUNCHPAD_URL_FIELD_NAME + "\" custom field not available. Cannot process Gerrit Review comments"));
			return messages;
		}
		
		Object bpURLFieldObj = issue.getCustomFieldValue(bpLinkField);
		if (bpURLFieldObj == null)
		{
			messages.add(new GenericMessageAction("\"" + LAUNCHPAD_URL_FIELD_NAME + "\" not provided. Please provide the Launchpad URL to view the whiteboard for this issue."));
			return messages;
		}
		
		String bpURL = bpURLFieldObj.toString().trim();
		
		if(bpURL.length() == 0)
		{
			messages.add(new GenericMessageAction("To view the Launchpad Blueprint for this issue please provide the " + LAUNCHPAD_URL_FIELD_NAME));
			return messages;
		}

		if ( !bpURL.matches(URL_REGEX) )
		{
			messages.add(new GenericMessageAction("Launchpad URL not properly formatted. Please provide URL that appears as follows:<br/> https://blueprints.launchpad.net/devel/PROJECT NAME/+spec/BLUEPRINT NAME"));
			return messages;
		}
		
		String apiQuery = bpURL.substring((bpURL.lastIndexOf(BASE_LAUNCHPAD_BLUEPRINT_HOST) + BASE_LAUNCHPAD_BLUEPRINT_HOST.length()));
		
		String url = BASE_LAUNCHPAD_API_URL + API_VERSION + apiQuery;
		
		try 
		{			
			//establish api connection
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

				// open the new connection
				conn = (HttpURLConnection) new URL(newUrl).openConnection();
			}

			//parse returned json
			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String inputLine;
			StringBuffer json = new StringBuffer();

			while ((inputLine = in.readLine()) != null) 
			{
				json.append(inputLine);
			}
			in.close();
			
			//generate tab content
			JSONTokener tokener = new JSONTokener( json.toString() );
		    JSONObject finalResult = new JSONObject( tokener );
		    if ( finalResult.has(WHITEBOARD) && finalResult.getString(WHITEBOARD).length() > 0)
		    {
		    	messages.add(new GenericMessageAction(escapeHtml(finalResult.getString(WHITEBOARD)).replaceAll("\n", "<br/>")));
		    }
		    else
		    {
		    	messages.add(new GenericMessageAction("No whiteboard for this blueprint."));
		    }
		    
		} catch (JSONException e) {
			// whiteboard JSON key not found
			e.printStackTrace();
		} catch (FileNotFoundException e){
			//unable to find the requested whiteboard
			messages.add(new GenericMessageAction("Unable to find desired blueprint. Please check that the URL references the correct blueprint."));
			return messages;
		} catch (IOException e) {
			// Exception in attempting to read from Launchpad API
			e.printStackTrace();
		}

		return messages;
	}

	/* (non-Javadoc)
	 * @see com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel#showPanel(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
	 */
	@Override
	public boolean showPanel(Issue issue, User remoteUser) {
		return true;
	}
}
