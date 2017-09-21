package com.adon.nc.hidefields.service;

import com.adon.nc.hidefields.entity.HidingRule;

import java.util.List;
import java.util.Set;

public interface HidingRuleService {


    List<HidingRule> getRules();

    void addRule(String title, Set<String> groups, String fieldsToHide, int jqlFilter, boolean isInversed);

    boolean updateRule(int id, String title, Set<String> groups, String fieldsToHide, int jqlFilter, boolean isInversed);

    int deleteRuleById(int id);

    /**
     * Get Rules for current user
     * @return
     */
    List<HidingRule> getRulesForUser(String username);

}
