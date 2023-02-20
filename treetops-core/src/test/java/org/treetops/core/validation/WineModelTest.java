package org.treetops.core.validation;

import io.github.horoc.treetops.core.predictor.Predictor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/18
 */
public class WineModelTest extends LoadModelTemplate {

    @Override
    protected double[] getFeature() {
        return new double[] {0.913332708127694, -0.5981563241524619, -0.42590882302929406, -0.9293651811915726, 1.2819851519177938, 0.4885310849747506, 0.874184282556729, -1.2236095387553816,
            0.050987616822829956, 0.34255654624083676, -0.16430337010617904, 0.830960739456274, 0.9970864580546609};
    }

    @Test
    public void testPredictByGeneratedClass() {
        try {
            Predictor predictor = loadModel("wine_model", true);
            double[] result = predictor.predict(getFeature());
            assertDoubleEquals(0.9849612333276241D, result[0]);
            assertDoubleEquals(0.008531186707393178D, result[1]);
            assertDoubleEquals(0.006507579964982725D, result[2]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testSimplePredictor() {
        try {
            Predictor predictor = loadModel("wine_model", false);
            double[] result = predictor.predict(getFeature());
            assertDoubleEquals(0.9849612333276241D, result[0]);
            assertDoubleEquals(0.008531186707393178D, result[1]);
            assertDoubleEquals(0.006507579964982725D, result[2]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }

    private void assertDoubleEquals(double expected, double actual) {
        Assertions.assertTrue(Math.abs(expected - actual) < 1e-35f);
    }
}
