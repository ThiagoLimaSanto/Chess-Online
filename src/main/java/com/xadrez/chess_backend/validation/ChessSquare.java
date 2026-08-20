package com.xadrez.chess_backend.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {})
@NotBlank
@Pattern(regexp = "^[a-h][1-8]$")
@ReportAsSingleViolation
@Target(FIELD)
@Retention(RUNTIME)
public @interface ChessSquare {

    String message() default "A casa deve estar no formato válido, como e2";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}