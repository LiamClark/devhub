package nl.tudelft.ewi.devhub.server.database.controllers;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static nl.tudelft.ewi.devhub.server.database.entities.QCourseEdition.courseEdition;
import static nl.tudelft.ewi.devhub.server.database.entities.QGroup.group;

public class CourseEditions extends Controller<CourseEdition> {

	@Inject
	public CourseEditions(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public CourseEdition find(long id) {
		return ensureNotNull(query().selectFrom(courseEdition)
			.where(courseEdition.id.eq(id))
			.fetchOne(), "Could not find course with id: " + id);
	}

	@Transactional
	public CourseEdition find(String courseCode, String period) {
		Preconditions.checkNotNull(courseCode);
		Preconditions.checkNotNull(period);

		return ensureNotNull(query().selectFrom(courseEdition)
			.where(courseEdition.code.equalsIgnoreCase(period)
				.and(courseEdition.course.code.equalsIgnoreCase(courseCode)))
			.fetchOne(), "Could not find course with code: " + courseCode);
	}

    @Transactional
    public List<CourseEdition> listParticipatingCourses(User user) {
		return query().from(group)
				.where(group.members.contains(user))
				.select(group.courseEdition)
				.fetch();
    }

	/**
	 * @param user
	 * @return Get the assisting {@link CourseEdition CourseEditions} for a {@link User}.
	 * @deprecated Use {@link User#getAssists()} instead.
	 * @see User#getAssists()
	 */
	@Deprecated
    @Transactional
    public Collection<CourseEdition> listAssistingCourses(User user) {
		return user.getAssists();
    }

    @Transactional
    public List<CourseEdition> listAdministratingCourses(User user) {
        if(user.isAdmin()) {
            return query().selectFrom(courseEdition).fetch();
        }
        return ImmutableList.of();
    }

	private JPAQuery<CourseEdition> activeCoursesBaseQuery() {
		Date now = new Date();
		return query().selectFrom(courseEdition)
			.where(courseEdition.timeSpan.start.before(now)
				.and(courseEdition.timeSpan.end.isNull()
					.or(courseEdition.timeSpan.end.after(now))));
	}

	@Transactional
	public List<CourseEdition> listActiveCourses() {
		return activeCoursesBaseQuery()
			.orderBy(courseOrdering())
			.fetch();
	}

	@Transactional
	public CourseEdition getActiveCourseEdition(Course course) {
		return ensureNotNull(activeCoursesBaseQuery()
			.where(courseEdition.course.eq(course))
			.fetchOne(), "Could not find active course edition for " + course);
	}

	@Transactional
	public List<CourseEdition> listNotYetParticipatedCourses(User user) {
		Preconditions.checkNotNull(user);

		List<CourseEdition> participatingCourses = JPAExpressions.selectFrom(group)
			.where(group.members.contains(user))
				.select(group.courseEdition)
			.fetch();

		return activeCoursesBaseQuery()
			.where(courseEdition.notIn(participatingCourses))
			.orderBy(courseOrdering())
			.fetch();
	}

	private static OrderSpecifier<?>[] courseOrdering() {
		return new OrderSpecifier<?>[] {
			courseEdition.course.code.asc(),
			courseEdition.course.name.asc(),
			courseEdition.timeSpan.start.asc()
		};
	}
}
