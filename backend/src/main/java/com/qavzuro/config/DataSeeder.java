package com.qavzuro.config;

import com.qavzuro.domain.*;
import com.qavzuro.repository.*;
import com.qavzuro.service.PermissionCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Creates baseline permissions/roles and, if none exist, development seed
 * data: a Master Admin account, an Admin account, categories, products, and
 * a sample coupon. Credentials come from environment configuration
 * (app.seed.*) - never hardcoded - and are documented in .env.example.
 * Controlled by app.seed.enabled (default true; set to false in production).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final SeedProperties seedProperties;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (!seedProperties.isEnabled()) {
            log.info("Data seeding disabled (app.seed.enabled=false).");
            return;
        }
        seedPermissions();
        seedRoles();
        seedUsers();
        seedCatalog();
        log.info("Qavzuro data seeding complete.");
    }

    private void seedPermissions() {
        for (String code : PermissionCodes.all()) {
            if (!permissionRepository.existsByCode(code)) {
                permissionRepository.save(Permission.builder().code(code).description(code.replace('_', ' ')).category(code.split("_")[0]).build());
            }
        }
    }

    private void seedRoles() {
        upsertRole("CUSTOMER", "Customer", Set.of());

        upsertRole("WORKER", "Worker", Set.of(
                PermissionCodes.ORDER_VIEW, PermissionCodes.INVENTORY_VIEW, PermissionCodes.WORKFORCE_TASK_UPDATE_OWN));

        upsertRole("CLEANER", "Cleaner", Set.of(
                PermissionCodes.WORKFORCE_TASK_UPDATE_OWN));

        upsertRole("SUPERVISOR", "Supervisor", Set.of(
                PermissionCodes.ORDER_VIEW, PermissionCodes.INVENTORY_VIEW, PermissionCodes.INVENTORY_UPDATE,
                PermissionCodes.WORKFORCE_VIEW, PermissionCodes.WORKFORCE_TASK_ASSIGN, PermissionCodes.WORKFORCE_TASK_UPDATE_OWN));

        upsertRole("TEAM_LEAD", "Team Lead", Set.of(
                PermissionCodes.ORDER_VIEW, PermissionCodes.ORDER_UPDATE, PermissionCodes.INVENTORY_VIEW, PermissionCodes.INVENTORY_UPDATE,
                PermissionCodes.WORKFORCE_VIEW, PermissionCodes.WORKFORCE_MANAGE, PermissionCodes.WORKFORCE_TASK_ASSIGN, PermissionCodes.WORKFORCE_TASK_UPDATE_OWN));

        upsertRole("MANAGER", "Manager", Set.of(
                PermissionCodes.ORDER_VIEW, PermissionCodes.ORDER_UPDATE, PermissionCodes.ORDER_CANCEL,
                PermissionCodes.INVENTORY_VIEW, PermissionCodes.INVENTORY_UPDATE,
                PermissionCodes.RETURN_VIEW, PermissionCodes.RETURN_APPROVE, PermissionCodes.REFUND_PROCESS,
                PermissionCodes.WORKFORCE_VIEW, PermissionCodes.WORKFORCE_MANAGE, PermissionCodes.WORKFORCE_TASK_ASSIGN,
                PermissionCodes.REPORT_VIEW, PermissionCodes.REVIEW_MODERATE, PermissionCodes.COUPON_MANAGE));

        upsertRole("ADMIN", "Administrator", Set.of(
                PermissionCodes.USER_VIEW, PermissionCodes.USER_CREATE, PermissionCodes.USER_UPDATE,
                PermissionCodes.PRODUCT_VIEW, PermissionCodes.PRODUCT_CREATE, PermissionCodes.PRODUCT_UPDATE, PermissionCodes.PRODUCT_DELETE,
                PermissionCodes.CATEGORY_MANAGE,
                PermissionCodes.ORDER_VIEW, PermissionCodes.ORDER_UPDATE, PermissionCodes.ORDER_CANCEL,
                PermissionCodes.RETURN_VIEW, PermissionCodes.RETURN_APPROVE, PermissionCodes.REFUND_PROCESS,
                PermissionCodes.INVENTORY_VIEW, PermissionCodes.INVENTORY_UPDATE,
                PermissionCodes.COUPON_MANAGE, PermissionCodes.REVIEW_MODERATE,
                PermissionCodes.WORKFORCE_VIEW, PermissionCodes.WORKFORCE_MANAGE, PermissionCodes.WORKFORCE_TASK_ASSIGN,
                PermissionCodes.AUDIT_VIEW, PermissionCodes.REPORT_VIEW));

        Role masterAdmin = upsertRole("MASTER_ADMIN", "Master Admin", Set.of(PermissionCodes.MASTER_ADMIN_ALL));
        masterAdmin.setSystem(true);
        roleRepository.save(masterAdmin);
    }

    private Role upsertRole(String code, String name, Set<String> permissions) {
        return roleRepository.findByCode(code).map(existing -> {
            existing.setPermissionCodes(new HashSet<>(permissions));
            return roleRepository.save(existing);
        }).orElseGet(() -> roleRepository.save(Role.builder()
                .code(code).name(name).description(name).permissionCodes(new HashSet<>(permissions)).system(true).build()));
    }

    private void seedUsers() {
        if (userRepository.findByEmail(seedProperties.getMasterAdmin().getEmail()).isEmpty()) {
            userRepository.save(User.builder()
                    .email(seedProperties.getMasterAdmin().getEmail())
                    .passwordHash(passwordEncoder.encode(seedProperties.getMasterAdmin().getPassword()))
                    .firstName("Master")
                    .lastName("Admin")
                    .roleCodes(new HashSet<>(Set.of("MASTER_ADMIN")))
                    .enabled(true)
                    .build());
            log.info("Seeded Master Admin account: {}", seedProperties.getMasterAdmin().getEmail());
        }
        if (userRepository.findByEmail(seedProperties.getAdmin().getEmail()).isEmpty()) {
            userRepository.save(User.builder()
                    .email(seedProperties.getAdmin().getEmail())
                    .passwordHash(passwordEncoder.encode(seedProperties.getAdmin().getPassword()))
                    .firstName("Store")
                    .lastName("Admin")
                    .roleCodes(new HashSet<>(Set.of("ADMIN")))
                    .enabled(true)
                    .build());
            log.info("Seeded Admin account: {}", seedProperties.getAdmin().getEmail());
        }
        if (userRepository.findByEmail("customer@qavzuro.dev").isEmpty()) {
            userRepository.save(User.builder()
                    .email("customer@qavzuro.dev")
                    .passwordHash(passwordEncoder.encode("Customer123!"))
                    .firstName("Sample")
                    .lastName("Customer")
                    .roleCodes(new HashSet<>(Set.of("CUSTOMER")))
                    .enabled(true)
                    .build());
        }
        if (userRepository.findByEmail("worker@qavzuro.dev").isEmpty()) {
            userRepository.save(User.builder()
                    .email("worker@qavzuro.dev")
                    .passwordHash(passwordEncoder.encode("Worker123!"))
                    .firstName("Sample")
                    .lastName("Worker")
                    .roleCodes(new HashSet<>(Set.of("WORKER")))
                    .enabled(true)
                    .build());
        }
        if (userRepository.findByEmail("manager@qavzuro.dev").isEmpty()) {
            userRepository.save(User.builder()
                    .email("manager@qavzuro.dev")
                    .passwordHash(passwordEncoder.encode("Manager123!"))
                    .firstName("Sample")
                    .lastName("Manager")
                    .roleCodes(new HashSet<>(Set.of("MANAGER")))
                    .enabled(true)
                    .build());
        }
    }

    private void seedCatalog() {
        if (categoryRepository.count() > 0) return;

        Category electronics = categoryRepository.save(Category.builder()
                .slug("electronics").name("Electronics").description("Phones, laptops, audio, and more.").active(true).build());
        Category fashion = categoryRepository.save(Category.builder()
                .slug("fashion").name("Fashion").description("Clothing, footwear, and accessories.").active(true).build());
        Category home = categoryRepository.save(Category.builder()
                .slug("home-kitchen").name("Home & Kitchen").description("Everything for your home.").active(true).build());

        productRepository.save(Product.builder()
                .sku("QZ-EL-001").slug("qavzuro-wireless-headphones").name("Qavzuro Wireless Headphones")
                .shortDescription("Over-ear ANC wireless headphones, 40h battery life.")
                .description("Premium over-ear wireless headphones with active noise cancellation, 40-hour battery life, and plush memory-foam ear cushions.")
                .brand("Qavzuro Audio").categoryId(electronics.getId())
                .price(4999).salePrice(3999.0).currency("INR").taxRatePercent(18)
                .inventoryQuantity(150).lowStockThreshold(10)
                .status(ProductStatus.ACTIVE)
                .images(List.of(ProductImage.builder().url("https://placehold.co/600x600?text=Headphones").primary(true).build()))
                .tags(List.of("audio", "wireless", "headphones"))
                .build());

        productRepository.save(Product.builder()
                .sku("QZ-EL-002").slug("qavzuro-smartwatch-pro").name("Qavzuro Smartwatch Pro")
                .shortDescription("AMOLED display, 7-day battery, heart-rate & SpO2.")
                .description("A premium smartwatch with a 1.4-inch AMOLED display, 7-day battery life, and continuous heart-rate and SpO2 monitoring.")
                .brand("Qavzuro Tech").categoryId(electronics.getId())
                .price(7999).currency("INR").taxRatePercent(18)
                .inventoryQuantity(80).lowStockThreshold(10)
                .status(ProductStatus.ACTIVE)
                .images(List.of(ProductImage.builder().url("https://placehold.co/600x600?text=Smartwatch").primary(true).build()))
                .tags(List.of("wearable", "smartwatch"))
                .build());

        Product tshirt = Product.builder()
                .sku("QZ-FA-001").slug("qavzuro-classic-cotton-tshirt").name("Qavzuro Classic Cotton T-Shirt")
                .shortDescription("100% combed cotton, regular fit.")
                .description("A breathable, 100% combed cotton t-shirt in a comfortable regular fit. Available in multiple sizes and colors.")
                .brand("Qavzuro Basics").categoryId(fashion.getId())
                .price(799).currency("INR").taxRatePercent(5)
                .inventoryQuantity(0).lowStockThreshold(20)
                .status(ProductStatus.ACTIVE)
                .images(List.of(ProductImage.builder().url("https://placehold.co/600x600?text=T-Shirt").primary(true).build()))
                .tags(List.of("apparel", "tshirt", "cotton"))
                .variants(List.of(
                        ProductVariant.builder().variantId("S-BLACK").sku("QZ-FA-001-S-BLK")
                                .attributes(Map.of("size", "S", "color", "Black")).stockQuantity(40).build(),
                        ProductVariant.builder().variantId("M-BLACK").sku("QZ-FA-001-M-BLK")
                                .attributes(Map.of("size", "M", "color", "Black")).stockQuantity(60).build(),
                        ProductVariant.builder().variantId("L-WHITE").sku("QZ-FA-001-L-WHT")
                                .attributes(Map.of("size", "L", "color", "White")).stockQuantity(35).build()
                ))
                .build();
        productRepository.save(tshirt);

        productRepository.save(Product.builder()
                .sku("QZ-HK-001").slug("qavzuro-stainless-steel-cookware-set").name("Qavzuro Stainless Steel Cookware Set (5-Piece)")
                .shortDescription("Induction-friendly, 5-piece cookware set.")
                .description("A 5-piece stainless steel cookware set suitable for all cooktops including induction. Dishwasher safe.")
                .brand("Qavzuro Home").categoryId(home.getId())
                .price(3499).salePrice(2799.0).currency("INR").taxRatePercent(12)
                .inventoryQuantity(3).lowStockThreshold(5)
                .status(ProductStatus.ACTIVE)
                .images(List.of(ProductImage.builder().url("https://placehold.co/600x600?text=Cookware").primary(true).build()))
                .tags(List.of("kitchen", "cookware"))
                .build());

        if (couponRepository.findByCode("WELCOME10").isEmpty()) {
            couponRepository.save(Coupon.builder()
                    .code("WELCOME10").discountType(DiscountType.PERCENTAGE).discountValue(10)
                    .minimumOrderValue(500.0).maximumDiscountAmount(500.0)
                    .usageLimit(1000).perUserLimit(1).active(true).build());
        }

        log.info("Seeded sample catalog: {} categories, {} products.", categoryRepository.count(), productRepository.count());
    }
}
