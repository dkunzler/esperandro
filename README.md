[![Maven Central](https://img.shields.io/maven-central/v/de.devland.esperandro/esperandro-api?style&flat-square)](https://search.maven.org/search?q=g:de.devland.esperandro)
[![Build Status](https://img.shields.io/travis/dkunzler/esperandro?style&flat-square)](https://travis-ci.org/dkunzler/esperandro)

# esperandro


Easy SharedPreference Engine foR ANDROid

### What?

esperandro makes it simple to use `SharedPreference`s in a nicer and less error-prone way.

**Without *esperandro* a key is referenced by a string:**

* `String myPreference = preferences.getString("myPreference", "default value") // load preference`
* `preferences.edit().putString("myPreference", myPreference).commit() // store preference`



**With *esperandro* you adhere to an interface that you defined:**

    @SharedPreferences
    interface MyPreferences {
        String getMyPreference();
        void setMyPreference(String myPreference);
    }
    
* `String myPreference = preferences.getMyPreference() // load preference`
* `preferences.setMyPreference(myPreference) // store preference`


Type safe, easy, less error-prone.

Interested? [Get Started](https://github.com/dkunzler/esperandro/wiki/Basic-Usage)

### Tell me more!

Please refer to the [wiki](https://github.com/dkunzler/esperandro/wiki) for in-depth examples and all available features.

See the [changelog](changelog.md) for a brief overview of recent changes.


### Gradle artifacts


    // essential dependencies
    implementation 'de.devland.esperandro:esperandro-api:<insert version>'
    annotationProcessor 'de.devland.esperandro:esperandro-preference-gen:<insert version>'

    // optional, if object serialization is needed via gson
    implementation 'de.devland.esperandro:esperandro-gson-addon:<insert version>'
    
    // optional, if object serialization is needed via jackson
    implementation 'de.devland.esperandro:esperandro-jackson-addon:<insert version>'
    
    // additional processor to generate a file with all keys as string constants
    annotationProcessor 'de.devland.esperandro:esperandro-keys-gen:<insert version>'
    
    // additional processor to generate a file with all keys as string resources
    annotationProcessor 'de.devland.esperandro:esperandro-resources-gen:<insert version>'
    
