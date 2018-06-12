package com.springframework.commands;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class IngredientCommand {
    private String id=UUID.randomUUID().toString();
    private String recipeId;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    @Max(20)
    private BigDecimal amount;

    private UnitOfMeasureCommand uom;
}
