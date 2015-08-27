package nl.tudelft.ewi.devhub.server.database.entities;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.devhub.server.database.Configurable;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.builds.BuildInstructionEntity;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Entity
@Table(name = "course_edition")
@ToString(of = {"id", "course"})
@EqualsAndHashCode(of = { "id" })
public class CourseEdition implements Comparable<CourseEdition>, Configurable {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@JoinColumn(name = "course_id")
	@ManyToOne(optional = false, cascade = CascadeType.PERSIST)
	private Course course;

	@NotNull
	@Embedded
	private TimeSpan timeSpan;

	@NotNull(message = "error.course-min-group-empty")
	@Column(name = "min_group_size")
	private Integer minGroupSize;

	@NotNull(message = "error.course-max-group-empty")
	@Column(name = "max_group_size")
	private Integer maxGroupSize;

	@Column(name = "template_repository_url")
	private String templateRepositoryUrl;

	@OrderBy("groupNumber ASC")
	@OneToMany(mappedBy = "courseEdition", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Group> groups;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(
		name="course_assistants",
		joinColumns={@JoinColumn(name="course_edition_id", referencedColumnName="id")},
		inverseJoinColumns={@JoinColumn(name="user_id", referencedColumnName="id")})
	private Set<User> assistants;

    @OrderBy("dueDate ASC, name ASC")
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Assignment> assignments;

	@ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "build_instruction")
	private BuildInstructionEntity buildInstruction;

	@ElementCollection
	@JoinTable(name="course_properties", joinColumns=@JoinColumn(name="course_edition_id"))
	@MapKeyColumn(name="property_key")
	@Column(name="property_value")
	private Map<String, String> properties;

	@Deprecated
	public String getCode() {
		return getCourse().getCode();
	}

	@Deprecated
	public String getName() {
		return getCourse().getName();
	}

	@Override
	public int compareTo(CourseEdition o) {
		return ComparisonChain.start()
			.compare(getCourse(), o.getCourse())
			.compare(getTimeSpan().getStart(), o.getTimeSpan().getStart())
			.result();
	}

	public URI createRepositoryName(Group group) {
		Preconditions.checkNotNull(group);

		URI base = URI.create("courses");
		String coursePart = getCourse().getCode().toLowerCase();

		TimeSpan timeSpan = getTimeSpan();
		if(timeSpan.getEnd() != null) {
			coursePart += String.format("-%02d%02d", timeSpan.getStart().getYear() % 100, timeSpan.getEnd().getYear() % 100);
		}

		Long groupNumber = group.getGroupNumber();
		Preconditions.checkNotNull(groupNumber);
		return base.resolve(coursePart).resolve("group-" + groupNumber);
	}

}
