package de.devland.esperandro.processor;

import de.devland.esperandro.annotations.Default;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author David Kunzler on 18.07.2017.
 */
// TODO add adder/remover type
class PreferenceClassifier {

    private static final String SUFFIX_SEPARATOR = "$";
    private static final String SUFFIX_DEFAULT = SUFFIX_SEPARATOR + "Default";
    private static final String SUFFIX_ADD = SUFFIX_SEPARATOR + "add";
    private static final String SUFFIX_REMOVE = SUFFIX_SEPARATOR + "remove";


    static String preferenceName(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        String preferenceName = methodName;
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            preferenceName = methodName.split("[" + SUFFIX_SEPARATOR + "]")[0]; // first part before separator is preferenceName
        }

        return preferenceName;
    }

    static String preferenceName(Method method) {
        String methodName = method.getName();
        String preferenceName = methodName;
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            preferenceName = methodName.split("[" + SUFFIX_SEPARATOR + "]")[0]; // first part before separator is preferenceName
        }

        return preferenceName;
    }

    @SuppressWarnings("Duplicates") // not a real duplicate since ExecutableElement and Method are incompatible
    static void analyze(ExecutableElement method, PreferenceInformation info) {
        if (!info.hasGetter && isGetter(method)) {
            info.hasGetter = true;
            info.getterElement = method;
            info.defaultAnnotation = method.getAnnotation(Default.class);
        }
        if (!info.hasSetter && isSetter(method)) {
            info.hasSetter = true;
            info.setterElement = method;
        }
        if (!info.hasCommitSetter && isCommitSetter(method)) {
            info.hasCommitSetter = true;
            info.setterElement = method;
        }
        if (!info.hasRuntimeDefaultGetter && isRuntimeDefaultGetter(method)) {
            info.hasRuntimeDefaultGetter = true;
            info.runtimeDefaultGetterElement = method;
        }
        if (!info.hasAdder && isAdder(method)) {
            info.hasAdder = true;
            info.adderElement = method;
        }
        if (!info.hasRemover && isRemover(method)) {
            info.hasRemover = true;
            info.removerElement = method;
        }

        if (info.typeInformation == null) {
            if (isGetter(method) || isRuntimeDefaultGetter(method)) {
                info.typeInformation = typeInformationFromReturnType(method);
            }
            if (isSetter(method) || isCommitSetter(method)) {
                info.typeInformation = typeInformationFromArgumentType(method);
            }
        }
    }

    @SuppressWarnings("Duplicates") // not a real duplicate since ExecutableElement and Method are incompatible
    static void analyze(Method method, PreferenceInformation info) {
        if (!info.hasGetter && isGetter(method)) {
            info.hasGetter = true;
            info.defaultAnnotation = method.getAnnotation(Default.class);
        }
        if (!info.hasSetter) info.hasSetter = isSetter(method);
        if (!info.hasCommitSetter) info.hasCommitSetter = isCommitSetter(method);
        if (!info.hasRuntimeDefaultGetter) info.hasRuntimeDefaultGetter = isRuntimeDefaultGetter(method);
        if (!info.hasAdder) info.hasAdder = isAdder(method);
        if (!info.hasRemover) info.hasRemover = isRemover(method);

        if (info.typeInformation == null) {
            if (isGetter(method) || isRuntimeDefaultGetter(method)) {
                info.typeInformation = typeInformationFromReturnType(method);
            }
            if (isSetter(method) || isCommitSetter(method)) {
                info.typeInformation = typeInformationFromArgumentType(method);
            }
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

    private static boolean isGetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            return false;
        }

        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(returnType);

        boolean hasParameters = parameters != null && parameters.size() > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;

        return !hasParameters && hasValidReturnType;

    }

    private static boolean isGetter(Method method) {
        String methodName = method.getName();
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            return false;
        }

        Class<?>[] parameters = method.getParameterTypes();
        Type returnType = method.getGenericReturnType();
        PreferenceTypeInformation preferenceTypeInformation = PreferenceTypeInformation.from(returnType);

        boolean hasParameters = parameters != null && parameters.length > 0;
        boolean hasValidReturnType = preferenceTypeInformation.getPreferenceType() != PreferenceType.UNKNOWN;

        return !hasParameters && hasValidReturnType;
    }

    private static boolean isRuntimeDefaultGetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (!methodName.endsWith(SUFFIX_DEFAULT)) {
            return false;
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

        return hasValidReturnType && hasRuntimeDefault;
    }

    private static boolean isRuntimeDefaultGetter(Method method) {
        String methodName = method.getName();
        if (!methodName.endsWith(SUFFIX_DEFAULT)) {
            return false;
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

        return hasValidReturnType && hasRuntimeDefault;
    }

    private static boolean isSetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            return false;
        }

        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();

        boolean hasParameter = parameters != null && parameters.size() == 1;
        boolean hasValidReturnType = TypeKind.VOID.equals(returnTypeKind);
        boolean hasValidPreferenceType = hasParameter && PreferenceTypeInformation.from(parameters.get(0).asType()).getPreferenceType() != PreferenceType.UNKNOWN;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !nameEndsWithDefaultSuffix) {
            isPutter = true;
        }
        return isPutter;
    }

    private static boolean isSetter(Method method) {
        String methodName = method.getName();
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            return false;
        }

        boolean isPutter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();

        boolean hasParameter = parameterTypes != null && parameterTypes.length == 1;
        boolean hasValidReturnType = method.getReturnType().toString().equals("void");
        //noinspection SimplifiableConditionalExpression
        boolean hasValidPreferenceType = hasParameter ? PreferenceTypeInformation.from(parameterTypes[0]).getPreferenceType() != PreferenceType.UNKNOWN : false;
        boolean hasRuntimeDefault = false;

        if (hasParameter) {
            Class<?> parameterType = method.getParameterTypes()[0];

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            boolean nameEndsWithDefaultSuffix = method.getName().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);
            if (parameterTypeEqualsReturnType && nameEndsWithDefaultSuffix) {
                hasRuntimeDefault = true;
            }
        }

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !hasRuntimeDefault) {
            isPutter = true;
        }
        return isPutter;
    }

    private static boolean isCommitSetter(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            return false;
        }

        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();

        boolean hasParameter = parameters != null && parameters.size() == 1;
        boolean hasValidReturnType = TypeKind.BOOLEAN.equals(returnTypeKind);
        boolean hasValidPreferenceType = hasParameter && PreferenceTypeInformation.from(parameters.get(0).asType()).getPreferenceType() != PreferenceType.UNKNOWN;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !nameEndsWithDefaultSuffix) {
            isPutter = true;
        }
        return isPutter;
    }

    private static boolean isCommitSetter(Method method) {
        String methodName = method.getName();
        if (methodName.contains(SUFFIX_SEPARATOR)) {
            return false;
        }

        boolean isPutter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();

        boolean hasParameter = parameterTypes != null && parameterTypes.length == 1;
        boolean hasValidReturnType = method.getReturnType().toString().equals("boolean");
        //noinspection SimplifiableConditionalExpression
        boolean hasValidPreferenceType = hasParameter ? PreferenceTypeInformation.from(parameterTypes[0]).getPreferenceType() != PreferenceType.UNKNOWN : false;
        boolean hasRuntimeDefault = false;

        if (hasParameter) {
            Class<?> parameterType = method.getParameterTypes()[0];

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            boolean nameEndsWithDefaultSuffix = method.getName().endsWith(Constants.RUNTIME_DEFAULT_SUFFIX);
            if (parameterTypeEqualsReturnType && nameEndsWithDefaultSuffix) {
                hasRuntimeDefault = true;
            }
        }

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !hasRuntimeDefault) {
            isPutter = true;
        }
        return isPutter;
    }

    private static boolean isAdder(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (!methodName.endsWith(SUFFIX_ADD)) {
            return false;
        }

        // TODO
        // has void return type
        // has one argument
        return true;
    }

    private static boolean isAdder(Method method) {
        String methodName = method.getName();
        if (!methodName.endsWith(SUFFIX_ADD)) {
            return false;
        }

        // TODO
        // has void return type
        // has one argument
        return true;
    }

    private static boolean isRemover(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        if (!methodName.endsWith(SUFFIX_REMOVE)) {
            return false;
        }

        // TODO
        // has void return type
        // has one argument
        return true;
    }

    private static boolean isRemover(Method method) {
        String methodName = method.getName();
        if (!methodName.endsWith(SUFFIX_REMOVE)) {
            return false;
        }

        // TODO
        // has void return type
        // has one argument
        return true;
    }
}
