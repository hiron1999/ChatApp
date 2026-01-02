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
    @Value(value = "${spring.kafka.consumer.group}")
    private String groupId;
    @Value(value = "${spring.kafka.topic}")
    private String topicKey;
    @Autowired
    private KafkaRebalanceListenerService rebalanceListenerService;



    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chatapp.WebSoketService.Model");

        return new DefaultKafkaConsumerFactory<>(props,new StringDeserializer(),new JsonDeserializer<>(Object.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setConsumerRebalanceListener(rebalanceListenerService);
        return factory;
    }
//    @Bean
//    ConcurrentMessageListenerContainer<String,String> messageListenerContainer(){
//        ContainerProperties containerProperties = new ContainerProperties(topicKey);
//        containerProperties.setGroupId(groupId);
//        containerProperties.setConsumerRebalanceListener(rebalanceListenerService);
//
//        return new ConcurrentMessageListenerContainer<>(consumerFactory(),containerProperties);
//
//    }

}
