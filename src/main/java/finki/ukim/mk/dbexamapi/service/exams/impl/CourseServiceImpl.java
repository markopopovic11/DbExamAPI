package finki.ukim.mk.dbexamapi.service.exams.impl;

import finki.ukim.mk.dbexamapi.domain.dtos.exams.CourseDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.CourseCodeAlreadyExistsException;
import finki.ukim.mk.dbexamapi.domain.exceptions.exams.CourseDoesNotExistException;
import finki.ukim.mk.dbexamapi.domain.models.exams.Course;
import finki.ukim.mk.dbexamapi.repository.exams.CourseRepository;
import finki.ukim.mk.dbexamapi.service.exams.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public Optional<Course> findById(String id) {
        return courseRepository.findById(id);
    }

    @Override
    public Course findByIdNotNull(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new CourseDoesNotExistException(id));
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional
    public Course create(CourseDto courseDto) {
        if (courseRepository.existsByCode(courseDto.code())) {
            throw new CourseCodeAlreadyExistsException(courseDto.code());
        }

        Course course = new Course();
        course.setCode(courseDto.code());
        course.setName(courseDto.name());
        course.setActive(courseDto.active());

        Course saved = courseRepository.save(course);
        log.info("Created course with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Course update(String id, CourseDto courseDto) {
        Course course = findByIdNotNull(id);

        if (courseRepository.existsByCodeAndIdNot(courseDto.code(), id)) {
            throw new CourseCodeAlreadyExistsException(courseDto.code());
        }

        course.setCode(courseDto.code());
        course.setName(courseDto.name());
        course.setActive(courseDto.active());

        Course saved = courseRepository.save(course);
        log.info("Updated course with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Course deleteById(String id) {
        Course course = findByIdNotNull(id);
        courseRepository.delete(course);
        log.info("Deleted course with id: {}", id);
        return course;
    }
}
