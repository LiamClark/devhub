package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Map;


import static com.querydsl.core.group.GroupBy.groupBy;
import static nl.tudelft.ewi.devhub.server.database.entities.QBuildResult.buildResult;

public class BuildResults extends Controller<BuildResult> {

	@Inject
	public BuildResults(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public BuildResult find(Commit commit) {
		return find(commit.getRepository(), commit.getCommitId());
	}

	@Transactional
	public BuildResult find(RepositoryEntity repository, String commitId) {
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(commitId);
		
		BuildResult result = query().selectFrom(buildResult)
				.where(buildResult.commit.repository.id.eq(repository.getId()))
				.where(buildResult.commit.commitId.equalsIgnoreCase(commitId))
				.fetchOne();

		if (result == null) {
			throw new EntityNotFoundException();
		}
		return result;
	}

	@Transactional
	public Map<String, BuildResult> findBuildResults(RepositoryEntity repository, Collection<String> commitIds) {
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(commitIds);

		if(commitIds.isEmpty()) {
			return ImmutableMap.of();
		}

		return query().selectFrom(buildResult)
				.where(buildResult.commit.repository.eq(repository)
						.and(buildResult.commit.commitId.in(commitIds)))
				.transform(groupBy(buildResult.commit.commitId).as(buildResult));
	}

	@Transactional
	public boolean exists(Commit commit) {
		return exists(commit.getRepository(), commit.getCommitId());
	}
	
	@Transactional
	public boolean exists(RepositoryEntity repository, String commitId) {
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(commitId);
		
		try {
			find(repository, commitId);
			return true;
		}
		catch (EntityNotFoundException e) {
			return false;
		}
	}
	
}
