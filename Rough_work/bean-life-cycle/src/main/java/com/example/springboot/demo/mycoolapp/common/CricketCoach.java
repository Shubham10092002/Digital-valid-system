package com.example.springboot.demo.mycoolapp.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;


@Component

public class CricketCoach implements Coach {

    public CricketCoach() {
        System.out.println("CricketCoach constructor: " + getClass().getSimpleName());
    }

    @PostConstruct
    public void init() {
        System.out.println("CricketCoach init: " + getClass().getSimpleName());
    }

    @PreDestroy
    public void destroy() {
        System.out.println("CricketCoach destroy: " + getClass().getSimpleName());
    }


    @Override
    public String getDailyWorkout() {
        return "Cricket Coach";
    }
}
