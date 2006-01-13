package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.ParameterContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class ObjectParameter extends ObjectContent implements ParameterContent {
    private final NakedObject object;
    private final String name;
    private final NakedObjectSpecification specification;
    private final ActionHelper invocation;
    private final int i;
    private final boolean isRequired;
    private final NakedObject[] options;

    public ObjectParameter(
            String name,
            Naked naked,
            NakedObjectSpecification specification,
            boolean required,
            NakedObject[] options, int i, ActionHelper invocation) {
        this.name = name;
        this.specification = specification;
        this.isRequired = required;
        this.options = options;
        this.i = i;
        this.invocation = invocation;
        object = (NakedObject) naked;
    }

    public ObjectParameter(ObjectParameter content, NakedObject object) {
        name = content.name;
        specification = content.specification;
        isRequired = content.isRequired;
        options = content.options;
        i = content.i;
        invocation = content.invocation;
        this.object = object;
    }

    public Consent canClear() {
        return Allow.DEFAULT;
    }

    public Consent canSet(NakedObject dragSource) {
        if (dragSource.getSpecification().isOfType(specification)) {
            return Allow.DEFAULT;
        } else {
            return new Veto("Object must be " + specification.getShortName());
        }
    }

    public void clear() {
        setObject(null);
    }

    public void debugDetails(DebugString debug) {
        debug.appendln(4, "name", name);
        debug.appendln(4, "required", isRequired);
        debug.appendln(4, "object", object);
    }

    /*public String getIconName() {
        return object.getIconName();
    }*/

/*    public Image getIconPicture(int iconHeight) {
        NakedObjectSpecification specification = object.getSpecification();
        return ImageFactory.getInstance().loadIcon(specification, "", iconHeight);
    }
*/
    public Naked getNaked() {
        return object;
    }

    public NakedObject getObject() {
        return object;
    }
    
    public NakedObject[] getOptions() {
        return options;
    }

    public boolean isObject() {
        return true;
    }

    public boolean isRequired() {
        return isRequired;
    }
    
    public boolean isPersistable() {
        return false;
    }
    
    public boolean isTransient() {
        return object != null && object.getResolveState().isTransient();
    }

    public void contentMenuOptions(MenuOptionSet options) {
        if (object != null) {
            options.add(MenuOptionSet.USER, new MenuOption("Clear parameter") {

                public void execute(Workspace workspace, View view, Location at) {
                    clear();
                    view.getParent().invalidateContent();
                }
            });
        }
    }

    public void setObject(NakedObject object) {
        invocation.setParameter(i, object);
    }

    public String title() {
        return object == null ? "" : object.titleString();
    }

    public String toString() {
        ToString toString = new ToString(this);
        toString.append("label", name);
        toString.append("required", isRequired);
        toString.append("spec", getSpecification().getFullName());
        toString.append("object", object == null ? "null" : object.titleString());
        return toString.toString();
    }

    public String getParameterName() {
        return name;
    }

    public NakedObjectSpecification getSpecification() {
        return specification;
    }

    public String getDescription() {
        return null;
    }

    public String getId() {
        return null;
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */