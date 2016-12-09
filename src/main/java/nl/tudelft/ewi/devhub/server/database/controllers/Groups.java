package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.base.Preconditions;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static nl.tudelft.ewi.devhub.server.database.entities.QGroup.group;



public class Groups extends Controller<Group> {

	@Inject
	public Groups(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public Group findByRepoName(String repoName) {
		Preconditions.checkNotNull(repoName);
		Group res = query().selectFrom(group)
			.where(group.repository.repositoryName.equalsIgnoreCase(repoName))
			.fetchOne();

		return ensureNotNull(res, "Could not find group by repository name: " + repoName);
	}

	@Transactional
	public List<Group> find(CourseEdition course) {
		Preconditions.checkNotNull(course);
		return query().selectFrom(group)
			.where(group.courseEdition.id.eq(course.getId()))
			.orderBy(group.groupNumber.asc())
			.fetch();
	}

	@Transactional
	public List<Group> listFor(User user) {
		Preconditions.checkNotNull(user);
		return query().selectFrom(group)
			.where(group.members.contains(user))
			.orderBy(group.groupNumber.asc())
			.fetch();
	}

	@Transactional
	public Group find(CourseEdition course, long groupNumber) {
		Preconditions.checkNotNull(course);
		Group res = query().selectFrom(group)
			.where(group.courseEdition.id.eq(course.getId()))
			.where(group.groupNumber.eq(groupNumber))
			.fetchOne();

		return ensureNotNull(res, "Could not find group by course: " + course + " and groupNumber: " + groupNumber);
	}

	@Transactional
	public Group find(CourseEdition courseEdition, User user) {
		Preconditions.checkNotNull(courseEdition);
		Preconditions.checkNotNull(user);

		return ensureNotNull(query().selectFrom(group)
			.where(group.members.contains(user)
			.and(group.courseEdition.eq(courseEdition)))
			.fetchOne(),
			String.format("Could not find group by course %s and user %s", courseEdition, user));
	}

}
