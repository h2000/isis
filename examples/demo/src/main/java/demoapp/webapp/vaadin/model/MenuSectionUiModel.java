package demoapp.webapp.vaadin.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MenuSectionUiModel {

    final String name;

    public boolean hasSubMenuItems() {
        return !serviceAndActionUiModels.isEmpty();
    }

    final List<ServiceAndActionUiModel> serviceAndActionUiModels = new ArrayList<>();

    public void addAction(final ServiceAndActionUiModel serviceAndActionUiModel) {
        serviceAndActionUiModels.add(serviceAndActionUiModel);
    }
}
