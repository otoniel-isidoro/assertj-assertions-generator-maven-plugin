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
package org.assertj.maven.generator;

import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newTreeSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.assertj.assertions.generator.AssertionsEntryPointType;

public class AssertionsGeneratorReport {

  private static final String INDENT = "- ";
  private static final String SECTION_START = "--- ";
  private static final String SECTION_END = " ---\n";
  private String directoryPathWhereAssertionFilesAreGenerated;
  private Set<String> generatedCustomAssertionFileNames;
  private Map<AssertionsEntryPointType, File> assertionsEntryPointFilesByType;
  private String[] inputPackages;
  private String[] inputClasses;
  private Exception exception;
  private Collection<Class<?>> excludedClassesFromAssertionGeneration;
  private Set<String> inputClassesNotFound;

  public AssertionsGeneratorReport() {
	assertionsEntryPointFilesByType = newTreeMap();
	generatedCustomAssertionFileNames = newTreeSet();
	inputClassesNotFound = newTreeSet();
	directoryPathWhereAssertionFilesAreGenerated = "no directory set";
  }

  public void setDirectoryPathWhereAssertionFilesAreGenerated(String directory) {
	this.directoryPathWhereAssertionFilesAreGenerated = directory;
  }

  public void addGeneratedAssertionFile(File generatedCustomAssertionFile) throws IOException {
	generatedCustomAssertionFileNames.add(generatedCustomAssertionFile.getCanonicalPath());
  }

  public String getReportContent() {
	StringBuilder reportBuilder = new StringBuilder("\n");
	reportBuilder.append("\n");
	reportBuilder.append("====================================\n");
	reportBuilder.append("AssertJ assertions generation report\n");
	reportBuilder.append("====================================\n");
	buildGeneratorParametersReport(reportBuilder);
	reportBuilder.append("\n");
	reportBuilder.append(SECTION_START).append("Generator results").append(SECTION_END);
	if (generationError()) {
	  buildGeneratorReportError(reportBuilder);
	} else if (nothingGenerated()) {
	  buildGeneratorReportWhenNothingWasGenerated(reportBuilder);
	} else {
	  buildGeneratorReportSuccess(reportBuilder);
	}
	return reportBuilder.toString();
  }

  private void buildGeneratorReportSuccess(StringBuilder reportBuilder) {
	reportBuilder.append("\n");
	reportBuilder.append("Directory where custom assertions files have been generated:\n");
	reportBuilder.append(INDENT).append(directoryPathWhereAssertionFilesAreGenerated).append("\n");
	reportBuilder.append("\n");
	reportBuilder.append("Custom assertions files generated:\n");
	for (String fileName : generatedCustomAssertionFileNames) {
	  reportBuilder.append(INDENT).append(fileName).append("\n");
	}
	if (!inputClassesNotFound.isEmpty()) {
	  reportBuilder.append("\n");
	  reportBuilder.append("No custom assertions files generated for the following input classes as they were not found:\n");
	  for (String inputClassNotFound : inputClassesNotFound) {
		reportBuilder.append(INDENT).append(inputClassNotFound).append("\n");
	  }
	}
	reportEntryPointClassesGeneration(reportBuilder);
  }

  private void reportEntryPointClassesGeneration(StringBuilder reportBuilder) {
	for (AssertionsEntryPointType type : assertionsEntryPointFilesByType.keySet()) {
	  if (assertionsEntryPointFilesByType.get(type) != null) {
		String entryPointClassName = remove(type.getFileName(), ".java");
		reportBuilder.append("\n")
		             .append(entryPointClassName).append(" entry point class has been generated in file:\n")
		             .append(INDENT).append(assertionsEntryPointFilesByType.get(type).getAbsolutePath()).append("\n");
	  }
	}
  }

