/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.job.cloud.mesos.stragety;

import com.dangdang.ddframe.job.cloud.JobContext;
import com.dangdang.ddframe.job.cloud.mesos.HardwareResource;
import com.dangdang.ddframe.job.cloud.mesos.fixture.OfferBuilder;
import com.dangdang.ddframe.job.cloud.state.fixture.CloudJobConfigurationBuilder;
import org.apache.mesos.Protos;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ExhaustFirstResourceAllocateStrategyTest {
    
    @Test
    public void assertAllocateForList() {
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(
                Arrays.asList(new HardwareResource(OfferBuilder.createOffer("offer_0", 8d, 1280d)), new HardwareResource(OfferBuilder.createOffer("offer_1", 8d, 1280d))));
        List<Protos.TaskInfo> actual = resourceAllocateStrategy.allocate(
                Arrays.asList(JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_0")), JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1"))));
        assertThat(actual.size(), is(10));
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getTaskId().getValue(), startsWith("test_job_0@-@" + i));
            if (i < 8) {
                assertThat(actual.get(i).getSlaveId().getValue(), is("slave-offer_0"));
            } else {
                assertThat(actual.get(i).getSlaveId().getValue(), is("slave-offer_1"));
            }
        }
    }
    
    @Test
    public void assertAllocateForMap() {
        ResourceAllocateStrategy resourceAllocateStrategy = new ExhaustFirstResourceAllocateStrategy(
                Arrays.asList(new HardwareResource(OfferBuilder.createOffer("offer_0", 15d, 5120d)), new HardwareResource(OfferBuilder.createOffer("offer_1", 10d, 1280d))));
        Map<String, JobContext> map = new LinkedHashMap<>(2, 1);
        map.put("test_job_0@-@00", JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_0")));
        map.put("test_job_1@-@00", JobContext.from(CloudJobConfigurationBuilder.createCloudJobConfiguration("test_job_1")));
        Map<String, List<Protos.TaskInfo>> actual = resourceAllocateStrategy.allocate(map);
        assertThat(actual.size(), is(2));
        List<Protos.TaskInfo> actualTaskInfoList0 = actual.get("test_job_0@-@00");
        assertThat(actualTaskInfoList0.size(), is(10));
        for (int i = 0; i < actualTaskInfoList0.size(); i++) {
            assertThat(actualTaskInfoList0.get(i).getTaskId().getValue(), startsWith("test_job_0@-@" + i));
            assertThat(actualTaskInfoList0.get(i).getSlaveId().getValue(), is("slave-offer_0"));
        }
        List<Protos.TaskInfo> actualTaskInfoList1 = actual.get("test_job_1@-@00");
        assertThat(actualTaskInfoList1.size(), is(10));
        for (int i = 0; i < actualTaskInfoList1.size(); i++) {
            assertThat(actualTaskInfoList1.get(i).getTaskId().getValue(), startsWith("test_job_1@-@" + i));
            if (i < 5) {
                assertThat(actualTaskInfoList1.get(i).getSlaveId().getValue(), is("slave-offer_0"));
            } else {
                assertThat(actualTaskInfoList1.get(i).getSlaveId().getValue(), is("slave-offer_1"));
            }
        }
    }
}