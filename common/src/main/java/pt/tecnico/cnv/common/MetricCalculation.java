package pt.tecnico.cnv.common;

/**
 * Created by miguel on 17/05/17.
 */
public final class MetricCalculation {

    public static final long calculate(long blocks, long method_calls, long blocks_failures) {
        return blocks+method_calls+blocks_failures;
    }
}
