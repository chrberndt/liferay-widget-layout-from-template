package com.chberndt.liferay.installer;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.util.PropsValues;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Berndt
 */
@Component(immediate = true, service = SimpleLayoutInstaller.class)
public class SimpleLayoutInstaller {

	// Based on the model of com.liferay.layout.admin.web.internal.portlet.action.AddSimpleLayoutMVCActionCommand

	protected void createLayout(ServiceContext serviceContext)
		throws Exception {

		_log.info("createLayout()");

		// TODO: Retrieve the intended scope

		long groupId = 37344;

		// TODO: Decide whether the created layout should be public or private

		boolean privateLayout = false;

		// TODO: Define the page's parent layout

		long parentLayoutId = 0;
		Map<Locale, String> nameMap = HashMapBuilder.put(
			LocaleUtil.getDefault(), "Widget Page From Blog Template"
		).build();

		String type = LayoutConstants.TYPE_PORTLET;

		Layout layout = _layoutLocalService.addLayout(
			serviceContext.getUserId(), groupId, privateLayout, parentLayoutId,
			nameMap, new HashMap<>(), new HashMap<>(), new HashMap<>(),
			new HashMap<>(), type, null, false, new HashMap<>(),
			serviceContext);

		if (!Objects.equals(type, LayoutConstants.TYPE_CONTENT)) {
			LayoutTypePortlet layoutTypePortlet =
				(LayoutTypePortlet)layout.getLayoutType();

			layoutTypePortlet.setLayoutTemplateId(
				serviceContext.getUserId(),
				PropsValues.DEFAULT_LAYOUT_TEMPLATE_ID);

			_layoutLocalService.updateLayout(
				groupId, privateLayout, layout.getLayoutId(),
				layout.getTypeSettings());
		}

		Layout draftLayout = layout.fetchDraftLayout();

		if (draftLayout != null) {
			_layoutLocalService.updateLayout(
				groupId, privateLayout, layout.getLayoutId(),
				draftLayout.getModifiedDate());
		}
	}

	protected ServiceContext getServiceContext() throws PortalException {

		// This strategy is limited to single instance configurations

		User user = _userLocalService.getDefaultUser(
			_portal.getDefaultCompanyId());

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setUserId(user.getUserId());

		// TODO: Retrieve the template's uuid

		serviceContext.setAttribute(
			"layoutPrototypeUuid", "cddd9639-1ca7-4344-d69b-c954a2ebacc0");

		return serviceContext;
	}

	@Activate
	private void _activate() throws Exception {
		_log.info("activate()");
		createLayout(getServiceContext());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SimpleLayoutInstaller.class);

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}