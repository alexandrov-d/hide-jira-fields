package com.adon.nc.hidefields.servlet;

import com.adon.nc.hidefields.service.HideFieldService;
import com.adon.nc.hidefields.service.HidingRuleService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ConfigServlet extends HttpServlet {

    private static Logger log = LoggerFactory.getLogger(ConfigServlet.class);

    private final LoginUriProvider loginUriProvider;
    private final UserManager userManager;
    private final TemplateRenderer templateRenderer;
    private final HidingRuleService hidingRuleService;
    private final HideFieldService hideFieldService;
    private final GroupManager groupManager;
    private final SearchRequestService   searchRequestService;


    public ConfigServlet(
            UserManager userManager,
            LoginUriProvider loginUriProvider,
            TemplateRenderer templateRenderer,
            HidingRuleService hidingRuleService,
            HideFieldService hfs,
            GroupManager groupManager,
            SearchRequestService   srs
    ) {
        this.loginUriProvider = loginUriProvider;
        this.userManager = userManager;
        this.templateRenderer = templateRenderer;
        this.hidingRuleService = hidingRuleService;
        this.groupManager = groupManager;
        this.hideFieldService = hfs;
        this.searchRequestService   = srs;
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        UserProfile user = userManager.getRemoteUser(req);
        if (user == null || !userManager.isSystemAdmin(user.getUserKey())){
            log.warn("user doesn't has permissions to view this page");
            redirectToLogin(req, res);
            return;
        }

        Map<String, Object> data = Maps.newHashMap();
        log.debug("Get all issus fields");
        data.put("issueFields", hideFieldService.findAllFields());

        Collection<Group> groups = groupManager.getAllGroups();
        log.debug(groups.size() + " User groups founded.");
        data.put("allGroups", groups);

        log.debug("Getting Hiding rules");
        data.put("ruleList", hidingRuleService.getRules());

        log.debug("Getting jql filters...");
        ApplicationUser appUser = ComponentAccessor.getJiraAuthenticationContext().getUser();
        Collection<SearchRequest> jqlFilters  = searchRequestService.getNonPrivateFilters(appUser);
        log.debug(jqlFilters.size() + " JQL fileters founded.");
        data.put("jqlFilters", jqlFilters);

        res.setContentType("text/html;charset=utf-8");
        templateRenderer.render("templates/init.vm", data, res.getWriter());
    }

    @RequiresXsrfCheck
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        final String title = req.getParameter("title");
        final String[] groups = req.getParameterValues("groups[]");
        final String fieldsToHide = StringUtils.join(req.getParameterValues("fieldsToHide[]"), ',');
        String filter =  req.getParameter("jql-filter");
        final int jqlFilter = filter != null && !filter.isEmpty() ?  Integer.parseInt(filter) : 0;
        String inverse =  req.getParameter("inverse");
        final boolean isInversed = inverse != null && inverse.equals("true");

        log.debug("Posting fields:[ Title:" + title + ", groups: " + groups + ", fieldsToHide: " + fieldsToHide +  "]");

        hidingRuleService.addRule(title, new HashSet<>(Arrays.asList(groups)), fieldsToHide, jqlFilter, isInversed);
        res.sendRedirect(req.getContextPath() + "/plugins/servlet/hide-fields-for-groups");
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request){
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null){
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}