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

package com.example.optaplanner.generic.solver.move.factory;

import com.example.optaplanner.generic.domain.Employee;
import com.example.optaplanner.generic.domain.NurseRoster;
import com.example.optaplanner.generic.domain.ShiftAssignment;
import com.example.optaplanner.generic.domain.solver.MovableShiftAssignmentSelectionFilter;
import com.example.optaplanner.generic.solver.move.EmployeeChangeMove;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
//import org.optaplanner.examples.nurserostering.domain.Employee;
;
//import org.optaplanner.examples.nurserostering.domain.ShiftAssignment;
//import org.optaplanner.examples.nurserostering.domain.solver.MovableShiftAssignmentSelectionFilter;
//import org.optaplanner.examples.nurserostering.solver.move.EmployeeChangeMove;

import java.util.ArrayList;
import java.util.List;

public class EmployeeChangeMoveFactory implements MoveListFactory<NurseRoster> {

    private MovableShiftAssignmentSelectionFilter filter = new MovableShiftAssignmentSelectionFilter();

    @Override
    public List<EmployeeChangeMove> createMoveList(NurseRoster nurseRoster) {
        List<EmployeeChangeMove> moveList = new ArrayList<>();
        List<Employee> employeeList = nurseRoster.getEmployeeList();
        for (ShiftAssignment shiftAssignment : nurseRoster.getShiftAssignmentList()) {
            if (filter.accept(nurseRoster, shiftAssignment)) {
                for (Employee employee : employeeList) {
                    moveList.add(new EmployeeChangeMove(shiftAssignment, employee));
                }
            }
        }
        return moveList;
    }

}
