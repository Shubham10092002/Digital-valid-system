package com.example.digitalWalletDemo.validation.walletValidation;

import com.example.digitalWalletDemo.dto.transactiondto.TransactionRequestDTO;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import com.example.digitalWalletDemo.repository.walletRepository.WalletRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class SufficientBalanceValidator implements ConstraintValidator<SufficientBalance, TransactionRequestDTO> {

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public boolean isValid(TransactionRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true; // other validations handle null
        if (dto.getAmount() == null || dto.getWalletId() == null) return true; // field-level will catch nulls

        String type = dto.getType();
        if (type == null) return true; // field-level validation will catch

        boolean isDebit;
        try {
            isDebit = "DEBIT".equalsIgnoreCase(type.trim());
        } catch (Exception e) {
            return true; // Illegal transaction type will be handled elsewhere
        }

        if (!isDebit) return true; // only check for debits

        Optional<Wallet> opt = walletRepository.findById(dto.getWalletId());
        if (opt.isEmpty()) {
            // If wallet not found we return true -> let controller/service throw proper WalletIdNotFoundException
            // Alternatively, you can add a field error here, but cross-layer exceptions may be cleaner.
            return true;
        }

        Wallet wallet = opt.get();
        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
        return balance.compareTo(dto.getAmount()) >= 0;
    }
}
