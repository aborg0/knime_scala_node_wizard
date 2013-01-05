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

import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.workbench.plugin.KNIMEExtensionPlugin;

/**
 * This page enables the user to enter the information needed to create the
 * extension plugin project. The Wizard collects the values via a substitution
 * map, that is used to fill out the templates.
 *
 * @author Florian Georg, University of Konstanz
 * @author Christoph Sieb, University of Konstanz
 */
@SuppressWarnings("restriction")
public class NewKNIMEPluginWizardPage extends WizardPage implements Listener {
    private static final Pattern PACKAGE_RE =
        Pattern.compile("^[a-z][a-z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");


    static final String SUBST_PROJECT_NAME = "__PROJECT_NAME__";

    static final String SUBST_BASE_PACKAGE = "__BASE_PACKAGE__";

    static final String SUBST_BASE_PACKAGE_LAST = "__BASE_PACKAGE_LAST__";

    static final String SUBST_NODE_NAME = "__NODE_NAME__";

    static final String SUBST_DESCRIPTION = "__DESCRIPTION__";

    static final String SUBST_JAR_NAME = "__JAR_NAME__";

    static final String SUBST_VENDOR_NAME = "__VENDOR_NAME__";

    static final String SUBST_CURRENT_YEAR = "__CURRENT_YEAR__";

    private static final String SUBST_NODE_TYPE = "__NODE_TYPE__";

    private Text m_projectNameField;

    private Combo m_comboExistingProjects;

    private Button m_newProjectRadio;

    private Button m_existingProjectRadio;

    private Text m_textBasePackage;

    private Text m_textNodeName;

    private Text m_textVendor;

    private Text m_textDescription;

    private Combo m_comboNodeType;

    private Button m_includeSampleCode;

    private Button m_useImplicits;

    private TreeSelection m_selection;

    private String m_newPackageTempStore = "";

    private String m_existingPackageTempStore = "";

    private Button m_packageBrowseButton;

    private IJavaProject m_currentJavaProject;

    // load this icon only once per session (static)
    private static final ImageDescriptor ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin(KNIMEExtensionPlugin.ID,
                    "icons/knime_extension55.png");

    /**
     * Constructor for WizardPage.
     *
     * @param selection The initial selection
     */
    public NewKNIMEPluginWizardPage(final ISelection selection) {
        super("wizardPage");
        setTitle("Create new KNIME Node-Extension (Scala)");
        setDescription("This wizard creates a KNIME Node-Extension "
                + "(optionally with an initial plugin project)");
        setImageDescriptor(ICON);
        if (selection instanceof TreeSelection) {
            m_selection = (TreeSelection)selection;
        }
    }

    /**
     *
     * @return The substitution map
     */
    public Properties getSubstitutionMap() {
        Properties map = new Properties();

        map.put(SUBST_PROJECT_NAME, getProjectName());
        map.put(SUBST_BASE_PACKAGE, m_textBasePackage.getText());
        map.put(SUBST_BASE_PACKAGE_LAST, m_textBasePackage.getText().substring(
        		m_textBasePackage.getText().lastIndexOf('.') + 1));
        map.put(SUBST_NODE_NAME, m_textNodeName.getText());
        // the description is preprocessed here
        // not so nice, format the created java file instead (this also
        // reflects then the users preferences!)
        map.put(SUBST_DESCRIPTION,
                m_textDescription.getText().replaceAll("\\n", " * \\n"));
        map.put(SUBST_VENDOR_NAME, m_textVendor.getText());
        map.put(SUBST_JAR_NAME, m_textNodeName.getText().toLowerCase() + ".jar");
        map.put(SUBST_NODE_TYPE, m_comboNodeType.getText());
        return map;

    }

    /**
     * Return the status of the checkmark.
     *
     * @return true if sample code should be included in the templates.
     */
    public boolean getIncludeSampleCode() {
        return m_includeSampleCode.getSelection();
    }

    /**
     * Return the status of the checkmark.
     *
     * @return true if the project should use implicits.
     */
    public boolean getUseImplicits() {
        return m_useImplicits.getSelection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());

