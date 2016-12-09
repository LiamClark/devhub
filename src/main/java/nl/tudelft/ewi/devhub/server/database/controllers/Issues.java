package nl.tudelft.ewi.devhub.server.database.controllers;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;

import static nl.tudelft.ewi.devhub.server.database.entities.issues.QIssue.issue;

public class Issues extends Controller<Issue> {

	@Inject
	public Issues(EntityManager em) {
		super(em);
	}
	
	@Transactional
	public List<Issue> findAssignedIssues(final RepositoryEntity repo, User user){
		return query().selectFrom(issue)
			.where(issue.repository.eq(repo).and(issue.assignee.eq(user)))
			.fetch();
	}
	
	@Transactional
	public List<Issue> findOpenIssues(final RepositoryEntity repo){
		return query().selectFrom(issue)
			.where(issue.repository.eq(repo).and(issue.open.isTrue()))
			.fetch();
	}
	
	@Transactional
	public List<Issue> findClosedIssues(final RepositoryEntity repo){
		return query().selectFrom(issue)
			.where(issue.repository.eq(repo).and(issue.open.isFalse()))
			.fetch();
	}

	@Transactional
	public List<Issue> findUnassignedIssues(final RepositoryEntity repo){
		return query().selectFrom(issue)
			.where(issue.repository.eq(repo).and(issue.assignee.isNull()))
			.fetch();
	}

	@Transactional
	public List<Issue> findIssueById(final RepositoryEntity repo, final long id){
		return query().selectFrom(issue)
			.where(issue.repository.eq(repo).and(issue.issueId.eq(id)))
			.fetch();
	}

}
