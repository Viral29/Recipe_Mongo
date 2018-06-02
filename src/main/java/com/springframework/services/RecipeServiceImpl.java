package com.springframework.services;

import com.mongodb.Mongo;
import com.mongodb.async.client.MongoDatabase;
import com.springframework.commands.RecipeCommand;
import com.springframework.converters.RecipeCommandToRecipe;
import com.springframework.converters.RecipeToRecipeCommand;
import com.springframework.domain.Recipe;
import com.springframework.repositories.reactive.RecipeReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 6/13/17.
 */
@Slf4j
//@EnableReactiveMongoRepositories
@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeReactiveRepository recipeReactiveRepository;
    private final RecipeCommandToRecipe recipeCommandToRecipe;
    private final RecipeToRecipeCommand recipeToRecipeCommand;

    public RecipeServiceImpl(RecipeReactiveRepository recipeReactiveRepository, RecipeCommandToRecipe recipeCommandToRecipe, RecipeToRecipeCommand recipeToRecipeCommand)
    {
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.recipeCommandToRecipe = recipeCommandToRecipe;
        this.recipeToRecipeCommand = recipeToRecipeCommand;
    }

    @Override
    public Flux<Recipe> getRecipes() {
        log.debug("I'm in the service");
       return recipeReactiveRepository.findAll();

    }

    @Override
    public Mono<Recipe> findById(String id) {

        return recipeReactiveRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<RecipeCommand> findCommandById(String id) {

        return Mono.just(recipeToRecipeCommand.convert(findById(id).block()));
    }

    @Override
    @Transactional
    public Mono<RecipeCommand> saveRecipeCommand(RecipeCommand command) {

        log.debug("Adding new recipe");

        //command.setId();

        log.debug("Trying to get ID"+recipeReactiveRepository.save(recipeCommandToRecipe.convert(command)).block().getId());

        Recipe savedRecipe = recipeReactiveRepository.save(recipeCommandToRecipe.convert(command)).block();

        return Mono.just(recipeToRecipeCommand.convert(savedRecipe));
    }

    @Override
    public Mono<Void> deleteById(String idToDelete) {

        recipeReactiveRepository.deleteById(idToDelete).block();
        return Mono.empty();
    }
}
