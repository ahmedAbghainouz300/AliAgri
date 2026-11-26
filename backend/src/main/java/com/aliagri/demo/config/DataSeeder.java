package com.aliagri.demo.config;

import com.aliagri.demo.model.Category;
import com.aliagri.demo.model.Unit;
import com.aliagri.demo.repository.CategoryRepository;
import com.aliagri.demo.repository.UnitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepo, UnitRepository unitRepo) {
        return args -> {
            if (categoryRepo.count() == 0) {
                categoryRepo.saveAll(List.of(
                        new Category("Pesticide", "Produits de protection des cultures"),
                        new Category("Fertilizer", "Engrais et amendements"),
                        new Category("Tool", "Outils agricoles et équipements")));
            }
            if (unitRepo.count() == 0) {
                unitRepo.saveAll(List.of(
                        new Unit("kg"),
                        new Unit("L"),
                        new Unit("unité")));
            }
        };
    }
}