package com.example.digitalWalletDemo.mapping;

import com.example.digitalWalletDemo.dto.userdto.UserResponseDTO;
import com.example.digitalWalletDemo.model.userModel.User;
import com.example.digitalWalletDemo.model.walletModel.Wallet;
import org.springframework.stereotype.Component;

@Component
public class userResponseMapper {

    public UserResponseDTO toUserResponseDTO(User user, Wallet wallet) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                wallet.getId(),
                wallet.getWalletName(),
                wallet.getBalance()
        );
    }
}
