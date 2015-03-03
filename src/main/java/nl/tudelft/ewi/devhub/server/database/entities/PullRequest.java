package nl.tudelft.ewi.devhub.server.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name="pull_requests")
@EqualsAndHashCode(of="issueId")
public class PullRequest {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long issueId;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "repository_name", referencedColumnName = "repository_name")
	private Group group;
	
	@NotNull
	@Column(name="branch_name")
	private String branchName;
	
	@Column(name="open")
	private boolean open;
	
}