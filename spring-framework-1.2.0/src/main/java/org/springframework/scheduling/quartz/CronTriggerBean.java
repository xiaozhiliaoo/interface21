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

package org.springframework.scheduling.quartz;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;

/**
 * Convenience subclass of Quartz' CronTrigger class that eases
 * bean-style usage.
 *
 * <p>CronTrigger itself is already a JavaBean but lacks sensible
 * defaults. This class uses the Spring bean name as job name, the
 * Quartz default group ("DEFAULT") as job group, the current time
 * as start time, and the default timezone if not specified.
 *
 * <p>This class will also register the trigger with the job name
 * and group of a given JobDetail. This allows SchedulerFactoryBean
 * to automatically register a trigger for the respective JobDetail,
 * instead of registering the JobDetail separately.
 *
 * @author Juergen Hoeller
 * @since 18.02.2004
 * @see #setName
 * @see #setGroup
 * @see #setStartTime
 * @see #setJobName
 * @see #setJobGroup
 * @see #setJobDetail
 * @see SchedulerFactoryBean#setTriggers
 * @see SchedulerFactoryBean#setJobDetails
 */
public class CronTriggerBean extends CronTrigger
    implements JobDetailAwareTrigger, BeanNameAware, InitializingBean {

	private static final Constants constants = new Constants(CronTrigger.class);

	private JobDetail jobDetail;

	private String beanName;


	/**
	 * Set the misfire instruction via the name of the corresponding
	 * constant in the SimpleTrigger class. Default is
	 * MISFIRE_INSTRUCTION_SMART_POLICY.
	 * @see CronTrigger#MISFIRE_INSTRUCTION_FIRE_ONCE_NOW
	 * @see CronTrigger#MISFIRE_INSTRUCTION_DO_NOTHING
	 * @see org.quartz.Trigger#MISFIRE_INSTRUCTION_SMART_POLICY
	 */
	public void setMisfireInstructionName(String constantName) {
		setMisfireInstruction(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set a list of TriggerListener names for this job, referring to
	 * non-global TriggerListeners registered with the Scheduler.
	 * <p>A TriggerListener name always refers to the name returned
	 * by the TriggerListener implementation.
	 * @see SchedulerFactoryBean#setTriggerListeners
	 * @see org.quartz.TriggerListener#getName
	 */
	public void setTriggerListenerNames(String[] names) {
		for (int i = 0; i < names.length; i++) {
			addTriggerListener(names[i]);
		}
	}

	/**
	 * Set the JobDetail that this trigger should be associated with.
	 * <p>This is typically used with a bean reference if the JobDetail
	 * is a Spring-managed bean. Alternatively, the trigger can also
	 * be associated with a job by name and group.
	 * @see #setJobName
	 * @see #setJobGroup
	 */
	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public JobDetail getJobDetail() {
		return jobDetail;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}


	public void afterPropertiesSet() throws ParseException {
		if (getName() == null) {
			setName(this.beanName);
		}
		if (getGroup() == null) {
			setGroup(Scheduler.DEFAULT_GROUP);
		}
		if (getStartTime() == null) {
			setStartTime(new Date());
		}
		if (getTimeZone() == null) {
			setTimeZone(TimeZone.getDefault());
		}
		if (this.jobDetail != null) {
			setJobName(this.jobDetail.getName());
			setJobGroup(this.jobDetail.getGroup());
		}
	}

}
