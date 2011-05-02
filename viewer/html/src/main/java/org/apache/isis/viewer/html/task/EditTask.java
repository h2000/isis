/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.html.task;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.propparam.maxlength.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.typicallength.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.context.Context;

public class EditTask extends Task {
    private static int size(final ObjectAdapter object) {
        final List<ObjectAssociation> fields =
            object.getSpecification().getAssociations(
                ObjectAssociationFilters.dynamicallyVisible(IsisContext.getAuthenticationSession(), object));
        return fields.size();
    }

    private static boolean skipField(final ObjectAdapter object, final ObjectAssociation fld) {
        return fld.isOneToManyAssociation() || fld.isUsable(IsisContext.getAuthenticationSession(), object).isVetoed();
    }

    private final ObjectAssociation[] fields;
    private final String newType;

    public EditTask(final Context context, final ObjectAdapter object) {
        super(context, "Edit", "", object, size(object));

        final List<ObjectAssociation> allFields =
            object.getSpecification().getAssociations(
                ObjectAssociationFilters.dynamicallyVisible(IsisContext.getAuthenticationSession(), object));

        fields = new ObjectAssociation[names.length];
        for (int i = 0, j = 0; j < allFields.size(); j++) {
            final ObjectAssociation fld = allFields.get(j);
            fields[i] = fld;
            names[i] = fld.getName();
            descriptions[i] = fld.getDescription();

            final Consent usableByUser = fld.isUsable(IsisContext.getAuthenticationSession(), object);
            if (usableByUser.isVetoed()) {
                descriptions[i] = usableByUser.getReason();
            }

            fieldSpecifications[i] = fld.getSpecification();
            initialState[i] = fld.get(object);
            if (skipField(object, fld)) {
                readOnly[i] = true;
            } else {
                readOnly[i] = false;
                optional[i] = !fld.isMandatory();
                if (fieldSpecifications[i].isParseable()) {
                    final MultiLineFacet multilineFacet = fld.getFacet(MultiLineFacet.class);
                    noLines[i] = multilineFacet.numberOfLines();
                    wraps[i] = !multilineFacet.preventWrapping();

                    final MaxLengthFacet maxLengthFacet = fld.getFacet(MaxLengthFacet.class);
                    maxLength[i] = maxLengthFacet.value();

                    final TypicalLengthFacet typicalLengthFacet = fld.getFacet(TypicalLengthFacet.class);
                    typicalLength[i] = typicalLengthFacet.value();
                }
            }
            i++;
        }

        final boolean isNew = object.getResolveState() == ResolveState.TRANSIENT;
        newType = isNew ? getTarget(context).getSpecification().getSingularName() : null;
    }

    @Override
    protected ObjectAdapter[][] getOptions(final Context context, final int from, final int len) {
        final ObjectAdapter target = getTarget(context);
        final ObjectAdapter[][] options = new ObjectAdapter[len][];
        for (int i = from, j = 0; j < len; i++, j++) {
            if (skipField(target, fields[i])) {
            } else {
                options[j] = fields[i].getChoices(target);
            }
        }
        return options;
    }

    @Override
    public void checkForValidity(final Context context) {
        final ObjectAdapter target = getTarget(context);
        final ObjectAdapter[] entries = getEntries(context);

        final int len = fields.length;
        for (int i = 0; i < len; i++) {
            if (readOnly[i] || errors[i] != null) {
                continue;
            }
            final ObjectAssociation fld = fields[i];
            if (fld.isOneToOneAssociation()) {
                final OneToOneAssociation oneToOneAssociation = (OneToOneAssociation) fld;
                final ObjectAdapter entryReference = entries[i];
                final ObjectAdapter currentReference = oneToOneAssociation.get(target);
                if (currentReference != entryReference) {
                    final Consent valueValid = ((OneToOneAssociation) fld).isAssociationValid(target, entryReference);
                    errors[i] = valueValid.getReason();
                }
            }
        }

        if (target.isTransient()) {
            saveState(target, entries);
            final Consent isValid = target.getSpecification().isValid(target);
            error = isValid.isVetoed() ? isValid.getReason() : null;
        }
    }

    @Override
    public ObjectAdapter completeTask(final Context context, final Page page) {
        final ObjectAdapter targetAdapter = getTarget(context);
        final ObjectAdapter[] entryAdapters = getEntries(context);

        if (targetAdapter.isTransient()) {
            final ObjectAction action =
                targetAdapter.getSpecification().getObjectAction(ActionType.USER, "save",
                    ObjectSpecification.EMPTY_LIST);
            if (action == null) {
                getPersistenceSession().makePersistent(targetAdapter);
            } else {
                action.execute(targetAdapter, new ObjectAdapter[0]);
            }
        } else {
            saveState(targetAdapter, entryAdapters);
        }

        return targetAdapter;
    }

    private void saveState(final ObjectAdapter targetAdapter, final ObjectAdapter[] entryAdapters) {
        for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation fld = fields[i];
            final ObjectAdapter entryAdapter = entryAdapters[i];
            final boolean isReadOnly = readOnly[i];

            if (isReadOnly) {
                continue;
            }

            if (fld.isOneToOneAssociation()) {
                final OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) fld);
                final Object entryPojo = AdapterUtils.unwrap(entryAdapter);
                if (entryPojo == null) {
                    if (oneToOneAssociation.get(targetAdapter) != null) {
                        oneToOneAssociation.clearAssociation(targetAdapter);
                    }
                } else {
                    final ObjectAdapter currentAdapter = oneToOneAssociation.get(targetAdapter);
                    final Object currentPojo = AdapterUtils.unwrap(currentAdapter);
                    if (currentAdapter == null || currentPojo == null || !currentPojo.equals(entryPojo)) {
                        oneToOneAssociation.setAssociation(targetAdapter, entryAdapter);
                    }
                }
            }
        }
    }

    @Override
    protected boolean simpleField(final ObjectSpecification type, final int i) {
        return !fields[i].hasChoices() || super.simpleField(type, i);
    }

    @Override
    public boolean isEditing() {
        return true;
    }

    @Override
    public String getName() {
        if (newType == null) {
            return super.getName();
        } else {
            return "New " + newType;
        }
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
