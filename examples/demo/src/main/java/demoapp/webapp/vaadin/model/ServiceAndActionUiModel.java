package demoapp.webapp.vaadin.model;

import java.util.Optional;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction.Util;

import lombok.Data;

@Data
public class ServiceAndActionUiModel {

    final EntityUiModel entityUiModel;
    final String serviceName;
    // TODO final ServiceActionLinkFactory linkAndLabelFactory;
    // TODO final EntityModel serviceEntityModel;
    final ObjectAction objectAction;
    final boolean isFirstSection;

    Optional<String> cssClassFa() {
        return Optional.ofNullable(Util.cssClassFaFor(objectAction));
    }

    Optional<String> cssClass(final ManagedObject managedObject) {
        return Optional.ofNullable(Util.cssClassFor(objectAction, managedObject));
    }
}
