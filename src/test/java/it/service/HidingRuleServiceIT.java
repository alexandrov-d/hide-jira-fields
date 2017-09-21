package it.service;

import com.adon.nc.hidefields.entity.HidingGroup;
import com.adon.nc.hidefields.entity.HidingRule;
import com.adon.nc.hidefields.service.HidingRuleService;
import com.adon.nc.hidefields.service.HidingRuleServiceImpl;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.groups.GroupManager;
import it.helper.AOHelper;
import net.java.ao.test.ActiveObjectsIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//@Data(HidingRuleServiceIT.HidingRuleServiceITDatabaseUpdater.class) // (1)
public class HidingRuleServiceIT extends ActiveObjectsIntegrationTest {

    private HidingRuleService ruleService;
    private static ActiveObjects ao;

    private static final String USERNAME_1 = "admin";
    private static final String RULE_TITLE_1 = "Rule number 1";
    private static final String RULE_TITLE_2 = "Rule number 2";
    private static final String RULE_TITLE_3 = "Rule number 3";
    private static final String HIDE_FIELD_ID_1 = "customfield01";
    private static final String HIDE_FIELD_ID_2 = "customfield02";
    private static final String HIDE_FIELD_ID_3 = "customfield03";
    private static final String GROUP_TITLE_1 = "jira-admins";
    private static final String GROUP_TITLE_2 = "jira-devs";
    private static final String GROUP_TITLE_3 = "jira-users";
    private static final int JQL_FILTER_ID = 10_100;

    private GroupManager groupManager;

    private AOHelper aoHelper;


    @Before
    public void setUp() throws Exception {

        assertNotNull(entityManager);
            ao = new TestActiveObjects(entityManager);

        //userManager = mock(UserManager.class);
        groupManager = mock(GroupManager.class);
        ruleService = new HidingRuleServiceImpl(ao, groupManager);
        ao.migrate(HidingRule.class, HidingGroup.class);

        Map<String, Object> map = new HashMap<>();
        ApplicationProperties props = new MockApplicationProperties(map);
        new MockComponentWorker()
            .addMock(ApplicationProperties.class, props)
            .init();

        this.aoHelper = new AOHelper(ao);
    }

    @After
    public void tearDown() throws Exception {
        ao.migrateDestructively(HidingRule.class, HidingGroup.class);
    }


    @Test
    public void ShouldCreateNewHidingRule() throws Exception {
        int ruleLengthBefore = ao.find(HidingRule.class).length;
        int groupLengthBefore = ao.find(HidingGroup.class).length;

        Set<String> groups = new HashSet<>();
        groups.add(GROUP_TITLE_1);
        groups.add(GROUP_TITLE_2);

        ruleService.addRule(RULE_TITLE_1, groups, HIDE_FIELD_ID_1, 2, false);
        assertEquals(ruleLengthBefore,ruleLengthBefore);
        assertEquals(ruleLengthBefore + 1, ao.find(HidingRule.class).length);
        assertEquals(groupLengthBefore + 2, ao.find(HidingGroup.class).length);

    }

    @Test
    public void ShouldUpdateRuleFields(){
        int jqlFilter = 10234;
        aoHelper.createRule(RULE_TITLE_1, HIDE_FIELD_ID_1, jqlFilter, false);
        HidingRule rule = aoHelper.findRuleByTitle(RULE_TITLE_1);
        assertEquals(jqlFilter, rule.getJQLFilter());
        Set<String> groups = new HashSet<>(Arrays.asList(new String[]{"s", "s"}));
        ruleService.updateRule(rule.getID(), RULE_TITLE_2, groups, HIDE_FIELD_ID_2, jqlFilter, true );

        HidingRule updatedRule = aoHelper.findRuleByID(rule.getID());
        assertEquals(true, updatedRule.isInverse());
        assertEquals(RULE_TITLE_2, updatedRule.getTitle());
    }


    @Test
    public void ShouldDeleteRuleWithGroupsById(){
        prepopulateDB();
        HidingRule[] list = ao.find(HidingRule.class);
        assertEquals(2, list.length);
        assertEquals(3,  ao.find(HidingGroup.class).length);

        int response = ruleService.deleteRuleById(list[0].getID());
        assertEquals(1, response);
        assertEquals(1,  ao.find(HidingRule.class).length);
        assertEquals(1,  ao.find(HidingGroup.class).length);
    }

