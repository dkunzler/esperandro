package de.devland.esperandro.tests;

import de.devland.esperandro.annotations.SharedPreferences;

@SharedPreferences
interface EsperandroProtectedExample {

    boolean value();

    void value(boolean value);
}
