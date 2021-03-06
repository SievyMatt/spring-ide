/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.livexp.ui.DynamicSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.FilteredDependenciesSection;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springframework.ide.eclipse.boot.wizard.MakeDefaultSection;
import org.springframework.ide.eclipse.boot.wizard.NewSpringBootWizard;
import org.springframework.ide.eclipse.boot.wizard.SearchBoxSection;
import org.springframework.ide.eclipse.boot.wizard.SelectedDependenciesSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.GroupSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageWithSections;

import com.google.common.collect.ImmutableList;

public class DependencyPage extends WizardPageWithSections {

	private static final int NUM_COLUMNS_FREQUENTLY_USED = 3;
	private static final int MAX_MOST_POPULAR = 3 * NUM_COLUMNS_FREQUENTLY_USED;
	private static final Point DEPENDENCY_SECTION_SIZE = new Point(SWT.DEFAULT, 300);

	private CheckBoxesSection<Dependency> frequentlyUsedCheckboxes;

	protected final InitializrFactoryModel<AddStartersModel> factoryModel;

	public DependencyPage(InitializrFactoryModel<AddStartersModel> factoryModel) {
		super("page2", "New Spring Starter Project Dependencies", null);
		this.factoryModel = factoryModel;
	}

	private void refreshFrequentlyUsedDependencies(AddStartersModel model) {
		List<CheckBoxModel<Dependency>> dependenciesCheckboxes = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		if (frequentlyUsedCheckboxes.isCreated()) {
			frequentlyUsedCheckboxes.setModel(dependenciesCheckboxes);
		}
		reflow();
	}

	@Override
	protected List<WizardPageSection> createSections() {
		LiveExpression<AddStartersModel> model = factoryModel.getModel();
		DynamicSection dynamicSection = new DynamicSection(this, model.apply((dynamicModel) -> {
			if (dynamicModel != null) {
				return createDynamicSections(dynamicModel);
			}
			return new CommentSection(this, NewSpringBootWizard.NO_CONTENT_AVAILABLE);
		} ));


		return ImmutableList.of(dynamicSection);
	}

	protected WizardPageSection createDynamicSections(AddStartersModel model) {

		model.onDependencyChange(() -> {
			Display.getDefault().asyncExec(() -> {
				IWizard wizard = DependencyPage.this.getWizard();
				if (wizard != null) {
					IWizardContainer container = wizard.getContainer();
					if (container != null) {
						container.updateButtons();
					}
				}
			});
		});

		List<WizardPageSection> sections = new ArrayList<>();

		sections.add(createFrequentlyUsedSection(model));
		sections.add(createTwoColumnSection(model));
		return new GroupSection(this, null, sections.toArray(new WizardPageSection[0])).grabVertical(true);
	}

	public WizardPageSection createTwoColumnSection(final AddStartersModel model) {
		return new GroupSection(this,null,
				new GroupSection(this, null,
						new CommentSection(this, "Available:"),
						getSearchSection(model),
						new GroupSection(this, "",
								new FilteredDependenciesSection(this, model.dependencies, model.searchBox.getFilter())
								.sizeHint(DEPENDENCY_SECTION_SIZE)
							)
							.grabVertical(true)
							.noMargins(true)
						)
						.grabVertical(true)
						.noMargins(true),
				new GroupSection(this, null,
						new CommentSection(this, "Selected:"),
						new GroupSection(this, "",
								new SelectedDependenciesSection(this, model.dependencies)
								.sizeHint(DEPENDENCY_SECTION_SIZE)
							)
							.grabVertical(true)
							.noMargins(true),
						new MakeDefaultSection(this, () -> {
							if (model.saveDefaultDependencies()) {
								refreshFrequentlyUsedDependencies(model);
							}
						}, () -> {
							model.dependencies.clearSelection();
						})
					)
				)
				.columns(2, true)
				.grabVertical(true);
	}

	protected WizardPageSection getSearchSection(final AddStartersModel model) {
		final SearchBoxSection searchBoxSection = new SearchBoxSection(this, model.searchBox.getText()) {
			@Override
			protected String getSearchHint() {
				return "Type to search dependencies";
			}
		};
//		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> getControl().addListener(SWT.Show, event -> searchBoxSection.focusControl()));
		return searchBoxSection;
	}

	protected WizardPageSection createFrequentlyUsedSection(AddStartersModel model) {
		List<CheckBoxModel<Dependency>> frequentDependencies = model.getFrequentlyUsedDependencies(MAX_MOST_POPULAR);
		frequentlyUsedCheckboxes = new CheckBoxesSection<>(this, frequentDependencies).columns(NUM_COLUMNS_FREQUENTLY_USED);
		GroupSection frequentlyUsedSection = new GroupSection(this,
				null,
				new CommentSection(this, "Frequently Used:"),
				new GroupSection(this, "", frequentlyUsedCheckboxes));
		frequentlyUsedSection.isVisible.setValue(!frequentDependencies.isEmpty());
		return frequentlyUsedSection;
	}

	@Override
	public boolean isPageComplete() {
		// We cannot complete from  the dependency page as
		// a user has to go to the  next page to manually accept changes
		// into their existing project
		return false;
	}

	@Override
	public boolean canFlipToNextPage() {
		LiveExpression<AddStartersModel> model = factoryModel.getModel();
		return model != null && model.getValue() != null && model.getValue().canShowDiff();
	}

}
