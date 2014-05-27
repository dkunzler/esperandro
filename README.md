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

    buildscript {
        repositories {
          mavenCentral()
        }
        dependencies {
            // Android plugin
            classpath 'com.android.tools.build:gradle:0.10.+'
            // the latest version of the android-apt plugin from https://bitbucket.org/hvisser/android-apt
            classpath 'com.neenbedankt.gradle.plugins:android-apt:1.2'
        }
    }

    apply plugin: 'android'
    apply plugin: 'android-apt'


    repositories {
         mavenCentral();
    }

    dependencies {
        compile 'de.devland.esperandro:esperandro-api:2.0.0'
        apt 'de.devland.esperandro:esperandro:2.0.0'

        // optional, if we want to use object serialization but don't provide our own Serializer
        // compile 'de.devland.esperandro:esperandro-gson-addon:2.0.0'
        // or
        // compile 'de.devland.esperandro:esperandro-jackson-addon:2.0.0'
    }
    
Current Travis status

[![Build Status](https://api.travis-ci.org/dkunzler/esperandro.png?branch=master)](https://travis-ci.org/dkunzler/esperandro)
