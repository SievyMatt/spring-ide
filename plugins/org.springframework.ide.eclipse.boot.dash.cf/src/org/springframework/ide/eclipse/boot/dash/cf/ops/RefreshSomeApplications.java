/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.ops;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Operation for refreshing existing cloud applications.
 *
 * @author Alex Boyko
 *
 */
public class RefreshSomeApplications extends CloudOperation {

	private Collection<CFApplication> apps;

	public RefreshSomeApplications(CloudFoundryBootDashModel model, Collection<CFApplication> appsToRefresh) {
		super("Refreshing applications", model);
		this.apps = appsToRefresh;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		if (apps != null && !apps.isEmpty()) {
			this.model.setBaseRefreshState(RefreshState.loading("Fetching App Instances..."));
			try {
				ClientRequests client = model.getRunTarget().getClient();
				for (CloudAppDashElement app : model.getApplicationValues()) {
					String appName = app.getName();
					CFApplicationDetail newDetails = client.getApplication(appName);
					app.setDetailedData(newDetails);
				}
				model.setBaseRefreshState(RefreshState.READY);
			} catch (Throwable e) {
				model.setBaseRefreshState(RefreshState.error(e));
				throw ExceptionUtil.exception(e);
			}
		}
	}

}
