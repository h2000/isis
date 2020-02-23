package demoapp.webapp.vaadin;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

        final List<? extends ObjectAssociation> propertyList = specification
            .streamAssociations(Contributed.INCLUDED)
            .filter(filter)
            .collect(Collectors.toList());
        final FormLayout formLayout = new FormLayout();
        add(formLayout);
        propertyList.forEach(o -> {
            final ManagedObject prop = o.get(managedObject);
            final ObjectSpecification propSpec = prop.getSpecification();
//            add(new Div(new Text(o.getName()), new Text(":"), new Text(prop.titleString(null))));
            final TextField textField = new TextField();
            textField.setLabel(o.getName());
            textField.setValue(prop.titleString(null));
            formLayout.add(textField);
        });

    }


}
