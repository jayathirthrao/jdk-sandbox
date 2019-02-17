/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package sun.net;

import java.io.IOException;
import java.net.SocketImpl;

/**
 * Implemented by the platform's SocketImpl implementations.
 */

public interface PlatformSocketImpl {

    /**
     * Invoked by ServerSocket to fix up the SocketImpl state after a connection
     * is accepted by a custom SocketImpl
     */
    void postCustomAccept() throws IOException;

    /**
     * Copy the state from this connected SocketImpl to a target SocketImpl. If
     * the target SocketImpl is not a newly created SocketImpl then it is first
     * closed to release any resources. The target SocketImpl becomes the owner
     * of the file descriptor, this SocketImpl is marked as closed and should
     * be discarded.
     */
    void copyTo(SocketImpl si);
}
