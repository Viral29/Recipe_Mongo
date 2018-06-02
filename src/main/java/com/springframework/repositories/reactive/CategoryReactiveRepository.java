package com.springframework.repositories.reactive;

import com.springframework.domain.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import reactor.core.publisher.Mono;

import java.util.Optional;

@EnableReactiveMongoRepositories
public interface CategoryReactiveRepository extends ReactiveMongoRepository<Category,String> {
    Mono<Category> findByDescription(String description);
}
