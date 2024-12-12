package com.writenbite.bisonfun.api.config;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import com.writenbite.bisonfun.api.types.builder.configurator.VideoContentBasicInfoBuilderConfigurator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicInfoConfiguratorRegistry {
    private final Map<Class<?>, VideoContentBasicInfoBuilderConfigurator<? extends RawVideoContent>> configuratorMap = new HashMap<>();

    public BasicInfoConfiguratorRegistry(List<VideoContentBasicInfoBuilderConfigurator<? extends RawVideoContent>> configurators) {
        for (VideoContentBasicInfoBuilderConfigurator<? extends RawVideoContent> configurator : configurators) {
            Class<?> genericType = resolveGenericType(configurator.getClass());
            configuratorMap.put(genericType, configurator);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends RawVideoContent> VideoContentBasicInfoBuilderConfigurator<T> getConfigurator(Class<T> type) {
        return (VideoContentBasicInfoBuilderConfigurator<T>) configuratorMap.get(type);
    }

    private Class<?> resolveGenericType(Class<?> configuratorClass) {

        Class<?> currentClass = configuratorClass;
        while (currentClass != null && !currentClass.equals(VideoContentBasicInfoBuilderConfigurator.class)) {
            Type genericSuperClass = configuratorClass.getGenericSuperclass();

            if (genericSuperClass instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                if (typeArguments.length > 0) {
                    if(typeArguments[0] instanceof Class<?> clazz){
                        return clazz;
                    }else if (typeArguments[0] instanceof TypeVariable){
                        currentClass = currentClass.getSuperclass();
                        continue;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new IllegalArgumentException("Could not resolve generic type for " + configuratorClass);
    }
}
