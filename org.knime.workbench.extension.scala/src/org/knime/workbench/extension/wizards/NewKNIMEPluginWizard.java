/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ----------------------------------------------------------------------------
 */
package org.knime.workbench.extension.wizards;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.knime.workbench.plugin.KNIMEExtensionPlugin;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Wizard for creating a new Plugin-Project, containing a "stub implementation"
 * of NodeModel/Dialog/View.
 *
 * @author Florian Georg, University of Konstanz
 * @author Christoph Sieb, University of Konstanz
 */
//@SuppressWarnings("restriction")
public class NewKNIMEPluginWizard extends Wizard implements INewWizard {
    private static final String EOL = System.getProperty("line.separator");

    private NewKNIMEPluginWizardPage m_page;

    private ISelection m_selection;

    /**
     * Constructor for NewKNIMEPluginWizard.
     */
    public NewKNIMEPluginWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    /**
     * Adding the page to the wizard.
     */

    @Override
    public void addPages() {
        m_page = new NewKNIMEPluginWizardPage(m_selection);
        addPage(m_page);
    }

    /**
     * This method is called when 'Finish' button is pressed in the wizard. We
     * will create an operation and run it using wizard as execution context.
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        final String projectName;
        if (m_page.addToExistingProject()) {
            projectName = m_page.getExistingProjectName();
        } else {
            projectName = m_page.getProjectName();
        }
        final Properties substitutions = m_page.getSubstitutionMap();
        final boolean includeSampleCode = m_page.getIncludeSampleCode();
        final boolean useImplicits = m_page.getUseImplicits();

        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor)
                    throws InvocationTargetException {
                try {
                    doFinish(projectName, substitutions, includeSampleCode, useImplicits,
                            monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            getContainer().run(false, false, op);
        } catch (InterruptedException e) {
            MessageDialog.openError(getShell(), "Error", e.getMessage());
            logError(e);
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException
                    .getMessage());
            logError(e);
            return false;
        }
        return true;
    }

    /**
     * Logs an error.
     *
     * @param e the exception
     */
    private void logError(final Exception e) {
        KNIMEExtensionPlugin.getDefault().getLog().log(
                new Status(IStatus.ERROR, KNIMEExtensionPlugin.getDefault()
                        .getBundle().getSymbolicName(), 0, e.getMessage() + "",
                        e));
    }

