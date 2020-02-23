package demoapp.webapp.vaadin;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public class ObjectFormView extends VerticalLayout {

    public ObjectFormView(final ManagedObject managedObject) {
        final ObjectSpecification specification = managedObject.getSpecification();
        final String title = specification.getTitle(null, managedObject);
        add(new H1(title));

        final Predicate<ObjectAssociation> filter = ObjectMember::isPropertyOrCollection;

        final List<? extends ObjectAssociation> objectAssociations = specification
                .streamAssociations(Contributed.INCLUDED)
                .filter(filter)
                .collect(Collectors.toList());
        final FormLayout formLayout = new FormLayout();
        add(formLayout);
        objectAssociations.forEach(objectAssociation -> {
            final ManagedObject assocObject = objectAssociation.get(managedObject);
            final ObjectSpecification propSpec = assocObject.getSpecification();
            switch (propSpec.getBeanSort()) {
            case VALUE: {
                formLayout.add(createValueField(objectAssociation, assocObject));
                break;
            }
            case COLLECTION: {
                formLayout.add(createCollectionComponent(objectAssociation, assocObject));
                break;
            }
            case VIEW_MODEL:
            case ENTITY:
            case MANAGED_BEAN_CONTRIBUTING:
            case MANAGED_BEAN_NOT_CONTRIBUTING:
            case MIXIN:
            case UNKNOWN:
            default: {
                final String value = propSpec.toString();
                final TextField textField = new TextField(value);
                textField.setLabel(
                        "Unhandled kind assoc.: " + propSpec.getBeanSort() + " " + objectAssociation.getName());
                textField.setValue(propSpec.toString());
                textField.setInvalid(true);
                formLayout.add(textField);
                break;
            }
            }
        });

    }

    private Component createCollectionComponent(
            final ObjectAssociation objectAssociation,
            final ManagedObject assocObject) {
        final String label = "Collection:" + objectAssociation.getName();
        final Object pojo = assocObject.getPojo();
        if (pojo instanceof Collection) {
            final ComboBox<ManagedObject> listBox = new ComboBox<>();
            listBox.setLabel(label);
            final CollectionFacet facet = objectAssociation.getFacet(CollectionFacet.class);
            final List<ManagedObject> objects = facet.stream(assocObject).collect(Collectors.toList());
            listBox.setItems(objects);
            listBox.setItemLabelGenerator(o -> assocObject.getSpecification().getTitle(null, o));
            return listBox;
        } else if (pojo == null) {
            final TextField textField = new TextField();
            textField.setLabel(label);

            textField.setValue("<NULL>");
            return textField;
        } else {
            final TextField textField = new TextField();
            textField.setLabel(label);

            textField.setValue("Unknown collection type:" + pojo.getClass());
            return textField;
        }
    }

    private Component createValueField(final ObjectAssociation association, final ManagedObject assocObject) {
        // TODO how to handle object type / id
        return createTextField(association, assocObject);
    }

    private TextField createTextField(final ObjectAssociation association, final ManagedObject assocObject) {
        final TextField textField = new TextField();
        textField.setLabel(association.getName());
        textField.setValue(assocObject.titleString(null));
        return textField;
    }
}
