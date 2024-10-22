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
                    .build(), new double[]{5000, 3000, 1000});

            branchFacadeService.create(BranchDto.builder()
                    .branchName("MUTS")
                    .branchAddress("Mutsamudu, Chitsangani")
                    .build(), new double[]{2500, 1800, 1000});

            BranchDto savedBranch1 = branchFacadeService.findAll().get(0);
            BranchDto savedBranch2 = branchFacadeService.findAll().get(1);

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

                customerFacadeService.create(customer, accountName, accountType, AccountProfile.PERSONAL);
            }

            //TODO: Save avatars and signatures photos
            CustomersPhotoLoader avatarsLoader  = new CustomersPhotoLoader(DEFAULT_AVATARS_PATH, ASSET_AVATARS_PATH);
            avatarsLoader.load();

            CustomersPhotoLoader signaturesLoader  = new CustomersPhotoLoader(DEFAULT_SIGNATURES_PATH, ASSET_SIGNATURES_PATH);
            signaturesLoader.load();

        };
    }

    private List<CustomerDto> customersList() {
        CustomerDto customer1 = CustomerDto.builder()
                .firstName("Ali")
                .lastName("Madi")
                .gender("MALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN001234")
                .identityExpiration(LocalDate.of(2030, 8, 12))
                .identityIssue("Moroni")
                .birthDate(LocalDate.of(1990, 5, 21))
                .birthPlace("Hopital El Maarouf")
                .profession("Entrepreneur")
                .email("ali.madi@gmail.com")
                .phone("+2693214567")
                .residence(Country.KM.getName())
                .address("Rue des Palmiers")
                .postCode("97600")
                .profilePicture("NIN001234.jpg")
                .signaturePicture("NIN001234.jpg")
                .build();

        CustomerDto customer2 = CustomerDto.builder()
                .firstName("Fatima")
                .lastName("Said")
                .gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("PASSPORT")
                .identityNumber("NBE100237")
                .identityExpiration(LocalDate.of(2029, 1, 15))
                .identityIssue("Anjouan")
                .birthDate(LocalDate.of(1993, 12, 30))
                .birthPlace("Hopital de Hombo")
                .profession("Teacher")
                .email("fatima.said@yahoo.com")
                .phone("+2694432768")
                .residence(Country.KM.getName())
                .address("Avenue Ahmed Abdallah")
                .postCode("97610")
                .profilePicture("NBE100237.jpg")
                .signaturePicture("NBE100237.jpg")
                .build();

        CustomerDto customer3 = CustomerDto.builder()
                .firstName("Youssouf")
                .lastName("Mohamed")
                .gender("MALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN450912")
                .identityExpiration(LocalDate.of(2028, 3, 25))
                .identityIssue("Mutsamudu")
                .birthDate(LocalDate.of(1988, 7, 14))
                .birthPlace("Hopital de Domoni")
                .profession("Mechanic")
                .email("youssouf.mohamed@outlook.com")
                .phone("+2696753120")
                .residence(Country.KM.getName())
                .address("Quartier Bazimini")
                .postCode("97620")
                .profilePicture("NIN450912.jpg")
                .signaturePicture("NIN450912.jpg")
                .build();

        CustomerDto customer4 = CustomerDto.builder()
                .firstName("Amina")
                .lastName("Bacar"
                ).gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("PASSPORT")
                .identityNumber("NBE209876")
                .identityExpiration(LocalDate.of(2031, 6, 5))
                .identityIssue("Fomboni")
                .birthDate(LocalDate.of(1995, 9, 18))
                .birthPlace("Centre Médical de Fomboni")
                .profession("Pharmacist")
                .email("amina.bacar@gmail.com")
                .phone("+2698743210")
                .residence(Country.KM.getName())
                .address("Rue de la Corniche")
                .postCode("97630")
                .profilePicture("NBE209876.jpg")
                .signaturePicture("NBE209876.jpg")
                .build();

        CustomerDto customer5 = CustomerDto.builder()
                .firstName("Houssein")
                .lastName("Abdallah")
                .gender("MALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN378456")
                .identityExpiration(LocalDate.of(2027, 11, 20))
                .identityIssue("Moroni")
                .birthDate(LocalDate.of(1991, 2, 10))
                .birthPlace("Hopital El Maarouf")
                .profession("Civil Engineer")
                .email("houssein.abdallah@km.com")
                .phone("+2698746532")
                .residence(Country.KM.getName())
                .address("Route Nationale 1")
                .postCode("97640")
                .profilePicture("NIN378456.jpg")
                .signaturePicture("NIN378456.jpg")
                .build();

        CustomerDto customer6 = CustomerDto.builder()
                .firstName("Saïda")
                .lastName("Ahmed")
                .gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("PASSPORT")
                .identityNumber("NBE567324")
                .identityExpiration(LocalDate.of(2028, 9, 14))
                .identityIssue("Anjouan")
                .birthDate(LocalDate.of(1987, 4, 11))
                .birthPlace("Centre de Santé de Sima")
                .profession("Nurse")
                .email("saida.ahmed@gmail.com")
                .phone("+2693458769")
                .residence(Country.KM.getName())
                .address("Rue des Tisserands")
                .postCode("97650")
                .profilePicture("NBE567324.jpg")
                .signaturePicture("NBE567324.jpg")
                .build();

        CustomerDto customer7 = CustomerDto.builder()
                .firstName("Ibrahim")
                .lastName("Said")
                .gender("MALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN876453")
                .identityExpiration(LocalDate.of(2032, 2, 22))
                .identityIssue("Moroni")
                .birthDate(LocalDate.of(1996, 3, 17))
                .birthPlace("Clinique Salama")
                .profession("Software Developer")
                .email("ibrahim.said@protonmail.com")
                .phone("+2697342185")
                .residence(Country.KM.getName())
                .address("Rue des Lotus")
                .postCode("97600")
                .profilePicture("NIN876453.jpg")
                .signaturePicture("NIN876453.jpg")
                .build();

        CustomerDto customer8 = CustomerDto.builder()
                .firstName("Fahida")
                .lastName("Ali")
                .gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("PASSPORT")
                .identityNumber("NBE023945")
                .identityExpiration(LocalDate.of(2031, 12, 12))
                .identityIssue("Fomboni")
                .birthDate(LocalDate.of(1992, 6, 28))
                .birthPlace("Centre Médical de Fomboni")
                .profession("Bank Teller")
                .email("fahida.ali@outlook.com")
                .phone("+2697564132")
                .residence(Country.KM.getName())
                .address("Quartier Djoumoichongo")
                .postCode("97630")
                .profilePicture("NBE023945.jpg")
                .signaturePicture("NBE023945.jpg")
                .build();

        CustomerDto customer9 = CustomerDto.builder()
                .firstName("Salim")
                .lastName("Bourhane")
                .gender("MALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN034512")
                .identityExpiration(LocalDate.of(2029, 8, 29))
                .identityIssue("Mutsamudu")
                .birthDate(LocalDate.of(1990, 7, 23))
                .birthPlace("Centre Hospitalier de Mutsamudu")
                .profession("Police Officer")
                .email("salim.bourhane@km.com")
                .phone("+2698346217")
                .residence(Country.KM.getName())
                .address("Quartier Citadelle")
                .postCode("97620")
                .profilePicture("NIN034512.jpg")
                .signaturePicture("NIN034512.jpg")
                .build();

        CustomerDto customer10 = CustomerDto.builder()
                .firstName("Oumou")
                .lastName("Aboubacar")
                .gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("PASSPORT")
                .identityNumber("NBE256748")
                .identityExpiration(LocalDate.of(2027, 4, 20))
                .identityIssue("Moroni")
                .birthDate(LocalDate.of(1985, 11, 6))
                .birthPlace("Hopital El Maarouf")
                .profession("Businesswoman")
                .email("oumou.aboubacar@gmail.com")
                .phone("+2696123487")
                .residence(Country.KM.getName())
                .address("Rue des Cocotiers")
                .postCode("97600")
                .profilePicture("NBE256748.jpg")
                .signaturePicture("NBE256748.jpg")
                .build();

        CustomerDto customer11 = CustomerDto.builder()
                .firstName("Mariam")
                .lastName("Soeuf")
                .gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN563291")
                .identityExpiration(LocalDate.of(2032, 10, 11))
                .identityIssue("Mutsamudu")
                .birthDate(LocalDate.of(1994, 9, 13))
                .birthPlace("Hopital de Hombo")
                .profession("Receptionist")
                .email("mariam.soeuf@hotmail.com")
                .phone("+2698532146")
                .residence(Country.KM.getName())
                .address("Rue de la Liberté")
                .postCode("97620")
                .profilePicture("NIN563291.jpg")
                .signaturePicture("NIN563291.jpg")
                .build();

        CustomerDto customer12 = CustomerDto.builder()
                .firstName("Ahmed")
                .lastName("Assoumani")
                .gender("MALE")
                .nationality(Country.KM.getName())
                .identityType("PASSPORT")
                .identityNumber("NBE432876")
                .identityExpiration(LocalDate.of(2031, 5, 9))
                .identityIssue("Fomboni")
                .birthDate(LocalDate.of(1989, 10, 22))
                .birthPlace("Centre Médical de Fomboni")
                .profession("Accountant")
                .email("ahmed.assoumani@km.com")
                .phone("+2699468123")
                .residence(Country.KM.getName())
                .address("Avenue Ali Soilihi")
                .postCode("97630")
                .profilePicture("NBE432876.jpg")
                .signaturePicture("NBE432876.jpg")
                .build();

        CustomerDto customer13 = CustomerDto.builder()
                .firstName("Aziza")
                .lastName("Mohamed")
                .gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN987654")
                .identityExpiration(LocalDate.of(2029, 11, 17))
                .identityIssue("Moroni")
                .birthDate(LocalDate.of(1997, 1, 10))
                .birthPlace("Hopital El Maarouf")
                .profession("Sales Manager")
                .email("aziza.mohamed@km.com")
                .phone("+2699563418")
                .residence(Country.KM.getName())
                .address("Rue de la Paix")
                .postCode("97600")
                .profilePicture("NIN987654.jpg")
                .signaturePicture("NIN987654.jpg")
                .build();

        CustomerDto customer14 = CustomerDto.builder()
                .firstName("Omar")
                .lastName("Ali")
                .gender("MALE")
                .nationality(Country.KM.getName())
                .identityType("PASSPORT")
                .identityNumber("NBE654321")
                .identityExpiration(LocalDate.of(2033, 2, 4))
                .identityIssue("Anjouan")
                .birthDate(LocalDate.of(1992, 4, 6))
                .birthPlace("Hopital de Hombo")
                .profession("Tour Guide")
                .email("omar.ali@km.com")
                .phone("+2698653214")
                .residence(Country.KM.getName())
                .address("Quartier Mbéni")
                .postCode("97610")
                .profilePicture("NBE654321.jpg")
                .signaturePicture("NBE654321.jpg")
                .build();

        CustomerDto customer15 = CustomerDto.builder()
                .firstName("Shamima")
                .lastName("Abdallah")
                .gender("FEMALE")
                .nationality(Country.KM.getName())
                .identityType("CARD")
                .identityNumber("NIN432109")
                .identityExpiration(LocalDate.of(2028, 7, 23))
                .identityIssue("Mutsamudu")
                .birthDate(LocalDate.of(1991, 12, 2))
                .birthPlace("Hopital de Domoni")
                .profession("Secretary")
                .email("shamima.abdallah@gmail.com")
                .phone("+2697349865")
                .residence(Country.KM.getName())
                .address("Quartier Mirontsi")
                .postCode("97620")
                .profilePicture("NIN432109.jpg")
                .signaturePicture("NIN432109.jpg")
                .build();

        CustomerDto customer16 = CustomerDto.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .gender("MALE")
                .nationality(Country.FR.getName())
                .identityType("PASSPORT")
                .identityNumber("FRA987654")
                .identityExpiration(LocalDate.of(2030, 10, 1))
                .identityIssue("Paris")
                .birthDate(LocalDate.of(1985, 4, 22))
                .birthPlace("Hôpital Necker")
                .profession("Financial Analyst")
                .email("jean.dupont@gmail.com")
                .phone("+2693217890")
                .residence(Country.KM.getName())
                .address("Rue des Bananiers")
                .postCode("97600")
                .profilePicture("FRA987654.jpg")
                .signaturePicture("FRA987654.jpg")
                .build();

        CustomerDto customer17 = CustomerDto.builder()
                .firstName("John")
                .lastName("Smith")
                .gender("MALE")
                .nationality(Country.US.getName())
                .identityType("PASSPORT")
                .identityNumber("USA123456")
                .identityExpiration(LocalDate.of(2028, 5, 15))
                .identityIssue("New York")
                .birthDate(LocalDate.of(1987, 3, 10))
                .birthPlace("St. Mary’s Hospital")
                .profession("Marketing Director")
                .email("john.smith@yahoo.com")
                .phone("+2694456789")
                .residence(Country.KM.getName())
                .address("Avenue des Coquillages")
                .postCode("97600")
                .profilePicture("USA123456.jpg")
                .signaturePicture("USA123456.jpg")
                .build();

        CustomerDto customer18 = CustomerDto.builder()
                .firstName("Cheikh")
                .lastName("Diop")
                .gender("MALE")
                .nationality(Country.SN.getName())
                .identityType("PASSPORT")
                .identityNumber("SEN456789")
                .identityExpiration(LocalDate.of(2029, 7, 18))
                .identityIssue("Dakar")
                .birthDate(LocalDate.of(1990, 9, 20))
                .birthPlace("Hôpital de Fann")
                .profession("IT Consultant")
                .email("cheikh.diop@senegal.com")
                .phone("+2695678901")
                .residence(Country.KM.getName())
                .address("Rue des Jasmins")
                .postCode("97620")
                .profilePicture("SEN456789.jpg")
                .signaturePicture("SEN456789.jpg")
                .build();

        CustomerDto customer19 = CustomerDto.builder()
                .firstName("Awa")
                .lastName("Kone")
                .gender("FEMALE")
                .nationality(Country.CI.getName())
                .identityType("PASSPORT")
                .identityNumber("CIV789012")
                .identityExpiration(LocalDate.of(2031, 11, 25))
                .identityIssue("Abidjan")
                .birthDate(LocalDate.of(1994, 6, 12))
                .birthPlace("CHU de Treichville")
                .profession("HR Manager")
                .email("awa.kone@gmail.com")
                .phone("+2696789012")
                .residence(Country.KM.getName())
                .address("Rue des Manguiers")
                .postCode("97610")
                .profilePicture("CIV789012.jpg")
                .signaturePicture("CIV789012.jpg")
                .build();

        CustomerDto customer20 = CustomerDto.builder()
                .firstName("Khalid")
                .lastName("El Amrani")
                .gender("MALE")
                .nationality(Country.MA.getName())
                .identityType("PASSPORT")
                .identityNumber("MAR654321")
                .identityExpiration(LocalDate.of(2027, 4, 30))
                .identityIssue("Casablanca")
                .birthDate(LocalDate.of(1992, 12, 5))
                .birthPlace("Clinique Chifa")
                .profession("Architect")
                .email("khalid.elamrani@ma.com")
                .phone("+2697890123")
                .residence(Country.KM.getName())
                .address("Avenue des Étoiles")
                .postCode("97630")
                .profilePicture("MAR654321.jpg")
                .signaturePicture("MAR654321.jpg")
                .build();


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
