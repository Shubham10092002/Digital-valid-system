package com.example.springboot.demo.mycoolapp.common;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class TrackCoach implements Coach {

    public TrackCoach() {
        System.out.println("TrackCoach constructor" + getClass().getSimpleName());
    }
    @Override
    public String getDailyWorkout() {
        return "run a hard 5k regularly";
    }
}
