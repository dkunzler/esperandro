package de.devland.esperandro;

import android.content.SharedPreferences;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.devland.esperandro.tests.EsperandroAnnotationExample;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AnnotationsTest {

    private EsperandroAnnotationExample annotationPreferences;
    private SharedPreferences androidPreferences;

    @Before
    public void setup() {
        annotationPreferences = Esperandro.getPreferences(EsperandroAnnotationExample.class, RuntimeEnvironment.application);
        androidPreferences = annotationPreferences.get();
    }

    @After
    public void tearDown() {
        annotationPreferences.clearAll();
    }

    @Test
    public void saveLoad() {
        annotationPreferences.veryUnrelatedName(99);
        Assert.assertEquals(99, androidPreferences.getInt("relatedPreference", 0)); // saved correctly
        Assert.assertEquals(99, annotationPreferences.unrelatedName()); // loaded correctly
    }
}
