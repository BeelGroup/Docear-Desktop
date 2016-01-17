# Desktop
Docear's desktop version (GPL)

Docear is an open source project. It is completely free, you can download and change the source code to 
your need and taste. If you want to share your developed features or bugfixes with us or if you want to 
test our software you will be highly welcome to do so.  :-)

The wiki gives an introduction of how you can download the source code, compile it, extend it and share 
your work with us.

We highly appreciate any offer to help and would love to work closely with you!

# Testing Docear
We will release experimental releases occasionally. Please note that our experimental releases were not 
thoroughly tested by us. You should not work with them on productive data or regularly make a backup of 
all your data. As a registered Sourceforge user you can subscribe to the forum to be notified about new 
posts. They contain new features which are to be integrated in our official version of Docear.

If you want to test our experimental releases you are highly welcome. Please tell us about any bugs and 
issues you can find.

# Code Documentation
Docear is based on the mind mapping software Freeplane. Like Freeplane it consists of severall OSGi 
plugins which offer their functionality to the core components “freeplane” and “docear_plugin_core”. 
In general all Docear plugins use “docear_plugin_” as a prefix to their name whereas Freeplane plugins 
use “freeplane_plugin_” as a prefix.

Docear is not a competitor project to Freeplane but targets a different groups of users which results 
in a close relationship between both the Docear and the Freeplane team. All cross-project decisions 
are made together, Docear merges with the Freeplane code regularly and Docear will probably be included 
as an extension to future versions of Freeplane.

In addition the Docear team is actively contributing to the Freeplane code, which means that all 
features which are useful for a mind mapping software are directly developed as Freeplane plugins. 
For instance we have implented the workspace component. It does not depend on any Docear specific 
OSGi plugin and is named “freeplane_plugin_workspace”. All features which follow a scientfic purpose, 
like literate or reference management, are developed as Docear OSGi plugins.

If you need any help with Freeplane specific code, the developer’s wiki of the Freeplane project and 
Freelane’s developer forum would be a good location to start searching for answers. There is currently 
no counterpart for the Docear projects. If you need any help regarding Docear specific code, please 
contact us directly.

# Finding a development task
Please visit the issues to get an idea of what we are currently 
working on.

If you want to help us developing Docear, please join our mailing list 
https://groups.google.com/forum/#!forum/docear-dev and describe what feature you want to implement or 
which bugfix you can provide. You can also ask us to find a task together with you.

When developing for Docear, please adhere to the following guide:

1. Please see issues for what to work on. Before working on your task, please make sure that you have 
a clean and unchanged branch from our official Docear repository. During and at the end of your work 
you should merge with the Docear repository regularly to make sure that your code still works with a 
newer version of Docear. After you have done your work please clean your code from unnecessary methods 
or debugging messages.

2. Please use Docear or freeplane methods in your code whenever possible. Do not create any unnecessary 
redundancies.

3. Please add new classes to the right package in the right plugin. Keep in mind that Docear specific 
plugins only share common dependencies on the “docear_plugin_core” and the “freeplane” plugin.

4. Please keep your code simple and well structured
