package com.example.ordercreator.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    @Value("${cloud.aws.credentials.access-key:mock-access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:mock-secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static:us-east-1}")
    private String region;

    @Value("${cloud.aws.sns.topic.order-created:arn:aws:sns:us-east-1:000000000000:mock-order-created}")
    private String orderCreatedTopicArn;

    @Bean
    public AmazonSNS amazonSNS() {
        // Check if we're using mock credentials
        if ("mock-access-key".equals(accessKey) || "mock-secret-key".equals(secretKey)) {
            // Return a mock implementation for local development
            return AmazonSNSClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials("mock-access-key", "mock-secret-key")))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                            "http://localhost:4566", region))  // LocalStack endpoint
                    .build();
        }
        
        // Use real AWS credentials
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonSNSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
}
