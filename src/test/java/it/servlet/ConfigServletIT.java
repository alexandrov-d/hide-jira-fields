package it.servlet;

import com.adon.nc.hidefields.service.HideFieldService;
import com.adon.nc.hidefields.service.HidingRuleService;
import com.adon.nc.hidefields.servlet.ConfigServlet;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.*;


public class ConfigServletIT {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private UserManager userManager;
    private HideFieldService hfs;
    private HidingRuleService hrs;
    private ConfigServlet servlet;
    private TemplateRenderer renderer;
    private LoginUriProvider loginUriProvider;

    @Before
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        userManager = mock(UserManager.class);
        hfs = mock(HideFieldService.class);
        hrs = mock(HidingRuleService.class);
        renderer = mock(TemplateRenderer.class);
        loginUriProvider = mock(LoginUriProvider.class);

        servlet = new ConfigServlet( userManager,
            loginUriProvider,
            renderer, hrs, hfs,
            mock(GroupManager.class),
            mock(SearchRequestService.class)
        );

    }

    @Test
    public void ShouldInvokeRedirectWhenNoUser() throws IOException, ServletException {
        when(request.getRequestURL()).thenReturn(new StringBuffer("/some-url"));
        when(loginUriProvider.getLoginUri(any(URI.class))).thenReturn(URI.create("dsadsd"));
        servlet.doGet(request, response);
        verify(response, times(1)).sendRedirect("dsadsd");
    }

    @Test
    public void ShouldRedirectToRightPath() throws IOException, ServletException {
        when(request.getParameter("jql-filter")).thenReturn("");
        when(request.getParameter("inverse")).thenReturn(null);
        when(request.getParameterValues("groups[]")).thenReturn(new String[]{});
        servlet.doPost(request, response);
        verify(response, times(1)).sendRedirect(contains("/plugins/servlet/hide-fields-for-groups"));
    }





}
