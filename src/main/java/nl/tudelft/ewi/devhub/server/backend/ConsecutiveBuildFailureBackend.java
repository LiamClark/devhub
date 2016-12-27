package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.Assignment;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Delivery;
import nl.tudelft.ewi.devhub.server.database.entities.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Checks whether between the previous assignments delivery and the current deliver, or the start of time if it's the first assignment
 * any commits through any branches that are reachable from the commit that has been handed in contained 4 or more consecutive build failures.
 */
public class ConsecutiveBuildFailureBackend {
    public static final int MAX_CONSECUTIVE_BUILD_FAILURES = 4;
    private Commits commits;
    private Deliveries deliveries;

    @Inject
    public ConsecutiveBuildFailureBackend(Commits commits, Deliveries deliveries) {
        this.commits = commits;
        this.deliveries = deliveries;
    }

    public boolean hasConsecutiveBuildFailures(Group group, Delivery delivery) {
        final List<List<Commit>> commitsToCheck = commitsToCheckForBuildFailures(group, delivery);
        return consecutiveBuildFailures(commitsToCheck);
    }

    protected static boolean consecutiveBuildFailures(List<List<Commit>> commitsToCheck) {
        return commitsToCheck.stream().anyMatch(ConsecutiveBuildFailureBackend::hasFourFailingCommits);
    }


    protected static boolean hasFourFailingCommits(List<Commit> commits) {
        int consecutive = 0;

        for (Commit commit : commits) {
            if (commit.getBuildResult().hasFailed()) {
                consecutive++;
                if (consecutive == MAX_CONSECUTIVE_BUILD_FAILURES) {
                    return true;
                }
            } else {
                consecutive = 0;
            }
        }

        return false;
    }

    protected Optional<Commit> findRootCommit(Group group, Delivery delivery) {
        final List<Assignment> assignments = group.getCourseEdition().getAssignments().stream()
                .sorted(Assignment::compareTo)
                .collect(toList());

        final int index = assignments.indexOf(delivery.getAssignment());
        final boolean firstAssignment = index < 1;

        if (firstAssignment) {
            return commits.firstCommitInRepo(group.getRepository());
        } else {
            final Assignment previousAssignment = assignments.get(index - 1);
            return deliveries.getLastDelivery(previousAssignment, group).map(Delivery::getCommit);
        }
    }

    protected List<List<Commit>> commitsToCheckForBuildFailures(Group group, Delivery delivery) {
        final Commit deliveryCommit = delivery.getCommit();
        Optional<List<List<Commit>>> commits = findRootCommit(group, delivery).map(prevCommit -> commitsTillRoot(deliveryCommit, prevCommit));

        return commits.orElse(Lists.newArrayList());
    }

    protected static List<List<Commit>> commitsTillRoot(Commit commit, Commit root) {
        List<List<Commit>> commits = Lists.newArrayList();
        commitsBetween(commit, root, commits, new ArrayList<>());

        return commits;
    }

    private static void commitsBetween(Commit current, Commit root, List<List<Commit>> allCommitPaths, List<Commit> currentCommits) {
        currentCommits.add(current);

        if (isPastEnd(current, root)) {
            allCommitPaths.add(currentCommits);
        } else {
            List<Commit> parents = current.getParents();
            if (parents.size() == 1) {
                commitsBetween(parents.get(0), root, allCommitPaths, currentCommits);
            } else {
                for (Commit commit : parents) {
                    List<Commit> currentCopy = Lists.newArrayList(currentCommits);
                    commitsBetween(commit, root, allCommitPaths, currentCopy);
                }
            }
        }
    }

    /**
     * We are at the end of commits to consider for consecutive build failures if we either rejoined the last commit or are past it in time.
     *
     * @param end    the commit of the last delivery for the previous assignment.
     * @param commit the commit we are considering right now.
     * @return true if the current commit shouldn't be considered anymore.
     */
    private static boolean isPastEnd(Commit end, Commit commit) {
        return commit.getCommitTime().before(end.getCommitTime()) || commit.equals(end);
    }
}
