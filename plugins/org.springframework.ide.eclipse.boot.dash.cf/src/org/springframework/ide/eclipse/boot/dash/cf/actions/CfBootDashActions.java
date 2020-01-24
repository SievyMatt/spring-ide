package org.springframework.ide.eclipse.boot.dash.cf.actions;

import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.liveprocess.LiveProcessCommandsExecutor;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.views.AbstractBootDashAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.collect.ImmutableList;

public class CfBootDashActions {

	public static BootDashActions.Factory factory = (
			BootDashViewModel model,
			MultiSelection<BootDashElement> selection,
			LiveExpression<BootDashModel> section,
			SimpleDIContext context,
			LiveProcessCommandsExecutor liveProcessCmds
	) -> {
		ImmutableList.Builder<AbstractBootDashAction> builder = ImmutableList.builder();
		if (section!=null) {
			builder.add(new UpdatePasswordAction(section, context));
			builder.add(new OpenCloudAdminConsoleAction(section, context));
			builder.add(new ToggleBootDashModelConnection(section, context));
			builder.add(new CustmomizeTargetAppManagerURLAction(section, context));
		}
		return builder.build();
	};


}
