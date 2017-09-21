package it.service;

import com.adon.nc.hidefields.entity.HidingGroup;
import com.adon.nc.hidefields.entity.HidingRule;
import com.adon.nc.hidefields.service.HideFieldService;
import com.adon.nc.hidefields.service.HideFieldServiceImpl;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import it.helper.AOHelper;
import net.java.ao.test.ActiveObjectsIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class HideFieldServiceIT extends ActiveObjectsIntegrationTest {

    private static final String USERNAME_1 = "admin";
    private static final String RULE_TITLE_1 = "Rule number 1";
    private static final String RULE_TITLE_2 = "Rule number 2";
    private static final String HIDE_FIELD_ID_3 = "customfield03";
    private static final String HIDE_FIELD_ID_4_3F = "customfield03,summary,ussuetype";
    private static final String ISSUE_KEY_1 = "ASD-1";
    private static final String ISSUE_KEY_2 = "ASD-34";


    private HideFieldService hideFieldService;
    private static ActiveObjects ao;
    private SearchResults sResults;
    private AOHelper aoHelper;


    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);

        hideFieldService = new HideFieldServiceImpl();
        ao.migrate(HidingRule.class, HidingGroup.class);

        final ApplicationUser admin = new MockApplicationUser(USERNAME_1);
        final JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        Mockito.when(jiraAuthenticationContext.getUser()).thenReturn(admin);


        SearchRequestService srs = mock(SearchRequestService.class);
        SearchRequest sRequest = mock(SearchRequest.class);
        SearchProvider searchProvider = mock(SearchProvider.class);
        sResults = mock(SearchResults.class);


        Mockito.when(srs.getFilter(any( JiraServiceContextImpl.class), any(long.class))).thenReturn(sRequest);
        Mockito.when(searchProvider.search(any(Query.class), any(ApplicationUser.class), any(PagerFilter.class))).thenReturn(sResults);



        Map<String, Object> map = new HashMap<>();
        ApplicationProperties props = new MockApplicationProperties(map);
        new MockComponentWorker()
            .addMock(ApplicationProperties.class, props)
            .addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
            .addMock(SearchRequestService.class, srs)
            .addMock(SearchProvider.class, searchProvider)
            .init();
        this.aoHelper = new AOHelper(ao);
    }


    @Test
    public void ShouldReturn3UniqueRulesWhenNoIssueFilterSet() throws SearchException {
        List<HidingRule> ruleList = new ArrayList<>();
        ruleList.add(aoHelper.insertRule(RULE_TITLE_1, HIDE_FIELD_ID_3, 0));
        ruleList.add(aoHelper.insertRule(RULE_TITLE_2, HIDE_FIELD_ID_4_3F, 0));

        Set<String> hideList = hideFieldService.getFieldListToHide(ruleList, "ASF-1");
        assertEquals(3, hideList.size());
    }

    @Test
    public void ShouldReturnZeroFields_WhenJqlFilterIsSetButIssueKeyNotInFilter() {
                List<HidingRule> ruleList = new ArrayList<>();
        ruleList.add(aoHelper.insertRule(RULE_TITLE_2, HIDE_FIELD_ID_4_3F, 5));
        Set<String> hideList = hideFieldService.getFieldListToHide(ruleList, "asdasdasd-1");
        assertEquals(0, hideList.size());
    }
    @Test
    public void ShouldReturn3Fields_WhenJqlFilterIsSetAndIssueWithKeyInJQLFilter() {
        ArrayList<Issue> issueList = new ArrayList<>();
        issueList.add(new MockIssue(1, ISSUE_KEY_1));
        issueList.add(new MockIssue(2, ISSUE_KEY_2));
        Mockito.when(sResults.getIssues()).thenReturn(issueList);

        List<HidingRule> ruleList = new ArrayList<>();
        ruleList.add(aoHelper.insertRule(RULE_TITLE_2, HIDE_FIELD_ID_4_3F, 5));
        Set<String> hideList = hideFieldService.getFieldListToHide(ruleList, ISSUE_KEY_1);
        assertEquals(3, hideList.size());
    }

    @Test
    public void JustTestingCoveragePlugin2() {
        String s = hideFieldService.hideFields("String", new HashSet<String>());
        assertEquals("<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  String\n" +
                " </body>\n" +
                "</html>", s);
    }


}
