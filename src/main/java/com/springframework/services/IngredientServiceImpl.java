package com.springframework.services;

import com.springframework.commands.IngredientCommand;
import com.springframework.converters.IngredientCommandToIngredient;
import com.springframework.converters.IngredientToIngredientCommand;
import com.springframework.domain.Ingredient;
import com.springframework.domain.Recipe;
import com.springframework.repositories.RecipeRepository;
import com.springframework.repositories.reactive.RecipeReactiveRepository;
import com.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeReactiveRepository recipeReactiveRepository;
    private final UnitOfMeasureReactiveRepository unitOfMeasureReactiveRepository;
    private final RecipeRepository recipeRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand, IngredientCommandToIngredient ingredientCommandToIngredient, RecipeReactiveRepository recipeReactiveRepository, UnitOfMeasureReactiveRepository unitOfMeasureReactiveRepository, RecipeRepository recipeRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.unitOfMeasureReactiveRepository = unitOfMeasureReactiveRepository;
        this.recipeRepository = recipeRepository;
    }

    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String ingredientId) {


        return recipeReactiveRepository.findById(recipeId)
                .flatMapIterable(Recipe::getIngredients)
                .filter(ingredient -> ingredient.getId().equalsIgnoreCase(ingredientId))
                .single()
                .map(ingredient -> {
                    IngredientCommand ingredientCommand = ingredientToIngredientCommand.convert(ingredient);
                    ingredientCommand.setRecipeId(recipeId);
                    return ingredientCommand;
                });


    }

    @Override
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {


        Optional<Recipe> recipeOptional = recipeRepository.findById(command.getRecipeId());

        if(!recipeOptional.isPresent()){

            //todo toss error if not found!
            log.error("Recipe not found for id: " + command.getRecipeId());
            return Mono.just(new IngredientCommand());
        } else {
            Recipe recipe = recipeOptional.get();

            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(command.getId()))
                    .findFirst();

            if(ingredientOptional.isPresent()){
                Ingredient ingredientFound = ingredientOptional.get();
                ingredientFound.setDescription(command.getDescription());
                ingredientFound.setAmount(command.getAmount());
                ingredientFound.setUom(unitOfMeasureReactiveRepository
                        .findById(command.getUom().getId()).block());

                //        .orElseThrow(() -> new RuntimeException("UOM NOT FOUND"))); //todo address this
                if (ingredientFound.getUom() == null){
                    new RuntimeException("UOM NOT FOUND");
                }
            } else {
                //add new Ingredient
                Ingredient ingredient =  ingredientCommandToIngredient.convert(command);
                recipe.addIngredient(ingredient);
                log.debug("Adding New Ingredient"+" "+"Id is :   "+ingredient.getId()+" "+"Description  :   "+ingredient.getDescription());
            }

            Recipe savedRecipe = recipeReactiveRepository.save(recipe).block();

            Optional<Ingredient> savedIngredientOptional = savedRecipe.getIngredients().stream()
                    .filter(recipeIngredients -> recipeIngredients.getId().equals(command.getId()))
                    .findFirst();

            //check by description
            if(savedIngredientOptional.get().getId() ==null){
                //not totally safe... But best guess
                savedIngredientOptional = savedRecipe.getIngredients().stream()
                        .filter(recipeIngredients -> recipeIngredients.getDescription().equalsIgnoreCase(command.getDescription()))
                        .filter(recipeIngredients -> recipeIngredients.getAmount().equals(command.getAmount()))
                        .filter(recipeIngredients -> recipeIngredients.getUom().getId().equalsIgnoreCase(command.getUom().getId()))
                        .findFirst();
                log.debug("checking for the ingredient who's id is not set  "+savedIngredientOptional.get().getId());
            }

            //todo check for fail

            //enhance with id value
            IngredientCommand ingredientCommandSaved = ingredientToIngredientCommand.convert(savedIngredientOptional.get());
            ingredientCommandSaved.setRecipeId(recipe.getId());

            return Mono.just(ingredientCommandSaved);
        }

    }

    @Override
    public Mono<Void> deleteById(String recipeId, String idToDelete) {

        log.debug("Deleting ingredient: " + recipeId + ":" + idToDelete);

        Recipe recipe = recipeRepository.findById(recipeId).get();

        if(recipe!=null){
            log.debug("found recipe");

            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(idToDelete))
                    .findFirst();

            if(ingredientOptional.isPresent()){
                log.debug("found Ingredient");
                recipe.getIngredients().remove(ingredientOptional.get());
                recipeRepository.save(recipe);
            }
        } else {
            log.debug("Recipe Id Not found. Id:" + recipeId);
        }
        return Mono.empty();
    }
}
