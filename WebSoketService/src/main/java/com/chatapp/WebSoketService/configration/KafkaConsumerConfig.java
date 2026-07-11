package com.chatapp.WebSoketService.configration;

import com.chatapp.WebSoketService.Model.Message;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    @Value(value = "${spring.kafka.consumer.group.user}")
    private String private_msg_consume;
    @Value(value = "${spring.kafka.consumer.group.room}")
    private String group_msg_consume;

    @Value(value = "${spring.kafka.topic.private}")
    private String topicKey;
    @Autowired
    private KafkaRebalanceListenerService rebalanceListenerService;


// ── Shared base config ──────────────────────────────────

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chatapp.WebSoketService.Model");
//        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }

    private JsonDeserializer<Object> jsonDeserializer() {
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(Object.class);
        deserializer.addTrustedPackages("com.chatapp.WebSoketService.Model");
//        deserializer.setUseTypeHeaders(false);
        return deserializer;
    }

    // ── Private message consumer ────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> privateConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, private_msg_consume);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(privateConsumerFactory());
        factory.getContainerProperties().setConsumerRebalanceListener(rebalanceListenerService);
        return factory;
    }

    // ── Group message consumer ──────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> groupConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group_msg_consume);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> groupKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(groupConsumerFactory());
        factory.getContainerProperties().setConsumerRebalanceListener(rebalanceListenerService);
        return factory;
    }

}
