package com.writenbite.bisonfun.api.config;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import com.writenbite.bisonfun.api.types.builder.configurator.VideoContentBasicInfoBuilderConfigurator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

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
        // Collect all candidate keys that are superclasses or superinterfaces of type
        List<Class<?>> candidates = configuratorMap.keySet().stream()
                .filter(key -> key.isAssignableFrom(type))
                .toList();

        // Calculate the distance from clazz to each candidate
        Map<Class<?>, Integer> distances = new HashMap<>();
        for (Class<?> candidate : candidates) {
            int distance = getDistance(type, candidate);
            distances.put(candidate, distance);
        }

        // Find the most specific candidate
        Optional<Class<?>> mostSpecific = candidates.stream()
                .min(Comparator.comparingInt(distances::get));

        // Retrieve the configurator for the most specific candidate
        return mostSpecific.map(key -> (VideoContentBasicInfoBuilderConfigurator<T>) configuratorMap.get(key))
                .orElseThrow(() -> new IllegalArgumentException("Could not find configurator for " + type.getName()));
    }

    private int getDistance(Class<?> clazz, Class<?> candidate) {
        if (candidate.isAssignableFrom(clazz)) {
            if (clazz.equals(candidate)) {
                return 0;
            }
            Queue<Class<?>> queue = new LinkedList<>();
            queue.add(clazz);
            int distance = 0;
            while (!queue.isEmpty()) {
                int size = queue.size();
                for (int i = 0; i < size; i++) {
                    Class<?> current = queue.poll();
                    if(current == null){
                        continue;
                    }
                    if (current.equals(candidate)) {
                        return distance;
                    }
                    if (current.getSuperclass() != null) {
                        queue.add(current.getSuperclass());
                    }
                    queue.addAll(Arrays.asList(current.getInterfaces()));
                }
                distance++;
            }
        }
        return Integer.MAX_VALUE;
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
