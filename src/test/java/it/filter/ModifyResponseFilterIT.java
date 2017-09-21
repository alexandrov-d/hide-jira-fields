package it.filter;

import com.adon.nc.hidefields.filter.ModifyResponseFilter;
import com.adon.nc.hidefields.service.HideFieldService;
import com.adon.nc.hidefields.service.HidingRuleService;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class ModifyResponseFilterIT {

    private HttpServletRequest mockRequest;
    private HttpServletResponse  mockResponse;
    private FilterChain chain;
    private UserManager  userManager;
    private HideFieldService hfs;
    private HidingRuleService hrs;

    @Before
    public void setUp() {
        final ApplicationUser admin = new MockApplicationUser("admin");
        final JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(jiraAuthenticationContext.getUser()).thenReturn(admin);
        new MockComponentWorker()
                .addMock(JiraAuthenticationContext.class, jiraAuthenticationContext)
                .init();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        userManager = mock(UserManager.class);
        hfs = mock(HideFieldService.class);
        hrs = mock(HidingRuleService.class);
    }

    @Test
    public void shouldReturnUnmodifedPageWhenNoUserOrNoIssueKey() throws IOException, ServletException {
        when(mockResponse.getContentType()).thenReturn("text/html");
        ModifyResponseFilter servlet = new ModifyResponseFilter(hfs, hrs, userManager );

        when(mockRequest.getRequestURI()).thenReturn("/jira/browse/");

        servlet.doFilter(mockRequest, mockResponse, chain);
        verify(mockResponse, never()).getWriter();
    }

    @Test
    public void shouldInvokeInnerServicesWhenUserAndIssuKeyProvided() throws IOException, ServletException {
        when(mockResponse.getContentType()).thenReturn("text/html");
        ModifyResponseFilter servlet = new ModifyResponseFilter(hfs, hrs, userManager );

        final String  ISSUE_KEY = "ES-8";
        when(mockRequest.getRequestURI()).thenReturn("/jira/browse/" + ISSUE_KEY);

        final String  user = "admin";
        when(userManager.getRemoteUserKey()).thenReturn(new UserKey(user));

        servlet.doFilter(mockRequest, mockResponse, chain);
        verify(hrs, times(1)).getRulesForUser(user);
    }

    @Test
    public void JustTestingCoveragePlugin() throws ServletException {
        ModifyResponseFilter filter = new ModifyResponseFilter(hfs, hrs, userManager );
        filter.init(Mockito.mock(FilterConfig.class));
        filter.destroy();
    }


}
