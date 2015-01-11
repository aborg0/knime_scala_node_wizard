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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Version;

/**
 * Helper class to access some target platform related functionality. The API changed in Eclipse 3.8, therefore
 * we need to distinguish between 3.7 and 3.8 and upwards. The {@link #getInstance()} method creates the right
 * implementation depending on the PDE version.
 *
 * @author Thorsten Meinl, KNIME.com, Zurich, Switzerland
 */
abstract class TPHelper {
    private static final Version PDE_CHANGE_VERSION = new Version(3, 8, 0);

    /**
     * Sets up the KNIME target platform that is bundled in this plug-in.
     *
     * @param monitor a progress monitor
     *
     * @throws URISyntaxException if the bundled TP file cannot be resolved
     * @throws CoreException if a core problem occurs
     * @throws IOException if an I/O error occurs
     */
    public abstract void setupTargetPlatform(final SubMonitor monitor) throws URISyntaxException, CoreException,
        IOException;

    /**
     * Returns whether the currently active target platform is the "Running Platform".
     *
     * @return <code>true</code> if it is the running platform, <code>false</code> if it's something else
     */
    public abstract boolean currentTPIsRunningPlatform();

    public static TPHelper getInstance() {
        Version version = Platform.getBundle("org.eclipse.pde.core").getVersion();
		if (version.compareTo(PDE_CHANGE_VERSION) >= 0) {
            return new TPHelperImpl38();
        } else {
        	throw new UnsupportedOperationException("org.eclipse.pde.core: " + version);
            //return new TPHelperImpl37();
        }
    }
}
