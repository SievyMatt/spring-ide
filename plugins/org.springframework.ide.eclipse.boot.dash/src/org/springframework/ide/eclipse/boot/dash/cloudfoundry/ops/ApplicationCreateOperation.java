/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.LinkedHashSet;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.LocalRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

public class ApplicationCreateOperation extends CloudApplicationOperation {

	private final CloudApplicationDeploymentProperties deploymentProperties;
	private final RunState preferedRunState;

	/**
	 *
	 * @param deploymentProperties
	 * @param model
	 * @param preferedRunState
	 *            the state that the app should be in when the op is run. For
	 *            example "STARTING". This overrides the actual runstate in CF
	 */
	public ApplicationCreateOperation(CloudApplicationDeploymentProperties deploymentProperties,
			CloudFoundryBootDashModel model, RunState preferedRunState) {
		super("Deploying application: " + deploymentProperties.getAppName(), model, deploymentProperties.getAppName());
		this.deploymentProperties = deploymentProperties;
		this.preferedRunState = preferedRunState;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		String appName = deploymentProperties.getAppName();

		monitor.beginTask("Checking application: " + appName, 10);

		IStatus status = deploymentProperties.validate();
		monitor.worked(5);

		if (!status.isOK()) {
			throw new CoreException(status);
		}

		CloudAppInstances appInstances = createApplication(monitor);

		monitor.worked(5);

		if (appInstances == null) {
			throw BootDashActivator.asCoreException("Failed to create a Cloud application for : "
					+ deploymentProperties.getAppName() + ". Please try to redeploy again or check your connection.");
		}

		IProject project = deploymentProperties.getProject();
		BootDashElement bde = this.model.addElement(appInstances, project);
		if (project != null && bde != null) {
			BootDashElement localElement = findLocalBdeForProject(project);
			if (localElement != null) {
				copyTags(localElement, bde);
			}
		}

		boolean checkTermination = true;
		this.eventHandler.fireEvent(eventFactory.updateRunState(appInstances, getDashElement(), preferedRunState),
				checkTermination);
	}

	private static BootDashElement findLocalBdeForProject(IProject project) {
		BootDashModel localModel = BootDashActivator.getDefault().getModel()
				.getSectionByTargetId(LocalRunTarget.INSTANCE.getId());
		if (localModel != null) {
			for (BootDashElement bde : localModel.getElements().getValue()) {
				if (project.equals(bde.getProject())) {
					return bde;
				}
			}
		}
		return null;
	}

	private static void copyTags(BootDashElement sourceBde, BootDashElement targetBde) {
		LinkedHashSet<String> tagsToCopy = sourceBde.getTags();
		if (tagsToCopy != null && !tagsToCopy.isEmpty()) {
			LinkedHashSet<String> targetTags = targetBde.getTags();
			for (String tag : tagsToCopy) {
				targetTags.add(tag);
			}
			targetBde.setTags(targetTags);
		}
	}

	protected CloudAppInstances createApplication(IProgressMonitor monitor) throws Exception {

		monitor.beginTask("Creating application: " + deploymentProperties.getAppName(), 10);
		try {

			logAndUpdateMonitor("Creating application: " + deploymentProperties.getAppName(), monitor);
			requests.createApplication(deploymentProperties);
			monitor.worked(5);

		} catch (Exception e) {
			// Clean-up: If app creation failed, check if the app was created
			// anyway
			// and delete it to allow users to redeploy
			CloudApplication toCleanUp = requests.getApplication(deploymentProperties.getAppName());
			if (toCleanUp != null) {
				requests.deleteApplication(toCleanUp.getName());
			}
			throw e;
		}
		// Fetch the created Cloud Application
		logAndUpdateMonitor(
				"Verifying that the application was created successfully: " + deploymentProperties.getAppName(),
				monitor);
		CloudAppInstances instances = requests.getExistingApplicationInstances(deploymentProperties.getAppName());
		monitor.worked(5);

		return instances;
	}

}