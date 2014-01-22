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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;

public class GerritTabPanel extends AbstractIssueTabPanel implements
		IssueTabPanel {
	private static final Logger log = LoggerFactory
			.getLogger(GerritTabPanel.class);
	private final String HOST = "review.openstack.org";
	private final String USER = "mpolanco";
	private final int PORT = 29418;
	private final String KEY_COMMENTS = "comments";
	
	public List getActions(Issue issue, User remoteUser) {
		List<IssueAction> messages = new ArrayList<IssueAction>();
		
		CustomField gerritLinkField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Git Commit ID");
		String gitID = issue.getCustomFieldValue(gerritLinkField).toString();
		if(!gitID.matches("[-a-zA-Z0-9]*"))
		{
			messages.add(new GenericMessageAction("Commit ID not properly formatted"));
			return messages;
		}
		
		String sshCom = "ssh -p " + PORT + " " + USER + "@" + HOST + " gerrit query --format=json --current-patch-set --comments change:" + gitID;
		
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
		    	JSONArray comments = finalResult.getJSONArray(KEY_COMMENTS);
		    	for (int k = 0; k < comments.length(); k++)
		    	{
		    		JSONObject commentJSON = comments.getJSONObject(k);
		    		GerritCommentAction commentAction= createCommentActionFromJSON(commentJSON);
		    		messages.add(commentAction);
		    	}
		    	
		    	//reverse message order to display most recent first
		    	Collections.reverse(messages);
		    }

		} catch (Exception e) {
			System.err.println(e);
		}

		
		return messages;
	}
	
	private GerritCommentAction createCommentActionFromJSON(JSONObject commentJSON)
	{
		GerritCommentAction commentText = null;
		try 
		{
			Calendar calendar = Calendar.getInstance();
			
			JSONObject reviewerJSON = commentJSON.getJSONObject("reviewer");
			String reviewer = reviewerJSON.getString("name");
			String username = reviewerJSON.getString("username");
			
			calendar.setTimeInMillis(commentJSON.getLong("timestamp") * DateUtils.SECOND_MILLIS);
			Date commentDate = calendar.getTime();
			
			String message = commentJSON.getString("message");
			
			/*FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
			RendererManager rendererManager = ComponentAccessor.getRendererManager();
			CommentManager commentManager = ComponentAccessor.getCommentManager();
			ApplicationUser commenter = ComponentAccessor.getUserManager().getUserByNameEvenWhenUnknown(reviewer);
			
			
			
			CommentImpl comment = new CommentImpl(commentManager, commenter, commenter, message, "", 1L, commentDate, commentDate, issue);
			Comment comment = commentManager.create(issue, commenter, message, "", 0L, commentDate, false);
			Comment comment = commentManager.create(issue, commenter, message, false);
			
			IssueTabPanelModuleDescriptor;
			
			commentText = new CommentAction(this.descriptor, comment, false, true, false, rendererManager, fieldLayoutManager, dateTimeFormatter);
			
			System.err.println(commenter.); */
			
			
			commentText = new GerritCommentAction(commentDate, reviewer, username, message);
			
		} 
		catch (Exception e) 
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
		
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mmZ");
		String date = formatter.format(commentDate).replaceAll(" ", "T");
		
		String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		String newMessage = message.replaceAll(regex, "<a href=\"$0\">$0</a>").replaceAll("\n", "<br/>");
		
		String html = "" +
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
		
		return html;
	}

	@Override
	public boolean isDisplayActionAllTab() {
		return false;
	}

}
