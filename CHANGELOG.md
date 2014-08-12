Changelog
=========

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
