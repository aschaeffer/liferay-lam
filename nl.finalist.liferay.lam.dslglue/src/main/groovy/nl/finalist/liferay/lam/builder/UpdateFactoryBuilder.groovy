package nl.finalist.liferay.lam.builder;

import groovy.util.FactoryBuilderSupport;
import nl.finalist.liferay.lam.api.PortalSettings
import nl.finalist.liferay.lam.api.Site;
import nl.finalist.liferay.lam.api.Vocabulary;
import nl.finalist.liferay.lam.api.Category;
import nl.finalist.liferay.lam.api.WebContent;
import nl.finalist.liferay.lam.builder.factory.UpdatePortalSettingsFactory
import nl.finalist.liferay.lam.builder.factory.UpdateSiteFactory;
import nl.finalist.liferay.lam.builder.factory.UpdateVocabularyFactory;
import nl.finalist.liferay.lam.builder.factory.UpdateCategoryFactory;
import nl.finalist.liferay.lam.builder.factory.UpdateWebContentFactory;

class UpdateFactoryBuilder extends FactoryBuilderSupport {



    UpdateFactoryBuilder(PortalSettings portalSettingsService, Vocabulary vocabularyService, Site siteService, Category categoryService, WebContent webContentService) {

        registerFactory("portalSettings", new UpdatePortalSettingsFactory(portalSettingsService));
        registerFactory("vocabulary", new UpdateVocabularyFactory(vocabularyService));
        registerFactory("site", new UpdateSiteFactory(siteService));
        registerFactory("category", new UpdateCategoryFactory(categoryService));
         registerFactory("webcontent", new UpdateWebContentFactory(webContentService));
    }
}
