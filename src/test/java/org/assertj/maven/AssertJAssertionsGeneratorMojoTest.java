/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2014 the original author or authors.
 */
package org.assertj.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.util.Arrays.array;
import static org.assertj.core.util.Lists.newArrayList;
import static org.assertj.maven.AssertJAssertionsGeneratorMojo.shouldHaveNonEmptyPackagesOrClasses;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.assertj.assertions.generator.BaseAssertionGenerator;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.maven.generator.AssertionsGenerator;
import org.assertj.maven.generator.AssertionsGeneratorReport;
import org.assertj.maven.test.Employee;
import org.assertj.maven.test.name.Name;
import org.assertj.maven.test.name.NameService;
import org.assertj.maven.test2.adress.Address;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssertJAssertionsGeneratorMojoTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private AssertJAssertionsGeneratorMojo assertjAssertionsGeneratorMojo;
  private MavenProject mavenProject;

  @Before
  public void setUp() throws Exception {
    mavenProject = mock(MavenProject.class);
    assertjAssertionsGeneratorMojo = new AssertJAssertionsGeneratorMojo();
    assertjAssertionsGeneratorMojo.project = mavenProject;
    assertjAssertionsGeneratorMojo.targetDir = temporaryFolder.getRoot().getAbsolutePath();
  }

  @Test
  public void executing_plugin_with_classes_and_packages_parameter_only_should_pass() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.maven.test", "org.assertj.maven.test2");
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.Employee");
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsFileFor(Address.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).exists();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).exists();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).exists();
  } 

  @Test
  public void executing_plugin_with_hierarchical_option_should_generate_hierarchical_assertions() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.maven.test", "org.assertj.maven.test2");
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.Employee");
    assertjAssertionsGeneratorMojo.hierarchical = true;
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(abstractAssertionsFileFor(Employee.class)).exists();
    assertThat(assertionsFileFor(Address.class)).exists();
    assertThat(abstractAssertionsFileFor(Address.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).exists();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).exists();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).exists();
  }

  @Test
  public void shoud_not_generate_assertions_for_assert_classes() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.MyAssert");
    assertjAssertionsGeneratorMojo.packages = array("some.package");
    assertjAssertionsGeneratorMojo.hierarchical = true;
    assertjAssertionsGeneratorMojo.execute();
    assertThat(assertionsFileFor("org.assertj.maven.test.MyAssertAssert")).doesNotExist();
  }

  @Test
  public void shoud_not_generate_assertions_for_assertions_classes() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.MyAssertions");
    assertjAssertionsGeneratorMojo.packages = array("some.package");
    assertjAssertionsGeneratorMojo.execute();
    assertThat(assertionsFileFor("org.assertj.maven.test.MyAssertionsAssert")).doesNotExist();
  }
  
  @Test
  public void executing_plugin_with_classes_parameter_only_should_pass() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.Employee",
                                                   "org.assertj.maven.test2.adress.Address");
    List<String> classes = newArrayList(Address.class.getName());
    assertjAssertionsGeneratorMojo.hierarchical = true;
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsFileFor(Address.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).exists();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).exists();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).exists();
  }

  @Test
  public void executing_plugin_with_custom_entry_point_class_package_should_pass() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.Employee");
    assertjAssertionsGeneratorMojo.entryPointClassPackage = "my.custom.pkg";
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFileWithCustomPackage()).exists();
  }

  @Test
  public void should_not_generate_entry_point_classes_if_disabled() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.Employee");
    List<String> classes = newArrayList(Employee.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    assertjAssertionsGeneratorMojo.generateAssertions = false;
    assertjAssertionsGeneratorMojo.generateBddAssertions = false;
    assertjAssertionsGeneratorMojo.generateSoftAssertions = false;
    assertjAssertionsGeneratorMojo.generateJUnitSoftAssertions = false;
    
    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).doesNotExist();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).doesNotExist();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).doesNotExist();
    assertThat(assertionsEntryPointFile("JUniSoftAssertions.java")).doesNotExist();
  }

  @Test
  public void executing_plugin_with_fake_package_should_not_generate_anything() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("fakepackage");
    List<String> classes = newArrayList();
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(temporaryFolder.getRoot().list()).isEmpty();
  }

  @Test
  public void executing_plugin_with_skip_set_to_true_should_not_generate_anything() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.maven.test.Employee");
    assertjAssertionsGeneratorMojo.skip = true;
    List<String> classes = newArrayList();
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(temporaryFolder.getRoot().list()).isEmpty();
  }

  @Test
  public void plugin_should_only_generate_assertions_for_included_classes() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.maven.test");
    assertjAssertionsGeneratorMojo.includes = array(".*Name");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(assertionsFileFor(Name.class)).exists();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }

  @Test
  public void plugin_should_not_generate_assertions_for_excluded_classes() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.maven.test");
    assertjAssertionsGeneratorMojo.excludes = array(".*Employee", ".*Service");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    
    assertjAssertionsGeneratorMojo.execute();
    
    assertThat(assertionsFileFor(Name.class)).exists();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }
  
  @Test
  public void plugin_should_not_generate_any_assertions_as_all_package_classes_are_excluded() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.maven.test");
    assertjAssertionsGeneratorMojo.excludes = array(".*Employ..", ".*Name.*");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    
    assertjAssertionsGeneratorMojo.execute();
    
    assertThat(assertionsFileFor(Name.class)).doesNotExist();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }
  
  @Test
  public void plugin_should_not_generate_assertions_for_classes_matching_both_include_and_exclude_pattern() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.maven.test");
    assertjAssertionsGeneratorMojo.includes = array(".*Employee", ".*Name.*");
    assertjAssertionsGeneratorMojo.excludes = array(".*Employee", ".*Service");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    
    assertjAssertionsGeneratorMojo.execute();
    
    assertThat(assertionsFileFor(Name.class)).exists();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void executing_plugin_with_error_should_be_reported_in_generator_report() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.Employee");
    when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(Employee.class.getName()));
    // let's throws an IOException when generating custom assertions
    AssertionsGenerator generator = new AssertionsGenerator(Thread.currentThread().getContextClassLoader());
    BaseAssertionGenerator baseGenerator = mock(BaseAssertionGenerator.class);
    generator.setBaseGenerator(baseGenerator);
    when(baseGenerator.generateCustomAssertionFor(any(ClassDescription.class))).thenThrow(IOException.class);
    AssertionsGeneratorReport report = assertjAssertionsGeneratorMojo.executeWithAssertionGenerator(generator);

    assertThat(report.getReportedException()).isInstanceOf(IOException.class);
    assertThat(temporaryFolder.getRoot().list()).isEmpty();
  }

  @Test
  public void input_classes_not_found_should_be_listed_in_generator_report() throws Exception {
	assertjAssertionsGeneratorMojo.classes = array("org.assertj.maven.test.Employee", "org.Foo", "org.Bar");
	when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(Employee.class.getName()));
	AssertionsGenerator generator = new AssertionsGenerator(Thread.currentThread().getContextClassLoader());
	
	AssertionsGeneratorReport report = assertjAssertionsGeneratorMojo.executeWithAssertionGenerator(generator);
	
    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(report.getInputClassesNotFound()).as("check report").containsExactly("org.Bar", "org.Foo");
  }
  
  @Test
  public void should_fail_if_packages_and_classes_parameters_are_null() throws Exception {
    try {
      assertjAssertionsGeneratorMojo.execute();
      failBecauseExceptionWasNotThrown(MojoFailureException.class);
    } catch (MojoFailureException e) {
      assertThat(e).hasMessage(shouldHaveNonEmptyPackagesOrClasses());
    }
  }

  private File assertionsFileFor(Class<?> clazz) throws IOException {
    return new File(temporaryFolder.getRoot(), basePathName(clazz) + "Assert.java");
  }

  private File assertionsFileFor(String className) throws IOException {
    return new File(temporaryFolder.getRoot(), className.replace('.', File.separatorChar) + ".java");
  }

  private File abstractAssertionsFileFor(Class<?> clazz) throws IOException {
    return new File(temporaryFolder.getRoot(), basePathName("Abstract", clazz) + "Assert.java");
  }

  private static String basePathName(Class<?> clazz) {
    return basePathName("", clazz);
  }

  private static String basePathName(String prefix, Class<?> clazz) {
    return clazz.getPackage().getName().replace('.', File.separatorChar) + File.separator + prefix
           + clazz.getSimpleName();
  }

  private File assertionsEntryPointFile(String simpleName) throws IOException {
    return new File(temporaryFolder.getRoot(), "org.assertj.maven.test".replace('.', File.separatorChar)
                                               + File.separator + simpleName);
  }

  private File assertionsEntryPointFileWithCustomPackage() throws IOException {
    return new File(temporaryFolder.getRoot(), "my.custom.pkg".replace('.', File.separatorChar) + File.separator
                                               + "Assertions.java");
  }
}
