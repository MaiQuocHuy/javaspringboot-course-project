package project.ktc.springboot_app.course.enums;

public enum CourseRating {
  THREE(3),
  FOUR(4),
  FIVE(5);

  private final int value;

  CourseRating(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
