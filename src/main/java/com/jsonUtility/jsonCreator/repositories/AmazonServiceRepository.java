package com.jsonUtility.jsonCreator.repositories;

import com.jsonUtility.jsonCreator.model.AmazonService;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by shrey on 3/27/2019.
 */
public interface AmazonServiceRepository extends JpaRepository<AmazonService,Long> {
    AmazonService findByServiceName(String serviceName);
}
