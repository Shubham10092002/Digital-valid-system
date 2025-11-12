package com.example.digitalWalletDemo.validation.walletValidation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SufficientBalanceValidator.class)
@Documented
public @interface SufficientBalance {
    String message() default "Insufficient wallet balance for this debit transaction";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
