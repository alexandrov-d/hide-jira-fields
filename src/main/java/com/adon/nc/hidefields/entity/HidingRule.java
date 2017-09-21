package com.adon.nc.hidefields.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;
import net.java.ao.schema.Default;
import net.java.ao.schema.StringLength;

@Preload
public interface HidingRule extends Entity{

    String getTitle();
    void setTitle(String title);

    @StringLength(450)
    String getFields();
    void setFields(String title);

    long getJQLFilter();
    void setJQLFilter(long id);

    //IF true apply when user NOT IN HidinGroups
    @Default("0")
    boolean isInverse();
    void setInverse(boolean inverse);

    @OneToMany
    HidingGroup[] getGroups();
}
