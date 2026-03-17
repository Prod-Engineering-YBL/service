package ro.unibuc.prodeng.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.ReportEntity;



public interface ReportRepository extends MongoRepository<ReportEntity, String> {
    
    List<ReportEntity> findByAssignedUserId(String userId);

}
