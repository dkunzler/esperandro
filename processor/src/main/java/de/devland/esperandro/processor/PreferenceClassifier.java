package de.devland.esperandro.processor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import de.devland.esperandro.annotations.Default;

/**
 * @author David Kunzler on 18.07.2017.
 */
// TODO add adder/remover type
class PreferenceClassifier {

    static String preferenceName(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        String preferenceName = methodName;
        if (methodName.contains(Constants.SUFFIX_SEPARATOR)) {
            preferenceName = methodName.split("[" + Constants.SUFFIX_SEPARATOR + "]")[0]; // first part before separator is preferenceName
        }

        return preferenceName;
    }

    static String preferenceName(Method method) {
        String methodName = method.getName();
        String preferenceName = methodName;
        if (methodName.contains(Constants.SUFFIX_SEPARATOR)) {
            preferenceName = methodName.split("[" + Constants.SUFFIX_SEPARATOR + "]")[0]; // first part before separator is preferenceName
        }

        return preferenceName;
    }

    @SuppressWarnings("Duplicates") // not a real duplicate since ExecutableElement and Method are incompatible
    static void analyze(ExecutableElement method, PreferenceInformation info) {
        if (info.getter == null) {
            info.getter = getGetter(method);
        }
        if (info.setter == null) {
            info.setter = getSetter(method);
        }
        if (info.commitSetter == null) {
            info.commitSetter = getCommitSetter(method);
        }
        if (info.runtimeDefaultGetter == null) {
            info.runtimeDefaultGetter = getRuntimeDefaultGetter(method);
        }
        if (info.adder == null) {
            info.adder = getAdder(method);
        }
        if (info.remover == null) {
            info.remover = getRemover(method);
        }
        if (info.contains == null) {
            info.contains = getContains(method);
        }
    }

    @SuppressWarnings("Duplicates") // not a real duplicate since ExecutableElement and Method are incompatible
    static void analyze(Method method, PreferenceInformation info) {
        if (info.getter == null) {
            info.getter = getGetter(method);
        }
        if (info.setter == null) {
            info.setter = getSetter(method);
        }
        if (info.commitSetter == null) {
            info.commitSetter = getCommitSetter(method);
        }
        if (info.runtimeDefaultGetter == null) {
            info.runtimeDefaultGetter = getRuntimeDefaultGetter(method);
        }
        if (info.adder == null) {
            info.adder = getAdder(method);
        }
        if (info.remover == null) {
            info.remover = getRemover(method);
        }
        if (info.contains == null) {
            info.contains = getContains(method);
        }
    }


    private static PreferenceTypeInformation typeInformationFromReturnType(Method method) {
        return PreferenceTypeInformation.from(method.getGenericReturnType());
    }

    private static PreferenceTypeInformation typeInformationFromArgumentType(Method method) {
        Type parameterType = method.getGenericParameterTypes()[0];
        return PreferenceTypeInformation.from(parameterType);
    }

    private static PreferenceTypeInformation typeInformationFromReturnType(ExecutableElement method) {
        TypeMirror returnType = method.getReturnType();
        return PreferenceTypeInformation.from(returnType);
    }

    private static PreferenceTypeInformation typeInformationFromArgumentType(ExecutableElement method) {
        TypeMirror parameterType = method.getParameters().get(0).asType();
        return PreferenceTypeInformation.from(parameterType);
    }

    private static MethodInformation getGetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (methodName.contains(Constants.SUFFIX_SEPARATOR)) {
            return null;
        }

        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(returnType);

        boolean hasParameters = parameters != null && parameters.size() > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;

