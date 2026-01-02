package com.chatapp.WebSoketService.configration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KafkaRebalanceListenerService implements ConsumerRebalanceListener {
    @Value("${server.id}")
    private String serverId;
    @Autowired
    private ReactiveRedisTemplate<String,Object> redisTemplate;

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        System.out.println("Partitions revoked: " + partitions);
        // Add custom logic for partition revocation
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        System.out.println("Partitions assigned: " + partitions);
        List<String> pertkey = partitions.stream().map(TopicPartition::toString).toList();
        redisTemplate.opsForValue().set(serverId,pertkey)
                .doOnSuccess(res -> log.info("Partitions uploaded to Redis successfully.") )
                .doOnError(e-> log.error("Error uploading partitions to Redis: " + e.getMessage()))
                .subscribe();

    }
}
