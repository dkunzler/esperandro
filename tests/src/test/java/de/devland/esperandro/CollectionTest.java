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
        Assert.assertNull(esperandroPreferences.listPreference());
        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";

        esperandroPreferences.listPreference(Collections.singletonList(container));

        Container savedContainer = esperandroPreferences.listPreference().get(0);
        Assert.assertNotNull(savedContainer);
        Assert.assertEquals(container, savedContainer);
    }

    @Test
    public void addList() {
        Assert.assertNull(esperandroPreferences.setPreference());
        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";

        Container other = new Container();
        other.anotherValue = 42;
        other.value = "foo";

        List<Container> list = new ArrayList<>();
        list.add(container);
        esperandroPreferences.listPreference(list);
        esperandroPreferences.listPreference$Add(other);

        Assert.assertTrue(esperandroPreferences.listPreference().contains(container));
        Assert.assertTrue(esperandroPreferences.listPreference().contains(other));
    }

    @Test
    public void removeList() {
        Assert.assertNull(esperandroPreferences.setPreference());

        Container container = new Container();
        container.anotherValue = 5;
        container.value = "hello World";

        Container other = new Container();
        other.anotherValue = 42;
        other.value = "foo";

        List<Container> list = new ArrayList<>();
        list.add(container);
        esperandroPreferences.listPreference(list);
        esperandroPreferences.listPreference$Add(other);

        Assert.assertEquals(2, esperandroPreferences.listPreference().size());
        Assert.assertTrue(esperandroPreferences.listPreference().contains(container));
        Assert.assertTrue(esperandroPreferences.listPreference().contains(other));

        esperandroPreferences.listPreference$Remove(container);

        Assert.assertEquals(1, esperandroPreferences.listPreference().size());
        Assert.assertFalse(esperandroPreferences.listPreference().contains(container));
        Assert.assertTrue(esperandroPreferences.listPreference().contains(other));
    }

    @Test
    public void putGetSet() {
        Assert.assertNull(esperandroPreferences.setPreference());

        Set<String> set = new HashSet<>();
        set.add("foo");
        esperandroPreferences.setPreference(set);

        String value = esperandroPreferences.setPreference().iterator().next();
        Assert.assertNotNull(value);
        Assert.assertEquals("foo", value);
    }

    @Test
    public void addSet() {
        Assert.assertNull(esperandroPreferences.setPreference());

        Set<String> set = new HashSet<>();
        set.add("foo");
        esperandroPreferences.setPreference(set);
        esperandroPreferences.setPreference$Add("bar");

        Assert.assertTrue(esperandroPreferences.setPreference().contains("foo"));
        Assert.assertTrue(esperandroPreferences.setPreference().contains("bar"));
    }

    @Test
    public void removeSet() {
        Assert.assertNull(esperandroPreferences.setPreference());

        Set<String> set = new HashSet<>();
        set.add("foo");
        esperandroPreferences.setPreference(set);
        esperandroPreferences.setPreference$Add("bar");

        Assert.assertEquals(2, esperandroPreferences.setPreference().size());
        Assert.assertTrue(esperandroPreferences.setPreference().contains("foo"));
        Assert.assertTrue(esperandroPreferences.setPreference().contains("bar"));

        esperandroPreferences.setPreference$Remove("foo");

        Assert.assertEquals(1, esperandroPreferences.setPreference().size());
        Assert.assertFalse(esperandroPreferences.setPreference().contains("foo"));
        Assert.assertTrue(esperandroPreferences.setPreference().contains("bar"));
    }

    @Test
    public void stringSetDefault() {
        Assert.assertNotNull(esperandroPreferences.stringSetDefaultPreference());
        Assert.assertEquals(0, esperandroPreferences.stringSetDefaultPreference().size());
    }

    public void collectionDefault() {
        Assert.assertNotNull(esperandroPreferences.listDefaultPreference());
        Assert.assertEquals(0, esperandroPreferences.listDefaultPreference().size());
    }
}
