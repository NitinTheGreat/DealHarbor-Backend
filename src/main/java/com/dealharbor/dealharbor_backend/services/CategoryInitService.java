package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.entities.Category;
import com.dealharbor.dealharbor_backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Order(1) // Run before DatabaseInitService
public class CategoryInitService implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            initializeCategories();
            System.out.println("✅ Categories initialized successfully!");
        }
    }

    private void initializeCategories() {
        // MAIN CATEGORIES
        
        // 1. ELECTRONICS - Laptops, phones, gadgets, gaming
        Category electronics = createCategory("electronics", "Electronics", 
            "Laptops, phones, gadgets, gaming equipment", null, "/icons/electronics.png", 1);
        
        // 2. BOOKS & STUDY - Textbooks, notes, study materials
        Category books = createCategory("books-study", "Books & Study Materials", 
            "Textbooks, notes, study guides, academic materials", null, "/icons/books.png", 2);
        
        // 3. FASHION - Clothing, shoes, accessories
        Category fashion = createCategory("fashion", "Fashion & Clothing", 
            "Clothes, shoes, bags, accessories", null, "/icons/fashion.png", 3);
        
        // 4. FURNITURE - Dorm furniture, study desks, chairs
        Category furniture = createCategory("furniture", "Furniture & Home", 
            "Dorm furniture, study desks, chairs, home decor", null, "/icons/furniture.png", 4);
        
        // 5. SPORTS - Sports equipment, fitness, outdoor
        Category sports = createCategory("sports", "Sports & Fitness", 
            "Sports equipment, fitness gear, outdoor activities", null, "/icons/sports.png", 5);
        
        // 6. VEHICLES - Bikes, scooters, cars
        Category vehicles = createCategory("vehicles", "Vehicles & Transport", 
            "Bikes, scooters, cars, transport", null, "/icons/vehicles.png", 6);
        
        // 7. SERVICES - Tutoring, repairs, other services
        Category services = createCategory("services", "Services", 
            "Tutoring, repairs, freelance services", null, "/icons/services.png", 7);
        
        // 8. OTHERS - Everything else that doesn't fit above categories
        Category others = createCategory("others", "Others", 
            "Everything else not covered in other categories", null, "/icons/others.png", 8);

        // ELECTRONICS SUBCATEGORIES
        createCategory("laptops-computers", "Laptops & Computers", 
            "Laptops, desktops, computer accessories", electronics.getId(), null, 1);
        createCategory("smartphones", "Smartphones", 
            "Mobile phones and accessories", electronics.getId(), null, 2);
        createCategory("audio-video", "Audio & Video", 
            "Headphones, speakers, cameras, audio equipment", electronics.getId(), null, 3);
        createCategory("gaming", "Gaming", 
            "Gaming consoles, games, gaming accessories", electronics.getId(), null, 4);
        createCategory("tablets-ereaders", "Tablets & E-readers", 
            "Tablets, e-readers, digital devices", electronics.getId(), null, 5);
        createCategory("electronics-accessories", "Electronics Accessories", 
            "Chargers, cables, cases, other accessories", electronics.getId(), null, 6);

        // BOOKS & STUDY SUBCATEGORIES
        createCategory("computer-science", "Computer Science", 
            "CS textbooks, programming books, tech materials", books.getId(), null, 1);
        createCategory("engineering", "Engineering", 
            "Engineering textbooks and materials", books.getId(), null, 2);
        createCategory("business-management", "Business & Management", 
            "Business, MBA, management books", books.getId(), null, 3);
        createCategory("literature-languages", "Literature & Languages", 
            "Literature, language learning, humanities", books.getId(), null, 4);
        createCategory("science-math", "Science & Mathematics", 
            "Science, math, research materials", books.getId(), null, 5);
        createCategory("notes-guides", "Notes & Study Guides", 
            "Student notes, study guides, exam materials", books.getId(), null, 6);

        // FASHION SUBCATEGORIES
        createCategory("mens-clothing", "Men's Clothing", 
            "Shirts, pants, formal wear for men", fashion.getId(), null, 1);
        createCategory("womens-clothing", "Women's Clothing", 
            "Dresses, tops, formal wear for women", fashion.getId(), null, 2);
        createCategory("shoes", "Shoes", 
            "Formal shoes, sneakers, sandals", fashion.getId(), null, 3);
        createCategory("bags-accessories", "Bags & Accessories", 
            "Backpacks, handbags, wallets, accessories", fashion.getId(), null, 4);
        createCategory("watches-jewelry", "Watches & Jewelry", 
            "Watches, jewelry, fashion accessories", fashion.getId(), null, 5);

        // FURNITURE SUBCATEGORIES
        createCategory("study-furniture", "Study Furniture", 
            "Study desks, chairs, bookshelves", furniture.getId(), null, 1);
        createCategory("bedroom-furniture", "Bedroom Furniture", 
            "Beds, mattresses, wardrobes", furniture.getId(), null, 2);
        createCategory("storage-organization", "Storage & Organization", 
            "Storage boxes, organizers, shelves", furniture.getId(), null, 3);
        createCategory("home-decor", "Home Decor", 
            "Decorative items, plants, lighting", furniture.getId(), null, 4);

        // SPORTS SUBCATEGORIES
        createCategory("fitness-equipment", "Fitness Equipment", 
            "Gym equipment, weights, fitness accessories", sports.getId(), null, 1);
        createCategory("outdoor-sports", "Outdoor Sports", 
            "Cricket, football, tennis, outdoor games", sports.getId(), null, 2);
        createCategory("indoor-games", "Indoor Games", 
            "Board games, chess, indoor entertainment", sports.getId(), null, 3);
        createCategory("sports-apparel", "Sports Apparel", 
            "Sports clothing, shoes, gear", sports.getId(), null, 4);

        // VEHICLES SUBCATEGORIES
        createCategory("bicycles", "Bicycles", 
            "Bikes, cycling accessories", vehicles.getId(), null, 1);
        createCategory("scooters-motorcycles", "Scooters & Motorcycles", 
            "Two-wheelers, scooters, motorcycles", vehicles.getId(), null, 2);
        createCategory("cars", "Cars", 
            "Used cars, car accessories", vehicles.getId(), null, 3);
        createCategory("vehicle-accessories", "Vehicle Accessories", 
            "Helmets, vehicle parts, accessories", vehicles.getId(), null, 4);

        // SERVICES SUBCATEGORIES
        createCategory("tutoring", "Tutoring & Education", 
            "Academic tutoring, skill training", services.getId(), null, 1);
        createCategory("repair-services", "Repair Services", 
            "Electronics repair, maintenance services", services.getId(), null, 2);
        createCategory("freelance-services", "Freelance Services", 
            "Design, programming, content creation", services.getId(), null, 3);
        createCategory("event-services", "Event Services", 
            "Photography, event planning, entertainment", services.getId(), null, 4);
    }

    private Category createCategory(String id, String name, String description, 
                                  String parentId, String iconUrl, int sortOrder) {
        Category category = Category.builder()
                .id(id)
                .name(name)
                .description(description)
                .parentId(parentId)
                .iconUrl(iconUrl)
                .isActive(true)
                .sortOrder(sortOrder)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        return categoryRepository.save(category);
    }
}
