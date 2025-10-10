package com.example.springboot.demo.mycoolapp.common;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;


@Component
@Lazy
public class CricketCoach implements Coach {

    public CricketCoach() {
        System.out.println("CricketCoach constructor" + getClass().getSimpleName());
    }
    @Override
    public String getDailyWorkout() {
        return "Cricket Coach";
    }
}
