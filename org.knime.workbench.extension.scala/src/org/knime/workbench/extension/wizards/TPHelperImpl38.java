/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * ---------------------------------------------------------------------
 *
 * History
 *   19.11.2014 (thor): created
 */
package org.knime.workbench.extension.wizards;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Implementation of {@link TPHelper} for Eclipse >= 3.8.
 *
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 */
class TPHelperImpl38 extends TPHelper {
    @Override
    public void setupTargetPlatform(final SubMonitor monitor) throws URISyntaxException, CoreException, IOException {
        monitor.subTask("Setting up target platform");
        Bundle myself = FrameworkUtil.getBundle(getClass());
        BundleContext bundleContext = myself.getBundleContext();

        ServiceReference<ITargetPlatformService> serviceRef =
            bundleContext.getServiceReference(ITargetPlatformService.class);
        monitor.worked(2);
        try {
            ITargetPlatformService tpService = bundleContext.getService(serviceRef);
            // load KNIME TP definition from bundle
            ITargetHandle knimeTPHandle =
                tpService.getTarget(FileLocator.toFileURL(myself.getEntry("KNIME.target")).toURI());
            ITargetDefinition knimeTP = knimeTPHandle.getTargetDefinition();

            ITargetHandle defaultTpHandle = tpService.getWorkspaceTargetHandle();
            if (defaultTpHandle != null) {
                ITargetDefinition defaultTp = defaultTpHandle.getTargetDefinition();

                // check if we are already using the KNIME TP and return in this case
                if ((defaultTp.getName() != null) && defaultTp.getName().equals(knimeTP.getName())) {
                    return;
                }
            }

            // check if the KNIME TP is already present but not activated
            ITargetDefinition newTp = null;
            for (ITargetHandle th : tpService.getTargets(monitor.newChild(1))) {
                ITargetDefinition td = th.getTargetDefinition();
                if ((th != knimeTPHandle) && (td.getName() != null) && td.getName().equals(knimeTP.getName())) {
                    newTp = td;
                    break;
                }
            }

            if (newTp == null) {
                newTp = tpService.newTarget();
                tpService.copyTargetDefinition(knimeTP, newTp);
                tpService.saveTargetDefinition(newTp);
            }
            tpService.deleteTarget(knimeTPHandle);

            monitor.subTask("Resolving target platform");
            IStatus status = newTp.resolve(monitor.newChild(25));
            if (status.getSeverity() == IStatus.ERROR) {
                throw new CoreException(status);
            }

            LoadTargetDefinitionJob job = new LoadTargetDefinitionJob(newTp);
            monitor.subTask("Setting default target platform");
            status = job.runInWorkspace(monitor.newChild(2));
            if (status.getSeverity() == IStatus.ERROR) {
                throw new CoreException(status);
            }
        } finally {
            bundleContext.ungetService(serviceRef);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean currentTPIsRunningPlatform() {
        Bundle myself = FrameworkUtil.getBundle(getClass());
        BundleContext bundleContext = myself.getBundleContext();

        ServiceReference<ITargetPlatformService> serviceRef =
            bundleContext.getServiceReference(ITargetPlatformService.class);
        try {
            ITargetPlatformService tpService = bundleContext.getService(serviceRef);
            ITargetHandle defaultTpHandle = tpService.getWorkspaceTargetHandle();
            if (defaultTpHandle == null) {
                return true;
            }

            ITargetDefinition defaultTp = defaultTpHandle.getTargetDefinition();
            return "Running Platform".equals(defaultTp.getName());
        } catch (CoreException ex) {
            Platform.getLog(myself).log(
                new Status(IStatus.ERROR, myself.getSymbolicName(), "Could not check current target platform: "
                    + ex.getMessage(), ex));
            return false;
        } finally {
            bundleContext.ungetService(serviceRef);
        }
    }
}
