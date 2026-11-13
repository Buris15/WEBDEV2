# WEBDEV2 — WEEK 9 LAB ANSWERS
## Capstone Milestone 2: Database Integration + JPA Entity Model

**Name:** Adrian Lacsamana  
**Student ID:** 2510955
**GitHub Repository:** https://github.com/Buris15/webdev2  
**Branch:** week9  
**Date Submitted:** November 13, 2026

---

# TASK 1 — One-to-One + One-to-Many Mappings

### Approach / Explanation

I implemented a bidirectional One-to-One relationship between User and UserProfile. The User entity is the owning side because it contains `@JoinColumn(name = "profile_id")`, while UserProfile uses `mappedBy = "profile"`.

I also implemented a bidirectional One-to-Many relationship between Department and Employee. Employee owns the relationship through the `department_id` foreign key, while Department uses `mappedBy = "department"`. Helper methods were added to keep both sides of the relationship synchronized.

### Code / Endpoint(s) Used

**User.java**

```java
@OneToOne(cascade = CascadeType.ALL)
@JoinColumn(name = "profile_id")
private UserProfile profile;
```

**UserProfile.java**

```java
@OneToOne(mappedBy = "profile")
private User user;
```

**Department.java**

```java
@OneToMany(
    mappedBy = "department",
    cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    },
    orphanRemoval = true
)
private List<Employee> employees = new ArrayList<>();
```

**Employee.java**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

**Department helper methods**

```java
public void addEmployee(Employee employee) {
    employees.add(employee);
    employee.setDepartment(this);
}

public void removeEmployee(Employee employee) {
    employees.remove(employee);
    employee.setDepartment(null);
}
```

**RelationshipSmokeTestRunner.java** was used to save and retrieve the related records.

### Evidence

The application successfully displayed the User with its UserProfile and the Department with its related Employees.

Console output:

```text
===== DEPARTMENT RECORDS =====

Department: IT Department
Employee: John
Position: Developer
Employee: Mark
Position: System Administrator

===== USER PROFILE =====

Username: week9_user
Email: week9@example.com
Phone: 09123456789
Address: La Union, Philippines
```

**Screenshots:**
- [Insert User.java and UserProfile.java screenshot]
- [Insert Department.java and Employee.java screenshot]
- [Insert successful console output screenshot]
- [Insert MariaDB relationship query screenshot]

### Reflection Question

**Which entity owns the Department–Employee relationship and why, and what value must mappedBy contain?**

**Answer:**

Employee owns the Department–Employee relationship because its database table contains the `department_id` foreign key. The `mappedBy` value must be `department` because that is the name of the relationship field inside the Employee entity.

---

# TASK 2 — Many-to-Many Join Table + Association Entity

### Approach / Explanation

I first implemented a basic Many-to-Many relationship between Student and Course using `@ManyToMany` and `@JoinTable`. Student was the owning side, while Course used `mappedBy = "courses"`.

I then replaced the direct Many-to-Many relationship with an Enrollment association entity because the relationship needed additional attributes such as enrollment date and grade.

The Enrollment entity connects Student and Course through two Many-to-One relationships. I also added a composite unique constraint named `uk_student_course` to prevent duplicate enrollments.

### Code / Endpoint(s) Used

**Initial Student.java mapping**

```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "student_course",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id")
)
private Set<Course> courses = new HashSet<>();
```

**Initial Course.java mapping**

```java
@ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
private Set<Student> students = new HashSet<>();
```

**Final Enrollment.java**

```java
@Entity
@Table(
    name = "enrollment",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_course",
            columnNames = {"student_id", "course_id"}
        )
    }
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate enrollmentDate;

    private String grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
```

**Repositories and service:**

- StudentRepository.java
- CourseRepository.java
- EnrollmentRepository.java
- EnrollmentService.java
- EnrollmentSmokeTestRunner.java

### Evidence

The first enrollment was saved successfully.

