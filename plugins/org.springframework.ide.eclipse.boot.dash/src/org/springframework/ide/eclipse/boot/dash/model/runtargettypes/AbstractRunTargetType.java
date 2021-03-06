/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.pstore.IPropertyStore;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.pstore.PropertyStores;

/**
 * @author Kris De Volder
 */
public abstract class AbstractRunTargetType<Params> implements RunTargetType<Params> {

	private static final String NAME_TEMPLATE = "NAME_TEMPLATE";

	private String name;
	private IPropertyStore propertyStore;

	public AbstractRunTargetType(BootDashModelContext context, String name) {
		this.name = name;
		//TODO: there shouldn't be any exceptions to allow for target types that don't provide a context.
		// However this requires a bunch of refactoring to get rid of the global constants related to the
		// 'LOCAL' runtarget and type.
		if (context!=null) {
			this.propertyStore = PropertyStores.createSubStore(name, context.getViewProperties());
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "RunTargetType("+getName()+")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractRunTargetType other = (AbstractRunTargetType) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public IPropertyStore getPropertyStore() {
		return propertyStore;
	}

	@Override
	public PropertyStoreApi getPersistentProperties() {
		IPropertyStore store = getPropertyStore();
		if (store!=null) {
			return new PropertyStoreApi(store);
		}
		return null;
	}

	@Override
	public String getDefaultNameTemplate() {
		return null;
	}

	@Override
	public void setNameTemplate(String template) throws Exception {
		getPersistentProperties().put(NAME_TEMPLATE, template);
	}

	@Override
	public String getNameTemplate() {
		PropertyStoreApi props = getPersistentProperties();
		if (props!=null) {
			String customTemplate = props.get(NAME_TEMPLATE);
			if (customTemplate!=null) {
				return customTemplate;
			}
		}
		return getDefaultNameTemplate();
	}

	@Override
	public String getTemplateHelpText() {
		return null;
	}
}
