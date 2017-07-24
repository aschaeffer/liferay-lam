package nl.finalist.liferay.lam.builder;

import groovy.util.FactoryBuilderSupport;
import nl.finalist.liferay.lam.api.CustomFields;
import nl.finalist.liferay.lam.api.Vocabulary;
import nl.finalist.liferay.lam.api.Category;
import nl.finalist.liferay.lam.builder.factory.CreateVocabularyFactory;
import nl.finalist.liferay.lam.builder.factory.CreateCustomFieldsFactory;
import nl.finalist.liferay.lam.builder.factory.CreateCategoryFactory;

class CreateFactoryBuilder extends FactoryBuilderSupport {

    CreateFactoryBuilder(CustomFields customFieldsService, Vocabulary vocabularyService, Category categoryService) {
        registerFactory("customField", new CreateCustomFieldsFactory(customFieldsService));
        registerFactory("vocabulary", new CreateVocabularyFactory(vocabularyService));
        registerFactory("category", new CreateCategoryFactory(categoryService));
    }
}
