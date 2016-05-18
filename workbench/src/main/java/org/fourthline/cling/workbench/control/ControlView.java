/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.fourthline.cling.workbench.control;

import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.support.shared.View;

/**
 * @author Christian Bauer
 */
public interface ControlView extends View<ControlView.Presenter> {

    public interface Presenter {
        void init(Action action, ActionArgumentValue[] presetInputValues);

        void onInvoke();

        void onCancel();

        void onExpandText(String text);
    }

    void init(Action action, ActionArgumentValue[] presetInputValues);

    ActionArgumentValue[] getInputValues();

    void setOutputValues(ActionArgumentValue[] values);

    void setCancelEnabled(boolean enabled);

    void dispose();
}
