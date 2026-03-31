package com.cts.mediconnect;

import com.cts.mediconnect.model.*;
import com.cts.mediconnect.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Profile("seed")
@SpringBootApplication
public class Seed implements CommandLineRunner {

    @PersistenceContext private EntityManager em;

    @Autowired private HospitalRepository    hospitalRepo;
    @Autowired private DepartmentRepository  departmentRepo;
    @Autowired private UserRepository        userRepo;
    @Autowired private DoctorRepository      doctorRepo;
    @Autowired private PatientRepository     patientRepo;
    @Autowired private BedRepository         bedRepo;
    @Autowired private AppointmentRepository appointmentRepo;
    @Autowired private MedicalRecordRepository medicalRecordRepo;
    @Autowired private LabReportRepository   labReportRepo;
    @Autowired private NotificationRepository notificationRepo;
    @Autowired private InventoryRepository   inventoryRepo;

    // Shared state between seed methods (populated in dependency order)
    private Hospital   h1, h2, h3;
    private Department cardio1, ortho1, onco2, pedia3;
    private User       adminUser1, docUser1, docUser2, docUser3, docUser4;
    private User       patUser1, patUser2, patUser3;
    private Doctor     d1, d2, d3, d4;
    private Patient    p1, p2, p3;

    // Seeding order — index matters for dependency resolution
    private static final List<String> TABLES = List.of(
        "hospitals",       // 0
        "departments",     // 1
        "users",           // 2
        "doctors",         // 3
        "patients",        // 4
        "beds",            // 5
        "appointments",    // 6
        "medical_records", // 7
        "lab_reports",     // 8
        "notifications",   // 9
        "inventory"        // 10
    );

