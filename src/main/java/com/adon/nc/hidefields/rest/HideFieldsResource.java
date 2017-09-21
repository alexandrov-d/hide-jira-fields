package com.adon.nc.hidefields.rest;

import com.adon.nc.hidefields.entity.HidingRule;
import com.adon.nc.hidefields.service.HideFieldService;
import com.adon.nc.hidefields.service.HidingRuleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/rules")
@Produces({MediaType.APPLICATION_JSON})
public class HideFieldsResource {

    private static Logger log = LoggerFactory.getLogger(HideFieldsResource.class);

    private final HidingRuleService hidingRuleService;
    private final HideFieldService hideFieldService;
    private final JiraAuthenticationContext jiraAuthenticationContext;


    public HideFieldsResource( HidingRuleService hidingRuleService, HideFieldService hfs,  UserManager um){
        this.hidingRuleService = hidingRuleService;
        this.hideFieldService = hfs;
        jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }

    @POST
    @Path("/update")
    public Response update(@Context HttpServletRequest req) throws JSONException {

        final int id                    = Integer.parseInt(req.getParameter("id"));
        final String title              = req.getParameter("title");
        final String[] groups           = req.getParameterValues("groups");
        final String fieldsToHide       = StringUtils.join(req.getParameterValues("fieldsToHide"), ',');
        final String jqlF = req.getParameter("jqlFilter");
        int jqlFilter = 0;
        if ( jqlF != null && !jqlF.isEmpty()){
            jqlFilter = Integer.parseInt(jqlF);
        }
        String inverse =  req.getParameter("inverse");
        final boolean isInversed = inverse != null && inverse.equals("true");

        log.debug("Params to update [ ID: " + id + ", Title: " + title + ". Groups: "+ groups + ", Fields: '" + fieldsToHide + "', jqlFilter ID: " + jqlFilter + ", Inversed:" + isInversed + "]");

        if (id < 1 || groups == null || !hidingRuleService.updateRule(id, title, new HashSet<>(Arrays.asList(groups)), fieldsToHide, jqlFilter, isInversed)){
            log.error("Some problem during rule update");
            return Response.status(205).build();
        }
        return Response.noContent().build();
    }


    @GET
    @Path("/remove/{id}")
    public Response removeRule(@PathParam("id") int id) throws JSONException {
        if ( hidingRuleService.deleteRuleById(id) > 0){
            return Response.noContent().build();
        }else{
            return Response.status(205).build();
        }
    }


    @GET
    @Path("/get-fields-to-hide/{key}")
    public Response getFieldsToHide(@Context HttpServletRequest req, @PathParam("key") String issueKey) throws SearchException {
        ApplicationUser user = jiraAuthenticationContext.getUser();
        log.debug("Searching rules for user: {} ", user);

        List<HidingRule> rules = hidingRuleService.getRulesForUser(user.getUsername());
        Set<String> list = hideFieldService.getFieldListToHide(rules, issueKey);

        return Response.ok(list).build();
    }


}