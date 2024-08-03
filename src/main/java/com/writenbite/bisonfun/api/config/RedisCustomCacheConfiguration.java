package com.writenbite.bisonfun.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.function.Supplier;

@EnableCaching
@Configuration
public class RedisCustomCacheConfiguration implements CachingConfigurer {
    @Value("${bisonfun.rate-limit.requests-per-second}")
    private int requestsPerSecond;
    @Value("${bisonfun.rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisCustomCacheConfiguration(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @PostConstruct
    public void flushRedisCache() {
        redisConnectionFactory.getConnection().serverCommands().flushDb();
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                //Trending cache
                .withCacheConfiguration(
                        "animeTrends",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1))
                )
                .withCacheConfiguration(
                        "movieTrends",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1))
                )
                .withCacheConfiguration(
                        "tvTrends",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1))
                )
                //JSON of a video content
                .withCacheConfiguration(
                        "jsonAnime",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                        "jsonMovie",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                        "jsonShow",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30))
                );

    }

    @Bean
    public ProxyManager<String> lettuceBasedProxyManager() {
        LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory;
        RedisClient redisClient = (RedisClient) lettuceConnectionFactory.getNativeClient();
        StatefulRedisConnection<String, byte[]> redisConnection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return LettuceBasedProxyManager.builderFor(redisConnection)
                .withClientSideConfig(
                        ClientSideConfig.getDefault()
                )
                .build();
    }

    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration() {
        return () -> BucketConfiguration.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(requestsPerSecond)
                                .refillIntervally(requestsPerSecond, Duration.ofSeconds(1))
                                .build()
                )
                .addLimit(
                        Bandwidth.builder()
                                .capacity(requestsPerMinute)
                                .refillIntervally(requestsPerMinute, Duration.ofSeconds(30))
                                .build()
                )
                .build();
    }
}
