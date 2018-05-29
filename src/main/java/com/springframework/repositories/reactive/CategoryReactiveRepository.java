package com.springframework.repositories.reactive;

import com.springframework.domain.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryReactiveRepository extends ReactiveMongoRepository<Category,String> {
}
