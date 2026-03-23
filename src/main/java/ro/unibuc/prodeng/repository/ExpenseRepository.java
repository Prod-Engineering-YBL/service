package ro.unibuc.prodeng.repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ro.unibuc.prodeng.model.ExpenseEntity;

@Repository
public interface ExpenseRepository extends MongoRepository<ExpenseEntity, String> {
    List<ExpenseEntity> findByAssignedUserId(String userId);
    List<ExpenseEntity> findByAssignedUserIdAndAssignedCategoryId(String userId, String categoryId);     
    List<ExpenseEntity> findByAssignedCategoryId(String categoryId);
    List<ExpenseEntity> findByAssignedUserIdAndDateBetween(String userId, LocalDateTime start, LocalDateTime end);

}
