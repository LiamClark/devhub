package nl.tudelft.ewi.devhub.webtests.views;

import lombok.AllArgsConstructor;
import lombok.Value;
import nl.tudelft.ewi.devhub.server.database.entities.rubrics.Task;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RubricsView extends AuthenticatedView {
    private WebElement newRubric;

    public RubricsView(WebDriver driver, WebElement newRubric) {
        super(driver);
        this.newRubric = newRubric;
    }

    public void createNewTask() {
        new WebDriverWait(getDriver(), 5).until(
                ExpectedConditions.visibilityOf(newRubric)
        );

        newRubric.click();
    }
}
