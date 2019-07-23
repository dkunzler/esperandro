esperandro
==========

Easy SharedPreference Engine foR ANDROid

[Website with complete tutorial](http://dkunzler.github.io/esperandro)

esperandro is for everybody that uses `SharedPreference`s in his Android App and is tired of the verbose usage of them.

**Load and save preferences without *esperandro*:**

* `String superFancyPreference = preferences.getString("superFancyPreferenceKey", "default value")`
* `preferences.edit().putString("superFancyPreferenceKey", superFancyPrefence).commit()`



**Load and save preferences with *esperandro*:**

* `String superFancyPreference = preferences.superFancyPreferenceKey()`
* `preferences.superFancyPreferenceKey(superFancyPreference)`

Type safe, easy, less error-prone.

More information about integration and deeper explanation of usage can be found on [the website](http://dkunzler.github.io/esperandro).

For everybody that just thinks "give me the stuff":

    apply plugin: 'com.android.application'


    repositories {
         mavenCentral();
    }

    dependencies {
        implementation 'de.devland.esperandro:esperandro-api:3.0.0'
        annotationProcessor 'de.devland.esperandro:esperandro-preference-gen:3.0.0'

        // optional, if we want to use object serialization but don't provide our own Serializer
        // implementation 'de.devland.esperandro:esperandro-gson-addon:3.0.0'
        // or
        // implementation 'de.devland.esperandro:esperandro-jackson-addon:3.0.0'
        
        // additional processor to generate a file with all keys as string constants
        // annotationProcessor 'de.devland.esperandro:esperandro-keys-gen:3.0.0'
        
        // additional processor to generate a file with all keys as string resources
        // annotationProcessor 'de.devland.esperandro:esperandro-resources-gen:3.0.0'
    }
    
Current Travis status

[![Build Status](https://api.travis-ci.org/dkunzler/esperandro.png?branch=master)](https://travis-ci.org/dkunzler/esperandro)
