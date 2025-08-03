package project.ktc.springboot_app.course.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.category.entity.Category;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.earning.entity.InstructorEarning;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.entity.Payment;
import project.ktc.springboot_app.review.entity.Review;
import project.ktc.springboot_app.section.entity.Section;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course extends BaseEntity {
    @Column(nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "thumbnail_id")
    private String thumbnailId;

    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private CourseLevel level = CourseLevel.BEGINNER;

    @ManyToMany
    @JoinTable(name = "course_categories", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstructorEarning> instructorEarnings;
}