package org.treetops.core.validation;

import io.github.horoc.treetops.core.predictor.Predictor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/18
 */
public class DiabetesModelTest extends LoadModelTemplate {

    @Override
    protected double[] getFeature() {
        return new double[] {0.41865135341839177, 1.0, 2.2034765108899994, 1.4731908880433968, -0.7561792976005409, -0.5608918117369689, -0.5254405155610098,
            -0.054499187536269665, 0.07803482757967586, 0.8481708171566219};
    }

    @Test
    public void testPredictByGeneratedClass() {
        try {
            Predictor predictor = loadModel("diabetes_model", true);
            double[] result = predictor.predict(getFeature());
            Assertions.assertEquals(258.1874753775234D, result[0]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testSimplePredictor() {
        try {
            Predictor predictor = loadModel("diabetes_model", false);
            double[] result = predictor.predict(getFeature());
            Assertions.assertEquals(258.1874753775234D, result[0]);
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }
}
