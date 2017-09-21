package com.adon.nc.hidefields.service;

import com.adon.nc.hidefields.entity.HidingRule;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface HideFieldService {

    /**
     * Remove Fields from html content according to list
     * @param html content
     * @param fields list of fields to ide
     * @return
     */
    String hideFields(String html, Set<String> fields);

    /**
     * Get all fields available for hiding on a page
     * @return
     */
    List<Map<String, Object>> findAllFields();

    /**
     * Create List of string from HidingRules
     * @param rules
     * @param issueKey
     * @return
     */
    Set<String> getFieldListToHide(List<HidingRule> rules, String issueKey);
}