        initializeDialogUnits(parent);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        setPageComplete(validatePage());
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);

        // project specification group
        final Composite projectGroup = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // existing project specification group
        final Composite existProjectGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        existProjectGroup.setLayout(layout);
        existProjectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // create radiobutton to select new project option
        m_newProjectRadio = new Button(projectGroup, SWT.RADIO);
        m_newProjectRadio.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                m_existingProjectRadio.setSelection(false);
                m_comboExistingProjects.setEnabled(false);
                m_projectNameField.setEnabled(true);
                projectGroup.setEnabled(true);
                boolean valid = validatePage();
                setPageComplete(valid);
                m_existingPackageTempStore = m_textBasePackage.getText();
                m_textBasePackage.setText(m_newPackageTempStore);
                m_packageBrowseButton.setEnabled(false);
            }

        });

        // new project label
        Label projectLabel = new Label(projectGroup, SWT.NONE);
        projectLabel.setText("New Project Name:");
        projectLabel.setFont(projectGroup.getFont());

        // new project name entry field
        m_projectNameField = new Text(projectGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        m_projectNameField.setLayoutData(data);
        m_projectNameField.setFont(projectGroup.getFont());
        m_projectNameField.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(final Event e) {
                boolean valid = validatePage();
                setPageComplete(valid);

            }
        });

        // create radiobutton to select existing project option
        m_existingProjectRadio = new Button(existProjectGroup, SWT.RADIO);
        m_existingProjectRadio.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                m_newProjectRadio.setSelection(false);
                m_projectNameField.setEnabled(false);
                m_comboExistingProjects.setEnabled(true);
                boolean valid = validatePage();
                setPageComplete(valid);
                m_newPackageTempStore = m_textBasePackage.getText();
                m_textBasePackage.setText(m_existingPackageTempStore);
                // darf hier nicht verwendet werde
                // selection auch nicht
                // selection nur zum initialisieren
                // first selection in variable speichern und dann merken wie
                // auch new project variable.
                m_textBasePackage.setText(getSelectedPackage());
                m_packageBrowseButton.setEnabled(true);
            }
        });

        // new project label
        projectLabel = new Label(existProjectGroup, SWT.NONE);
        projectLabel.setText("Existing Project:");
        projectLabel.setFont(existProjectGroup.getFont());

        // combo box for existing project selection
        m_comboExistingProjects = createProjectsCombo(existProjectGroup);
        data = new GridData(GridData.FILL_BOTH);
        m_comboExistingProjects.setFont(composite.getFont());
        m_comboExistingProjects.setLayoutData(data);
        m_comboExistingProjects.addListener(SWT.Modify,
                m_existingProjectChangedListener);

        // Group for KNIME settings
        Group settingsGroup = new Group(composite, SWT.NONE);
        settingsGroup.setText("KNIME extension settings");
        layout = new GridLayout();
        layout.numColumns = 2;
        settingsGroup.setLayout(layout);
        settingsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        //
        // Node/Class name
        //
        Label label = new Label(settingsGroup, SWT.NONE);
        label.setText("Node class name (e.g. 'MyLearner') : ");
        label.setFont(composite.getFont());
        // base package text field
        m_textNodeName = new Text(settingsGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        m_textNodeName.setFont(composite.getFont());
        m_textNodeName.setLayoutData(data);
        m_textNodeName.addListener(SWT.Modify, this);

        //
        // Base package
        //
        layout.numColumns = 3;
        settingsGroup.setLayout(layout);
        label = new Label(settingsGroup, SWT.NONE);
        label.setText("Package name (e.g. 'org.myname') : ");
        label.setFont(composite.getFont());
        // base package text field
        data = new GridData(GridData.FILL_HORIZONTAL);
        Composite compo = new Composite(settingsGroup, SWT.NONE);
        layout.numColumns = 2;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginWidth = 0;
        data.horizontalIndent = 0;
        compo.setLayout(layout);
        compo.setLayoutData(data);
        m_textBasePackage = new Text(compo, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        m_textBasePackage.setFont(composite.getFont());
        m_textBasePackage.setLayoutData(data);
        m_textBasePackage.addListener(SWT.Modify, this);

        m_packageBrowseButton = new Button(compo, SWT.PUSH);
        m_packageBrowseButton.setText("Browse");
        m_packageBrowseButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (m_currentJavaProject != null) {
                    try {
                        SelectionDialog dialog =
                                JavaUI.createPackageDialog(
                                        getShell(),
                                        m_currentJavaProject,
                                        IJavaElementSearchConstants.CONSIDER_REQUIRED_PROJECTS);
                        dialog.open();
                        Object[] results = dialog.getResult();
                        if (results.length >= 1) {
                            if (results[0] instanceof IPackageFragment) {

                                m_textBasePackage
                                        .setText(((IPackageFragment)results[0])
                                                .getElementName());
                            }
                        }
                    } catch (Exception ex) {
                        // do nothing
                    }
                }
            }

        });

        //
        // Vendor name
        //
        label = new Label(settingsGroup, SWT.NONE);
        label.setText("Node vendor (e.g. Your Name): ");
        label.setFont(composite.getFont());
        // base package text field
        m_textVendor = new Text(settingsGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        m_textVendor.setFont(composite.getFont());
        m_textVendor.setLayoutData(data);
        m_textVendor.addListener(SWT.Modify, this);

        //
        // description
        //
        label = new Label(settingsGroup, SWT.NONE);
        label.setText("Node description text : ");
        // base package text field
        m_textDescription =
                new Text(settingsGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP
                        | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 80;
        m_textDescription.setFont(composite.getFont());
        m_textDescription.setLayoutData(data);
        m_textDescription.addListener(SWT.Modify, this);

        //
        // node type
        //
        label = new Label(settingsGroup, SWT.NONE);
        label.setText("Node type:");
        // base package combo field
        m_comboNodeType = createCategoryCombo(settingsGroup);
        data = new GridData(GridData.FILL_BOTH);
        m_comboNodeType.setFont(composite.getFont());
        m_comboNodeType.setLayoutData(data);
        m_comboNodeType.addListener(SWT.Modify, this);

        // if a selection was made in the navigator, set the selection
        // as default, otherwise select the new project option as default
        if (m_selection != null && !m_selection.isEmpty()) {
            m_existingProjectRadio.setSelection(true);
            m_projectNameField.setEnabled(false);
        } else {
            m_newProjectRadio.setSelection(true);
            m_comboExistingProjects.setEnabled(false);
        }
        if (m_existingProjectRadio.getSelection()) {
            m_textBasePackage.setText(getSelectedPackage());
        } else {
            m_packageBrowseButton.setEnabled(false);
        }

        //
        // include sample code checkbox
        // new project label
        m_includeSampleCode = new Button(composite, SWT.CHECK);
        m_includeSampleCode.setText("Include sample code in generated classes");
        m_includeSampleCode.setFont(projectGroup.getFont());
        data = new GridData(GridData.FILL_BOTH);
        data.verticalIndent = 10;
        data.horizontalIndent = 7;
        // data.horizontalAlignment = SWT.CENTER;
        m_includeSampleCode.setLayoutData(data);
        m_includeSampleCode.setSelection(true);

        //
        // include sample code checkbox
        // new project label
        m_useImplicits = new Button(composite, SWT.CHECK);
        m_useImplicits.setText("Use implicits in the project");
        m_useImplicits.setFont(projectGroup.getFont());
        data = new GridData(GridData.FILL_BOTH);
        data.verticalIndent = 10;
        data.horizontalIndent = 7;
        // data.horizontalAlignment = SWT.CENTER;
        m_useImplicits.setLayoutData(data);
        m_useImplicits.setSelection(true);
    }

    private String getSelectedPackage() {
        if (m_selection == null || m_selection.isEmpty()) {
            return "";
        }

        Object o = m_selection.getFirstElement();
        if (o instanceof IJavaElement) {
            if (o instanceof IPackageFragment) {
                return ((IPackageFragment)o).getElementName();
            } else {
                IJavaElement je = (IJavaElement)o;
                while (je.getParent() != null
                        && !(je.getParent() instanceof IPackageFragment)) {
                    je = je.getParent();
                }
                return je.getElementName();
            }
        }

        return "";
    }

    /**
     * Creates the combo box with the possible node types. Uses the information
     * from the core factory defining the types.
     *
     * @param parent the parent composite of the combo box
     *
     * @return the created combo box
     */
    private static Combo createCategoryCombo(final Composite parent) {

        Combo typeCombo = new Combo(parent, SWT.READ_ONLY | SWT.BORDER);

        for (NodeType type : NodeFactory.NodeType.values()) {

            // unknown is just an internal type
            if (!type.equals(NodeType.Unknown) && !type.equals(NodeType.Missing)) {
                typeCombo.add(type.toString());

                if (typeCombo.getText() == null
                        || typeCombo.getText().trim().equals("")) {
                    typeCombo.setText(type.toString());
                }
            }
        }

        return typeCombo;
    }

    /**
     * Creates the combo box with the possible node types. Uses the information
     * from the core factory defining the types.
     *
     * @param parent the parent composite of the combo box
     *
     * @return the created combo box
     */
    private Combo createProjectsCombo(final Composite parent) {
        Combo projectsCombo = new Combo(parent, SWT.READ_ONLY | SWT.BORDER);

        int i = 0;
        for (IProject project : NewKNIMEPluginWizard.getProjectsInWorkspace()) {
            // unknown is just an internal type

            projectsCombo.add(project.getName());
            // set default selection
            if (m_selection != null && !m_selection.isEmpty()) {
                IProject toCompare;
                Object o = m_selection.getFirstElement();
                if (o instanceof IJavaElement) {
                    m_currentJavaProject = ((IJavaElement)o).getJavaProject();
                    toCompare = m_currentJavaProject.getProject();
                } else if (o instanceof IResource) {
                    toCompare = ((IResource)o).getProject();
                    m_currentJavaProject =
                            JavaModelManager.getJavaModelManager()
                                    .getJavaModel().getJavaProject(toCompare);
                } else {
                    continue;
                }

                if (toCompare.equals(project)) {
                    projectsCombo.select(i);
                }
            }

            i++;
        }

        return projectsCombo;
    }

    /**
     * This checks the text fields after a modify event and sets the
     * errormessage if necessary. This calls <code>validatePage</code> to
     * actually validate the fields.
     *
     * @param event
     */
    @Override
    public void handleEvent(final Event event) {
        if (event.type != SWT.Modify) {
            return;
        }
        boolean valid = validatePage();
        setPageComplete(valid);
    }

    /**
     * Validates the page, e.g. checks whether the textfields contain valid
     * values.
     *
     * @return <code>true</code> if all values are valid, <code>false</code>
     *         otherwise
     */
    protected boolean validatePage() {

        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();

        if (m_newProjectRadio == null
                && (m_selection == null || m_selection.isEmpty())) {
            setErrorMessage(null);
            setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
            return false;
        }
        if (m_newProjectRadio == null && m_selection != null
                && !m_selection.isEmpty()) {
            setErrorMessage(null);
            setMessage(null);
            return false;
        }

        if (m_newProjectRadio != null && m_newProjectRadio.getSelection()) {

            String projectFieldContents = getProjectName();
            if (projectFieldContents.equals("")) {
                setErrorMessage(null);
                setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
                return false;
            }

            IStatus nameStatus =
                    workspace.validateName(projectFieldContents,
                            IResource.PROJECT);
            if (!nameStatus.isOK()) {
                setErrorMessage(nameStatus.getMessage());
                return false;
            }

            IProject handle =
                    ResourcesPlugin.getWorkspace().getRoot()
                            .getProject(getProjectName());
            if (handle.exists()) {
                setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
                return false;
            }
        }

        if (m_existingProjectRadio != null
                && m_existingProjectRadio.getSelection()) {
            if (getExistingProjectName().equals("") && m_existingProjectRadio.getSelection()) { //$NON-NLS-1$
                setErrorMessage(null);
                setMessage("Select an existing project.");
                return false;
            }
        }

        // Node name
        String nodeName = m_textNodeName.getText();
        if (nodeName.length() == 0) {
            setErrorMessage(null);
            setMessage("Please provide a Node name");
            return false;
        }
        if ((!Character.isLetter(nodeName.charAt(0)))
                || (nodeName.charAt(0) != nodeName.toUpperCase().charAt(0))) {
            setErrorMessage("Node name must start with an uppercase letter");
            return false;
        }
        for (int i = 0; i < nodeName.length(); i++) {
            char c = nodeName.charAt(i);
            if (!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) {
                setErrorMessage("The class name '" + nodeName + "' is invalid");
                return false;
            }
        }
        if (m_existingProjectRadio.getSelection()) {
            IProject project =
                    NewKNIMEPluginWizard
                            .getProjectForName(getExistingProjectName());
            String path =
                    "src/" + m_textBasePackage.getText().replace('.', '/')
                            + "/" + nodeName;
            IFile file = project.getFile(new Path(path + "NodeModel.java"));
            if (file.exists()) {
                setErrorMessage("A node with the given name already exists.");
                return false;
            }
        }

        // check package name
        String basePackage = m_textBasePackage.getText();
        if (basePackage.length() == 0) {
            setErrorMessage(null);
            setMessage("Please provide a package name");
            return false;
        }
        if (!PACKAGE_RE.matcher(basePackage).matches()) {
            setErrorMessage("The package name '" + basePackage + "' is invalid");
            return false;
        }

        // everything ok
        setErrorMessage(null);
        setMessage(null);
        return true;

    }

    public String getProjectName() {
        if (m_projectNameField == null) {
            return "";
        }

        return m_projectNameField.getText().trim();
    }

    Listener m_existingProjectChangedListener = new Listener() {
        @Override
        public void handleEvent(final Event e) {
            m_currentJavaProject =
                    JavaModelManager.getJavaModelManager().getJavaModel()
                            .getJavaProject(m_comboExistingProjects.getText());
            boolean valid = validatePage();
            setPageComplete(valid);
        }
    };

    public String getExistingProjectName() {
        return m_comboExistingProjects.getText();
    }

    public boolean addToExistingProject() {
        return m_existingProjectRadio.getSelection();
    }
}
