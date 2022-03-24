/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jmx.export.metadata;

/**
 * Metadata that indicates to register instances of a given class
 * with a JMX server. Only valid when used on a <code>Class</code>.
 * @author Rob Harrop
 * @since 1.2
 * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler
 * @see org.springframework.jmx.export.naming.MetadataNamingStrategy
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class ManagedResource extends AbstractJmxAttribute {

	private String objectName;

	private boolean log = false;

	private String logFile;

	private String persistPolicy = "Never";

	private int persistPeriod;

	private String persistName;

	private String persistLocation;


	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getObjectName() {
		return this.objectName;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	public boolean isLog() {
		return log;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setPersistPolicy(String persistPolicy) {
		this.persistPolicy = persistPolicy;
	}

	public String getPersistPolicy() {
		return persistPolicy;
	}

	public void setPersistPeriod(int persistPeriod) {
		this.persistPeriod = persistPeriod;
	}

	public int getPersistPeriod() {
		return persistPeriod;
	}

	public void setPersistName(String persistName) {
		this.persistName = persistName;
	}

	public String getPersistName() {
		return persistName;
	}

	public void setPersistLocation(String persistLocation) {
		this.persistLocation = persistLocation;
	}

	public String getPersistLocation() {
		return persistLocation;
	}

}
