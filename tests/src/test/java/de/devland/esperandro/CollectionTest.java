package de.devland.esperandro;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.devland.esperandro.serialization.JacksonSerializer;
import de.devland.esperandro.tests.EsperandroCollectionExample;
import de.devland.esperandro.tests.model.Container;

/**
 * @author David Kunzler on 19.07.2017.
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CollectionTest {

    private EsperandroCollectionExample esperandroPreferences;

    @Before
    public void setup() {
        esperandroPreferences = Esperandro.getPreferences(EsperandroCollectionExample.class, RuntimeEnvironment.application);
        Esperandro.setSerializer(new JacksonSerializer());
    }

    @After
    public void tearDown() {
        esperandroPreferences.clearAll();
    }

    @Test
    public void putGetList() {
        Assert.assertNull(esperandroPreferences.getListPreference());
        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";

        esperandroPreferences.setListPreference(Collections.singletonList(container));

        Container savedContainer = esperandroPreferences.getListPreference().get(0);
        Assert.assertNotNull(savedContainer);
        Assert.assertEquals(container, savedContainer);
    }

    @Test
    public void addList() {
        Assert.assertNull(esperandroPreferences.getListPreference());
        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";

        Container other = new Container();
        other.anotherValue = 42;
        other.value = "foo";

        List<Container> list = new ArrayList<>();
        list.add(container);
        esperandroPreferences.setListPreference(list);
        esperandroPreferences.listPreference$Add(other);

        Assert.assertTrue(esperandroPreferences.getListPreference().contains(container));
        Assert.assertTrue(esperandroPreferences.getListPreference().contains(other));
    }

    @Test
    public void removeList() {
        Assert.assertNull(esperandroPreferences.getListPreference());

        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";

        Container other = new Container();
        other.anotherValue = 42;
        other.value = "foo";

        List<Container> list = new ArrayList<>();
        list.add(container);
        esperandroPreferences.setListPreference(list);
        esperandroPreferences.listPreference$Add(other);

        Assert.assertEquals(2, esperandroPreferences.getListPreference().size());
        Assert.assertTrue(esperandroPreferences.getListPreference().contains(container));
        Assert.assertTrue(esperandroPreferences.getListPreference().contains(other));

        esperandroPreferences.listPreference$Remove(container);

        Assert.assertEquals(1, esperandroPreferences.getListPreference().size());
        Assert.assertFalse(esperandroPreferences.getListPreference().contains(container));
        Assert.assertTrue(esperandroPreferences.getListPreference().contains(other));
    }

    @Test
    public void putGetSet() {
        Assert.assertNull(esperandroPreferences.getSetPreference());

        Set<String> set = new HashSet<>();
        set.add("foo");
        esperandroPreferences.setSetPreference(set);

        String value = esperandroPreferences.getSetPreference().iterator().next();
        Assert.assertNotNull(value);
        Assert.assertEquals("foo", value);
    }

    @Test
    public void addSet() {
        Assert.assertNull(esperandroPreferences.getSetPreference());

        Set<String> set = new HashSet<>();
        set.add("foo");
        esperandroPreferences.setSetPreference(set);
        esperandroPreferences.setPreference$Add("bar");

        Assert.assertTrue(esperandroPreferences.getSetPreference().contains("foo"));
        Assert.assertTrue(esperandroPreferences.getSetPreference().contains("bar"));
    }

    @Test
    public void removeSet() {
        Assert.assertNull(esperandroPreferences.getSetPreference());

        Set<String> set = new HashSet<>();
        set.add("foo");
        esperandroPreferences.setSetPreference(set);
        esperandroPreferences.setPreference$Add("bar");

        Assert.assertEquals(2, esperandroPreferences.getSetPreference().size());
        Assert.assertTrue(esperandroPreferences.getSetPreference().contains("foo"));
        Assert.assertTrue(esperandroPreferences.getSetPreference().contains("bar"));

        esperandroPreferences.setPreference$Remove("foo");

        Assert.assertEquals(1, esperandroPreferences.getSetPreference().size());
        Assert.assertFalse(esperandroPreferences.getSetPreference().contains("foo"));
        Assert.assertTrue(esperandroPreferences.getSetPreference().contains("bar"));
    }

    @Test
    public void stringSetDefault() {
        Assert.assertNotNull(esperandroPreferences.getStringSetDefaultPreference());
        Assert.assertEquals(0, esperandroPreferences.getStringSetDefaultPreference().size());
    }

    public void collectionDefault() {
        Assert.assertNotNull(esperandroPreferences.getListDefaultPreference());
        Assert.assertEquals(0, esperandroPreferences.getListDefaultPreference().size());
    }
}