    // Direct dependencies (indices into TABLES)
    private static final Map<Integer, int[]> DEPS = Map.ofEntries(
        Map.entry(0,  new int[]{}),           // hospitals
        Map.entry(1,  new int[]{0}),          // departments  ← hospitals
        Map.entry(2,  new int[]{0}),          // users        ← hospitals
        Map.entry(3,  new int[]{0, 1, 2}),    // doctors      ← hospitals, departments, users
        Map.entry(4,  new int[]{0, 2}),       // patients     ← hospitals, users
        Map.entry(5,  new int[]{0, 4}),       // beds         ← hospitals, patients
        Map.entry(6,  new int[]{0, 3, 4}),    // appointments ← hospitals, doctors, patients
        Map.entry(7,  new int[]{0, 3, 4}),    // medical_records
        Map.entry(8,  new int[]{0, 3, 4}),    // lab_reports
        Map.entry(9,  new int[]{0, 2}),       // notifications ← hospitals, users
        Map.entry(10, new int[]{0})           // inventory    ← hospitals
    );

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Seed.class);
        app.setAdditionalProfiles("seed");
        app.run(args);
    }

    @Override
    @Transactional
    public void run(String... args) {
        Set<Integer> selected = promptUser();
        if (selected.isEmpty()) {
            System.out.println("No tables selected. Exiting.");
            return;
        }
        truncate(selected);
        seedSelected(selected);
        System.out.println("\nSeeding complete.");
    }

    // ── CLI prompt ────────────────────────────────────────────────────────────────

    private Set<Integer> promptUser() {
        System.out.println("\n=== MediConnect Database Seeder ===\n");
        System.out.println("Select tables to seed (comma-separated numbers), or type 'all':\n");

        for (int i = 0; i < TABLES.size(); i++) {
            int[] deps = DEPS.get(i);
            String depLabel = "";
            if (deps.length > 0) {
                StringJoiner sj = new StringJoiner(", ", "  [requires: ", "]");
                for (int d : deps) sj.add(TABLES.get(d));
                depLabel = sj.toString();
            }
            System.out.printf("  %2d. %-20s%s%n", i + 1, TABLES.get(i), depLabel);
        }

        System.out.print("\nEnter selection: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();

        Set<Integer> chosen = new LinkedHashSet<>();
        if (input.equalsIgnoreCase("all")) {
            for (int i = 0; i < TABLES.size(); i++) chosen.add(i);
        } else {
            for (String part : input.split(",")) {
                String token = part.trim();
                if (token.isEmpty()) continue;
                try {
                    int n = Integer.parseInt(token) - 1;
                    if (n >= 0 && n < TABLES.size()) chosen.add(n);
                    else System.out.println("  ! Skipping out-of-range number: " + (n + 1));
                } catch (NumberFormatException e) {
                    System.out.println("  ! Skipping unrecognised input: " + token);
                }
            }
        }

        // Resolve and expand dependencies
        Set<Integer> resolved = new LinkedHashSet<>();
        for (int idx : chosen) resolveDeps(idx, resolved);

        Set<Integer> autoAdded = new LinkedHashSet<>(resolved);
        autoAdded.removeAll(chosen);
        if (!autoAdded.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ");
            autoAdded.forEach(i -> sj.add(TABLES.get(i)));
            System.out.println("\nAuto-including dependencies: " + sj);
        }

        StringJoiner sj = new StringJoiner(", ");
        resolved.forEach(i -> sj.add(TABLES.get(i)));
        System.out.println("Will seed: " + sj + "\n");

        return resolved;
    }

    private void resolveDeps(int idx, Set<Integer> resolved) {
        for (int dep : DEPS.get(idx)) resolveDeps(dep, resolved);
        resolved.add(idx);
    }

    // ── Truncation ────────────────────────────────────────────────────────────────

    private void truncate(Set<Integer> toSeed) {
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        List<Integer> reversed = new ArrayList<>(toSeed);
        Collections.reverse(reversed);
        for (int idx : reversed) {
            em.createNativeQuery("TRUNCATE TABLE " + TABLES.get(idx)).executeUpdate();
            System.out.println("Truncated: " + TABLES.get(idx));
        }
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        System.out.println();
    }

    // ── Dispatcher ───────────────────────────────────────────────────────────────

    private void seedSelected(Set<Integer> toSeed) {
        for (int idx : toSeed) {
            switch (idx) {
                case 0  -> seedHospitals();
                case 1  -> seedDepartments();
                case 2  -> seedUsers();
                case 3  -> seedDoctors();
                case 4  -> seedPatients();
                case 5  -> seedBeds();
                case 6  -> seedAppointments();
                case 7  -> seedMedicalRecords();
                case 8  -> seedLabReports();
                case 9  -> seedNotifications();
                case 10 -> seedInventory();
            }
        }
    }

    // ── Per-table seed methods ────────────────────────────────────────────────────

    private void seedHospitals() {
        h1 = new Hospital();
        h1.setHospitalName("Apollo Hospitals");
        h1.setAddress("154 Greams Road");
        h1.setCity("Chennai");
        h1.setPhone("044-28293333");
        h1.setTotalBeds(500);
        h1.setAvailableBeds(120);
        h1 = hospitalRepo.save(h1);

        h2 = new Hospital();
        h2.setHospitalName("Fortis Healthcare");
        h2.setAddress("76 Bannerghatta Road");
        h2.setCity("Bangalore");
        h2.setPhone("080-66214444");
        h2.setTotalBeds(350);
        h2.setAvailableBeds(80);
        h2 = hospitalRepo.save(h2);

        h3 = new Hospital();
        h3.setHospitalName("AIIMS Delhi");
        h3.setAddress("Ansari Nagar East");
        h3.setCity("Delhi");
        h3.setPhone("011-26588500");
        h3.setTotalBeds(1000);
        h3.setAvailableBeds(300);
        h3 = hospitalRepo.save(h3);

        System.out.println("Seeded: hospitals (3 records)");
    }

    private void seedDepartments() {
        cardio1 = dept("Cardiology",     h1);
        ortho1  = dept("Orthopaedics",   h1);
                  dept("Neurology",      h1);
                  dept("Cardiology",     h2);
        onco2   = dept("Oncology",       h2);
        pedia3  = dept("Paediatrics",    h3);
                  dept("General Medicine", h3);
        System.out.println("Seeded: departments (7 records)");
    }

    private void seedUsers() {
        adminUser1 = user("Ravi Kumar",          "ravi.admin@apollo.com",  "admin123", "9876543210", "ADMIN",   h1,   null,           null);
                     user("Priya Sharma",         "priya.admin@fortis.com", "admin123", "9876543211", "ADMIN",   h2,   null,           null);
        docUser1   = user("Dr. Arjun Mehta",     "arjun.mehta@apollo.com", "doc123",   "9876500001", "DOCTOR",  h1,   "Cardiology",   "AVAILABLE");
        docUser2   = user("Dr. Sneha Iyer",      "sneha.iyer@apollo.com",  "doc123",   "9876500002", "DOCTOR",  h1,   "Orthopaedics", "AVAILABLE");
        docUser3   = user("Dr. Karan Malhotra",  "karan.m@fortis.com",     "doc123",   "9876500003", "DOCTOR",  h2,   "Oncology",     "BUSY");
        docUser4   = user("Dr. Anita Reddy",     "anita.r@aiims.com",      "doc123",   "9876500004", "DOCTOR",  h3,   "Paediatrics",  "AVAILABLE");
        patUser1   = user("Arun Patel",          "arun.patel@gmail.com",   "pat123",   "9900001111", "PATIENT", null, null,           null);
        patUser2   = user("Divya Nair",          "divya.nair@gmail.com",   "pat123",   "9900002222", "PATIENT", null, null,           null);
        patUser3   = user("Suresh Babu",         "suresh.babu@gmail.com",  "pat123",   "9900003333", "PATIENT", null, null,           null);
        System.out.println("Seeded: users (9 records)");
    }

    private void seedDoctors() {
        d1 = doctor(docUser1, cardio1, h1, "Cardiology",   "AVAILABLE");
        d2 = doctor(docUser2, ortho1,  h1, "Orthopaedics", "AVAILABLE");
        d3 = doctor(docUser3, onco2,   h2, "Oncology",     "BUSY");
        d4 = doctor(docUser4, pedia3,  h3, "Paediatrics",  "AVAILABLE");
        System.out.println("Seeded: doctors (4 records)");
    }

    private void seedPatients() {
        p1 = patient(patUser1, LocalDate.of(1990, 5, 14), "Male",   "B+", "12 MG Road, Chennai",       "9800001111");
        p2 = patient(patUser2, LocalDate.of(1985, 9, 22), "Female", "O+", "34 Koramangala, Bangalore",  "9800002222");
        p3 = patient(patUser3, LocalDate.of(2000, 1,  3), "Male",   "A-", "56 Lajpat Nagar, Delhi",    "9800003333");
        System.out.println("Seeded: patients (3 records)");
    }

    private void seedBeds() {
        bed(h1, "General Ward", "G-101",  "OCCUPIED",  p1);
        bed(h1, "General Ward", "G-102",  "AVAILABLE", null);
        bed(h1, "ICU",          "ICU-01", "AVAILABLE", null);
        bed(h2, "General Ward", "G-201",  "OCCUPIED",  p2);
        bed(h2, "Private",      "P-101",  "AVAILABLE", null);
        bed(h3, "Paediatric",   "PD-01",  "OCCUPIED",  p3);
        bed(h3, "General Ward", "G-301",  "AVAILABLE", null);
        System.out.println("Seeded: beds (7 records)");
    }

    private void seedAppointments() {
        appointment(p1, d1, h1, LocalDate.of(2026, 4,  5), LocalTime.of(10,  0), "CONFIRMED", "IN_PERSON", null);
        appointment(p2, d3, h2, LocalDate.of(2026, 4,  7), LocalTime.of(14, 30), "PENDING",   "IN_PERSON", null);
        appointment(p1, d2, h1, LocalDate.of(2026, 3, 20), LocalTime.of( 9,  0), "COMPLETED", "IN_PERSON", null);
        appointment(p3, d4, h3, LocalDate.of(2026, 4, 10), LocalTime.of(11,  0), "CONFIRMED", "ONLINE",    "https://meet.mediconnect.in/session/abc123");
        System.out.println("Seeded: appointments (4 records)");
    }

    private void seedMedicalRecords() {
        medRecord(p1, d1, h1, LocalDate.of(2026, 3, 20),
            "Hypertension Stage 1", "Lifestyle modification and medication",
            "Amlodipine 5mg once daily",
            "Patient advised to reduce sodium intake and exercise regularly.");
        medRecord(p2, d3, h2, LocalDate.of(2026, 2, 15),
            "Breast Cancer - Stage II", "Chemotherapy + Radiation",
            "Tamoxifen 20mg",
            "Follow-up in 4 weeks post first chemotherapy cycle.");
        medRecord(p3, d4, h3, LocalDate.of(2026, 3, 28),
            "Acute Tonsillitis", "Antibiotics and rest",
            "Amoxicillin 250mg thrice daily for 7 days",
            "Patient to avoid cold drinks. Review if no improvement in 5 days.");
        System.out.println("Seeded: medical_records (3 records)");
    }

    private void seedLabReports() {
        labReport(p1, d1, h1, "Complete Blood Count",  "WBC: 7.2 k/uL, RBC: 4.8 M/uL, HGB: 14.2 g/dL - Normal", null,                                LocalDate.of(2026, 3, 19));
        labReport(p1, d1, h1, "Lipid Profile",         "LDL: 145 mg/dL (High), HDL: 38 mg/dL (Low)",              null,                                LocalDate.of(2026, 3, 19));
        labReport(p2, d3, h2, "Tumour Marker CA 15-3", "CA 15-3: 52 U/mL (Elevated)",                              "https://reports.mediconnect.in/r2", LocalDate.of(2026, 2, 14));
        labReport(p3, d4, h3, "Throat Swab Culture",   "Group A Streptococcus positive",                            null,                                LocalDate.of(2026, 3, 27));
        System.out.println("Seeded: lab_reports (4 records)");
    }

    private void seedNotifications() {
        notification(adminUser1, "SYSTEM",      "Daily bed occupancy report is ready.",                           false, LocalDateTime.of(2026, 3, 31,  8,  0));
        notification(docUser1,   "APPOINTMENT", "New appointment scheduled: Arun Patel on Apr 5 at 10:00 AM.",  false, LocalDateTime.of(2026, 3, 30,  9, 15));
        notification(patUser1,   "APPOINTMENT", "Your appointment with Dr. Arjun Mehta is confirmed.",           true,  LocalDateTime.of(2026, 3, 30,  9, 16));
        notification(patUser1,   "LAB_REPORT",  "Your Lipid Profile report is now available.",                    true,  LocalDateTime.of(2026, 3, 20, 11,  0));
        notification(patUser2,   "APPOINTMENT", "Your appointment with Dr. Karan Malhotra is pending approval.", false, LocalDateTime.of(2026, 3, 31, 10,  0));
        notification(docUser3,   "APPOINTMENT", "Appointment request from Divya Nair on Apr 7 at 2:30 PM.",     false, LocalDateTime.of(2026, 3, 31, 10,  1));
        System.out.println("Seeded: notifications (6 records)");
    }

    private void seedInventory() {
        inventory(h1, "Paracetamol 500mg",    "Medication",   500, 100);
        inventory(h1, "Surgical Gloves (L)",  "Consumables",  200,  50);
        inventory(h1, "IV Saline 500ml",      "Fluids",       150,  30);
        inventory(h1, "Digital Thermometer",  "Equipment",     25,   5);
        inventory(h2, "Morphine 10mg/ml",     "Medication",    80,  20);
        inventory(h2, "Chemotherapy Needles", "Consumables",  120,  40);
        inventory(h2, "Oncology Gloves (M)",  "Consumables",  300,  60);
        inventory(h3, "Amoxicillin 250mg",    "Medication",   400,  80);
        inventory(h3, "Paediatric Nebuliser", "Equipment",      8,   2);
        inventory(h3, "Surgical Masks",       "Consumables", 1000, 200);
        System.out.println("Seeded: inventory (10 records)");
    }

    // ── Builder helpers ───────────────────────────────────────────────────────────

    private Department dept(String name, Hospital hospital) {
        Department d = new Department();
        d.setDepartmentName(name);
        d.setHospital(hospital);
        return departmentRepo.save(d);
    }

    private User user(String name, String email, String password, String phone,
                      String role, Hospital hospital, String specialization, String availability) {
        User u = new User();
        u.setName(name); u.setEmail(email); u.setPassword(password); u.setPhone(phone);
        u.setRole(role); u.setHospital(hospital);
        u.setSpecialization(specialization); u.setAvailabilityStatus(availability);
        return userRepo.save(u);
    }

    private Doctor doctor(User user, Department dept, Hospital hospital,
                          String specialization, String availability) {
        Doctor d = new Doctor();
        d.setUser(user); d.setDepartment(dept); d.setHospital(hospital);
        d.setSpecialization(specialization); d.setAvailabilityStatus(availability);
        return doctorRepo.save(d);
    }

    private Patient patient(User user, LocalDate dob, String gender, String bloodGroup,
                            String address, String emergencyContact) {
        Patient p = new Patient();
        p.setUser(user); p.setDateOfBirth(dob); p.setGender(gender);
        p.setBloodGroup(bloodGroup); p.setAddress(address); p.setEmergencyContact(emergencyContact);
        return patientRepo.save(p);
    }

    private void bed(Hospital hospital, String ward, String bedNumber, String status, Patient patient) {
        Bed b = new Bed();
        b.setHospital(hospital); b.setWard(ward); b.setBedNumber(bedNumber);
        b.setStatus(status); b.setPatient(patient);
        bedRepo.save(b);
    }

    private void appointment(Patient patient, Doctor doctor, Hospital hospital,
                             LocalDate date, LocalTime time, String status,
                             String type, String sessionUrl) {
        Appointment a = new Appointment();
        a.setPatient(patient); a.setDoctor(doctor); a.setHospital(hospital);
        a.setAppointmentDate(date); a.setAppointmentTime(time);
        a.setStatus(status); a.setAppointmentType(type); a.setSessionUrl(sessionUrl);
        appointmentRepo.save(a);
    }

    private void medRecord(Patient patient, Doctor doctor, Hospital hospital, LocalDate date,
                           String diagnosis, String treatment, String prescription, String notes) {
        MedicalRecord r = new MedicalRecord();
        r.setPatient(patient); r.setDoctor(doctor); r.setHospital(hospital);
        r.setRecordDate(date); r.setDiagnosis(diagnosis); r.setTreatment(treatment);
        r.setPrescription(prescription); r.setNotes(notes);
        medicalRecordRepo.save(r);
    }

    private void labReport(Patient patient, Doctor doctor, Hospital hospital, String testName,
                           String result, String reportUrl, LocalDate reportDate) {
        LabReport lr = new LabReport();
        lr.setPatient(patient); lr.setDoctor(doctor); lr.setHospital(hospital);
        lr.setTestName(testName); lr.setResult(result);
        lr.setReportUrl(reportUrl); lr.setReportDate(reportDate);
        labReportRepo.save(lr);
    }

    private void notification(User user, String type, String message,
                              boolean isRead, LocalDateTime createdAt) {
        Notification n = new Notification();
        n.setUser(user); n.setNotificationType(type); n.setMessage(message);
        n.setIsRead(isRead); n.setCreatedAt(createdAt);
        notificationRepo.save(n);
    }

    private void inventory(Hospital hospital, String itemName, String category,
                           int quantity, int reorderLevel) {
        Inventory i = new Inventory();
        i.setHospital(hospital); i.setItemName(itemName); i.setCategory(category);
        i.setQuantity(quantity); i.setReorderLevel(reorderLevel);
        inventoryRepo.save(i);
    }
}
