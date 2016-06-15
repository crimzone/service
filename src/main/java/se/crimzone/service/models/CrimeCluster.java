package se.crimzone.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2016-06-15T17:27:33.477Z")
public class CrimeCluster {

	private GeoJsonPoint location = null;
	private Integer count = null;

	/**
	 **/
	public CrimeCluster location(GeoJsonPoint location) {
		this.location = location;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("location")
	public GeoJsonPoint getLocation() {
		return location;
	}

	public void setLocation(GeoJsonPoint location) {
		this.location = location;
	}

	/**
	 **/
	public CrimeCluster count(Integer count) {
		this.count = count;
		return this;
	}


	@ApiModelProperty(required = true, value = "")
	@JsonProperty("count")
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CrimeCluster crimeCluster = (CrimeCluster) o;
		return Objects.equals(location, crimeCluster.location) &&
				Objects.equals(count, crimeCluster.count);
	}

	@Override
	public int hashCode() {
		return Objects.hash(location, count);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CrimeCluster {\n");

		sb.append("    location: ").append(toIndentedString(location)).append("\n");
		sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