```text
ENROLLMENT SAVED SUCCESSFULLY
Enrollment ID: 7
Grade: A
```

The second enrollment was rejected because it used the same Student and Course combination.

```text
Duplicate entry '1-1' for key 'uk_student_course'

DUPLICATE ENROLLMENT REJECTED SUCCESSFULLY
```

**Screenshots:**
- [Insert initial Student.java and Course.java mapping screenshot]
- [Insert Enrollment.java screenshot]
- [Insert successful enrollment console screenshot]
- [Insert duplicate enrollment rejection screenshot]
- [Insert MariaDB enrollment table screenshot]

### Reflection Question

**When must a many-to-many relationship be modeled as an association entity instead of a direct @ManyToMany, and what did the unique constraint protect in your test?**

**Answer:**

A Many-to-Many relationship should use an association entity when the relationship itself needs additional information, such as enrollment date and grade. In my activity, I used Enrollment to store this information between Student and Course. The unique constraint prevents the same student from enrolling in the same course more than once.

---

# TASK 3 — Fetch Strategies Experiment: LAZY vs EAGER

### Approach / Explanation

I configured the Employee–Department relationship using `FetchType.LAZY`. I first attempted to access the Department outside an active transaction, which reproduced a LazyInitializationException.

I fixed the problem using two approaches. The first uses `@Transactional` to keep the persistence context active while accessing the Department. The second uses a JPQL `JOIN FETCH` query to retrieve the Employee and Department together.

I enabled SQL logging in application.properties to compare the queries generated by each approach.

### Code / Endpoint(s) Used

**Employee.java**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id", nullable = false)
private Department department;
```

**EmployeeRepository.java**

```java
@Query("""
    SELECT e
    FROM Employee e
    JOIN FETCH e.department
    WHERE e.id = :id
""")
Optional<Employee> findByIdWithDepartment(
    @Param("id") Long id
);
```

**EmployeeFetchService.java**

```java
// Without transaction
public Employee getEmployeeWithoutTransaction(Long id) {
    return employeeRepository.findById(id)
        .orElseThrow();
}

// Using transaction
@Transactional(readOnly = true)
public String getDepartmentNameTransactional(Long id) {

    Employee employee = employeeRepository.findById(id)
        .orElseThrow();

    return employee.getDepartment().getName();
}

