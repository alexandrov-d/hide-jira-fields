package com.adon.nc.hidefields.filter;

import com.adon.nc.hidefields.entity.HidingRule;
import com.adon.nc.hidefields.filter.wrapper.HtmlResponseWrapper;
import com.adon.nc.hidefields.service.HideFieldService;
import com.adon.nc.hidefields.service.HidingRuleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.user.UserManager;
import org.apache.velocity.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ModifyResponseFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(ModifyResponseFilter.class);

    private HideFieldService hideFieldService;
    private HidingRuleService hidingRuleService;
    private JiraAuthenticationContext jiraAuthenticationContext;

    public ModifyResponseFilter(HideFieldService hfs, HidingRuleService hrs, UserManager um){
        this.hideFieldService = hfs;
        this.hidingRuleService = hrs;
        jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (response.getContentType() != null && response.getContentType().contains("text/html") && request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            String issueKey = getKeyFromUrl(req.getRequestURI());

            ApplicationUser user = jiraAuthenticationContext.getUser();
            log.debug("IssueKey: {}, User: {}", issueKey, user);
            if( user != null && issueKey != null){
                log.debug("Search rules for user: "  + user.toString());
                List<HidingRule> rules = hidingRuleService.getRulesForUser(user.getUsername());
                Set<String> list = hideFieldService.getFieldListToHide(rules, issueKey);
                if ( list.size() > 0 ){
                    log.debug("Invoking filter on a html content...");
                    HtmlResponseWrapper capturingResponseWrapper = new HtmlResponseWrapper((HttpServletResponse) response);
                    chain.doFilter(request, capturingResponseWrapper);
                    String replacedContent = hideFieldService.hideFields(capturingResponseWrapper.getCaptureAsString(), list);
                    response.getWriter().write(replacedContent);
                    return;
                }
            }

        }

        chain.doFilter(request, response);
    }


    /**
     * get Last element form url - it must be an issueKey
     * @param uri string
     * @return issueKey string
     */
    private String getKeyFromUrl(String uri){
        String[] chunks  = StringUtils.split(uri, "/");
        if (chunks.length > 1 && chunks[chunks.length-2].equals("browse") && !chunks[chunks.length-1].isEmpty()){
            return chunks[chunks.length-1];
        }
        return null;
    }

}
