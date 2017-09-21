package com.adon.nc.hidefields.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface HidingGroup extends Entity{

    HidingRule getRule();
    void setRule(HidingRule rule);

    String getTitle();
    void setTitle(String title);

}
