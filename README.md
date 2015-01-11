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