// Using JOIN FETCH
public Employee getEmployeeWithFetchJoin(Long id) {

    return employeeRepository.findByIdWithDepartment(id)
        .orElseThrow();
}
```

### Evidence

**Test 1 — Lazy Without Transaction**

The application successfully reproduced:

```text
LAZY INITIALIZATION EXCEPTION REPRODUCED!
LazyInitializationException
```

**Test 2 — Using @Transactional**

```text
Department: IT Department
TRANSACTIONAL FIX SUCCESSFUL!
```

**Test 3 — Using JOIN FETCH**

```text
Employee: John
Department: IT Department
JOIN FETCH FIX SUCCESSFUL!
```

### SQL Query Comparison

Based on the actual SQL logs:

| Approach | Number of SELECT queries | Result |
|---|---:|---|
| LAZY without transaction | 1 | LazyInitializationException |
| @Transactional | 2 | Successful |
| JOIN FETCH | 1 | Successful |

The transactional method generated two SELECT statements: one for the Employee and another for the Department.

The JOIN FETCH method generated one SELECT statement containing an SQL JOIN between the Employee and Department tables.

**Screenshots:**
- [Insert LazyInitializationException screenshot]
- [Insert @Transactional success screenshot]
- [Insert JOIN FETCH success screenshot]
- [Insert SQL query comparison screenshot]

### Reflection Question

**What causes LazyInitializationException, and why is prefer-LAZY plus explicit fetch join/@EntityGraph preferable to marking everything EAGER?**

**Answer:**

LazyInitializationException occurs when the application tries to access related data after the Hibernate session or persistence context has already closed.

Using LAZY loading prevents unnecessary data retrieval. We can use `@Transactional`, `JOIN FETCH`, or `@EntityGraph` when related information is needed. This is better than making everything EAGER because it avoids loading unnecessary data and helps maintain application performance.

---

# TASK 4 — Cascade + orphanRemoval: Safe vs. Dangerous

### Approach / Explanation

I configured the Department–Employee relationship using `CascadeType.PERSIST` and `CascadeType.MERGE`, together with `orphanRemoval = true`.

This allows new Employees to be saved when their Department is saved. It also deletes an Employee when removed from the Department's employee collection.

For the Student–Course relationship, I avoided `CascadeType.REMOVE` and `CascadeType.ALL` because courses can be shared by multiple students.

I explicitly removed Student A's enrollment record before deleting Student A to protect the shared Course and Student B's enrollment.

### Code / Endpoint(s) Used

**Department.java**

```java
@OneToMany(
    mappedBy = "department",
    cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    },
    orphanRemoval = true
)
private List<Employee> employees = new ArrayList<>();
```

**Student.java**

```java
@OneToMany(
    mappedBy = "student",
    fetch = FetchType.LAZY,
    cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    }
)
private List<Enrollment> enrollments = new ArrayList<>();
```

**Course.java**

```java
@OneToMany(
    mappedBy = "course",
    fetch = FetchType.LAZY,
    cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    }
)
private List<Enrollment> enrollments = new ArrayList<>();
```

**CascadeDemoService.java** was used to test persistence, orphan removal, and shared Course protection.

### Evidence

The console output from the cascade demonstration showed:

```text
===== CASCADE PERSIST TEST =====

Department saved: Week9 Cascade Demo Department
Employee saved through cascade: true

===== ORPHAN REMOVAL TEST =====

Employee exists after removal: false
ORPHAN REMOVAL SUCCESSFUL: true
```

The shared Course test showed:

```text
===== SAFE STUDENT DELETE TEST =====

Student A deleted successfully.
Shared course still exists: true
Student B still enrolled: true
```

**Screenshots:**
- [Insert Department.java cascade configuration screenshot]
- [Insert Student.java and Course.java screenshot]
- [Insert orphan removal console output screenshot]
- [Insert shared Course verification screenshot]
- [Insert MariaDB before-and-after query screenshots]

### Reflection Question

**What is the difference between CascadeType.REMOVE and orphanRemoval = true, and in which situation is each appropriate or dangerous?**

**Answer:**

CascadeType.REMOVE deletes related child records when the parent entity is deleted. It is appropriate when the child belongs exclusively to the parent, but dangerous when the related entity is shared.

orphanRemoval = true deletes a child when it is removed from the parent's collection. It is useful for composition relationships such as Department and Employee. It should not be used carelessly on shared relationships.

In my activity, orphanRemoval successfully deleted the removed Employee while the shared Course remained in the database after Student A was deleted.

---

# TASK 5 — Capstone Milestone 2: ERD to JPA Entity Model

### Approach / Explanation

For my capstone project, I designed a JPA entity model for an Automatic Pig Feeder System. The proposed model contains four entities: Pig, Feeder, FeedingSchedule, and FeederAssignment.

A Pig can be assigned to different feeders, and a Feeder can be associated with multiple pigs. I modeled this Many-to-Many relationship using FeederAssignment as an association entity, which stores the assignment date and active status.

I also implemented a One-to-Many relationship between Feeder and FeedingSchedule. The relationships use LAZY fetching to avoid unnecessary database loading, while feeding schedules use PERSIST and MERGE cascade with orphan removal.

### ERD to JPA Entity Mapping

| Entity | Database Table | Relationship |
|---|---|---|
| Pig | pigs | One-to-Many with FeederAssignment |
| Feeder | feeders | One-to-Many with FeederAssignment |
| FeedingSchedule | feeding_schedules | Many-to-One with Feeder |
| FeederAssignment | feeder_assignments | Many-to-One with Pig and Feeder |

### Foreign Keys

- `feeder_assignments.pig_id` references `pigs.id`.
- `feeder_assignments.feeder_id` references `feeders.id`.
- `feeding_schedules.feeder_id` references `feeders.id`.

### Fetch and Cascade Decisions

**Pig–FeederAssignment:**

LAZY fetching is used because assignments are loaded only when needed. Automatic REMOVE cascade is avoided because assignments connect separate entities.

**Feeder–FeederAssignment:**

LAZY fetching prevents unnecessary loading of related assignments. REMOVE cascade is avoided to protect shared relationships.

**Feeder–FeedingSchedule:**

LAZY fetching prevents unnecessary schedule loading. PERSIST and MERGE cascade are used because schedules belong to their Feeder. Orphan removal deletes schedules removed from the Feeder's collection.

### Code / Endpoint(s) Used

**Pig.java**

```java
@OneToMany(
    mappedBy = "pig",
    fetch = FetchType.LAZY
)
private List<FeederAssignment> assignments =
        new ArrayList<>();
