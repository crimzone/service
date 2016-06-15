package se.crimzone.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-06-15T17:27:33.477Z")
public class Crimes {

	private List<Crime> individual = new ArrayList<Crime>();
	private List<CrimeCluster> clusters = new ArrayList<CrimeCluster>();

	/**
	 **/
	public Crimes individual(List<Crime> individual) {
		this.individual = individual;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("individual")
	public List<Crime> getIndividual() {
		return individual;
	}

	public void setIndividual(List<Crime> individual) {
		this.individual = individual;
	}

	/**
	 **/
	public Crimes clusters(List<CrimeCluster> clusters) {
		this.clusters = clusters;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("clusters")
	public List<CrimeCluster> getClusters() {
		return clusters;
	}

	public void setClusters(List<CrimeCluster> clusters) {
		this.clusters = clusters;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Crimes crimes = (Crimes) o;
		return Objects.equals(individual, crimes.individual) &&
				Objects.equals(clusters, crimes.clusters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(individual, clusters);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Crimes {\n");

		sb.append("    individual: ").append(toIndentedString(individual)).append("\n");
		sb.append("    clusters: ").append(toIndentedString(clusters)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}

