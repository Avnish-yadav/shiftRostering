package com.example.optaplanner.generic.solver.score;

import com.example.optaplanner.generic.domain.Customer;
import com.example.optaplanner.generic.domain.Standstill;
import com.example.optaplanner.generic.domain.Vehicle;
import com.example.optaplanner.generic.domain.VehicleRoutingSolution;
import com.example.optaplanner.generic.domain.timewindowed.TimeWindowedCustomer;
import com.example.optaplanner.generic.domain.timewindowed.TimeWindowedVehicleRoutingSolution;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleRoutingIncrementalScoreCalculator extends AbstractIncrementalScoreCalculator<VehicleRoutingSolution> {

    private boolean timeWindowed;
    private Map<Vehicle, Integer> vehicleDemandMap;

    private long hardScore;
    private long softScore;

    @Override
    public void resetWorkingSolution(VehicleRoutingSolution solution) {
        timeWindowed = solution instanceof TimeWindowedVehicleRoutingSolution;
        List<Vehicle> vehicleList = solution.getVehicleList();
        vehicleDemandMap = new HashMap<>(vehicleList.size());
        for (Vehicle vehicle : vehicleList) {
            vehicleDemandMap.put(vehicle, 0);
        }
        hardScore = 0L;
        softScore = 0L;
        for (Customer customer : solution.getCustomerList()) {
            insertPreviousStandstill(customer);
            insertVehicle(customer);
            // Do not do insertNextCustomer(customer) to avoid counting distanceFromLastCustomerToDepot twice
            if (timeWindowed) {
                insertArrivalTime((TimeWindowedCustomer) customer);
            }
        }
    }

    @Override
    public void beforeEntityAdded(Object entity) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(Object entity) {
        if (entity instanceof Vehicle) {
            return;
        }
        insertPreviousStandstill((Customer) entity);
        insertVehicle((Customer) entity);
        // Do not do insertNextCustomer(customer) to avoid counting distanceFromLastCustomerToDepot twice
        if (timeWindowed) {
            insertArrivalTime((TimeWindowedCustomer) entity);
        }
    }

    @Override
    public void beforeVariableChanged(Object entity, String variableName) {
        if (entity instanceof Vehicle) {
            return;
        }
        switch (variableName) {
            case "previousStandstill":
                retractPreviousStandstill((Customer) entity);
                break;
            case "vehicle":
                retractVehicle((Customer) entity);
                break;
            case "nextCustomer":
                retractNextCustomer((Customer) entity);
                break;
            case "arrivalTime":
                retractArrivalTime((TimeWindowedCustomer) entity);
                break;
            default:
                throw new IllegalArgumentException("Unsupported variableName (" + variableName + ").");
        }
    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        if (entity instanceof Vehicle) {
            return;
        }
        switch (variableName) {
            case "previousStandstill":
                insertPreviousStandstill((Customer) entity);
                break;
            case "vehicle":
                insertVehicle((Customer) entity);
                break;
            case "nextCustomer":
                insertNextCustomer((Customer) entity);
                break;
            case "arrivalTime":
                insertArrivalTime((TimeWindowedCustomer) entity);
                break;
            default:
                throw new IllegalArgumentException("Unsupported variableName (" + variableName + ").");
        }
    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        if (entity instanceof Vehicle) {
            return;
        }
        retractPreviousStandstill((Customer) entity);
        retractVehicle((Customer) entity);
        // Do not do retractNextCustomer(customer) to avoid counting distanceFromLastCustomerToDepot twice
        if (timeWindowed) {
            retractArrivalTime((TimeWindowedCustomer) entity);
        }
    }

    @Override
    public void afterEntityRemoved(Object entity) {
        // Do nothing
    }

    private void insertPreviousStandstill(Customer customer) {
        Standstill previousStandstill = customer.getPreviousStandstill();
        if (previousStandstill != null) {
            // Score constraint distanceToPreviousStandstill
            softScore -= customer.getDistanceFromPreviousStandstill();
        }
    }

    private void retractPreviousStandstill(Customer customer) {
        Standstill previousStandstill = customer.getPreviousStandstill();
        if (previousStandstill != null) {
            // Score constraint distanceToPreviousStandstill
            softScore += customer.getDistanceFromPreviousStandstill();
        }
    }

    private void insertVehicle(Customer customer) {
        Vehicle vehicle = customer.getVehicle();
        if (vehicle != null) {
            // Score constraint vehicleCapacity
            int capacity = vehicle.getCapacity();
            int oldDemand = vehicleDemandMap.get(vehicle);
            int newDemand = oldDemand + customer.getDemand();
            hardScore += Math.min(capacity - newDemand, 0) - Math.min(capacity - oldDemand, 0);
            vehicleDemandMap.put(vehicle, newDemand);
            if (customer.getNextCustomer() == null) {
                // Score constraint distanceFromLastCustomerToDepot
                softScore -= customer.getLocation().getDistanceTo(vehicle.getLocation());
            }
        }
    }

    private void retractVehicle(Customer customer) {
        Vehicle vehicle = customer.getVehicle();
        if (vehicle != null) {
            // Score constraint vehicleCapacity
            int capacity = vehicle.getCapacity();
            int oldDemand = vehicleDemandMap.get(vehicle);
            int newDemand = oldDemand - customer.getDemand();
            hardScore += Math.min(capacity - newDemand, 0) - Math.min(capacity - oldDemand, 0);
            vehicleDemandMap.put(vehicle, newDemand);
            if (customer.getNextCustomer() == null) {
                // Score constraint distanceFromLastCustomerToDepot
                softScore += customer.getLocation().getDistanceTo(vehicle.getLocation());
            }
        }
    }

    private void insertNextCustomer(Customer customer) {
        Vehicle vehicle = customer.getVehicle();
        if (vehicle != null) {
            if (customer.getNextCustomer() == null) {
                // Score constraint distanceFromLastCustomerToDepot
                softScore -= customer.getLocation().getDistanceTo(vehicle.getLocation());
            }
        }
    }

    private void retractNextCustomer(Customer customer) {
        Vehicle vehicle = customer.getVehicle();
        if (vehicle != null) {
            if (customer.getNextCustomer() == null) {
                // Score constraint distanceFromLastCustomerToDepot
                softScore += customer.getLocation().getDistanceTo(vehicle.getLocation());
            }
        }
    }

    private void insertArrivalTime(TimeWindowedCustomer customer) {
        Long arrivalTime = customer.getArrivalTime();
        if (arrivalTime != null) {
            long dueTime = customer.getDueTime();
            if (dueTime < arrivalTime) {
                // Score constraint arrivalAfterDueTime
                hardScore -= (arrivalTime - dueTime);
            }
        }
        // Score constraint arrivalAfterDueTimeAtDepot is a built-in hard constraint in VehicleRoutingImporter
    }

    private void retractArrivalTime(TimeWindowedCustomer customer) {
        Long arrivalTime = customer.getArrivalTime();
        if (arrivalTime != null) {
            long dueTime = customer.getDueTime();
            if (dueTime < arrivalTime) {
                // Score constraint arrivalAfterDueTime
                hardScore += (arrivalTime - dueTime);
            }
        }
    }

    @Override
    public HardSoftLongScore calculateScore() {
        return HardSoftLongScore.valueOf(hardScore, softScore);
    }

}
