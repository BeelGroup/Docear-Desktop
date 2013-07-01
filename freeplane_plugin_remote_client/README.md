# Plugin webservice

## Depdendency management

### Advantages of Apache Ivy

* change only one line of code in ivy.xml to add a dependency
* before this mechanism you had to download the jar, enter it ant.properties, enter it in build.xml and add it in MANIFEST.MF

### Resolve dependencies

* run `ant resolve` or `ant build` or `ant build`

### How to add new dependencies

1. go to http://mvnrepository.com/ and enter the library name in the search input field
    * example `jersey-bundle`
2. click on the result list the name to the correct library
    * you see http://mvnrepository.com/artifact/com.sun.jersey/jersey-bundle
3. click on the desired version
    * for example 1.16 you will go to http://mvnrepository.com/artifact/com.sun.jersey/jersey-bundle/1.16
4. click in the tab list of Maven, Ivy, Grape, Gradle, Buildr, SBT the `Ivy` tab
    * you will see in the textarea input field `<dependency org="com.sun.jersey" name="jersey-bundle" rev="1.16"/>`
5. add the xml snippet into the `<dependencies>` tag of docear_plugin_webservice/ivy.xml
