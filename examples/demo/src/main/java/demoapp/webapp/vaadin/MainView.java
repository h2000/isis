package demoapp.webapp.vaadin;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import demoapp.webapp.vaadin.model.EntityUiModel;
import demoapp.webapp.vaadin.model.MenuSectionUiModel;
import demoapp.webapp.vaadin.model.ServiceAndActionUiModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.val;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.menubars.MenuSection;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.services.menu.MenuBarsService.Type;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@Route()
public class MainView extends VerticalLayout {

    private final Logger log = LogManager.getLogger(MainView.class);

    public MainView(
        @Autowired final IsisSessionFactory isisSessionFactory,
        @Autowired final SpecificationLoader specificationLoader,
        @Autowired final MetaModelContext metaModelContext,
        @Autowired final IsisConfiguration isisConfiguration
    ) {
        final IsisWebAppCommonContext isisWebAppCommonContext = IsisWebAppCommonContext.of(metaModelContext);

        final MenuBarsServiceBS3 menuBarsService = metaModelContext.getServiceRegistry()
            .lookupServiceElseFail(MenuBarsServiceBS3.class);
        final BS3MenuBars bs3MenuBars = menuBarsService.menuBars(Type.DEFAULT);

        final MenuBar menuBar = new MenuBar();
        final Text selected = new Text("");
        final Div details = new Div();
        final Div message = new Div(new Text("Selected: "), selected, details);

        add(menuBar);
        add(message);

        final List<MenuSectionUiModel> menuSectionUiModels = buildMenuModel(log, isisWebAppCommonContext, bs3MenuBars);
        log.warn("menu model:\n ");
        menuSectionUiModels.forEach(m -> log.warn("\t{}", m));

        menuSectionUiModels.forEach(sectionUiModel -> {
                final MenuItem menuItem = menuBar.addItem(sectionUiModel.getName());
                final SubMenu subMenu = menuItem.getSubMenu();
                sectionUiModel.getServiceAndActionUiModels().forEach(a -> {
                    final ObjectAction objectAction = a.getObjectAction();
                    subMenu.addItem(objectAction.getName(),
                        e -> {
                            details.removeAll();
                            final VerticalLayout verticalLayout = new VerticalLayout();
                            details.add(verticalLayout);

                            selected.setText(objectAction.toString());
                            objectAction.getParameters();
                            verticalLayout.add(new Div(new Text("Name: " + objectAction.getName())));
                            verticalLayout.add(new Div(new Text("Description: " + objectAction.getDescription())));
                            verticalLayout.add(new Div(new Text("Parameters: " + objectAction.getParameters())));
                            final Div actionResult = new Div();


                            if (objectAction.isAction() && objectAction.getParameters().isEmpty()) {
                                verticalLayout.add(new Button("run", buttonClickEvent -> {
                                    final ManagedObject sectionObject = a.getEntityUiModel().getManagedObject();
                                    final ManagedObject result = objectAction
                                        .execute(
                                            sectionObject,
                                            null,
                                            Collections.emptyList(),
                                            InteractionInitiatedBy.USER
                                        );
                                    actionResult.removeAll();
                                    actionResult.add(new ObjectFormView(result));
                                }));
                                verticalLayout.add(actionResult);
                            }
                        }
                    );
                    objectAction.getSemantics();
                });
            }
        );
    }

    // copied from org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions.ServiceActionUtil.buildMenu
    public static List<MenuSectionUiModel> buildMenuModel(
        final Logger log,
        final IsisWebAppCommonContext commonContext,
        final BS3MenuBars menuBars
    ) {

        // TODO handle menuBars.getSecondary(), menuBars.getTertiary()
        final BS3MenuBar menuBar = menuBars.getPrimary();

        // we no longer use ServiceActionsModel#getObject() because the model only holds the services for the
        // menuBar in question, whereas the "Other" menu may reference a service which is defined for some other menubar

        final List<MenuSectionUiModel> menuSections = new ArrayList<>();
        for (final BS3Menu menu : menuBar.getMenus()) {

            final MenuSectionUiModel menuSectionUiModel = new MenuSectionUiModel(menu.getNamed());

            for (final MenuSection menuSection : menu.getSections()) {

                boolean isFirstSection = true;

                for (final ServiceActionLayoutData actionLayoutData : menuSection.getServiceActions()) {
                    val serviceSpecId = actionLayoutData.getObjectType();

                    final ManagedObject serviceAdapter = commonContext.lookupServiceAdapterById(serviceSpecId);
                    if (serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync
                        // with actual configured modules
                        continue;
                    }
                    // TODO Wicket final EntityModel entityModel = EntityModel.ofAdapter(commonContext, serviceAdapter);
                    final EntityUiModel entityUiModel =
                        new EntityUiModel(commonContext, serviceAdapter);

                    final ObjectAction objectAction =
                        serviceAdapter
                            .getSpecification()
                            .getObjectAction(actionLayoutData.getId())
                            .orElse(null);
                    if (objectAction == null) {
                        log.warn("No such action {}", actionLayoutData.getId());
                        continue;
                    }
                    final ServiceAndActionUiModel serviceAndActionUiModel =
                        new ServiceAndActionUiModel(
                            entityUiModel,
                            actionLayoutData.getNamed(),
                            objectAction,
                            isFirstSection);

                    menuSectionUiModel.addAction(serviceAndActionUiModel);
                    isFirstSection = false;

                    // TODO Wicket
                    //                    final CssMenuItem.Builder subMenuItemBuilder = menuSectionModel.newSubMenuItem(serviceAndAction);
                    //                    if (subMenuItemBuilder == null) {
                    //                        // either service or this action is not visible
                    //                        continue;
                    //                    }
                    //                    subMenuItemBuilder.build();
                }
            }
            if (menuSectionUiModel.hasSubMenuItems()) {
                menuSections.add(menuSectionUiModel);
            }
        }
        return menuSections;
    }

}