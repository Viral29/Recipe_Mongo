package com.springframework.services;

import com.springframework.commands.RecipeCommand;
import com.springframework.domain.Recipe;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 6/13/17.
 */
public interface RecipeService {

    Flux<Recipe> getRecipes();

    Mono<Recipe> findById(String id);

    Mono<RecipeCommand> findCommandById(String id);

    Mono<RecipeCommand> saveRecipeCommand(RecipeCommand command);

    Mono<Void> deleteById(String idToDelete);
}
