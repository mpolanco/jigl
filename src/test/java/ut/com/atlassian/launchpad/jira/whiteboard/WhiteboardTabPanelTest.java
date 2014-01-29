package ut.com.atlassian.launchpad.jira.whiteboard;

import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.jira.mock.*;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.managers.MockCustomFieldManager;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.user.MockUser;
import com.atlassian.launchpad.jira.whiteboard.WhiteboardTabPanel;

/**
 * @since 3.5
 */
@RunWith(MockitoJUnitRunner.class)
public class WhiteboardTabPanelTest extends TestCase {

	@Mock
	private Issue mockIssue = new MockIssue();
	@Mock
	private User mockUser = new MockUser("Test");
	@Mock
	private CustomField mockCustomField;
	@Mock
	private MockComponentWorker mw;
	@Mock
	private MockCustomFieldManager cfm;
	
	private WhiteboardTabPanel testPanel;
	private final String LAUNCHPAD_URL_FIELD_NAME = "Launchpad Blueprint URL";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		testPanel = new WhiteboardTabPanel();
		mw = new MockComponentWorker()
			.addMock(CustomFieldManager.class, cfm)
			.init();
		ComponentAccessor.initialiseWorker(mw);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test(expected=Exception.class)
    public void testSomething() throws Exception {

        //GerritTabPanel testClass = new GerritTabPanel();

        throw new Exception("GerritTabPanel has no tests!");

    }
	
	/*
	@Test
	public void testCustomFieldNotAvailable() throws Exception {
		String expectedMessage = "\"" + LAUNCHPAD_URL_FIELD_NAME + "\" custom field not available. Cannot process Gerrit Review comments";
		when(
				ComponentAccessor.getCustomFieldManager()
						.getCustomFieldObjectByName(LAUNCHPAD_URL_FIELD_NAME))
				.thenReturn(null);

		List<IssueAction> actions = testPanel.getActions(mockIssue, mockUser);
		assertTrue("Number of messages should be 1 but received: " + actions.size(), actions.size() == 1);
		assertEquals(expectedMessage, actions.get(0).getHtml());
	}
	
	@Test
	public void testCustomFieldValueReturn() throws Exception {
		String expectedMessage = "\"" + LAUNCHPAD_URL_FIELD_NAME + "\" not provided. Please provide the Launchpad URL to view the whiteboard for this issue.";
		when(
				ComponentAccessor.getCustomFieldManager()
						.getCustomFieldObjectByName(LAUNCHPAD_URL_FIELD_NAME))
				.thenReturn(mockCustomField);
		
		when(mockIssue.getCustomFieldValue(mockCustomField)).thenReturn(null);
		
		List<IssueAction> actions = testPanel.getActions(mockIssue, mockUser);
		assertTrue("Number of messages should be 1 but received: " + actions.size(), actions.size() == 1);
		assertEquals(expectedMessage, actions.get(0).getHtml());
	}
	
	@Test
	public void testCustomFieldValueReturnZeroLength() throws Exception {
		String expectedMessage = "To view the Launchpad Blueprint for this issue please provide the " + LAUNCHPAD_URL_FIELD_NAME;
		when(
				ComponentAccessor.getCustomFieldManager()
						.getCustomFieldObjectByName(LAUNCHPAD_URL_FIELD_NAME))
				.thenReturn(mockCustomField);
		
		when(mockIssue.getCustomFieldValue(mockCustomField)).thenReturn("");
		
		List<IssueAction> actions = testPanel.getActions(mockIssue, mockUser);
		assertTrue("Number of messages should be 1 but received: " + actions.size(), actions.size() == 1);
		assertEquals(expectedMessage, actions.get(0).getHtml());
	}
	
	@Test
	public void testCustomFieldValueReturnMatchesURLRegex() throws Exception {
		String expectedMessage = "Launchpad URL not properly formatted. Please provide URL that appears as follows:<br/> https://blueprints.launchpad.net/devel/PROJECT NAME/+spec/BLUEPRINT NAME";
		when(
				ComponentAccessor.getCustomFieldManager()
						.getCustomFieldObjectByName(LAUNCHPAD_URL_FIELD_NAME))
				.thenReturn(mockCustomField);
		
		when(mockIssue.getCustomFieldValue(mockCustomField)).thenReturn("test");
		
		List<IssueAction> actions = testPanel.getActions(mockIssue, mockUser);
		assertTrue("Number of messages should be 1 but received: " + actions.size(), actions.size() == 1);
		assertEquals(expectedMessage, actions.get(0).getHtml());
	}
	*/
}
