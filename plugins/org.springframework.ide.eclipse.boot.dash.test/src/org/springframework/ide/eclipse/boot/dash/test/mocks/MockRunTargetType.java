/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.cf.runtarget.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

import com.google.gson.Gson;

public class MockRunTargetType extends AbstractRunTargetType<CloudFoundryTargetProperties> {

	private boolean requiresCredentials;
	private BootDashModelContext context;

	public MockRunTargetType(BootDashModelContext context, String name) {
		super(context, name);
		this.context = context;
	}

	@Override
	public boolean canInstantiate() {
		return true;
	}

	@Override
	public void openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
	}

	@Override
	public RunTarget createRunTarget(CloudFoundryTargetProperties properties) {
		return new MockRunTarget(this, properties, requiresCredentials);
	}

	@Override
	public ImageDescriptor getIcon() {
		return null;
	}

	public void setRequiresCredentials(boolean requires) {
		this.requiresCredentials = requires;
	}

	@Override
	public CloudFoundryTargetProperties parseParams(String serializedTargetParams) {
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		Map<String,String> map = gson.fromJson(serializedTargetParams, Map.class);
		return new CloudFoundryTargetProperties(new TargetProperties(map, this), this, context);
	}

	@Override
	public String serialize(CloudFoundryTargetProperties props) {
		return props.toJson();
	}
}
