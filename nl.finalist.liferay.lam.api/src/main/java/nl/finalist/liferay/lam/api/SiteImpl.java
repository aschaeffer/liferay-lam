package nl.finalist.liferay.lam.api;

import com.liferay.exportimport.kernel.service.StagingLocalService;
import com.liferay.portal.kernel.exception.DuplicateGroupException;
import com.liferay.portal.kernel.exception.GroupFriendlyURLException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import nl.finalist.liferay.lam.api.model.PageModel;

@Component(immediate = true, service = Site.class)
public class SiteImpl implements Site {

	@Reference
	private GroupLocalService groupService;
    @Reference
    private StagingLocalService stagingLocalService;

	@Reference
	private CustomFields customFieldsService;
	@Reference
	private Page pageService;
    @Reference
    private DefaultValue defaultValue;
	private static final Log LOG = LogFactoryUtil.getLog(SiteImpl.class);

	@Override
	public void addSite(Map<Locale, String> nameMap, Map<Locale, String> descriptionMap, String friendlyURL,
			Map<String, String> customFields, List<PageModel> pages) {
		try {
			Group group = groupService.addGroup(defaultValue.getDefaultUserId(), GroupConstants.DEFAULT_PARENT_GROUP_ID,
					Group.class.getName(), 0L, GroupConstants.DEFAULT_LIVE_GROUP_ID, nameMap, descriptionMap,
					GroupConstants.TYPE_SITE_OPEN, true, GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION, friendlyURL,
					true, false, true, null);
			LOG.info(String.format("Site %s was added", nameMap.get(LocaleUtil.getSiteDefault())));

			if (customFields != null) {
				for (String fieldName : customFields.keySet()) {
					customFieldsService.addCustomFieldValue(Group.class.getName(), fieldName, group.getPrimaryKey(),
							customFields.get(fieldName));
					LOG.info(String.format("Custom field '%s' now has value '%s'", fieldName, customFields.get(fieldName)));
				}
			}

		
			if (pages != null) {
				
				LOG.debug(String.format("Adding pages defaultuser : %d , group : %d", defaultValue.getDefaultUserId(), group.getGroupId()));

				for (PageModel page : pages) {
					LOG.debug("Add page : " + page.getNameMap().get(LocaleUtil.getSiteDefault().toString()));
					pageService.addPage(group.getGroupKey(), page);
				}
			}
		} catch (DuplicateGroupException | GroupFriendlyURLException e1) {
			LOG.warn(String.format("The site %s already exists.", nameMap.get(LocaleUtil.getSiteDefault())));
		} catch (PortalException e) {
			LOG.error(String.format("While adding group for site '%s'", friendlyURL), e);
		}
	}

	@Override
	public void updateSite(String groupKey, Map<Locale, String> nameMap, Map<Locale, String> descriptionMap,
			String friendlyURL, Map<String, String> customFields, List<PageModel> pages, Boolean stagingEnabled) {
		Group group;
		try {
			group = groupService.getGroup(PortalUtil.getDefaultCompanyId(), groupKey);
			if (nameMap == null || nameMap.isEmpty()) {
				nameMap = group.getNameMap();
			}
			if (descriptionMap == null || descriptionMap.isEmpty()) {
				descriptionMap = group.getDescriptionMap();
			}
			if (friendlyURL == null || friendlyURL.isEmpty()) {
				friendlyURL = group.getFriendlyURL();
			}

			groupService.updateGroup(group.getGroupId(), GroupConstants.DEFAULT_PARENT_GROUP_ID, nameMap,
					descriptionMap, GroupConstants.TYPE_SITE_OPEN, true, GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION,
					friendlyURL, false, true, null);
			LOG.info(String.format("Group (aka site) %s was updated", group.getGroupKey()));

			if (customFields != null) {
				for (String fieldName : customFields.keySet()) {
					customFieldsService.updateCustomFieldValue(Group.class.getName(), fieldName, group.getPrimaryKey(),
							customFields.get(fieldName));
					LOG.debug(
							String.format("Custom field %s now has value %s", fieldName, customFields.get(fieldName)));
				}
			}
			for (PageModel page : pages) {
				Set<String> locales = page.getFriendlyUrlMap().keySet();
				// TODO: I don't think this for-loop is correct. It now adds pages for each locale, whereas it should just add a page for the default locale
				for (String locale : locales) {
					Layout existingPage = pageService.fetchLayout(group.getGroupId(), false, page.getFriendlyUrlMap().get(locale));
					if (existingPage != null) {
						pageService.updatePage(existingPage, group.getGroupId(), page);
						LOG.info(String.format("page %s is updated ", page.getNameMap().get(locale)));
						break;
					} else {
						pageService.addPage(groupKey, page);
						LOG.info(String.format("page doesn't exists so it has been added: %s", page.getNameMap().get(locale)));
					}
				}
			}
			
            if (group != null && stagingEnabled != null) {
                long groupId = group.getGroupId();
                if (stagingEnabled) {
                    if (!group.hasStagingGroup()) {
                        stagingLocalService.enableLocalStaging(defaultValue.getDefaultUserId(), group, false, false, new ServiceContext());
                        LOG.info(String.format("Staging was enabled for group %s", groupId));
                    } else {
                        LOG.info(String.format("No changes propagated; staging was already enabled for group %s", groupId));
                    }
                } else {
                    if (group.hasStagingGroup()) {
                        stagingLocalService.disableStaging(group, new ServiceContext());
                        LOG.info(String.format("Staging was disabled for group %s", groupId));
                    } else {
                        LOG.info(String.format("No changes propagated; staging was already disabled for group %s", groupId));
                    }
                }
            }
		} catch (PortalException e) {
			LOG.error("The group was not updated.", e);
		}
	}

	@Override
	public void deleteSite(String groupKey) {
		Group group;
		try {
			group = groupService.getGroup(PortalUtil.getDefaultCompanyId(), groupKey);
			groupService.deleteGroup(group.getGroupId());
			LOG.info(String.format("Group %s was deleted", groupKey));
		} catch (PortalException e) {
			LOG.error("The group was not deleted.");
		}
	}
}
