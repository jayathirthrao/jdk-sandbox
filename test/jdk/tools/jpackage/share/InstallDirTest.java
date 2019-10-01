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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import jdk.jpackage.test.TKit;
import jdk.jpackage.test.PackageTest;
import jdk.jpackage.test.PackageType;
import jdk.jpackage.test.Functional;
import jdk.jpackage.test.JPackageCommand;
import jdk.jpackage.test.Annotations.Parameter;

/**
 * Test --install-dir parameter. Output of the test should be installdirtest*.*
 * package bundle. The output package should provide the same functionality as
 * the default package but install test application in specified directory.
 *
 * Linux:
 *
 * Application should be installed in /opt/jpackage/installdirtest folder.
 *
 * Mac:
 *
 * Application should be installed in /Applications/jpackage/installdirtest.app
 * folder.
 *
 * Windows:
 *
 * Application should be installed in %ProgramFiles%/TestVendor/InstallDirTest1234
 * folder.
 */

/*
 * @test
 * @summary jpackage with --install-dir
 * @library ../helpers
 * @build jdk.jpackage.test.*
 * @compile InstallDirTest.java
 * @modules jdk.jpackage/jdk.jpackage.internal
 * @run main/othervm/timeout=360 -Xmx512m jdk.jpackage.test.Main
 *  --jpt-run=InstallDirTest.testCommon
 */

/*
 * @test
 * @summary jpackage with --install-dir
 * @library ../helpers
 * @build jdk.jpackage.test.*
 * @compile InstallDirTest.java
 * @modules jdk.jpackage/jdk.jpackage.internal
 * @requires (os.family == "linux")
 * @run main/othervm/timeout=360 -Xmx512m jdk.jpackage.test.Main
 *  --jpt-run=InstallDirTest.testLinuxInvalid,testLinuxUnsupported
 */
public class InstallDirTest {

    public static void testCommon() {
        final Map<PackageType, Path> INSTALL_DIRS = Functional.identity(() -> {
            Map<PackageType, Path> reply = new HashMap<>();
            reply.put(PackageType.WIN_MSI, Path.of("TestVendor\\InstallDirTest1234"));
            reply.put(PackageType.WIN_EXE, reply.get(PackageType.WIN_MSI));

            reply.put(PackageType.LINUX_DEB, Path.of("/opt/jpackage"));
            reply.put(PackageType.LINUX_RPM, reply.get(PackageType.LINUX_DEB));

            reply.put(PackageType.MAC_PKG, Path.of("/Application/jpackage"));
            reply.put(PackageType.MAC_DMG, reply.get(PackageType.MAC_PKG));

            return reply;
        }).get();

        new PackageTest().configureHelloApp()
        .addInitializer(cmd -> {
            cmd.addArguments("--install-dir", INSTALL_DIRS.get(
                    cmd.packageType()));
        }).run();
    }

    @Parameter("/")
    @Parameter(".")
    @Parameter("foo")
    @Parameter("/opt/foo/.././.")
    public static void testLinuxInvalid(String installDir) {
        testLinuxBad(installDir, "Invalid installation directory");
    }

    @Parameter("/usr")
    @Parameter("/usr/local")
    @Parameter("/usr/foo")
    public static void testLinuxUnsupported(String installDir) {
        testLinuxBad(installDir, "currently unsupported");
    }

    private static void testLinuxBad(String installDir,
            String errorMessageSubstring) {
        new PackageTest().configureHelloApp()
                .setExpectedExitCode(1)
                .forTypes(PackageType.LINUX)
                .addInitializer(cmd -> {
                    cmd.addArguments("--install-dir", installDir);
                    cmd.saveConsoleOutput(true);
                })
                .addBundleVerifier((cmd, result) -> {
                    String errorMessage = JPackageCommand.filterOutput(result.
                            getOutput().stream()).filter(line -> line.contains(
                            errorMessageSubstring)).findFirst().orElse(null);
                    TKit.assertNotNull(errorMessage, String.format(
                            "Check output contains [%s] substring",
                            errorMessageSubstring));
                })
                .run();
    }
}
