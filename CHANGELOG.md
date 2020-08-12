Changelog
=========
4.0.0
-----
 * Changed default getter/putter naming scheme. Methods need a get/set prefix now. The prefence name
 is determined by the string after get/set changing the first letter to lowercase. Example: 
 `getStringPreference` leads to a preference key named `stringPreference`
 * Add `@Get/@Put` annotations to be able to use arbitrary named methods.
 * Methods with runtime defaults don't need a Suffix anymore since the name is unique now
 * Add possibility to use boolean return type for collection actions that propagate the result
 of `add/remove` back to the caller
 * Removed support for reflection in the processor. That means that all used interfaces must be in
 the same compilation unit. (not in a binary library dependency)


3.0.3
-----
 * fix #73
 * update Jackson because of security issue

3.0.2
-----
 * fix large Heap on build by not retaining `ProcessingEnvironment`
 * update Jackson because of security issue

3.0.1
-----
 * suppress "unchecked" warnings in generated code
 * update Jackson because of security issue

3.0.0
-----
 * complete overhaul of processor structure, split into 3 seperate processor artifacts:
    - _esperandro-preference-gen_ The default processor that generates implementations for 
    interfaces annotated with @SharedPreferences. This one is incremental.
    - _esperandro-resources-gen_ A processor that generates a resource file for accessing the
    names of the preferences in other resource files via @string/... This is not incremental.
    - _esperandro-keys-gen_ A processor that generates a Java class with the same name as the 
    preference interface plus *Keys* suffix. This contains string constants for all defined
    preferences if string based access is still needed. This one is incremental.
 * removed deprecated values
 * moved @GenerateStringResources and @Cached from experimental package
 * incorporated UnsafeActions into SharedPreferenceActions
 * update dependencies
 * ability to choose LruCache implementation from support, androidx or framework

2.7.1
-----
 * fix ofStatement default for serialized preferences

2.7.0
-----
 * fix 51
 * add ofStatement definition to @Default annotation

2.6.0
-----
 * fix #48
 * fix #47, use same visibility for generated preferences as interface
 * add char and byte support
 * update dependencies

2.5.2
-----
 * simple set and get methods via name of preference
 * Add support for Arrays

2.5.1
-----
 * class default for complex types (proved class will be instantiated with default constructor)

2.5.0
-----
 * internal refactoring
 * generate String constants of preference names
 * possibility to add and remove values from Collection types via $Add and $Remove prefixes

2.4.1
-----
 * fix warning message
 * automatic cache size

2.4.0
-----
 * fix #41, #38
 * add resetCache method on CacheActions
 * fix NullPointerException in cache
 * warn when caching default SharedPreferences
 * experimental String resource generation

2.3.1
-----
 * fix #37
 * update dependencies

2.3.0
-----
 * generated container classes implement the Serializable interface
 * caching via @Cached annotation

2.2.0
-----
 * fix for issue #29: search JacksonSerializer automatically on classpath
 * do not generate API Level checks
 * dropped complete support for API < 9
 * update dependencies

2.1.0
-----
 * fix for issue #23 by wrapping generics into a container
 * bumped dependency versions (this fixes #25)
 * new SharedPreferenceActions action to clear only values that were explicitly defined in the interface

2.0.0
-----
 * Allow getter with a runtime default by appending the `$Default` suffix
 * Add Jackson Serializer plugin

1.1.2
-----
 * small follow-up bugfix regarding #16

1.1.1
-----
 * fix #16
 * add prefix for esperandro compiler messages
 * update dependencies

1.1
---
 * changed default file system sync of preferences from commit() to apply(). Reduces load on the UI Thread.
 * added support for setters with boolean return type. Those still use commit() to be able to tell about the success of the operation
 * added action "initDefaults". Can be used to initialize default values for immediate access in PreferenceActivities
 * circumvent exception when using maven and robolectric

1.0
---
 * updated JavaWriter to 2.1.2
 * added remove functionality in SharedPreferenceActions
 * minor bugfix when using generics as serializable Objects

0.13
----
 * allow storing and accessing of serializable Objects into the preferences
 * fixed interface inheritance for library projects in eclipse and for super interfaces in jars

0.12
----
 * added interface inheritance

0.11
----
 * bugfixes

0.10
----
 * bugfixes

0.9
---
 * intial release
