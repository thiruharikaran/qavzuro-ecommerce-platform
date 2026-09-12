package com.qavzuro.repository;

import com.qavzuro.domain.WorkforceTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkforceTaskRepository extends MongoRepository<WorkforceTask, String> {
    Page<WorkforceTask> findByAssignedToUserId(String userId, Pageable pageable);
    Page<WorkforceTask> findByAssignedByUserId(String userId, Pageable pageable);
}
