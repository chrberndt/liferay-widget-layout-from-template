package com.chberndt.liferay.installer;

import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutPrototype;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.LayoutPrototypeLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.sites.kernel.util.SitesUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Berndt
 */
@Component(immediate = true, service = SimpleLayoutInstaller.class)
public class SimpleLayoutInstaller {

	// Based on the model of
	// com.liferay.layout.admin.web.internal.portlet.action.AddContentLayoutMVCActionCommand

	protected void createLayout(ServiceContext serviceContext)
		throws Exception {

		_log.info("createLayout()");

		// TODO: Retrieve the target scope

		long groupId = 20123;

		// TODO: Decide whether the created layout should be public or private

		boolean privateLayout = false;

		// TODO: Define the page's parent layout

		long parentLayoutId = 0;

		Map<Locale, String> nameMap = new HashMap<>();

		nameMap.put(
			LocaleUtil.getSiteDefault(), "Widget Page From Blogs Template");

		// TODO: Retrieve the template's layoutPageTemplateEntryId

		long layoutPageTemplateEntryId = 34638;

		Layout layout = null;

		try {
			LayoutPageTemplateEntry layoutPageTemplateEntry =
				_layoutPageTemplateEntryLocalService.
					fetchLayoutPageTemplateEntry(layoutPageTemplateEntryId);

			if ((layoutPageTemplateEntry != null) &&
				(layoutPageTemplateEntry.getLayoutPrototypeId() > 0)) {

				LayoutPrototype layoutPrototype =
					_layoutPrototypeLocalService.getLayoutPrototype(
						layoutPageTemplateEntry.getLayoutPrototypeId());

				serviceContext.setAttribute(
					"layoutPrototypeUuid", layoutPrototype.getUuid());

				layout = _layoutLocalService.addLayout(
					serviceContext.getUserId(), groupId, privateLayout,
					parentLayoutId, nameMap, new HashMap<>(), new HashMap<>(),
					new HashMap<>(), new HashMap<>(),
					LayoutConstants.TYPE_PORTLET, null, false, new HashMap<>(),
					serviceContext);

				// Force propagation from page template to page. See LPS-48430.

				SitesUtil.mergeLayoutPrototypeLayout(layout.getGroup(), layout);
			}
			else {
				_layoutLocalService.addLayout(
					serviceContext.getUserId(), groupId, privateLayout,
					parentLayoutId,
					_portal.getClassNameId(LayoutPageTemplateEntry.class),
					layoutPageTemplateEntryId, nameMap, new HashMap<>(),
					new HashMap<>(), new HashMap<>(), new HashMap<>(),
					LayoutConstants.TYPE_CONTENT, null, false, false,
					new HashMap<>(), serviceContext);
			}
		}
		catch (PortalException pe) {
			if (_log.isErrorEnabled()) {
				_log.error(pe, pe);
			}
		}
	}

	protected ServiceContext getServiceContext() throws PortalException {

		// This strategy is limited to single instance configurations

		User user = _userLocalService.getDefaultUser(
			_portal.getDefaultCompanyId());

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setUserId(user.getUserId());

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
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference
	private LayoutPrototypeLocalService _layoutPrototypeLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}