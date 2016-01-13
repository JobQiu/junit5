/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * @since 5.0
 */
class JUnit5TestTree {

	private final Map<TestIdentifier, Description> descriptions = new HashMap<>();
	private final TestPlan plan;
	private final Description suiteDescription;

	JUnit5TestTree(TestPlan plan, Class<?> testClass) {
		this.plan = plan;
		this.suiteDescription = generateDescription(plan, testClass);
	}

	public TestPlan getTestPlan() {
		return plan;
	}

	Description getSuiteDescription() {
		return this.suiteDescription;
	}

	Description getDescription(TestIdentifier identifier) {
		return this.descriptions.get(identifier);
	}

	private Description generateDescription(TestPlan testPlan, Class<?> testClass) {
		Description suiteDescription = Description.createSuiteDescription(testClass.getName());
		buildDescriptionTree(suiteDescription, testPlan);
		return suiteDescription;
	}

	private void buildDescriptionTree(Description suiteDescription, TestPlan testPlan) {
		testPlan.getRoots().stream().forEach(
			testIdentifier -> buildDescription(testIdentifier, suiteDescription, testPlan));
	}

	private void buildDescription(TestIdentifier identifier, Description parent, TestPlan testPlan) {
		Description newDescription = createJUnit4Description(identifier, testPlan);
		parent.addChild(newDescription);
		this.descriptions.put(identifier, newDescription);
		testPlan.getChildren(identifier).stream().forEach(
			testIdentifier -> buildDescription(testIdentifier, newDescription, testPlan));
	}

	private Description createJUnit4Description(TestIdentifier identifier, TestPlan testPlan) {
		if (identifier.isTest()) {
			return Description.createTestDescription(testPlan.getParent(identifier).get().getDisplayName(),
				identifier.getDisplayName(), identifier.getUniqueId());
		}
		else {
			return Description.createSuiteDescription(identifier.getDisplayName(), identifier.getUniqueId());
		}
	}

	Set<TestIdentifier> getByDescription(Filter filter) {
		return descriptions.entrySet().stream().filter(entry -> filter.shouldRun(entry.getValue())).map(
			Entry::getKey).collect(toSet());
	}

}
