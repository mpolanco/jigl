package com.atlassian.tutorial.jira.gerrittabpanel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;

/**
 * @author marc7279
 * GerritTabPanel creates the "Gerrit" tab in a Jira issue. For a given git hash, the tab
 * will retrieve and display the related review comments.
 */
public class GerritTabPanel extends AbstractIssueTabPanel implements
		IssueTabPanel {
	private final String HOST = "review.openstack.org";
	private final String USER = "rackjira";
	private final int PORT = 29418;
	private final String KEY_COMMENTS = "comments";
	private final String GERRIT_ID_FIELD_NAME = "Gerrit ID";
	
	/* (non-Javadoc)
	 * @see com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel#getActions(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
	 */
	@Override
	public List<IssueAction> getActions(Issue issue, User remoteUser) {
		List<IssueAction> messages = new ArrayList<IssueAction>();
		
		//read and validate the Gerrit commit hash
		CustomField gerritLinkFieldContent = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(GERRIT_ID_FIELD_NAME);
		if(gerritLinkFieldContent == null)
		{
			messages.add(new GenericMessageAction("\"" + GERRIT_ID_FIELD_NAME + "\" custom field not available. Cannot process Gerrit Review comments"));
			return messages;
		}
		
		Object gitHashFieldObj = issue.getCustomFieldValue(gerritLinkFieldContent);
		if (gitHashFieldObj == null)
		{
			messages.add(new GenericMessageAction("\"" + GERRIT_ID_FIELD_NAME + "\" not provided. Please provide Gerrit ID to see review comments for this issue."));
			return messages;
		}
		
		String gitHash = gitHashFieldObj.toString().trim();
		if(gitHash.length() == 0)
		{
			messages.add(new GenericMessageAction("To view related Gerrit review comments for this issue please provide the Git Commit Hash"));
			return messages;
		}
		
		else if(!isValidGitHash(gitHash))
		{
			messages.add(new GenericMessageAction("Gerrit Commit ID not properly formatted"));
			return messages;
		}
		
		//frame command to access SSH API
		String sshCom = "ssh -i /home/jira/.ssh/id_rsa -p " + PORT + " " + USER + "@" + HOST + " gerrit query --format=json --current-patch-set --comments change:" + gitHash;
		
		try {
			Process p = Runtime.getRuntime().exec(sshCom);
		    p.waitFor();
		    BufferedReader reader = 
		         new BufferedReader(new InputStreamReader(p.getInputStream()));
		 
		    String line = reader.readLine();
		    if (line != null)
		    {
		    	JSONTokener tokener = new JSONTokener(line);
		    	JSONObject finalResult = new JSONObject(tokener);
		    	if (!finalResult.has(KEY_COMMENTS))
		    	{
		    		messages.add(new GenericMessageAction("No comments available for this Gerrit review."));
					return messages;
		    	}
		    	
		    	JSONArray commentsJson = finalResult.getJSONArray(KEY_COMMENTS);
		    	for (int k = 0; k < commentsJson.length(); k++)
		    	{
		    		JSONObject commentJson = commentsJson.getJSONObject(k);
		    		GerritCommentAction commentAction = createCommentActionFromJSON(commentJson);
		    		messages.add(commentAction);
		    	}
		    	
		    	String status = finalResult.getString("status");
		    	messages.add(new GenericMessageAction("<h2>Gerrit Status: " + status + "</h2>"));
		    	
		    	//reverse message order to display most recent first
		    	Collections.reverse(messages);
		    }

		} catch (JSONException e) {
			// JSON returned from SSH API did not contain proper keys
			e.printStackTrace();
		} catch (InterruptedException e) {
			// Exception in attempting to execute the ssh command
			e.printStackTrace();
		} catch (IOException e) {
			// Exception in reading from the input stream
			e.printStackTrace();
		} 
		
		return messages;
	}
	
	private boolean isValidGitHash(String gitHash)
	{
		//TODO: better hash verification?
		return gitHash.matches("[-a-zA-Z0-9]*");
	}
	
	private GerritCommentAction createCommentActionFromJSON(JSONObject commentJSON)
	{
		GerritCommentAction commentText = null;
		try 
		{	
			JSONObject reviewerJSON = commentJSON.getJSONObject("reviewer");
			String reviewer = reviewerJSON.getString("name");
			String username = reviewerJSON.getString("username");
			String message = commentJSON.getString("message");
			Date commentDate = new Date(commentJSON.getLong("timestamp") * DateUtils.SECOND_MILLIS);
						
			commentText = new GerritCommentAction(commentDate, reviewer, username, message);
			
		} 
		catch (JSONException e) 
		{
			// JSON returned from SSH API did not have a key available
			e.printStackTrace();
		}
		
		return commentText;
	}

	/* (non-Javadoc)
	 * @see com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel#showPanel(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see com.atlassian.jira.plugin.issuetabpanel.IssueAction#getTimePerformed()
	 */
	@Override
	public Date getTimePerformed() {
		return commentDate;
	}

	/* (non-Javadoc)
	 * @see com.atlassian.jira.plugin.issuetabpanel.IssueAction#getHtml()
	 */
	@Override
	public String getHtml() {
		// convert date for time tags in html
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mmZ");
		String date = formatter.format(commentDate).replaceAll(" ", "T");
		
		// append url tags to register links
		String urlRegex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		String newMessage = message.replaceAll(urlRegex, "<a href=\"$0\">$0</a>").replaceAll("\n", "<br/>"); // replace new lines with break tags
		
		String commentHTML = "" +
		"<div class='issue-data-block activity-comment twixi-block  expanded'>" +
		"  <div class='twixi-wrap verbose actionContainer'>" +
		"    <div class='action-head'>" +
		"      <a href='#' class='twixi'><span class='icon twixi-opened'><span>Hide</span></span></a>" +
		"      <div class='action-details'>" +
		"        <a class='user-hover user-avatar' rel=" + username + " href='#'><span class='aui-avatar aui-avatar-xsmall'><span class='aui-avatar-inner'><img src='/jira/secure/useravatar?size=xsmall&amp;avatarId=10122'></span></span>" + reviewer + " (" + username + ") </a>" +
		"        added a comment - <span class='subText'><span class='date user-tz'><time class='livestamp' datetime='" + date + "'></time></span></span>" +
		"      </div>" +
		"    </div>" +
		"    <div class='action-body flooded'>" +
		"      <p>" + newMessage + "</p>" +
		"    </div>" +
		"  </div>" +
		"  <div class='twixi-wrap concise actionContainer'>" +
		"    <div class='action-head'>" +
		"      <a href='#' class='twixi'><span class='icon twixi-closed'><span>Show</span></span></a>" +
		"      <div class='action-details flooded'>" +
		"        <a class='user-hover user-avatar' rel=" + username + " href='#'><span class='aui-avatar aui-avatar-xsmall'><span class='aui-avatar-inner'><img src='/jira/secure/useravatar?size=xsmall&amp;avatarId=10122'></span></span>" + reviewer + " (" + username + ") </a>" +
		"        added a comment - <span class='subText'><span class='date user-tz'><time class='livestamp' datetime='" + date + "'></time></span></span>" + "  |  " + message + "</div>" +
		"    </div>" +
		"  </div>" +
		"</div>";
		
		return commentHTML;
	}

	/* (non-Javadoc)
	 * @see com.atlassian.jira.plugin.issuetabpanel.IssueAction#isDisplayActionAllTab()
	 */
	@Override
	public boolean isDisplayActionAllTab() {
		return false;
	}

}
