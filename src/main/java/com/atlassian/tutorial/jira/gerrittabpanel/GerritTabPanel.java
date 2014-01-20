package com.atlassian.tutorial.jira.gerrittabpanel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.omg.CORBA.REBIND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.tabpanels.*;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.*;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;

public class GerritTabPanel extends AbstractIssueTabPanel implements
		IssueTabPanel {
	private static final Logger log = LoggerFactory
			.getLogger(GerritTabPanel.class);
	private static final String HOST = "review.openstack.org";
	private static final String USER = "mpolanco";
	private static final int PORT = 29418;
	private static final String COMMENTS = "comments";

	public List getActions(Issue issue, User remoteUser) {
		List<IssueAction> messages = new ArrayList<IssueAction>();
		
		try {
			CustomField gerritLinkField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Git Commit ID");
			String gitID = issue.getCustomFieldValue(gerritLinkField).toString();
			System.err.println("GID" + gitID);
			
			Process p = Runtime.getRuntime().exec("ssh -p 29418 mpolanco@review.openstack.org gerrit query --format=json --current-patch-set --comments change:" + gitID); //I741b3a39e1ec24ee81d441788be72f7272");
		    p.waitFor();
		    
		    System.err.println("waiting");
		 
		    BufferedReader reader = 
		         new BufferedReader(new InputStreamReader(p.getInputStream()));
		 
		    String line = "";		
		    line = reader.readLine();
		    if (line != null)
		    {
		    	System.err.println("line: " + line);
		    	JSONTokener tokener = new JSONTokener(line);
		    	JSONObject finalResult = new JSONObject(tokener);
		    	JSONArray comments = finalResult.getJSONArray(COMMENTS);
		    	for (int k = 0; k < comments.length(); k++)
		    	{
		    		JSONObject commentJSON = comments.getJSONObject(k);
		    		GerritCommentAction commentText = formatComment(commentJSON);
		    		messages.add(commentText);
		    	}
		    	
		    	Collections.reverse(messages);
		    	
		    	System.err.println(finalResult);
		    }

		} catch (Exception e) {
			System.err.println(e);
		}

		
		return messages;
	}
	
	private GerritCommentAction formatComment(JSONObject commentJSON)
	{
		GerritCommentAction commentText = null;
		try 
		{
			Calendar calendar = Calendar.getInstance();
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			
			JSONObject reviewerJSON = commentJSON.getJSONObject("reviewer");
			String reviewer = reviewerJSON.getString("name");
			String username = reviewerJSON.getString("username");
			
			calendar.setTimeInMillis(commentJSON.getLong("timestamp"));
			Date commentDate = calendar.getTime();
			
			String message = commentJSON.getString("message") + "\n\n\n";
			
			commentText = new GerritCommentAction(commentDate, reviewer, username, message);
			
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return commentText;
	}

	public boolean showPanel(Issue issue, User remoteUser) {
		return true;
	}
}

class GerritCommentAction implements IssueAction {

	private final String reviewer;
	private final String username;
	private final String message;
	private final Date commentDate;

	public GerritCommentAction(Date date, String reviewer, String username,
			String message) {
		this.reviewer = reviewer;
		this.message = message;
		this.commentDate = date;
		this.username = username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction#getTimePerformed
	 * ()
	 */
	@Override
	public Date getTimePerformed() {
		return commentDate;
	}

	@Override
	public String getHtml() {
		// TODO Auto-generated method stub
		String html = "<div class='issue-data-block activity-comment twixi-block  expanded'>" + 
	    "<div class='twixi-wrap verbose actionContainer'>" + 
	    "    <div class='action-head'>" +  
	    "        <div class='action-details'>" + 
	    "			<a class='user-hover user-avatar' rel='" + username + "' href=#>" +
	    "			<span class='aui-avatar aui-avatar-xsmall'>" +
	    "			<span class='aui-avatar-inner'>" +
	    "			<img src='/jira/secure/useravatar?size=xsmall&amp;avatarId=10122'>" +
	    "			</span>" +
	    "			</span> " +
	    "			" + reviewer + " (" + username + ")" +
	    "			</a>" +
	    "		added a comment  - " +
	    "		<span class='commentdate_10000_verbose subText'>" +
	    "		<span class='date user-tz' title='17/Jan/14 1:59 PM'>" +
	    "		<time class='livestamp' datetime='2014-01-17T13:59-0600'>2 hours ago</time>" +
	    "		</span></span>  " +
	    "		</div>" +
	    "	</div>" +
	    "	<div class='action-body flooded'>" +
	    "		<p>" + message +
	    "		</p> " +
	    "	</div>" +
	    "</div>" +
	    "</div>";
		return html;
	}

	@Override
	public boolean isDisplayActionAllTab() {
		// TODO Auto-generated method stub
		return false;
	}

}