```

**Feeder.java**

```java
@OneToMany(
    mappedBy = "feeder",
    fetch = FetchType.LAZY,
    cascade = {
        CascadeType.PERSIST,
        CascadeType.MERGE
    },
    orphanRemoval = true
)
private List<FeedingSchedule> schedules =
        new ArrayList<>();
```

**FeedingSchedule.java**

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "feeder_id", nullable = false)
private Feeder feeder;
```

**FeederAssignment.java**

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "pig_id", nullable = false)
private Pig pig;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "feeder_id", nullable = false)
private Feeder feeder;
```

**Repositories:**

- PigRepository.java
- FeederRepository.java
- FeedingScheduleRepository.java
- FeederAssignmentRepository.java

**Smoke Test:**

- CapstoneSmokeTestService.java
- CapstoneSmokeTestRunner.java

The smoke test creates and retrieves a Pig, Feeder, FeedingSchedule, and FeederAssignment.

### Database Mapping

| Foreign Key | Table | References |
|---|---|---|
| pig_id | feeder_assignments | pigs.id |
| feeder_id | feeder_assignments | feeders.id |
| feeder_id | feeding_schedules | feeders.id |

### Evidence

- [Insert screenshot of original Milestone 1 ERD]
- [Insert screenshot of Pig.java and Feeder.java]
- [Insert screenshot of FeedingSchedule.java and FeederAssignment.java]
- [Insert screenshot of all four repository interfaces]
- [Insert screenshot of successful CapstoneSmokeTestRunner output]
- [Insert screenshot of MariaDB tables and saved records]

### Reflection Question

**For one relationship in your capstone model, state the cardinality, the owning side, where the FK/join table lives, and why you chose that fetch and cascade setting.**

**Answer:**

The Feeder–FeedingSchedule relationship is One-to-Many. FeedingSchedule is the owning side because it contains the `feeder_id` foreign key referencing the Feeder table. I used LAZY fetching to avoid loading schedules unnecessarily. I also used PERSIST and MERGE cascade with orphan removal because feeding schedules belong to a specific Feeder and should be removed when they are no longer part of its collection.

---

# OVERALL CONCLUSION

In this laboratory activity, I learned how to model and implement different database relationships using Spring Data JPA.

I created bidirectional One-to-One and One-to-Many relationships, implemented a Many-to-Many relationship using an association entity, and learned how to handle lazy loading using transactions and fetch joins.

I also practiced cascade operations and orphan removal to understand how deleting or modifying parent entities affects related records.

Finally, I applied these concepts to my Automatic Pig Feeder capstone project by creating JPA entities, repositories, and relationships that represent the system's database structure.

This activity improved my understanding of database relationships, entity ownership, foreign keys, fetch strategies, and persistence management in Spring Boot.
