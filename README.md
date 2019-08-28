[![Maven Central](https://images1-focus-opensocial.googleusercontent.com/gadgets/proxy?container=focus&url=https%3A%2F%2Fimg.shields.io%2Fmaven-central%2Fv%2Fde.devland.esperandro%2Fesperandro-api%3Fstyle%3Dflat-square)](https://search.maven.org/search?q=g:de.devland.esperandro)
[![Build Status](https://images1-focus-opensocial.googleusercontent.com/gadgets/proxy?container=focus&url=https%3A%2F%2Fimg.shields.io%2Ftravis%2Fdkunzler%2Fesperandro%3Fstyle%3Dflat-square)](https://travis-ci.org/dkunzler/esperandro)

# esperandro


Easy SharedPreference Engine foR ANDROid

Please refer to the [wiki](https://github.com/dkunzler/esperandro/wiki) for in-depth examples.

See the [changelog](changelog.md) for a brief overview of recent changes.

### Short example

esperandro makes it simple to use `SharedPreference`s in a nicer and less error-prone way.

**Without *esperandro* a key is referenced by a string:**

* `String myPreference = preferences.getString("myPreference", "default value") // load preference`
* `preferences.edit().putString("myPreference", myPreference).commit() // store preference`



**With *esperandro* you adhere to an interface that you defined:**

    @SharedPreferences
    interface MyPreferences {
        String myPreference();
        void myPreference(String myPreference);
    }
    
* `String myPreference = preferences.myPreference() // load preference`
* `preferences.myPreference(myPreference) // store preference`


Type safe, easy, less error-prone.

Interested? [Get Started](https://github.com/dkunzler/esperandro/wiki/Basic-Usage)

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
    
