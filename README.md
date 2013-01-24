knime_scala_node_wizard
=======================

The Scala version of KNIME node wizard.

How to install:
 - download the KNIME SDK
    (http://knime.org/downloads/overview)
 - install there the Scala IDE 2.1 (M3) version for Scala 2.10
    (http://scala-ide.org/download/milestone.html#scala_ide_21_milestone_3)
 - clone this repository
 - export the plugins
 - install the plugins to eclipse (dropins folder)

How to develop:
 - clone the repository
 - import the projects (to your KNIME SDK with Scala 2.10)
 - set the target platform to the definition defined in the org.knime.workbench.extension.scala.target project
 - test your modifications, by creating new projects, or adding new nodes to existing projects (in the test eclipse environment)

How to use:
 - create a new project using the wizard and have fun coding KNIME nodes in Scala ;)
