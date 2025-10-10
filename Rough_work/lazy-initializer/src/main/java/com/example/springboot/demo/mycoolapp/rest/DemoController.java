package com.example.springboot.demo.mycoolapp.rest;


import com.example.springboot.demo.mycoolapp.common.Coach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    // define private field for the dependency

    private Coach cricketCoach;

    @Autowired
    public DemoController(@Qualifier("cricketCoach") Coach theCricketCoach) {

        System.out.println("CricketCoach constructor" + getClass().getSimpleName());

        this.cricketCoach = theCricketCoach;
    }
    @GetMapping("/dailyworkout")
    public String getDailyWorkout() {
        return cricketCoach.getDailyWorkout();
    }
}
