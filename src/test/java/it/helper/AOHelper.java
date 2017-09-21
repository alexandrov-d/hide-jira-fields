package it.helper;

import com.adon.nc.hidefields.entity.HidingGroup;
import com.adon.nc.hidefields.entity.HidingRule;
import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.Query;

public class AOHelper {

    private  ActiveObjects ao;

    public  AOHelper(ActiveObjects aObj){
        this.ao = aObj;
    }

    public HidingRule findRuleByTitle(String title){
        HidingRule[] rules = ao.find(HidingRule.class, Query.select().where("TITLE = ?", title));
        if ( rules.length > 0){
            return rules[0];
        }else{
            return null;
        }
    }
    public HidingRule findRuleByID(int id){
        return ao.get(HidingRule.class, id);
    }

    public HidingRule insertRule( String title, String Fields, int jqlFilter ){
        HidingRule rule =  ao.create(HidingRule.class);
        rule.setTitle(title);
        rule.setFields(Fields);
        rule.setJQLFilter(jqlFilter);
        rule.save();
        return rule;
    }

    public HidingRule createRule( String title, String Fields, int jqlFilter, boolean isInverse ){
        HidingRule rule =  ao.create(HidingRule.class);
        rule.setTitle(title);
        rule.setFields(Fields);
        rule.setJQLFilter(jqlFilter);
        rule.setInverse(isInverse);
        rule.save();

        return rule;
    }

    public HidingGroup createGroup(String title, HidingRule rule ){
        HidingGroup group = ao.create(HidingGroup.class);
        group.setTitle(title);
        group.setRule(rule);
        group.save();
        return group;
    }


}
