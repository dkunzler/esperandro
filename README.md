esperandro
==========

Easy SharedPreference Engine foR ANDROid

[Website with complete tutorial](http://dkunzler.github.io/esperandro)

esperandro is for everybody that uses `SharedPreference`s in his Android App and is tired of the verbose usage of them.

A call like `String superFancyPreference = preferences.getString("superFancyPreferenceKey",
"default value")` would reduce to `String superFancyPreference = preferences
.superFancyPreferenceKey()`.
Furthermore a verbose put like `preferences.edit().putString("superFancyPreferenceKey",
superFancyPrefence).commit()` will look like `preferences.superFancyPreferenceKey(superFancyPreference)` when using
esperandro.

Looks clean and easy, doesn't it?

More information about integration and deeper explanation of usage can be found on [the website](http://dkunzler.github.io/esperandro).

For everybody that just thinks "give me the stuff":

    <dependency>
        <groupId>de.devland.esperandro</groupId>
        <artifactId>esperandro-api</artifactId>
        <version>1.0</version>
    </dependency>
    <dependency>
        <groupId>de.devland.esperandro</groupId>
        <artifactId>esperandro</artifactId>
        <version>1.0</version>
        <scope>provided</scope>
    </dependency>
