package com.dabel;

import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Country;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.UserDto;
import com.dabel.service.branch.BranchFacadeService;
import com.dabel.service.customer.CustomerFacadeService;
import com.dabel.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader {

    private static final String DEFAULT_AVATARS_PATH = "src/main/resources/static/default_avatars/";
    private static final String DEFAULT_SIGNATURES_PATH = "src/main/resources/static/default_signatures/";
    private static final String ASSET_AVATARS_PATH = "src/main/resources/static/assets/avatars/";
    private static final String ASSET_SIGNATURES_PATH = "src/main/resources/static/assets/signatures/";

    private static final String[] CUSTOMERS_IDENTITY_NUMBERS = {
            "NIN001234", "NBE100237", "NIN450912", "NBE209876", "NIN378456",
            "NBE567324", "NIN876453", "NBE023945", "NIN034512", "NBE256748",
            "NIN563291", "NBE432876", "NIN987654", "NBE654321", "NIN432109",
            "FRA987654", "USA123456", "SEN456789", "CIV789012", "MAR654321"
    };

    @Bean
    private CommandLineRunner load(BranchFacadeService branchFacadeService, UserService userService, CustomerFacadeService customerFacadeService) {
        return args -> {

            //TODO: create branches
            branchFacadeService.create(BranchDto.builder()
                    .branchName("HQ")
                    .branchAddress("Moroni, Place de la France")
                    .build(), new double[]{50000000, 300000, 100000});

            branchFacadeService.create(BranchDto.builder()
                    .branchName("MUTS")
                    .branchAddress("Mutsamudu, Chitsangani")
                    .build(), new double[]{25000000, 180000, 100000});

            BranchDto savedBranch1 = branchFacadeService.getAll().get(0);
            BranchDto savedBranch2 = branchFacadeService.getAll().get(1);

            //TODO: create users
            UserDto user1 = UserDto.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .username("johndoe@saulbank.com")
                    .role("ADMIN")
                    .branch(savedBranch1)
                    .build();

            UserDto user2 = UserDto.builder()
                    .firstName("Sarah")
                    .lastName("Hunt")
                    .username("sarah.hunt@saulbank.com")
                    .role("CASHIER")
                    .branch(savedBranch2)
                    .build();

            userService.create(user1);
            userService.create(user2);

            //TODO: create customers
            // Define customers with "PERSONAL" as default Account Profile and randomly define the Account Type and branch

            Random random = new Random();
            for (CustomerDto customer : customersList()) {

                String accountName = String.format("%s %s", customer.getFirstName(), customer.getLastName());
                AccountType accountType = random.nextBoolean() ? AccountType.SAVING : AccountType.BUSINESS;
                customer.setBranch( random.nextBoolean() ? savedBranch1 : savedBranch2);

                customerFacadeService.createNewCustomerWithAccount(customer, accountName, accountType, AccountProfile.PERSONAL);
            }

            //TODO: Save avatars and signatures photos
            CustomersPhotoLoader avatarsLoader  = new CustomersPhotoLoader(DEFAULT_AVATARS_PATH, ASSET_AVATARS_PATH);
            avatarsLoader.load();

            CustomersPhotoLoader signaturesLoader  = new CustomersPhotoLoader(DEFAULT_SIGNATURES_PATH, ASSET_SIGNATURES_PATH);
            signaturesLoader.load();

        };
    }

    public CustomerDto createCustomer(String firstName, String lastName, String gender, String nationality,
                                      String identityType, String identityNumber, LocalDate identityExpiration,
                                      String identityIssue, LocalDate birthDate, String birthPlace,
                                      String profession, String email, String phone, String address, String postCode) {

        String profileAndSignatureImageName = identityNumber + ".jpg";

        return CustomerDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .gender(gender)
                .nationality(nationality)
                .identityType(identityType)
                .identityNumber(identityNumber)
                .identityExpiration(identityExpiration)
                .identityIssue(identityIssue)
                .birthDate(birthDate)
                .birthPlace(birthPlace)
                .profession(profession)
                .email(email)
                .phone(phone)
                .residence(Country.KM.getName())
                .address(address)
                .postCode(postCode)
                .profilePicture(profileAndSignatureImageName)
                .signaturePicture(profileAndSignatureImageName)
                .build();
    }

    private List<CustomerDto> customersList() {
        CustomerDto customer1 = createCustomer("Ali", "Madi", "MALE", Country.KM.getName(), "CARD",
                "NIN001234", LocalDate.of(2030, 8, 12), "Moroni",
                LocalDate.of(1990, 5, 21), "Hopital El Maarouf", "Entrepreneur",
                "ali.madi@gmail.com", "+2693214567", "Rue des Palmiers", "97600");

        CustomerDto customer2 = createCustomer("Fatima", "Said", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE100237", LocalDate.of(2029, 1, 15), "Anjouan",
                LocalDate.of(1993, 12, 30), "Hopital de Hombo", "Teacher",
                "fatima.said@yahoo.com", "+2694432768", "Avenue Ahmed Abdallah", "97610");

        CustomerDto customer3 = createCustomer("Youssouf", "Mohamed", "MALE", Country.KM.getName(), "CARD",
                "NIN450912", LocalDate.of(2028, 3, 25), "Mutsamudu",
                LocalDate.of(1988, 7, 14), "Hopital de Domoni", "Mechanic",
                "youssouf.mohamed@outlook.com", "+2696753120", "Quartier Bazimini", "97620");

        CustomerDto customer4 = createCustomer("Amina", "Bacar", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE209876", LocalDate.of(2031, 6, 5), "Fomboni",
                LocalDate.of(1995, 9, 18), "Centre Médical de Fomboni", "Pharmacist",
                "amina.bacar@gmail.com", "+2698743210", "Rue de la Corniche", "97630");

        CustomerDto customer5 = createCustomer("Houssein", "Abdallah", "MALE", Country.KM.getName(), "CARD",
                "NIN378456", LocalDate.of(2027, 11, 20), "Moroni",
                LocalDate.of(1991, 2, 10), "Hopital El Maarouf", "Civil Engineer",
                "houssein.abdallah@km.com", "+2698746532", "Route Nationale 1", "97640");

        CustomerDto customer6 = createCustomer(
                "Saïda", "Ahmed", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE567324", LocalDate.of(2028, 9, 14), "Anjouan",
                LocalDate.of(1987, 4, 11), "Centre de Santé de Sima", "Nurse",
                "saida.ahmed@gmail.com", "+2693458769", "Rue des Tisserands", "97650"
        );

        CustomerDto customer7 = createCustomer(
                "Ibrahim", "Said", "MALE", Country.KM.getName(), "CARD",
                "NIN876453", LocalDate.of(2032, 2, 22), "Moroni",
                LocalDate.of(1996, 3, 17), "Clinique Salama", "Software Developer",
                "ibrahim.said@protonmail.com", "+2697342185", "Rue des Lotus", "97600"
        );

        CustomerDto customer8 = createCustomer(
                "Fahida", "Ali", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE023945", LocalDate.of(2031, 12, 12), "Fomboni",
                LocalDate.of(1992, 6, 28), "Centre Médical de Fomboni", "Bank Teller",
                "fahida.ali@outlook.com", "+2697564132", "Quartier Djoumoichongo", "97630"
        );

        CustomerDto customer9 = createCustomer(
                "Salim", "Bourhane", "MALE", Country.KM.getName(), "CARD",
                "NIN034512", LocalDate.of(2029, 8, 29), "Mutsamudu",
                LocalDate.of(1990, 7, 23), "Centre Hospitalier de Mutsamudu", "Police Officer",
                "salim.bourhane@km.com", "+2698346217", "Quartier Citadelle", "97620"
        );

        CustomerDto customer10 = createCustomer(
                "Oumou", "Aboubacar", "FEMALE", Country.KM.getName(), "PASSPORT",
                "NBE256748", LocalDate.of(2027, 4, 20), "Moroni",
                LocalDate.of(1985, 11, 6), "Hopital El Maarouf", "Businesswoman",
                "oumou.aboubacar@gmail.com", "+2696123487", "Rue des Cocotiers", "97600"
        );

        CustomerDto customer11 = createCustomer(
                "Mariam", "Soeuf", "FEMALE", Country.KM.getName(), "CARD",
                "NIN563291", LocalDate.of(2032, 10, 11), "Mutsamudu",
                LocalDate.of(1994, 9, 13), "Hopital de Hombo", "Receptionist",
                "mariam.soeuf@hotmail.com", "+2698532146", "Rue de la Liberté", "97620"
        );

        CustomerDto customer12 = createCustomer(
                "Ahmed", "Assoumani", "MALE", Country.KM.getName(), "PASSPORT",
                "NBE432876", LocalDate.of(2031, 5, 9), "Fomboni",
                LocalDate.of(1989, 10, 22), "Centre Médical de Fomboni", "Accountant",
                "ahmed.assoumani@km.com", "+2699468123", "Avenue Ali Soilihi", "97630"
        );

        CustomerDto customer13 = createCustomer(
                "Aziza", "Mohamed", "FEMALE", Country.KM.getName(), "CARD",
                "NIN987654", LocalDate.of(2029, 11, 17), "Moroni",
                LocalDate.of(1997, 1, 10), "Hopital El Maarouf", "Sales Manager",
                "aziza.mohamed@km.com", "+2699563418", "Rue de la Paix", "97600"
        );

        CustomerDto customer14 = createCustomer(
                "Omar", "Ali", "MALE", Country.KM.getName(), "PASSPORT",
                "NBE654321", LocalDate.of(2033, 2, 4), "Anjouan",
                LocalDate.of(1992, 4, 6), "Hopital de Hombo", "Tour Guide",
                "omar.ali@km.com", "+2698653214", "Quartier Mbéni", "97610"
        );

        CustomerDto customer15 = createCustomer(
                "Shamima", "Abdallah", "FEMALE", Country.KM.getName(), "CARD",
                "NIN432109", LocalDate.of(2028, 7, 23), "Mutsamudu",
                LocalDate.of(1991, 12, 2), "Hopital de Domoni", "Secretary",
                "shamima.abdallah@gmail.com", "+2697349865", "Quartier Mirontsi", "97620"
        );

        CustomerDto customer16 = createCustomer(
                "Jean", "Dupont", "MALE", Country.FR.getName(), "PASSPORT",
                "FRA987654", LocalDate.of(2030, 10, 1), "Paris",
                LocalDate.of(1985, 4, 22), "Hôpital Necker", "Financial Analyst",
                "jean.dupont@gmail.com", "+2693217890", "Rue des Bananiers", "97600"
        );

        CustomerDto customer17 = createCustomer(
                "John", "Smith", "MALE", Country.US.getName(), "PASSPORT",
                "USA123456", LocalDate.of(2028, 5, 15), "New York",
                LocalDate.of(1987, 3, 10), "St. Mary’s Hospital", "Marketing Director",
                "john.smith@yahoo.com", "+2694456789", "Avenue des Coquillages", "97600"
        );

        CustomerDto customer18 = createCustomer(
                "Cheikh", "Diop", "MALE", Country.SN.getName(), "PASSPORT",
                "SEN456789", LocalDate.of(2029, 7, 18), "Dakar",
                LocalDate.of(1990, 9, 20), "Hôpital de Fann", "IT Consultant",
                "cheikh.diop@senegal.com", "+2695678901", "Rue des Jasmins", "97620"
        );

        CustomerDto customer19 = createCustomer(
                "Awa", "Kone", "FEMALE", Country.CI.getName(), "PASSPORT",
                "CIV789012", LocalDate.of(2031, 11, 25), "Abidjan",
                LocalDate.of(1994, 6, 12), "CHU de Treichville", "HR Manager",
                "awa.kone@gmail.com", "+2696789012", "Rue des Manguiers", "97610"
        );

        CustomerDto customer20 = createCustomer(
                "Khalid", "El Amrani", "MALE", Country.MA.getName(), "PASSPORT",
                "MAR654321", LocalDate.of(2027, 4, 30), "Casablanca",
                LocalDate.of(1992, 12, 5), "Clinique Chifa", "Architect",
                "khalid.elamrani@ma.com", "+2697890123", "Avenue des Étoiles", "97630"
        );

        return Arrays.asList(
                customer1, customer2, customer3, customer4, customer5,
                customer6, customer7, customer8, customer9, customer10,
                customer11, customer12, customer13, customer14, customer15,
                customer16, customer17, customer18, customer19, customer20
        );
    }

    private record CustomersPhotoLoader(String DEFAULT_PATH, String DESTINATION_PATH) {

        public void load() {
            try {

                // Check if destination folder is empty before copy
                Path destinationDir = Paths.get(DESTINATION_PATH);
                long fileCount = Files.list(destinationDir).count();

                //in avatars folder we've a picture of logged user by default,
                //so we test if destination folder have one or less file before copy
                if(fileCount <= 1) {
                    for (int i = 1; i <= CUSTOMERS_IDENTITY_NUMBERS.length; i++) {
                        String oldImageName = DEFAULT_PATH + i + ".jpg";
                        String newImageName = DESTINATION_PATH + CUSTOMERS_IDENTITY_NUMBERS[i - 1] + ".jpg";
                        copyImage(oldImageName, newImageName);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        private void copyImage(String sourcePath, String destinationPath) throws IOException {
            Path source = Paths.get(sourcePath);
            Path destination = Paths.get(destinationPath);

            Files.createDirectories(destination.getParent()); // Create folder if is necessary
            try (FileInputStream inputStream = new FileInputStream(source.toFile());
                 FileOutputStream outputStream = new FileOutputStream(destination.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

}