    /**
     * Determine if the project with the given name is in the current workspace.
     *
     * @param projectName String the project name to check
     * @return boolean true if the project with the given name is in this
     *         workspace
     */
    static boolean isProjectInWorkspace(final String projectName) {
        IProject project = getProjectForName(projectName);
        if (project != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the project for the given project name. <code>null</code> if the
     * project is not in the workspace.
     *
     * @param projectName String the project name to check
     * @return {@link IProject} if the project with the given name is in this
     *         workspace, <code>null</code> otherwise
     */
    static IProject getProjectForName(final String projectName) {
        if (projectName == null) {
            return null;
        }
        IProject[] workspaceProjects = getProjectsInWorkspace();
        for (int i = 0; i < workspaceProjects.length; i++) {
            if (projectName.equals(workspaceProjects[i].getName())) {
                return workspaceProjects[i];
            }
        }
        return null;
    }

    /**
     * Retrieve all the projects in the current workspace.
     *
     * @return IProject[] array of IProject in the current workspace
     */
    static IProject[] getProjectsInWorkspace() {
        return ResourcesPlugin.getWorkspace().getRoot().getProjects();

    }

    /**
     * The worker method. It will find the container, create the file if missing
     * or just replace its contents, and open the editor on the newly created
     * file.
     *
     * @param projectName
     * @param substitutions
     * @param includeSampleCode
     * @param useImplicits use implicits in the templates
     * @param monitor
     */
    private void doFinish(final String projectName,
            final Properties substitutions, final boolean includeSampleCode,
            boolean useImplicits, final IProgressMonitor monitor) throws CoreException {
        // set the current year in the substitutions
        Calendar cal = new GregorianCalendar();
        substitutions.setProperty(NewKNIMEPluginWizardPage.SUBST_CURRENT_YEAR,
                Integer.toString(cal.get(Calendar.YEAR)));

        String packageName =
                substitutions.getProperty(
                        NewKNIMEPluginWizardPage.SUBST_BASE_PACKAGE,
                        "knime.dummy");
        String nodeName =
                substitutions.getProperty(
                        NewKNIMEPluginWizardPage.SUBST_NODE_NAME, "Dummy");

        boolean createNewProject = !isProjectInWorkspace(projectName);

        // the project, the plugin.xml, the manifest, the src/bin folder
        // are only created if a new project is requested
        IContainer container;
        if (createNewProject) {
            // create the hosting project
            monitor.beginTask("Creating " + projectName, 20);
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IProject project = root.getProject(projectName);
            if (project.exists()) {
                throwCoreException("Project \"" + projectName
                        + "\" already exist.");
            }
            project.create(monitor);
            project.open(monitor);
            container = project;
            monitor.worked(2);
            // 1. create plugin.xml / plugin.properties
            monitor.beginTask("Creating plugin descriptor/properties ....", 6);
            createFile("plugin.xml", "plugin.template", substitutions, monitor,
                    container);
            createFile("build.properties", "build.properties.template",
                    substitutions, monitor, container);
            createFile(".classpath", "classpath.template", substitutions,
                    monitor, container);
            createFile(".project", "project.template", substitutions, monitor,
                    container);
            monitor.worked(6);

            // 2. create Manifest.MF
            monitor.beginTask("Creating OSGI Manifest file ....", 2);
            final IFolder metaContainer =
                    container.getFolder(new Path("META-INF"));
            metaContainer.create(true, true, monitor);
            createFile("MANIFEST.MF", !useImplicits ? "MANIFEST.template" : "MANIFESTImplicits.template", substitutions,
                    monitor, metaContainer);
            monitor.worked(2);
        } else {
            container = getProjectForName(projectName);
            // extend the plugin.xml with the new node extension entry
            addNodeExtensionToPlugin((IProject)container, packageName + "."
                    + nodeName + "NodeFactory");
        }

        // 3. create src/bin folders
        final IFolder srcContainer = container.getFolder(new Path("src/main/scala"));
        final IFolder binContainer = container.getFolder(new Path("bin"));
        if (!srcContainer.exists()) {
            monitor.beginTask("Creating src folder ....", 6);
            if (!srcContainer.getParent().exists()) {
            	IFolder grandParent; 
            	if (!(grandParent = (IFolder)srcContainer.getParent().getParent()).exists()) {
            		grandParent.create(true, true, monitor);
            	}
            	IFolder parent = (IFolder) srcContainer.getParent();
        		parent.create(true, true, monitor);
            }
            srcContainer.create(true, true, monitor);
        }
        if (!binContainer.exists()) {
            monitor.beginTask("Creating bin folder ....", 2);
            binContainer.create(true, true, monitor);
        }

        monitor.worked(2);

        // 4. create package (folders)
        String[] pathSegments = packageName.split("\\.");
        monitor.beginTask("Creating package structure ....",
                pathSegments.length);
        IFolder packageContainer = container.getFolder(new Path("src/main/scala"));
        for (int i = 0; i < pathSegments.length; i++) {
            packageContainer =
                    packageContainer.getFolder(new Path(pathSegments[i]));
            if (!packageContainer.exists()) {
                packageContainer.create(true, true, monitor);
            }
            monitor.worked(1);
        }

        // 4.1. create Bundel Activator if this is a new project
        if (createNewProject) {
            monitor.beginTask("Creating Bundle Activator....", 1);
            createFile(nodeName + "NodePlugin.java",
                    "BundleActivator.template", substitutions, monitor,
                    packageContainer);
        }
        monitor.worked(1);

        // 5. create node factory
        monitor.beginTask("Creating node factory ....", 1);
        createFile(nodeName + "NodeFactory.scala", "NodeFactory.template",
                substitutions, monitor, packageContainer);

        monitor.worked(1);

        // 6. create node model
        monitor.beginTask("Creating node model ....", 1);
        String nodeModelTemplate = "NodeModel.template";
        if (!includeSampleCode) {
            nodeModelTemplate = "NodeModelEmpty.template";
        }
        if (useImplicits) {
        	nodeModelTemplate = nodeModelTemplate.replaceFirst("\\.template", "Implicits.template");
        }
        final IFile nodeModelFile =
                createFile(nodeName + "NodeModel.scala", nodeModelTemplate,
                        substitutions, monitor, packageContainer);

        monitor.worked(1);

        // 7. create node dialog
        monitor.beginTask("Creating node dialog ....", 1);
        String nodeDialogTemplate = "NodeDialog.template";
        if (!includeSampleCode) {
            nodeDialogTemplate = "NodeDialogEmpty.template";
        }
        createFile(nodeName + "NodeDialog.scala", nodeDialogTemplate,
                substitutions, monitor, packageContainer);
        monitor.worked(1);

        // 8. create node view
        monitor.beginTask("Creating node view ....", 1);
        String nodeViewTemplate = "NodeView.template";
        if (!includeSampleCode) {
            nodeViewTemplate = "NodeViewEmpty.template";
        }
        createFile(nodeName + "NodeView.scala", nodeViewTemplate,
                substitutions, monitor, packageContainer);
        monitor.worked(1);

        // 9. create node description xml file
        monitor.beginTask("Creating node description xml file ....", 1);
        createFile(nodeName + "NodeFactory.xml", "NodeDescriptionXML.template",
                substitutions, monitor, packageContainer);

        monitor.worked(1);

        // 10. create package.html file
        if (!packageContainer.getFile("package.html").exists()) {
            monitor.beginTask("Creating package.html file ....", 1);
            createFile("package.html", "packageHTML.template", substitutions,
                    monitor, packageContainer);
        }

        monitor.worked(1);

        // 11. copy additional files (icon, ...)
        if (!packageContainer.getFile("default.png").exists()) {
            monitor.beginTask("Adding additional files....", 2);
            IFile defIcon = packageContainer.getFile("default.png");

            // copy default.png
            URL url =
                    KNIMEExtensionPlugin.getDefault().getBundle().getEntry(
                            "templates/default.png");
            try {
                defIcon.create(url.openStream(), true, monitor);
            } catch (IOException e1) {
                e1.printStackTrace();
                throwCoreException(e1.getMessage());
            }
        }

        monitor.worked(2);

        // open the model file in the editor
        monitor.setTaskName("Opening file for editing...");
        getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbenchPage page =
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                .getActivePage();
                try {
                    IDE.openEditor(page, nodeModelFile, true);
                } catch (PartInitException e) {
                }
            }
        });
        monitor.worked(1);
    }

    private static void addNodeExtensionToPlugin(final IProject project,
            final String factoryClassName) {
        project.getFile("plugin.xml").exists();
        File pluginXml = project.getFile("plugin.xml").getLocation().toFile();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pluginXml);
            NodeList rootElements = document.getChildNodes();
            Node rootElement = null;
            for (int i = 0; i < rootElements.getLength(); i++) {
                Node element = rootElements.item(i);
                if (element.getNodeName().equals("plugin")) {
                    rootElement = element;
                    break;
                }
            }
            if (rootElement == null) {
                throw new RuntimeException(
                        "Project does not contain a valid plugin.xml");
            }

            NodeList children = rootElement.getChildNodes();
            Node nodeExtensionElement = null;
            for (int i = 0; i < children.getLength(); i++) {
                Node element = children.item(i);
                if (element.getNodeName().equals("extension")) {
                    NamedNodeMap attributes = element.getAttributes();
                    if (attributes.getNamedItem("point").getNodeValue().equals(
                            "org.knime.workbench.repository.nodes")) {
                        nodeExtensionElement = element;
                        break;
                    }

                }
            }
            // if a node extension point did not exist, create one
            if (nodeExtensionElement == null) {
                nodeExtensionElement = document.createElement("extension");
                Attr pointAttr = document.createAttribute("point");
                pointAttr.setValue("org.knime.workbench.repository.nodes");
                nodeExtensionElement.appendChild(pointAttr);
                rootElement.appendChild(nodeExtensionElement);
            }
            // now create a new node element
            Node newNodeElement = document.createElement("node");
            ((Element)newNodeElement).setAttribute("category-path", "/");
            ((Element)newNodeElement).setAttribute("factory-class",
                    factoryClassName);
            ((Element)newNodeElement).setAttribute("id", factoryClassName);
            nodeExtensionElement.appendChild(newNodeElement);
            // Prepare the DOM document for writing
            document.normalize();
            document.normalizeDocument();
            DOMSource source = new DOMSource(document);
            // Prepare the output file
            Result result = new StreamResult(pluginXml);

            // Write the DOM document to the file
            Transformer xformer =
                    TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
            // XMLSerializer serializer = new XMLSerializer();
            // serializer.setOutputCharStream(new FileWriter(pluginXml));
            // serializer.serialize(document);
        } catch (Exception pce) {
            throw new RuntimeException(pce);
        }
    }

    /**
     * @param monitor
     * @param container
     * @return The file
     * @throws CoreException
     */
    private IFile createFile(final String filename, final String templateFile,
            final Properties substitutions, final IProgressMonitor monitor,
            final IContainer container) throws CoreException {
        final IFile file = container.getFile(new Path(filename));
        try {
            InputStream stream =
                    openSubstitutedContentStream(templateFile, substitutions);
            if (file.exists()) {
                file.setContents(stream, true, true, monitor);
            } else {
                file.create(stream, true, monitor);
            }
            stream.close();
        } catch (IOException e) {
            throwCoreException(e.getMessage());
        }
        return file;
    }

    /**
     * We will initialize file contents with an empty String.
     *
     * @throws CoreException
     */
    private InputStream openSubstitutedContentStream(
            final String templateFileName, final Properties substitutions)
            throws CoreException {
        URL url =
                KNIMEExtensionPlugin.getDefault().getBundle().getEntry(
                        "templates/" + templateFileName);
        String contents = "";
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            StringBuffer buf = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buf.append(line).append(EOL);
            }
            reader.close();
            contents = buf.toString();

            // substitute all placeholders
            // TODO this eats memory... make it more beautiful
            for (Iterator<Object> it = substitutions.keySet().iterator();
                it.hasNext();) {
                String key = (String)it.next();
                String sub = substitutions.getProperty(key, "??" + key + "??");
                contents = contents.replaceAll(key,
                        Matcher.quoteReplacement(sub));
            }

        } catch (Exception e) {
            logError(e);
            throwCoreException("Can't process template file: url=" + url
                    + " ;file=" + templateFileName);
        }

        return new ByteArrayInputStream(contents.getBytes());
    }

    /**
     *
     * @param message
     * @throws CoreException
     */
    private void throwCoreException(final String message) throws CoreException {
        IStatus status =
                new Status(IStatus.ERROR, "org.knime.workbench.extension",
                        IStatus.OK, message, null);
        throw new CoreException(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IWorkbench workbench,
            final IStructuredSelection selection) {
        m_selection = selection;
    }

}
