package ro.unibuc.prodeng.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.unibuc.prodeng.model.BudgetEntity;

public interface BudgetRepository extends MongoRepository<BudgetEntity, String> {
    List<BudgetEntity> findByAssignedUserId(String assignedUserId);
}
