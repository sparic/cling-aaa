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

package org.fourthline.cling.workbench.browser;

import org.fourthline.cling.model.meta.Device;

import javax.swing.ImageIcon;

public class RootDeviceSelected {

    public ImageIcon icon;
    public Device device;

    public RootDeviceSelected(ImageIcon icon, Device device) {
        this.icon = icon;
        this.device = device;
    }

    public RootDeviceSelected(Device device) {
        this.device = device;
    }

}
