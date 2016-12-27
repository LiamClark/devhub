package nl.tudelft.ewi.devhub.server.backend;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.Deliveries;
import nl.tudelft.ewi.devhub.server.database.entities.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class ConsecutiveBuildFailureBackendTest {

    @RunWith(MockitoJUnitRunner.class)
    public static class RootCommitTests {
        private ConsecutiveBuildFailureBackend commitChecking;
        private Date now = new Date();

        @Mock
        private Deliveries deliveries;
        @Mock
        private Commits commits;
        @Mock
        private Group group;
        @Mock
        private CourseEdition courseEdition;
        @Mock
        private Commit firstCommit;
        @Mock
        private Delivery delivery;


        @Before
        public void setUp() {
            commitChecking = new ConsecutiveBuildFailureBackend(commits, deliveries);
            when(group.getCourseEdition()).thenReturn(courseEdition);
            when(commits.firstCommitInRepo(any())).thenReturn(Optional.of(firstCommit));
        }

        @Test
        public void testFindRootCommitNoPreviousDelivery() {
            Optional<Commit> rootCommit = commitChecking.findRootCommit(group, delivery);
            assertEquals(firstCommit, rootCommit.get());
        }

        @Test
        public void testFindRootCommit() {
            Assignment handedIn = mock(Assignment.class);
            Assignment previous = mock(Assignment.class);

            //assignments need to be in this order since sorting on mocks is a bit weird.
            when(courseEdition.getAssignments()).thenReturn(Lists.newArrayList(previous, handedIn));
            when(deliveries.getLastDelivery(previous, group)).thenReturn(Optional.of(delivery));
            when(delivery.getCommit()).thenReturn(firstCommit);
            when(delivery.getAssignment()).thenReturn(handedIn);

            Optional<Commit> rootCommit = commitChecking.findRootCommit(group, delivery);
            assertEquals(firstCommit, rootCommit.get());
        }
    }

    public static class CommitGraphTest {
        private Date now = new Date();

        @Test
        public void testCommitTraversalHasRightDimensions() {
            CommitGraph graph = graphWithConsecutiveFailures();
            List<List<Commit>> commitsTillRoot = ConsecutiveBuildFailureBackend.commitsTillRoot(graph.end, graph.root);

            //we have two branches.
            assertEquals(2, commitsTillRoot.size());
            // the first branch has 7 commits in it.
            assertEquals(7, commitsTillRoot.get(0).size());
        }

        @Test
        public void testConsecutiveBuildFailures() {
            CommitGraph graph = graphWithConsecutiveFailures();
            List<List<Commit>> commitsTillRoot = ConsecutiveBuildFailureBackend.commitsTillRoot(graph.end, graph.root);
            assertTrue(ConsecutiveBuildFailureBackend.consecutiveBuildFailures(commitsTillRoot));
        }

        @Test
        public void testNoConsecutiveBuildFailures() {
            CommitGraph graph = graphWithNoConsecutiveFailures();
            List<List<Commit>> commitsTillRoot = ConsecutiveBuildFailureBackend.commitsTillRoot(graph.end, graph.root);
            assertFalse(ConsecutiveBuildFailureBackend.consecutiveBuildFailures(commitsTillRoot));
        }

        /**
         * represent the following commit graphWithConsecutiveFailures
         * B  - D - E - F - G
         * /                     \
         * AD  -  C -                 HD
         * <p>
         * where D E F G had failures
         * and a postfix d is a failure.
         */
        private CommitGraph graphWithConsecutiveFailures() {
            Commit a = commitWithBuildResult(success(), "a");

            Commit b = withBuildResultAndParent(a, success(), "b");
            Commit c = withBuildResultAndParent(a, success(), "c");

            Commit d = withBuildResultAndParent(b, failure(), "d");
            Commit e = withBuildResultAndParent(d, failure(), "e");
            Commit f = withBuildResultAndParent(e, failure(), "f");
            Commit g = withBuildResultAndParent(f, failure(), "g");

            Commit h = commitWithBuildResult(success(), "h");
            h.setParents(Lists.newArrayList(g, c));

            return new CommitGraph(a, h);
        }

        private CommitGraph graphWithNoConsecutiveFailures() {
            Commit a = commitWithBuildResult(success(), "a");
            Commit b = withBuildResultAndParent(a, success(), "b");

            return new CommitGraph(a,b);
        }

        private Commit commitWithBuildResult(BuildResult result, String id) {
            Commit commit = new Commit();
            commit.setBuildResult(result);
            result.setCommit(commit);
            commit.setCommitTime(now);
            commit.setCommitId(id);

            return commit;
        }

        private Commit withBuildResultAndParent(Commit parent, BuildResult result, String id) {
            Commit commit = commitWithBuildResult(result, id);
            commit.setParents(Lists.newArrayList(parent));
            return commit;
        }

        @AllArgsConstructor
        public static class CommitGraph {
            final Commit root;
            final Commit end;
        }

        public static BuildResult failure() {
            BuildResult buildResult = new BuildResult();
            buildResult.setSuccess(false);
            return buildResult;
        }

        public static BuildResult success() {
            BuildResult buildResult = new BuildResult();
            buildResult.setSuccess(true);
            return buildResult;
        }
    }
}