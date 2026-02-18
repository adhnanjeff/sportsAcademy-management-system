package com.badminton.academy.config;

import com.badminton.academy.model.*;
import com.badminton.academy.model.enums.*;
import com.badminton.academy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private static final String INITIAL_SEED_KEY = "initial_seed_done";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@academy.com";
    private static final String DEFAULT_COACH_EMAIL = "coach@academy.com";
    private static final String DEFAULT_PARENT_EMAIL = "parent@academy.com";

    @Value("${app.seed-data:false}")
    private boolean seedData;

    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            CoachRepository coachRepository,
            StudentRepository studentRepository,
            ParentRepository parentRepository,
            BatchRepository batchRepository
    ) {
        return args -> {
            if (!seedData) {
                log.info("Initial seed skipped: app.seed-data is disabled");
                return;
            }

            ensureAppConfigTable();
            if (isSeedCompleted()) {
                log.info("Initial seed skipped: '{}' marker already present", INITIAL_SEED_KEY);
                return;
            }

            if (defaultSeedUsersExist(userRepository)) {
                log.warn("Default seed users already exist but marker is missing. Marking '{}' and skipping inserts", INITIAL_SEED_KEY);
                markSeedCompleted();
                return;
            }

            // Create Admin
            User admin = User.builder()
                    .email(DEFAULT_ADMIN_EMAIL)
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .fullName("Admin User")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .nationalIdNumber("ADMIN001")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .isEmailVerified(true)
                    .build();
            userRepository.save(admin);

            // Create Coach
            Coach coach = new Coach();
            coach.setEmail(DEFAULT_COACH_EMAIL);
            coach.setPassword(passwordEncoder.encode("coach123"));
            coach.setFirstName("John");
            coach.setLastName("Doe");
            coach.setFullName("John Doe");
            coach.setDateOfBirth(LocalDate.of(1985, 5, 15));
            coach.setNationalIdNumber("COACH001");
            coach.setRole(Role.COACH);
            coach.setYearsOfExperience(10);
            coach.setSpecialization("Advanced Training");
            coach.setIsActive(true);
            coach.setIsEmailVerified(true);
            coachRepository.save(coach);

            // Create Parent
            Parent parent = new Parent();
            parent.setEmail(DEFAULT_PARENT_EMAIL);
            parent.setPassword(passwordEncoder.encode("parent123"));
            parent.setFirstName("Jane");
            parent.setLastName("Smith");
            parent.setFullName("Jane Smith");
            parent.setDateOfBirth(LocalDate.of(1980, 3, 20));
            parent.setNationalIdNumber("PARENT001");
            parent.setRole(Role.PARENT);
            parent.setPhoneNumber("+1234567890");
            parent.setIsActive(true);
            parent.setIsEmailVerified(true);
            parentRepository.save(parent);

            // Create Student (no longer a User entity)
            Student student = new Student();
            student.setFirstName("Tom");
            student.setLastName("Smith");
            student.setFullName("Tom Smith");
            student.setGender(Gender.MALE);
            student.setDateOfBirth(LocalDate.of(2010, 7, 10));
            student.setNationalIdNumber("STUDENT001");
            student.setSkillLevel(SkillLevel.BEGINNER);
            student.setParent(parent);
            student.setIsActive(true);
            studentRepository.save(student);

            // Create Batch
            Batch batch = Batch.builder()
                    .name("Beginner Batch A")
                    .skillLevel(SkillLevel.BEGINNER)
                    .coach(coach)
                    .startTime(LocalTime.of(16, 0))
                    .endTime(LocalTime.of(17, 30))
                    .isActive(true)
                    .students(new HashSet<>())
                    .build();
            batch.getStudents().add(student);
            batchRepository.save(batch);

            markSeedCompleted();

            log.info("Database seeded successfully!");
            log.info("Admin: admin@academy.com / admin123");
            log.info("Coach: coach@academy.com / coach123");
            log.info("Parent: parent@academy.com / parent123");
            log.info("Student: Tom Smith (no login required)");
        };
    }

    @Bean
    CommandLineRunner createStudentReportingView() {
        return args -> {
            if (!seedData) {
                log.info("Student reporting view creation skipped: app.seed-data is disabled");
                return;
            }

            jdbcTemplate.execute("DROP VIEW IF EXISTS student_full_details CASCADE");

            String sql = """
                CREATE OR REPLACE VIEW student_full_details AS
                SELECT
                    s.id AS student_id,
                    s.first_name,
                    s.last_name,
                    s.full_name,
                    s.gender,
                    s.national_id_number,
                    s.date_of_birth,
                    s.phone_number,
                    s.address,
                    s.city,
                    s.state,
                    s.country,
                    s.photo_url,
                    s.is_active,
                    s.created_at,
                    s.updated_at,
                    s.skill_level,
                    s.parent_id,
                    pu.full_name AS parent_name,
                    COALESCE(array_remove(array_agg(DISTINCT bs.batch_id), NULL), '{}') AS batch_ids,
                    COALESCE(array_remove(array_agg(DISTINCT b.name), NULL), '{}') AS batch_names,
                    COALESCE(array_remove(array_agg(DISTINCT std.day_of_week::text), NULL), '{}') AS training_days
                FROM students s
                LEFT JOIN users pu ON pu.id = s.parent_id
                LEFT JOIN batch_students bs ON bs.student_id = s.id
                LEFT JOIN batches b ON b.id = bs.batch_id
                LEFT JOIN student_training_days std ON std.student_id = s.id
                GROUP BY
                    s.id, s.first_name, s.last_name, s.full_name, s.gender, s.national_id_number,
                    s.date_of_birth, s.phone_number, s.address, s.city, s.state, s.country, s.photo_url,
                    s.is_active, s.created_at, s.updated_at,
                    s.skill_level, s.parent_id, pu.full_name
                """;

            jdbcTemplate.execute(sql);
            log.info("Recreated database view: student_full_details");
        };
    }

    private void ensureAppConfigTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS app_config (
                config_key VARCHAR(100) PRIMARY KEY,
                config_value VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """);
    }

    private boolean isSeedCompleted() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_config WHERE config_key = ?",
                Integer.class,
                INITIAL_SEED_KEY
        );
        return count != null && count > 0;
    }

    private boolean defaultSeedUsersExist(UserRepository userRepository) {
        return userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)
                || userRepository.existsByEmail(DEFAULT_COACH_EMAIL)
                || userRepository.existsByEmail(DEFAULT_PARENT_EMAIL);
    }

    private void markSeedCompleted() {
        int updated = jdbcTemplate.update(
                "UPDATE app_config SET config_value = ?, updated_at = CURRENT_TIMESTAMP WHERE config_key = ?",
                "true",
                INITIAL_SEED_KEY
        );

        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO app_config (config_key, config_value, created_at, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    INITIAL_SEED_KEY,
                    "true"
            );
        }
    }
}
