package demoapp.webapp.vaadin.model;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;

import lombok.Data;

@Data
public class EntityUiModel {

    private final IsisWebAppCommonContext commonContext;
    private final ManagedObject managedObject;

    ManagedObject load() {
        return null;
    }

    public enum Mode {
        VIEW, EDIT
    }

    public enum RenderingHint {
        // normal form
        REGULAR(Where.OBJECT_FORMS),

        // inside parent table
        PARENTED_PROPERTY_COLUMN(Where.PARENTED_TABLES),
        PARENTED_TITLE_COLUMN(Where.PARENTED_TABLES),

        // stand alone table
        STANDALONE_PROPERTY_COLUMN(Where.STANDALONE_TABLES),
        STANDALONE_TITLE_COLUMN(Where.STANDALONE_TABLES);

        private final Where where;

        RenderingHint(final Where where) {
            this.where = where;
        }

        public boolean isRegular() {
            return this == REGULAR;
        }

        public boolean isInParentedTable() {
            return this == PARENTED_PROPERTY_COLUMN;
        }

        public boolean isInStandaloneTable() {
            return this == STANDALONE_PROPERTY_COLUMN;
        }

        public boolean isInTable() {
            return isInParentedTable() || isInStandaloneTable() || isInTableTitleColumn();
        }

        public boolean isInTableTitleColumn() {
            return isInParentedTableTitleColumn() || isInStandaloneTableTitleColumn();
        }

        public boolean isInParentedTableTitleColumn() {
            return this == PARENTED_TITLE_COLUMN;
        }

        public boolean isInStandaloneTableTitleColumn() {
            return this == STANDALONE_TITLE_COLUMN;
        }

        public Where asWhere() {
            return this.where;
        }
    }
}