        if (!hasParameters && hasValidReturnType) {
            return new MethodInformation(method.getAnnotation(Default.class),
                    method,
                    null,
                    PreferenceTypeInformation.from(returnType),
                    null
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getGetter(Method method) {
        String methodName = method.getName();
        if (methodName.contains(Constants.SUFFIX_SEPARATOR)) {
            return null;
        }

        Class<?>[] parameters = method.getParameterTypes();
        Type returnType = method.getGenericReturnType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(returnType);

        boolean hasParameters = parameters != null && parameters.length > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;

        if (!hasParameters && hasValidReturnType) {
            return new MethodInformation(method.getAnnotation(Default.class),
                    null,
                    method,
                    PreferenceTypeInformation.from(returnType),
                    null
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getRuntimeDefaultGetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (!methodName.endsWith(Constants.SUFFIX_DEFAULT)) {
            return null;
        }

        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(returnType);

        boolean hasParameters = parameters != null && parameters.size() > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;
        boolean hasRuntimeDefault = false;

        if (hasParameters && parameters.size() == 1) { // getter with default can have at most 1 parameter
            VariableElement parameter = parameters.get(0);
            TypeMirror parameterType = parameter.asType();

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            if (parameterTypeEqualsReturnType) {
                hasRuntimeDefault = true;
            }
        }

        if (hasValidReturnType && hasRuntimeDefault) {
            return new MethodInformation(method.getAnnotation(Default.class),
                    method,
                    null,
                    PreferenceTypeInformation.from(returnType),
                    PreferenceTypeInformation.from(parameters.get(0).asType())
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getRuntimeDefaultGetter(Method method) {
        String methodName = method.getName();
        if (!methodName.endsWith(Constants.SUFFIX_DEFAULT)) {
            return null;
        }

        Class<?>[] parameters = method.getParameterTypes();
        Type returnType = method.getGenericReturnType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(returnType);

        boolean hasParameters = parameters != null && parameters.length > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;
        boolean hasRuntimeDefault = false;

        if (hasParameters && parameters.length == 1) { // getter with default can have at most 1 parameter
            Class<?> parameterType = parameters[0];

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            if (parameterTypeEqualsReturnType) {
                hasRuntimeDefault = true;
            }
        }

        if (hasValidReturnType && hasRuntimeDefault) {
            return new MethodInformation(method.getAnnotation(Default.class),
                    null,
                    method,
                    PreferenceTypeInformation.from(returnType),
                    PreferenceTypeInformation.from(parameters[0])
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getSetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        return !methodName.contains(Constants.SUFFIX_SEPARATOR) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getSetter(Method method) {
        String methodName = method.getName();
        return !methodName.contains(Constants.SUFFIX_SEPARATOR) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getCommitSetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (methodName.contains(Constants.SUFFIX_SEPARATOR)) {
            return null;
        }

        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();

        boolean hasParameter = parameters != null && parameters.size() == 1;
        boolean hasValidReturnType = TypeKind.BOOLEAN.equals(returnTypeKind);
        boolean hasValidPreferenceType = hasParameter && PreferenceTypeInformation.from(parameters.get(0).asType()).getPreferenceType() != PreferenceType.UNKNOWN;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Constants.SUFFIX_DEFAULT);

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !nameEndsWithDefaultSuffix) {
            isPutter = true;
        }
        if (isPutter) {
            return new MethodInformation(
                    method.getAnnotation(Default.class),
                    method,
                    null,
                    PreferenceTypeInformation.from(boolean.class),
                    PreferenceTypeInformation.from(parameters.get(0).asType())
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getCommitSetter(Method method) {
        String methodName = method.getName();
        if (methodName.contains(Constants.SUFFIX_SEPARATOR)) {
            return null;
        }

        boolean isPutter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();

        boolean hasParameter = parameterTypes != null && parameterTypes.length == 1;
        boolean hasValidReturnType = method.getReturnType().toString().equals("boolean");
        //noinspection SimplifiableConditionalExpression
        boolean hasValidPreferenceType = hasParameter ? PreferenceTypeInformation.from(parameterTypes[0]).getPreferenceType() != PreferenceType.UNKNOWN : false;

        if (hasParameter && hasValidReturnType && hasValidPreferenceType) {
            isPutter = true;
        }
        if (isPutter) {
            return new MethodInformation(
                    method.getAnnotation(Default.class),
                    null,
                    method,
                    PreferenceTypeInformation.from(boolean.class),
                    PreferenceTypeInformation.from(parameterTypes[0])
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getAdder(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        return methodName.endsWith(Constants.SUFFIX_ADD) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getAdder(Method method) {
        String methodName = method.getName();
        return methodName.endsWith(Constants.SUFFIX_ADD) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getRemover(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        return methodName.endsWith(Constants.SUFFIX_REMOVE) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getRemover(Method method) {
        String methodName = method.getName();
        return methodName.endsWith(Constants.SUFFIX_REMOVE) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getContains(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        return methodName.endsWith(Constants.SUFFIX_CONTAINS) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getContains(Method method) {
        String methodName = method.getName();
        return methodName.endsWith(Constants.SUFFIX_CONTAINS) ? getVoidWithOneParameter(method) : null;
    }

    private static MethodInformation getVoidWithOneParameter(ExecutableElement method) {
        boolean isVoidWithOneParameter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();

        boolean hasParameter = parameters != null && parameters.size() == 1;
        boolean hasValidReturnType = TypeKind.VOID.equals(returnTypeKind);
        boolean hasValidPreferenceType = hasParameter && PreferenceTypeInformation.from(parameters.get(0).asType()).getPreferenceType() != PreferenceType.UNKNOWN;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Constants.SUFFIX_DEFAULT);

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !nameEndsWithDefaultSuffix) {
            isVoidWithOneParameter = true;
        }
        if (isVoidWithOneParameter) {
            return new MethodInformation(
                    method.getAnnotation(Default.class),
                    method,
                    null,
                    null,
                    PreferenceTypeInformation.from(parameters.get(0).asType())
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getVoidWithOneParameter(Method method) {
        boolean isVoidWithOneParameter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();

        boolean hasParameter = parameterTypes != null && parameterTypes.length == 1;
        boolean hasValidReturnType = method.getReturnType().toString().equals("void");
        //noinspection SimplifiableConditionalExpression
        boolean hasValidPreferenceType = hasParameter ? PreferenceTypeInformation.from(parameterTypes[0]).getPreferenceType() != PreferenceType.UNKNOWN : false;

        if (hasParameter && hasValidReturnType && hasValidPreferenceType) {
            isVoidWithOneParameter = true;
        }
        if (isVoidWithOneParameter) {
            return new MethodInformation(
                    method.getAnnotation(Default.class),
                    null,
                    method,
                    null,
                    PreferenceTypeInformation.from(parameterTypes[0])
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getBooleanWithOneParameter(ExecutableElement method) {
        boolean isVoidWithOneParameter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();

        boolean hasParameter = parameters != null && parameters.size() == 1;
        boolean hasValidReturnType = TypeKind.BOOLEAN.equals(returnTypeKind);
        boolean hasValidPreferenceType = hasParameter && PreferenceTypeInformation.from(parameters.get(0).asType()).getPreferenceType() != PreferenceType.UNKNOWN;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Constants.SUFFIX_DEFAULT);

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !nameEndsWithDefaultSuffix) {
            isVoidWithOneParameter = true;
        }
        if (isVoidWithOneParameter) {
            return new MethodInformation(
                    method.getAnnotation(Default.class),
                    method,
                    null,
                    null,
                    PreferenceTypeInformation.from(parameters.get(0).asType())
            );
        } else {
            return null;
        }
    }

    private static MethodInformation getBooleanWithOneParameter(Method method) {
        boolean isVoidWithOneParameter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();

        boolean hasParameter = parameterTypes != null && parameterTypes.length == 1;
        boolean hasValidReturnType = method.getReturnType().toString().equals("boolean");
        //noinspection SimplifiableConditionalExpression
        boolean hasValidPreferenceType = hasParameter ? PreferenceTypeInformation.from(parameterTypes[0]).getPreferenceType() != PreferenceType.UNKNOWN : false;

        if (hasParameter && hasValidReturnType && hasValidPreferenceType) {
            isVoidWithOneParameter = true;
        }
        if (isVoidWithOneParameter) {
            return new MethodInformation(
                    method.getAnnotation(Default.class),
                    null,
                    method,
                    null,
                    PreferenceTypeInformation.from(parameterTypes[0])
            );
        } else {
            return null;
        }
    }
}
