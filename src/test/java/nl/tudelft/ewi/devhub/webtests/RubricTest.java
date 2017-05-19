package nl.tudelft.ewi.devhub.webtests;

import com.google.inject.Inject;
import nl.tudelft.ewi.devhub.server.database.controllers.Assignments;
import nl.tudelft.ewi.devhub.server.database.controllers.CourseEditions;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Task;
import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.AssignmentOverviewView;
import nl.tudelft.ewi.devhub.webtests.views.CourseView;
import nl.tudelft.ewi.devhub.webtests.views.RubricsView;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RubricTest extends WebTest {

    @Inject
    private Assignments assignments;

    @Inject
    private CourseEditions courseEditions;

    public static final String ASSISTANT_NET_ID = "assistant1";
    public static final String ASSISTANT_PASSWORD = "assistant1";

    @Test
    public void testAssistantAddTask() throws InterruptedException {
        CourseView overview = openLoginScreen()
                .login(ASSISTANT_NET_ID, ASSISTANT_PASSWORD)
                .toCoursesView()
                .listAssistingCourses().get(0).click();

        final AssignmentOverviewView assignmentOverviewView = overview.listAssignments().get(0).openAssignmentOverviewView();
        final RubricsView rubricsView = assignmentOverviewView.openRubricsView();
        rubricsView.createNewTask();

        Thread.sleep(1000);


        final nl.tudelft.ewi.devhub.server.database.entities.Assignment modelAssignment = courseEditions.find(1).getAssignments().get(0);
        final List<Task> tasks = assignments.refresh(modelAssignment).getTasks();

        assertEquals(4, tasks.size());
        assertEquals(4, tasks.get(3).getOrdering());
    }

}
