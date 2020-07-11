package de.devland.esperandro.base.analysis;

import de.devland.esperandro.annotations.Get;
import de.devland.esperandro.annotations.Put;
import de.devland.esperandro.base.MethodAnalyzer;
import de.devland.esperandro.base.preferences.MethodInformation;
import de.devland.esperandro.base.preferences.MethodOperation;
import de.devland.esperandro.base.preferences.TypeInformation;

public class AnnotationMethodAnalyzer implements MethodAnalyzer {
    @Override
    public boolean isApplicableMethod(MethodInformation method) {
        boolean hasGetAnnotation = method.getAnnotation(Get.class) != null;
        boolean hasPutAnnotation = method.getAnnotation(Put.class) != null;

        return hasGetAnnotation || hasPutAnnotation;
    }

    @Override
    public String getPreferenceName(MethodInformation method) {
        Get get = method.getAnnotation(Get.class);
        Put put = method.getAnnotation(Put.class);

        if (get != null) {
            return get.value();
        } else if (put != null) {
            return put.value();
        } else {
            return null;
        }
    }

    @Override
    public MethodOperation getMethodOperation(MethodInformation method) {
        Get get = method.getAnnotation(Get.class);
        Put put = method.getAnnotation(Put.class);

        if (get != null) {
            return MethodOperation.GET;
        } else if (put != null) {
            return MethodOperation.PUT;
        } else {
            return MethodOperation.UNKNOWN;
        }
    }

    @Override
    public TypeInformation getPreferenceType(MethodInformation method) {
        return null;
    }
}
