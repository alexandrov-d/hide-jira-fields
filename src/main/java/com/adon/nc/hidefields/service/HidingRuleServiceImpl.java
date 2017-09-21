package com.adon.nc.hidefields.service;


import com.adon.nc.hidefields.entity.HidingGroup;
import com.adon.nc.hidefields.entity.HidingRule;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.security.groups.GroupManager;
import net.java.ao.Query;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HidingRuleServiceImpl implements HidingRuleService {

    private static Logger log = LoggerFactory.getLogger(HidingRuleServiceImpl.class);

    private final ActiveObjects ao;
    private GroupManager groupManager;


    public HidingRuleServiceImpl( ActiveObjects ao, GroupManager gm){
        this.ao = ao;
        this.groupManager = gm;

    }



    @Override
    public List<HidingRule> getRules() {
        HidingRule[] rules = ao.find(HidingRule.class);
        log.debug(rules.length + " Rules founded.");
        return  Arrays.asList(rules);
    }

    @Override
    public void addRule(String title, Set<String> groups, String fieldsToHide, int jqlFilter, boolean isInversed) {
        HidingRule rule = createRule(title, fieldsToHide, jqlFilter, isInversed);
        for(String groupTitle: groups){
            createGroup(groupTitle, rule);
        }
    }


    @Override
    public boolean updateRule(int id, String newTitle, Set<String> newGroups, String newFields, int jqlFilter, boolean isInversed) {

        log.debug("Updating Rule with ID: "  + id + ". With data** Title: " + newTitle + ", new Groups:"
                + newGroups + ". Hiding Fields:" + newFields + ", JQL Filter: " + jqlFilter);
        HidingRule rule = ao.get(HidingRule.class, id);
        if  ( rule == null ) {
            log.error("Can't find Rule with ID:"  + id);
            return false;
        }

        this.updateUserGroups(newGroups, rule);

        if ( newTitle != null){
            rule.setTitle(newTitle);
        }
        if ( newFields != null){
            rule.setFields(newFields);
        }
        rule.setJQLFilter(jqlFilter);
        rule.setInverse(isInversed);

        rule.save();
        log.debug("Rule updated.");
        return true;
    }

    private void updateUserGroups(Set<String> newGroups, HidingRule rule) {
        //Check existing groups
        for( HidingGroup group: rule.getGroups()){
            if ( newGroups.contains(group.getTitle()) ){
                log.debug("Rule already has group:"  + group.getTitle());
                newGroups.remove(group.getTitle());
            }else{
                log.debug("Deleting Rule group:"  + group.getTitle());
                ao.delete(group);
            }
        }

        //Create new groups
        for( String groupTitle: newGroups){
            createGroup(groupTitle, rule);
        }
    }


    @Override
    public int deleteRuleById(int id) {
        log.debug("Deleting rule ID: " + id);
        HidingRule rule = ao.get(HidingRule.class, id);
        if  ( rule == null ) {
            log.error("Can't find Rule with ID:"  + id);
            return 0;
        }
        ao.delete(rule.getGroups());
        log.debug(rule.getGroups().length + " Rule newGroups deleted");
        ao.delete(rule);
        log.debug("Rule deleted.");
        return 1;
    }

    @Override
    public List<HidingRule> getRulesForUser(String username) {

        Collection<Group> groups = groupManager.getGroupsForUser(username);
        log.debug("Number of Usergroups: " + groups.size());

        Collection<String> groupList = new ArrayList<>();
        for(Group group : groups){
            groupList.add(group.getName());
        }
        log.debug("Usergroups: " + groupList);

        String groupStr = StringUtils.join(groupList, "','");
        log.debug("Searching for rules for groups: {}", groupStr);

        final Query query = Query.select()
            .alias(HidingGroup.class, "groups")
            .alias(HidingRule.class, "rules")
            .join(HidingGroup.class, "rules.ID = groups.RULE_ID");
        String whereClause = "( groups.TITLE IN ('" + groupStr + "') AND rules.inverse = 0) " +
                " OR ( groups.TITLE NOT IN ('" + groupStr + "') AND rules.inverse = 1) ";
        query.setWhereClause(whereClause);
        HidingRule[] hideList = ao.find(HidingRule.class, query.distinct());
        log.debug(hideList.length + " hiding Rules founded.");

        List<HidingRule> resultRules = new ArrayList<>();


        for ( HidingRule item : hideList){
            if ( !item.isInverse()){
                resultRules.add(item);
            }else if ( !isUserGroupsInRuleGroup( groupList, item.getGroups())){
                resultRules.add(item);
            }
        }

        return  resultRules;
    }

    private boolean isUserGroupsInRuleGroup(Collection<String> userGroups, HidingGroup[] ruleGroups){
        for ( HidingGroup groupItem : ruleGroups){
            if ( userGroups.contains(groupItem.getTitle()) ){
                return true;
            }
        }
        return false;
    }

    private void printList(HidingRule[] list){
        for( HidingRule item : list){
            System.out.println(item.getTitle() + " | " +item.getGroups().length + " | " +  item.getFields() + item.getJQLFilter() );
        }
    }

    private HidingRule createRule( String title, String Fields, int jqlFilter, boolean inverse ){
        log.debug("Adding new Rule with Title: " + title + ", Inversed:" + inverse);
        HidingRule rule =  ao.create(HidingRule.class);
        rule.setTitle(title);
        rule.setFields(Fields);
        rule.setJQLFilter(jqlFilter);
        rule.setInverse(inverse);
        rule.save();
        log.debug("Added new Rule.");
        return rule;
    }

    private HidingGroup createGroup(String title, HidingRule rule ){
        log.debug("Adding group with Title: " + title + " to '" + title + "' rule");
        HidingGroup group = ao.create(HidingGroup.class);
        group.setTitle(title);
        group.setRule(rule);
        group.save();
        log.debug("Added new group.");
        return group;
    }

    private HidingRule findRuleByTitle(String title){
        HidingRule[] rules = ao.find(HidingRule.class, Query.select().where("TITLE = ?", title));
        if ( rules.length > 0){
            return rules[0];
        }else{
            return null;
        }
    }
}
