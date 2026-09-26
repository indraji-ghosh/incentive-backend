package org.example.incentivebackend.config;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.master.designation.entity.DesignationEntity;
import org.example.incentivebackend.module.master.designation.repository.DesignationRepository;
import org.example.incentivebackend.module.master.designationpermission.entity.DesignationPagePermissionEntity;
import org.example.incentivebackend.module.master.designationpermission.repository.DesignationPagePermissionRepository;
import org.example.incentivebackend.module.master.page.entity.PageEntity;
import org.example.incentivebackend.module.master.page.repository.PageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    public CommandLineRunner initPages(
            PageRepository pageRepository,
            DesignationRepository designationRepository,
            DesignationPagePermissionRepository permissionRepository) {
        return args -> {
            if (pageRepository.count() == 0) {
                List<PageEntity> pages = List.of(
                        createPage("PARTY_MASTER", "Party Master", "Master", "/master/party", 1),
                        createPage("CLIENT_MASTER", "Client Master", "Master", "/master/client", 2),
                        createPage("SITE_MASTER", "Site Master", "Master", "/master/site", 3),
                        createPage("CLIENT_SITE_MAP", "Client Site Mapping", "Master", "/master/client-site", 4),
                        createPage("BILL_ENTRY", "Bill Entry", "Transaction", "/transaction/bill", 5),
                        createPage("RAKE_ENTRY", "Rake Entry", "Transaction", "/transaction/rake", 6),
                        createPage("PARTY_PAYMENT", "Party Payment", "Transaction", "/transaction/party-payment", 7),
                        createPage("COMMISSION_PAYMENT", "Commission Payment", "Transaction", "/transaction/commission", 8),
                        createPage("CUSTOMER_PAYMENT", "Customer Payment", "Transaction", "/transaction/customer-payment", 9),
                        createPage("DESIGNATION_MASTER", "Designation Master", "Auth", "/auth/designation", 10),
                        createPage("PERMISSION_MATRIX", "Permission Matrix", "Auth", "/auth/permissions", 11)
                );
                pageRepository.saveAll(pages);
            }

            // Also ensure there is at least one designation for testing if empty
            if (designationRepository.count() == 0) {
                DesignationEntity admin = DesignationEntity.builder()
                        .designationCode("ADMIN")
                        .designationName("Administrator")
                        .level("Top")
                        .isActive(true)
                        .build();
                designationRepository.save(admin);
                
                // Give admin full access to all pages
                for (PageEntity page : pageRepository.findAll()) {
                    DesignationPagePermissionEntity perm = DesignationPagePermissionEntity.builder()
                            .designation(admin)
                            .page(page)
                            .canView(true)
                            .canCreate(true)
                            .canEdit(true)
                            .canDelete(true)
                            .canExport(true)
                            .build();
                    permissionRepository.save(perm);
                }
            }
        };
    }

    private PageEntity createPage(String code, String name, String module, String route, int order) {
        return PageEntity.builder()
                .pageCode(code)
                .pageName(name)
                .moduleName(module)
                .route(route)
                .displayOrder(order)
                .isActive(true)
                .build();
    }
}
