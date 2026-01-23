package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class UsernameGenerator {

    private final UserRepository userRepository;

    private static final int MAX_ATTEMPTS = 10;
    private final ResourceLoader resourceLoader;

    private String[] animals;
    private String[] adjectives;


    @Transactional
    public String generateUsername() {
        Random rand = new Random();
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String username = createUsername(rand);

            if(!userRepository.existsByUsername(username)) {
                return username;
            }
        }

        //Return random
        int nounIndex = rand.nextInt(animals.length);
        return animals[nounIndex] + rand.nextInt(10000);
    }

    private String createUsername(Random rand) {
        int animalIndex = rand.nextInt(animals.length);
        int adjectiveIndex = rand.nextInt(adjectives.length);
        return adjectives[adjectiveIndex] + " " + animals[animalIndex];
    }

    @PostConstruct
    void loadLists() {
        this.animals = loadFile("animal_names.txt");
        this.adjectives = loadFile("adjectives.txt");
    }

    private String[] loadFile(String fileName) {
        List<String> lines = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resourceLoader.getResource("classpath:" + fileName).getInputStream()))) {
            String line;
            while((line = reader.readLine()) != null) {
                if(!line.isBlank()) {
                    lines.add(line.trim());
                }
            }
        }

        catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + fileName);
        }
        return lines.toArray(new String[0]);
    }


}
