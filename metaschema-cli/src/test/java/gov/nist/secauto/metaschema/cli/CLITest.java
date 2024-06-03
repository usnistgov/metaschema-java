/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.cli;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Unit test for simple CLI.
 */
public class CLITest {
  private static final ExitCode NO_EXCEPTION_CLASS = null;

  void evaluateResult(@NonNull ExitStatus status, @NonNull ExitCode expectedCode) {
    status.generateMessage(true);
    assertAll(() -> assertEquals(expectedCode, status.getExitCode(), "exit code mismatch"),
        () -> assertNull(status.getThrowable(), "expected null Throwable"));
  }

  void evaluateResult(@NonNull ExitStatus status, @NonNull ExitCode expectedCode,
      @NonNull Class<? extends Throwable> thrownClass) {
    status.generateMessage(true);
    Throwable thrown = status.getThrowable();
    assert thrown != null;
    assertAll(() -> assertEquals(expectedCode, status.getExitCode(), "exit code mismatch"),
        () -> assertEquals(thrownClass, thrown.getClass(), "expected Throwable mismatch"));
  }

  private static Stream<Arguments> providesValues() {
    List<Arguments> values = new LinkedList<>() {
      {
        add(Arguments.of(new String[] {}, ExitCode.INVALID_COMMAND, NO_EXCEPTION_CLASS));
        add(Arguments.of(new String[] { "-h" }, ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(new String[] { "generate-schema", "--help" }, ExitCode.OK,
            NO_EXCEPTION_CLASS));
        add(Arguments.of(new String[] { "validate", "--help" }, ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(new String[] { "validate-content", "--help" }, ExitCode.OK,
            NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "validate",
                "../databind/src/test/resources/metaschema/fields_with_flags/metaschema.xml" },
            ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(new String[] { "generate-schema", "--overwrite", "--as", "JSON",
            "../databind/src/test/resources/metaschema/fields_with_flags/metaschema.xml",
            "target/schema-test.json" }, ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "validate-content", "--as=xml",
                "-m=../databind/src/test/resources/metaschema/bad_index-has-key/metaschema.xml",
                "../databind/src/test/resources/metaschema/bad_index-has-key/example.xml",
                "--show-stack-trace" },
            ExitCode.FAIL, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "validate-content", "--as=json",
                "-m=../databind/src/test/resources/metaschema/bad_index-has-key/metaschema.xml",
                "../databind/src/test/resources/metaschema/bad_index-has-key/example.json", "--show-stack-trace" },
            ExitCode.FAIL, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "validate",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/metaschema.xml",
                "--show-stack-trace" },
            ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "generate-schema",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/metaschema.xml",
                "--as", "xml",
            },
            ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "generate-schema",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/metaschema.xml",
                "--as", "json",
            },
            ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "validate-content",
                "-m",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/metaschema.xml",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/example.json",
                "--as=json"
            },
            ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "validate-content",
                "-m",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/metaschema.xml",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/example.xml",
                "--as=xml"
            },
            ExitCode.OK, NO_EXCEPTION_CLASS));
        add(Arguments.of(
            new String[] { "validate-content",
                "-m",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/metaschema.xml",
                "https://bad.domain.example.net/example.xml",
                "--as=xml"
            },
            ExitCode.IO_ERROR, java.net.UnknownHostException.class));
        add(Arguments.of(
            new String[] { "validate-content",
                "-m",
                "https://raw.githubusercontent.com/usnistgov/metaschema-java/28468999d802e69273df7e725d183c132e2b15d8/databind/src/test/resources/metaschema/simple/metaschema.xml",
                "https://nist.gov/example.xml",
                "--as=xml"
            },
            ExitCode.IO_ERROR, java.io.FileNotFoundException.class));
        add(Arguments.of(
            new String[] { "metapath", "list-functions" },
            ExitCode.OK, NO_EXCEPTION_CLASS));
      }
    };

    return values.stream();
  }

  @ParameterizedTest
  @MethodSource("providesValues")
  void testAllCommands(@NonNull String[] args, @NonNull ExitCode expectedExitCode,
      Class<? extends Throwable> expectedThrownClass) {
    String[] defaultArgs = { "--show-stack-trace" };
    String[] fullArgs = Stream.of(args, defaultArgs).flatMap(Stream::of)
        .toArray(String[]::new);
    if (expectedThrownClass == null) {
      evaluateResult(CLI.runCli(fullArgs), expectedExitCode);
    } else {
      evaluateResult(CLI.runCli(fullArgs), expectedExitCode, expectedThrownClass);
    }
  }
}