    @Test
    public void ShouldReturnZeroOnNotExistingDeletion(){
        int response = ruleService.deleteRuleById(1345234);
        assertEquals(0, response);
    }

    @Test
    public void ShouldReturnFalseOnUnexistedRuleUpdating(){
        boolean response = ruleService.updateRule(150506, "title", new HashSet<String>(), HIDE_FIELD_ID_3, 0, false);
        assertFalse(response);
    }


    @Test
    public void ShouldUpdateGroupsInExistingRule(){
        prepopulateDB();

        HidingRule ruleBefore = aoHelper.findRuleByTitle(RULE_TITLE_1);
        assertEquals(2, ruleBefore.getGroups().length);
        assertEquals(3,  ao.find(HidingGroup.class).length);
        assertEquals(HIDE_FIELD_ID_1, ruleBefore.getFields());
        int id = ruleBefore.getID();

        Set<String> groups = new HashSet<>();
        groups.add(GROUP_TITLE_3);

        boolean response = ruleService.updateRule(id, ruleBefore.getTitle(), groups, HIDE_FIELD_ID_3, 1, false);

        assertTrue(response);

        HidingRule ruleAfter = ao.get(HidingRule.class, id);

        assertEquals(1, ruleAfter.getGroups().length);
        assertEquals(2,  ao.find(HidingGroup.class).length);
        assertEquals(HIDE_FIELD_ID_3, ruleAfter.getFields());
        assertEquals(1, ruleAfter.getGroups().length);
        assertEquals(GROUP_TITLE_3, ruleAfter.getGroups()[0].getTitle());
    }

    @Test
    public void ShouldReturn1Rule_When1RuleIsInversed_AndUserOnlyIn1Group_And2RuleIsStright() throws OperationNotPermittedException, InvalidGroupException {
        prepopulateInversedDB();

        //Should return 1 rules
        Collection<Group> groups = new ArrayList<>();
        Group g1 = mock(Group.class);
        when(g1.getName()).thenReturn(GROUP_TITLE_1);
        groups.add(g1);
        Group g2 = mock(Group.class);
        when(g2.getName()).thenReturn(GROUP_TITLE_3);
        groups.add(g2);
        when(groupManager.getGroupsForUser(USERNAME_1)).thenReturn(groups);

        List<HidingRule> rules = ruleService.getRulesForUser(USERNAME_1);
        assertEquals(1, rules.size());
        assertEquals(RULE_TITLE_2, rules.get(0).getTitle());

    }

    @Test
    public void ShouldReturnListOfRulesForExistingGroups() throws OperationNotPermittedException, InvalidGroupException {

        prepopulateDB();

        assertEquals(3, ao.find(HidingGroup.class).length);
        assertEquals(2, ao.find(HidingRule.class).length);

        HidingRule rule = ao.create(HidingRule.class);

        rule.setFields(HIDE_FIELD_ID_3);
        rule.setTitle(RULE_TITLE_3);
        rule.save();

        HidingGroup group = ao.create(HidingGroup.class);
        group.setTitle(GROUP_TITLE_3);
        group.setRule(rule);
        group.save();

        assertEquals(4, ao.find(HidingGroup.class).length);
        assertEquals(3, ao.find(HidingRule.class).length);

        //Should return 2 rules
        Collection<Group> groups = new ArrayList<>();
        Group g1 = mock(Group.class);
        when(g1.getName()).thenReturn(GROUP_TITLE_1);
        groups.add(g1);
        Group g2 = mock(Group.class);
        when(g2.getName()).thenReturn(GROUP_TITLE_2);
        groups.add(g2);
        when(groupManager.getGroupsForUser(USERNAME_1)).thenReturn(groups);

        List<HidingRule> rules = ruleService.getRulesForUser(USERNAME_1);
        assertEquals(2, rules.size());

        //Should return 2 rules
        groups = new ArrayList<>();
        g1 = mock(Group.class);
        when(g1.getName()).thenReturn(GROUP_TITLE_2);
        groups.add(g1);
        when(groupManager.getGroupsForUser(USERNAME_1)).thenReturn(groups);
        rules = ruleService.getRulesForUser(USERNAME_1);
        assertEquals(2, rules.size());

        //Should return 1 rule
        groups = new ArrayList<>();
        g1 = mock(Group.class);
        when(g1.getName()).thenReturn(GROUP_TITLE_3);
        groups.add(g1);
        when(groupManager.getGroupsForUser(USERNAME_1)).thenReturn(groups);
        rules = ruleService.getRulesForUser(USERNAME_1);
        assertEquals(1, rules.size());
    }

