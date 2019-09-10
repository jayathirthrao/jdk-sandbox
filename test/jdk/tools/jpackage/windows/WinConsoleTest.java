/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

import java.nio.file.Path;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import jdk.jpackage.internal.IOUtils;
import jdk.jpackage.test.Test;
import jdk.jpackage.test.HelloApp;
import jdk.jpackage.test.JPackageCommand;

/*
 * @test
 * @summary jpackage with --win-console
 * @library ../helpers
 * @requires (os.family == "windows")
 * @modules jdk.jpackage/jdk.jpackage.internal
 * @run main/othervm -Xmx512m WinConsoleTest
 */
public class WinConsoleTest {

    public static void main(String[] args) throws IOException {
        JPackageCommand cmd = JPackageCommand.helloAppImage();
        final Path launcherPath = cmd.appImage().resolve(
                cmd.launcherPathInAppImage());

        IOUtils.deleteRecursive(cmd.outputDir().toFile());
        cmd.execute().assertExitCodeIsZero();
        HelloApp.executeAndVerifyOutput(launcherPath);
        checkSubsystem(launcherPath, false);

        IOUtils.deleteRecursive(cmd.outputDir().toFile());
        cmd.addArgument("--win-console").execute().assertExitCodeIsZero();
        HelloApp.executeAndVerifyOutput(launcherPath);
        checkSubsystem(launcherPath, true);
    }

    private static void checkSubsystem(Path path, boolean isConsole) throws
            IOException {
        final int subsystemGui = 2;
        final int subsystemConsole = 3;
        final int bufferSize = 512;

        final int expectedSubsystem = isConsole ? subsystemConsole : subsystemGui;

        try (InputStream inputStream = new FileInputStream(path.toString())) {
            byte[] bytes = new byte[bufferSize];
            Test.assertEquals(bufferSize, inputStream.read(bytes),
                    String.format("Check %d bytes were read from %s file",
                            bufferSize, path));

            // Check PE header for console or Win GUI app.
            // https://docs.microsoft.com/en-us/windows/desktop/api/winnt/ns-winnt-_image_nt_headers
            for (int i = 0; i < (bytes.length - 4); i++) {
                if (bytes[i] == 0x50 && bytes[i + 1] == 0x45
                        && bytes[i + 2] == 0x0 && bytes[i + 3] == 0x0) {

                    // Signature, File Header and subsystem offset.
                    i = i + 4 + 20 + 68;
                    byte subsystem = bytes[i];
                    Test.assertEquals(expectedSubsystem, subsystem,
                            String.format("Check subsystem of PE [%s] file",
                                    path));
                    return;
                }
            }
        }

        Test.assertUnexpected(String.format(
                "Subsystem not found in PE header of [%s] file", path));
    }
}
