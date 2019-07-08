package de.devland.esperandro;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Set;

import de.devland.esperandro.tests.UnsafeExample;
import de.devland.esperandro.tests.model.Container;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class UnsafeTest {

    private UnsafeExample unsafe;
    private Context mockContext;

    @Before
    public void initializeUnsafe() {
        unsafe = Esperandro.getPreferences(UnsafeExample.class, RuntimeEnvironment.application);
    }

    @After
    public void clear() {
        unsafe.clearAll();
    }

    @Before
    public void initializeMock() {
        mockContext = Mockito.mock(Context.class);
        Mockito.when(mockContext.getString(1)).thenReturn("simpleInt");
        Mockito.when(mockContext.getString(2)).thenReturn("container");
        Mockito.when(mockContext.getString(3)).thenReturn("string");
        Mockito.when(mockContext.getString(4)).thenReturn("putterWithoutGetter");
        Mockito.when(mockContext.getString(5)).thenReturn("getterWithoutPutter");
        Mockito.when(mockContext.getString(6)).thenReturn("unknownProperty");
    }

    @Test
    public void testReadGoodProperties() throws Exception {
        unsafe.simpleInt(42);
        unsafe.string("truth");
        unsafe.container(new Container());
        unsafe.putterWithoutGetter(1337L);
        unsafe.get().edit().putStringSet("getterWithoutPutter", Sets.newSet("a")).apply();

        Assert.assertEquals(42, (int) unsafe.getValue(mockContext, 1));
        Container container = unsafe.getValue(mockContext, 2);
        Assert.assertNotNull(container);
        Assert.assertEquals("truth", unsafe.getValue(mockContext, 3));
        Assert.assertEquals(1337L, (long) unsafe.getValue(mockContext, 4));
        Set<String> stringSet = unsafe.getValue(mockContext, 5);
        Assert.assertNotNull(stringSet);
        Assert.assertEquals(1, stringSet.size());
        Assert.assertTrue(stringSet.contains("a"));
    }

    @Test
    public void testWriteGoodProperties() throws Exception {
        unsafe.setValue(mockContext, 1, 21);
        unsafe.setValue(mockContext, 2, new Container());
        unsafe.setValue(mockContext, 3, "bar");
        unsafe.setValue(mockContext, 4, 23L);
        unsafe.setValue(mockContext, 5, Sets.newSet("1", "2"));

        Assert.assertEquals(21, unsafe.simpleInt());
        Assert.assertNotNull(unsafe.container());
        Assert.assertEquals("bar", unsafe.string());
        Assert.assertEquals(23L, unsafe.get().getLong("putterWithoutGetter", 0L));
        Set<String> stringSet = unsafe.getterWithoutPutter();
        Assert.assertNotNull(stringSet);
        Assert.assertEquals(2, stringSet.size());
        Assert.assertTrue(stringSet.contains("1"));
        Assert.assertTrue(stringSet.contains("2"));
    }

    @SuppressWarnings("unused")
    @Test(expected = ClassCastException.class)
    public void testReadWrongType() throws Exception {
        int nonInt = unsafe.getValue(mockContext, 3);
    }

    @Test(expected = ClassCastException.class)
    public void testWriteWrongType() throws Exception {
        unsafe.setValue(mockContext, 3, 42);
    }

    @Test(expected = UnsafeActions.UnknownKeyException.class)
    public void testReadUnknownProperty() throws Exception {
        unsafe.getValue(mockContext, 6);
    }

    @Test(expected = UnsafeActions.UnknownKeyException.class)
    public void testWriteUnknownProperty() throws Exception {
        unsafe.setValue(mockContext, 6, "foo");
    }
}
