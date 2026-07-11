package com.chatapp.WebSoketService.configration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KafkaRebalanceListenerService implements ConsumerRebalanceListener {
    @Value("${server.id}")
    private String serverId;
    @Autowired
    @Qualifier("reactiveRedisTemplate")
    private ReactiveRedisTemplate<String,Object> redisTemplate;

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        log.info("Partitions revoked: {}", partitions);

        // clean up Redis when partitions revoked
        redisTemplate.opsForHash()
                .delete(serverId)
                .doOnSuccess(res -> log.info("Partitions removed from Redis"))
                .doOnError(e -> log.error("Error removing partitions: {}", e.getMessage()))
                .subscribe();
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        log.info("Partitions assigned: {}", partitions);

        // group by topic ✅
        Map<String, List<String>> byTopic = partitions.stream()
                .collect(Collectors.groupingBy(
                        TopicPartition::topic,
                        Collectors.mapping(
                                p -> String.valueOf(p.partition()),
                                Collectors.toList()
                        )
                ));

        // store each topic's partitions separately
        byTopic.forEach((topic, topicPartitions) -> {
            String partitionsAsString = String.join(",", topicPartitions);
            redisTemplate.opsForHash()
                    .put(serverId, topic, partitionsAsString)
                    .doOnSuccess(res -> log.info("Topic {} partitions uploaded: {}", topic, topicPartitions))
                    .doOnError(e -> log.error("Error uploading partitions: {}", e.getMessage()))
                    .subscribe();
        });
    }
}
