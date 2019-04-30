/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package com.example.optaplanner.generic.domain.request;

import com.example.optaplanner.generic.common.domain.AbstractPersistable;
import com.example.optaplanner.generic.domain.Employee;
import com.example.optaplanner.generic.domain.Shift;
import com.thoughtworks.xstream.annotations.XStreamAlias;
//import org.optaplanner.examples.common.domain.AbstractPersistable;
//import org.optaplanner.examples.nurserostering.domain.Employee;
//import org.optaplanner.examples.nurserostering.domain.Shift;

@XStreamAlias("ShiftOnRequest")
public class ShiftOnRequest extends AbstractPersistable {

    private Employee employee;
    private Shift shift;
    private int weight;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return shift + "_ON_" + employee;
    }

}