  private void buildGeneratorReportWhenNothingWasGenerated(StringBuilder reportBuilder) {
	reportBuilder.append("\n");
	reportBuilder.append("No assertions generated as no classes have been found from given classes/packages.\n");
	if (isNotEmpty(inputClasses)) {
	  reportBuilder.append(INDENT).append("Given classes : ").append(Arrays.toString(inputClasses));
	  reportBuilder.append("\n");
	}
	if (isNotEmpty(inputPackages)) {
	  reportBuilder.append(INDENT).append("Given packages : ").append(Arrays.toString(inputPackages));
	  reportBuilder.append("\n");
	}
	if (isNotEmpty(excludedClassesFromAssertionGeneration)) {
	  reportBuilder.append(INDENT).append("Excluded classes : ").append(excludedClassesFromAssertionGeneration);
	}
  }

  private void buildGeneratorReportError(StringBuilder reportBuilder) {
	reportBuilder.append("\n");
	reportBuilder.append("Assertions failed with error : ").append(exception.getMessage());
	reportBuilder.append("\n");
	if (isNotEmpty(inputClasses)) {
	  reportBuilder.append(INDENT).append("Given classes were : ").append(Arrays.toString(inputClasses));
	  reportBuilder.append("\n");
	}
	if (isNotEmpty(inputPackages)) {
	  reportBuilder.append(INDENT).append("Given packages were : ").append(Arrays.toString(inputPackages));
	  reportBuilder.append("\n");
	}
	reportBuilder.append("\n");
	reportBuilder.append("Full error stack : ").append(getStackTrace(exception));
  }

  private void buildGeneratorParametersReport(StringBuilder reportBuilder) {
	reportBuilder.append("\n");
	reportBuilder.append(SECTION_START).append("Generator input parameters").append(SECTION_END).append("\n");
	if (isNotEmpty(inputPackages)) {
	  reportBuilder.append("Generating AssertJ assertions for classes in following packages and subpackages:\n");
	  for (String inputPackage : inputPackages) {
		reportBuilder.append(INDENT).append(inputPackage).append("\n");
	  }
	}
	if (isNotEmpty(inputClasses)) {
	  if (isNotEmpty(inputPackages)) {
		reportBuilder.append("\n");
	  }
	  reportBuilder.append("Generating AssertJ assertions for classes:\n");
	  for (String inputClass : inputClasses) {
		reportBuilder.append(INDENT).append(inputClass).append("\n");
	  }
	}
	if (isNotEmpty(excludedClassesFromAssertionGeneration)) {
	  reportBuilder.append("\n");
	  reportBuilder.append("Input classes excluded from assertions generation:\n");
	  for (Class<?> excludedClass : excludedClassesFromAssertionGeneration) {
		reportBuilder.append(INDENT).append(excludedClass.getName()).append("\n");
	  }
	}
  }

  private boolean generationError() {
	return exception != null;
  }

  private boolean nothingGenerated() {
	return generatedCustomAssertionFileNames.isEmpty();
  }

  public void reportEntryPointGeneration(AssertionsEntryPointType assertionsEntryPointType,
	                                     File assertionsEntryPointFile) {
	this.assertionsEntryPointFilesByType.put(assertionsEntryPointType, assertionsEntryPointFile);
  }

  public void setInputPackages(String[] packages) {
	this.inputPackages = packages;
  }

  public void setInputClasses(String[] classes) {
	this.inputClasses = classes;
  }

  public void setException(Exception exception) {
	this.exception = exception;
  }

  public Exception getReportedException() {
	return exception;
  }

  public void setExcludedClassesFromAssertionGeneration(Collection<Class<?>> excludedClassSet) {
	this.excludedClassesFromAssertionGeneration = excludedClassSet;
  }

  public Set<String> getInputClassesNotFound() {
	return inputClassesNotFound;
  }

  public void reportInputClassesNotFound(Set<Class<?>> classes, String[] inputClassNames) {
	Set<String> classesFound = newTreeSet();
	for (Class<?> clazz : classes) {
	  classesFound.add(clazz.getName());
	}
	for (String inputClass : inputClassNames) {
	  if (!classesFound.contains(inputClass)) {
		inputClassesNotFound.add(inputClass);
	  }
	}
  }
}
