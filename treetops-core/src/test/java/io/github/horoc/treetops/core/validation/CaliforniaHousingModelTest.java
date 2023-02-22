package io.github.horoc.treetops.core.validation;

import io.github.horoc.treetops.core.predictor.Predictor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/18
 */
public class CaliforniaHousingModelTest extends LoadModelTemplate {

    @Override
    protected double[] getFeature() {
        return new double[] {0.14798009991170735, -0.5275608347965259, 0.09460886182134916, -0.04474251825448332,
            0.11084370442811828, 0.10687073221002079, -1.428840535575722, 1.2576618924353786};
    }

    @Test
    public void testPredictByGeneratedClass() {
        try {
            Predictor predictor = loadModel("california_housing_model", true);
            double[] result = predictor.predict(getFeature());
            Assertions.assertEquals(1.6224174281069879D, result[0]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testSimplePredictor() {
        try {
            Predictor predictor = loadModel("california_housing_model", false);
            double[] result = predictor.predict(getFeature());
            Assertions.assertEquals(1.6224174281069879D, result[0]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }
}
