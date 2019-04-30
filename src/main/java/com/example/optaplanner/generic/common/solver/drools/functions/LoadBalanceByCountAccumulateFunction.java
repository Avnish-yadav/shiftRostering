package com.example.optaplanner.generic.common.solver.drools.functions;

import org.kie.api.runtime.rule.AccumulateFunction;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LoadBalanceByCountAccumulateFunction implements AccumulateFunction<LoadBalanceByCountAccumulateFunction.LoadBalanceByCountData> {

    protected static class LoadBalanceByCountData implements Serializable {

        private Map<Object, Long> groupCountMap;
        // the sum of squared deviation from zero
        private long squaredSum;

    }

    @Override
    public LoadBalanceByCountData createContext() {
        return new LoadBalanceByCountData();
    }

    @Override
    public void init(LoadBalanceByCountData data) {
        data.groupCountMap = new HashMap<>();
        data.squaredSum = 0L;
    }

    @Override
    public void accumulate(LoadBalanceByCountData data, Object groupBy) {
        long count = data.groupCountMap.compute(groupBy,
                (key, value) -> (value == null) ? 1L : value + 1L);
        // squaredZeroDeviation = squaredZeroDeviation - (count - 1)² + count²
        // <=> squaredZeroDeviation = squaredZeroDeviation + (2 * count - 1)
        data.squaredSum += (2 * count - 1);
    }

    @Override
    public boolean supportsReverse() {
        return true;
    }

    @Override
    public void reverse(LoadBalanceByCountData data, Object groupBy) {
        Long count = data.groupCountMap.compute(groupBy,
                (key, value) -> (value.longValue() == 1L) ? null : value - 1L);
        data.squaredSum -= (count == null) ? 1L : (2 * count + 1);
    }

    @Override
    public Class<LoadBalanceByCountResult> getResultType() {
        return LoadBalanceByCountResult.class;
    }

    @Override
    public LoadBalanceByCountResult getResult(LoadBalanceByCountData data) {
        return new LoadBalanceByCountResult(data.squaredSum);
    }

    @Override
    public void writeExternal(ObjectOutput out) {
    }

    @Override
    public void readExternal(ObjectInput in) {
    }

    public static class LoadBalanceByCountResult implements Serializable {

        private final long squaredSum;

        public LoadBalanceByCountResult(long squaredSum) {
            this.squaredSum = squaredSum;
        }

        public long getZeroDeviationSquaredSum() {
            return squaredSum;
        }

        /**
         * @return {@link #getZeroDeviationSquaredSumRoot(double)} multiplied by {@literal 1 000}
         */
        public long getZeroDeviationSquaredSumRootMillis() {
            return getZeroDeviationSquaredSumRoot(1_000.0);
        }

        /**
         * @return {@link #getZeroDeviationSquaredSumRoot(double)} multiplied by {@literal 1 000 000}
         */
        public long getZeroDeviationSquaredSumRootMicros() {
            return getZeroDeviationSquaredSumRoot(1_000_000.0);
        }

        /**
         * @param scaleMultiplier {@code > 0}
         * @return {@code >= 0}, {@code latexmath:[f(n) = \sqrt{\sum_{i=1}^{n} (x_i - 0)^2}]} multiplied by scaleMultiplier
         */
        public long getZeroDeviationSquaredSumRoot(double scaleMultiplier) {
            return (long) (Math.sqrt((double) squaredSum) * scaleMultiplier);
        }

    }

}
