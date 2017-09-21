package com.adon.nc.hidefields.service;


import com.adon.nc.hidefields.entity.HidingRule;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.velocity.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HideFieldServiceImpl implements HideFieldService {

    private static Logger log = LoggerFactory.getLogger(HideFieldServiceImpl.class);
    public HideFieldServiceImpl(){}


    @Override
    public List<Map<String, Object>> findAllFields()  {

        List<Map<String, Object>> list = new ArrayList<>();
        Set<SearchableField> sf = ComponentAccessor.getFieldManager().getSystemSearchableFields();
        log.debug(sf.size() + " System fields Found.");

        for( Field field: sf){
            Map<String, Object> item = new HashMap<>();
            item.put("id", field.getId());
            item.put("name", field.getName());
            list.add(item);
        }

        CustomFieldManager cfm = ComponentAccessor.getCustomFieldManager();
        List<CustomField> objList = cfm.getCustomFieldObjects();
        log.debug(objList.size() + " custom fields Found.");

        for (CustomField field : objList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", field.getId());
            item.put("name", field.getName());
            list.add(item);
        }

        log.debug(list.size() + " total fields Found.");
        return list;
    }

    @Override
    public String hideFields(String html, Set<String> fieldsToHide) {
        Document doc = Jsoup.parse(html,  "UTF-8");

        for(String fieldId : fieldsToHide){
            log.debug("Searching for field: " + fieldId );
            Element el = doc.getElementById(fieldId + "-val");

            //TODO Do it more prettier
            if ( el == null){
                el = doc.getElementById(fieldId + "-date");
            }
            if (el != null){
                log.debug("Found field for removing with ID:" + el.id());
                Element parent = el.parent();
                if ( parent.tagName().equals("dd")){
                    parent = parent.parent();
                }
                log.debug("Removing parent  element.");
                parent.remove();
            }
        }


        return doc.toString();
    }

    @Override
    public Set<String> getFieldListToHide(List<HidingRule> rules, String issueKey) {
        Set<String> list = new HashSet<>();

        if (rules.size() > 0){
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
            SearchProvider searchProvider = ComponentAccessor.getComponent(SearchProvider.class);

            List<Issue> issueList;
            for(HidingRule rule : rules){

                boolean hideField = true;
                if ( rule.getJQLFilter() > 0){
                    hideField = false;
                    log.debug("JQL Filter is set. Searching for issues that matching filter with ID: " + rule.getJQLFilter());

                    //TODO consider another way of filtering issues for beter performance
                    //  ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey);
                    // List<Issue> watched = ComponentAccessor.getIssueManager().getWatchedIssues(user);

                    SearchRequest request = ComponentAccessor.getComponent(SearchRequestService.class).getFilter(new JiraServiceContextImpl(user), rule.getJQLFilter());
                    if ( request != null) {
                        try {
                            issueList = searchProvider.search(request.getQuery(), user, PagerFilter.getUnlimitedFilter()).getIssues();
                            log.debug(issueList + " Issues founded that matching JQL Filter. ");
                            for (Issue issue : issueList) {
                                if (issue.getKey().equals(issueKey)) {
                                    log.debug("Issue with key '" + issueKey + "' matching current filter, so hiding fields");
                                    hideField = true;
                                    break;
                                }
                            }
                        } catch (SearchException e) {
                            e.printStackTrace();
                        }

                        log.debug("Hiding fields according to rule '" + rule.getTitle() + "':" + hideField);
                    }else{
                        log.warn("Filter for hiding fields not shared with user:" + user.getKey());
                    }
                }

                if ( hideField ){
                    String [] s = StringUtils.split(rule.getFields(), ",");
                    list.addAll(Arrays.asList(s));
                }
            }
        }
        log.debug("List of Fields to Hide: " + list);

        return list;
    }

}
