KNIME Scala node wizard
=======================

The Scala version of KNIME node wizard.

How to install:
 - download eclipse luna
    (http://www.eclipse.org/downloads/packages/eclipse-rcp-and-rap-developers/lunasr1) (Scala IDE might also be an option)
 - install there the Scala IDE 4.0.x version for Scala 2.11
    (http://download.scala-ide.org/sdk/lithium/e44/scala211/stable/site)
 - (optionally you might want to install the 4.4 Buckminster too: http://download.eclipse.org/tools/buckminster/updates-4.4M6 or http://download.eclipse.org/tools/buckminster/headless-4.4M6)
 - clone this repository
 - import the org.knime.workbench.extension.scala project
 - (set the target platform to the `Running Platform` -in case you have changed)
 - start eclipse with the plugin, import org.knime.workbench.extension.scala.helper plugin in case you want to use implicits.
 - (export the plugin org.knime.workbench.extension.scala.helper -in case you want to use it)
 - export the org.knime.workbench.extension.scala plugin (preferably with a feature and update site with the helper project)
 - install the plugin/feature.

How to develop:
 - clone the repository
 - import the projects
 - set the target platform to the definition defined in the org.knime.workbench.extension.scala.target project when you want to work on the helper, otherwise keep the `Running Platform`.
 - test your modifications, by creating new projects, or adding new nodes to existing projects (in the test eclipse environment)

How to use:
 - create a new project (Create a new KNIME Node-Extension (Scala)) using the wizard and have fun coding KNIME nodes in Scala ;)

Building with Buckminster
-------------------------
Based on [Build Scala projects with Eclipse Buckminster](http://www.michel-kraemer.com/build-scala-projects-with-eclipse-buckminster):
Download the [latest director](http://www.eclipse.org/downloads/download.php?file=/tools/buckminster/products/director_latest.zip) and unzip to a location (`c:\java\director` in this example).

~~`C:\Java\director>director -r http://download.eclipse.org/tools/buckminster/headless-4.3 -d c:\java\buckminster -p Buckminster -i org.eclipse.buckminster.cmdline.product`~~
~~`C:\Java\director>cd c:\java\buckminster`~~
~~`c:\Java\buckminster>buckminster install http://download.eclipse.org/tools/buckminster/headless-4.3 org.eclipse.buckminster.core.headless.feature`~~

~~`c:\Java\buckminster>buckminster install http://download.eclipse.org/tools/buckminster/headless-4.3 org.eclipse.buckminster.emma.headless.feature`~~
~~`No suitable feature/version found that matches org.eclipse.buckminster.emma.head
less.feature.feature.group`~~

~~`c:\Java\buckminster>buckminster install http://download.eclipse.org/tools/buckminster/headless-4.3 org.eclipse.buckminster.git.headless.feature`~~
~~`c:\Java\buckminster>buckminster install http://download.eclipse.org/tools/buckminster/headless-4.3 org.eclipse.buckminster.pde.headless.feature`~~
~~`c:\Java\buckminster>cd ..\director`~~

    c:\Java\director>director -r http://download.eclipse.org/releases/kepler/ -d c:\java\buckminster_eclipse -profileProperties "org.eclipse.update.install.features=true" -p SDKProfile -i org.eclipse.sdk.ide

Note: You might need to let Java out of your firewall at this point.

    c:\Java\director>cd ..\buckminster_eclipse
    c:\Java\buckminster_eclipse>eclipsec -nosplash -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/tools/buckminster/headless-4.3 -installIU org.eclipse.buckminster.core.headless.feature.feature.group -installIU org.eclipse.buckminster.git.headless.feature.feature.group -installIU org.eclipse.buckminster.pde.headless.feature.feature.group
    c:\Java\buckminster_eclipse>eclipsec -nosplash -application org.eclipse.equinox.p2.director -repository http://download.scala-ide.org/sdk/lithium/e38/scala211/stable/site -installIU org.scala-ide.sdt.feature.feature.group -installIU org.scala-ide.sdt.weaving.feature.feature.group
    echo -Dsdtcore.headless >> eclipse.ini

Usage (you should update the `buckminster.properties` to your needs, [here](knime_scala_node_wizard/org.knime.workbench.scala.site/buckminster.properties) is an example):

    c:\java\buckminster_eclipse\eclipsec -nosplash -application org.eclipse.buckminster.cmdline.headless importtargetdefinition -A /path/to/target/definition.target
    c:\java\buckminster_eclipse\eclipsec -nosplash -application org.eclipse.buckminster.cmdline.headless build
    c:\java\buckminster_eclipse\eclipsec -nosplash -application org.eclipse.buckminster.cmdline.headless perform -D target.os=* -D target.ws=* -D target.arch=* -P /path/to/buckminster.properties org.knime.workbench.scala.site#site.p2

Though [this](http://www.ralfebert.de/archive/eclipse_rcp/rcp_builds/) might be more useful.
