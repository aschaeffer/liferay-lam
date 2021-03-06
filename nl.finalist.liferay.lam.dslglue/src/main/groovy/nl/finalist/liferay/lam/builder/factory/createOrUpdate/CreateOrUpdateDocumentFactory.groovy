package nl.finalist.liferay.lam.builder.factory.createOrUpdate
import nl.finalist.liferay.lam.api.Document;
import nl.finalist.liferay.lam.util.LocaleMapConverter;
import nl.finalist.liferay.lam.dslglue.model.DocumentModel;

import org.osgi.framework.Bundle;


class CreateOrUpdateDocumentFactory extends AbstractFactory {

    Document DocumentService;
    Bundle bundle;

    CreateOrUpdateDocumentFactory(Document DocumentService, Bundle bundle) {
        this.DocumentService = DocumentService;
        this.bundle = bundle;
    }

    @Override
    Object newInstance(FactoryBuilderSupport builder, Object objectName, Object value, Map attributes)
                    throws InstantiationException, IllegalAccessException {
        new DocumentModel(attributes);
    }

    @Override
    void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        super.onNodeCompleted(builder, parent, node);
        DocumentModel model = (DocumentModel) node;
        DocumentService.createOrUpdateDocument(model.forSite, model.title, model.file, bundle);
    }
}