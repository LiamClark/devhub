package nl.tudelft.ewi.devhub.server.database.controllers;

import nl.tudelft.ewi.devhub.server.database.entities.QUser;
import nl.tudelft.ewi.devhub.server.database.entities.User;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import static com.querydsl.core.group.GroupBy.groupBy;

public class Users extends Controller<User> {

	@Inject
	public Users(EntityManager entityManager) {
		super(entityManager);
	}

	@Transactional
	public User find(long id) {
		User user = query().selectFrom(QUser.user)
			.where(QUser.user.id.eq(id))
			.fetchOne();

		return ensureNotNull(user, "Could not find user with id: " + id);
	}

	@Transactional
	public User findByNetId(String netId) {
		Preconditions.checkNotNull(netId);
		
		User user = query().selectFrom(QUser.user)
			.where(QUser.user.netId.equalsIgnoreCase(netId))
			.fetchOne();

		return ensureNotNull(user, "Could not find user with netID:" + netId);
	}

	@Transactional
	public List<User> listAllWithNetIdPrefix(String prefix) {
		Preconditions.checkNotNull(prefix);
		
		return query().selectFrom(QUser.user)
			.where(QUser.user.netId.startsWithIgnoreCase(prefix))
			.orderBy(QUser.user.netId.toLowerCase().asc())
			.fetch();
	}

	@Transactional
	public List<User> listAdministrators() {
		return query().selectFrom(QUser.user)
			.where(QUser.user.admin.isTrue())
			.fetch();
	}

	@Transactional
	public Map<String, User> mapByNetIds(Set<String> netIds) {
		Preconditions.checkNotNull(netIds);
		
		if (netIds.isEmpty()) {
			return Maps.newHashMap();
		}

		List<String> lowerCasedNetIds = netIds.stream()
				.map(String::toLowerCase)
				.collect(Collectors.toList());

		return query().selectFrom(QUser.user)
			.where(QUser.user.netId.toLowerCase().in(lowerCasedNetIds))
			.orderBy(QUser.user.netId.toLowerCase().asc())
			.transform(groupBy(QUser.user.netId).as(QUser.user));
	}

}