    @Test
    public void ShouldUpdateJQLFilterIfFilterIdIsZero(){
        final String title1 = "New Custom field title";
        final int jqlID = 3;
        aoHelper.insertRule(title1, HIDE_FIELD_ID_1, jqlID);

        HidingRule  r1 = aoHelper.findRuleByTitle(title1);
        assertEquals(title1, r1.getTitle());
        assertEquals(jqlID, r1.getJQLFilter());
        r1.setJQLFilter(0);
        r1.save();

        HidingRule r2 = aoHelper.findRuleByTitle(title1);
        assertEquals(0, r2.getJQLFilter());
    }

    @Test
    public void ShouldReturnEmptyListForUnexistingGroups(){
        String user = "admin";

        List<HidingRule> rules = ruleService.getRulesForUser(user);
        assertEquals(0, rules.size());

        rules = ruleService.getRulesForUser(user);
        assertEquals(0, rules.size());

    }

    @Test
    public void ShouldReturn1IverseRule(){
        HidingRule rule = aoHelper.createRule(RULE_TITLE_1, HIDE_FIELD_ID_1, JQL_FILTER_ID, false);
        aoHelper.createGroup(GROUP_TITLE_2, rule);

        HidingRule ruleInverse = aoHelper.createRule(RULE_TITLE_2, HIDE_FIELD_ID_2, JQL_FILTER_ID, true);
        aoHelper.createGroup(GROUP_TITLE_2, ruleInverse);

        Collection<Group> groups = new ArrayList<>();
        Group g1 = mock(Group.class);
        when(g1.getName()).thenReturn(GROUP_TITLE_1);
        groups.add(g1);
        when(groupManager.getGroupsForUser(USERNAME_1)).thenReturn(groups);

        List<HidingRule> rules = ruleService.getRulesForUser(USERNAME_1);
        assertEquals(1, rules.size());
        assertTrue( rules.get(0).isInverse());
        assertEquals(RULE_TITLE_2, rules.get(0).getTitle());
    }

    private void prepopulateDB(){

        HidingRule rule = ao.create(HidingRule.class);

        rule.setTitle(RULE_TITLE_1);
        rule.setFields(HIDE_FIELD_ID_1);
        rule.setJQLFilter(1);
        rule.save();

        HidingGroup group = ao.create(HidingGroup.class);
        group.setTitle(GROUP_TITLE_1);
        group.setRule(rule);
        group.save();

        group = ao.create(HidingGroup.class);
        group.setTitle(GROUP_TITLE_2);
        group.setRule(rule);
        group.save();
        //1 rule for 2 groups created


        rule = ao.create(HidingRule.class);
        rule.setFields(HIDE_FIELD_ID_2);
        rule.setTitle(RULE_TITLE_2);
        rule.setJQLFilter(3);
        rule.save();

        group = ao.create(HidingGroup.class);
        group.setTitle(GROUP_TITLE_2);
        group.setRule(rule);
        group.save();
        //1 rule for 1 groups created

    }

    private void prepopulateInversedDB(){

        HidingRule rule = ao.create(HidingRule.class);

        rule.setTitle(RULE_TITLE_1);
        rule.setFields(HIDE_FIELD_ID_1);
        rule.setJQLFilter(1);
        rule.setInverse(true);
        rule.save();

        HidingGroup group = ao.create(HidingGroup.class);
        group.setTitle(GROUP_TITLE_1);
        group.setRule(rule);
        group.save();

        group = ao.create(HidingGroup.class);
        group.setTitle(GROUP_TITLE_2);
        group.setRule(rule);
        group.save();

        HidingRule rule2 = ao.create(HidingRule.class);

        rule2.setTitle(RULE_TITLE_2);
        rule2.setFields(HIDE_FIELD_ID_2);
        rule2.save();

        HidingGroup group2 = ao.create(HidingGroup.class);
        group2.setTitle(GROUP_TITLE_1);
        group2.setRule(rule2);
        group2.save();

    }



}
