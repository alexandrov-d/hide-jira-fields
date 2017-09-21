package com.adon.nc.hidefields.rest.model;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.SearchableField;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestModel {
    @XmlElement
    private int id;

    @XmlElement
    private String title;

    @XmlElement
    private String groups;

    @XmlElement
    private String hideCustomFields;

    @XmlElement
    private String customFields;

    @XmlElement
    private String cfOptions;

    @XmlElement(name = "value")
    private String message;

    public RestModel() {
    }

    public RestModel(String message) {
        this.message = message;
    }

    public RestModel(int id, String title, String groups, String hideCustomFields, String customFields, String cfOptions) {
        this.id = id;
        this.title = title;
        this.groups = groups;
        this.hideCustomFields = hideCustomFields;
        this.customFields = customFields;
        this.cfOptions = cfOptions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /*private static List<JSONObject> getOptions(CustomField cf) {
        OptionsManager optionsManager = ComponentAccessor.getOptionsManager();
        FieldConfig fieldConfig = cf.getReleventConfig(new SearchContextImpl());
        Options options = optionsManager.getOptions(fieldConfig);
        List<JSONObject> jsonList = new ArrayList<>();

//        Options options = ComponentAccessor.getOptionsManager().getOptions(cf.getConfigurationSchemes().listIterator().next().getOneAndOnlyConfig());
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();


        *//*if (options == null) {
            return jsonList;
        }*//*


        try {
            for (Option val : options) {
                JSONObject json = new JSONObject();
//                CustomField customField = customFieldManager.getCustomFieldObjectByName(val.getValue());
                CustomField customField = customFieldManager.getCustomFieldObject(val.getValue());
                Options opts = ComponentAccessor.getOptionsManager().getOptions(customField.getConfigurationSchemes().listIterator().next().getOneAndOnlyConfig());

                json.put("opts", opts);

//                json.put("id", val.getOptionId());
//                json.put("value", val.getValue());

//                for(Option vr : val.getChildOptions()) {
//                    json.put("id", vr.getOptionId());
//                    json.put("value", vr.getValue());
//                }
//                json.put("extend", val.getChildOptions());
//                json.put("extend", val.getGenericValue());
//                json.put("parent-option", val.getParentOption());
                jsonList.addRule(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }  catch (Exception e) {
            e.printStackTrace();
        }

        return jsonList;
    }*/

    private static List<Map> getOptions(CustomFieldManager cfm, CustomField cf) {
        List<Map> list = new ArrayList<>();
        try {
            if(cf != null) {
                CustomField cfSearch = cfm.getCustomFieldObject(cf.getId());
                Options options = ComponentAccessor.getOptionsManager().getOptions(cfSearch.getConfigurationSchemes().listIterator().next().getOneAndOnlyConfig());

                for (Option option : options) {
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("id", option.getOptionId());
                    tmp.put("value", option.getValue());
                    list.add(tmp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Map> getCustomFields() {
        List<Map> list = new ArrayList<>();
        CustomFieldManager cfm = ComponentAccessor.getCustomFieldManager();
        Set<SearchableField> sf = ComponentAccessor.getFieldManager().getSystemSearchableFields();
        for(Field f: sf){
            f.toString();
        }
        List<CustomField> objList = cfm.getCustomFieldObjects();

        try {
            for (CustomField cf : objList) {
                String type = cf.getCustomFieldType().getName();

                if(type.equalsIgnoreCase("Date Time Picker") || type.equalsIgnoreCase("Date Picker")) continue;

                Map<String, Object> fields = new HashMap<>();
                List<Map> options = getOptions(cfm, cf);

                fields.put("id",cf.getId());
                fields.put("name",cf.getName());
                fields.put("options", options);
//                fields.put("cf-type", type);

                list.add(fields);
            }
        } catch (Exception e) {
            System.err.println("error desc: "+e.getMessage());
        }
        return list;
    }

    public static Map<String, Object> getCustomFields(String id) {
        Map<String, Object> fields = new HashMap<>();
        CustomFieldManager cfm = ComponentAccessor.getCustomFieldManager();
        CustomField cf = cfm.getCustomFieldObject(id);
        String type = cf.getCustomFieldType().getName();

        if(type.equalsIgnoreCase("Date Time Picker") || type.equalsIgnoreCase("Date Picker")) return fields;

        List<Map> options = getOptions(cfm, cf);

        try {
            fields.put("id",cf.getId());
            fields.put("name",cf.getName());
            fields.put("options", options);
        } catch (Exception e) {
            System.out.println("error desc: "+e.getMessage());
//            System.err.println("error desc: "+e.getMessage());
        }

        return fields;
    }


}