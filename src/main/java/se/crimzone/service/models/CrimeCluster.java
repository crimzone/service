package se.crimzone.service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaInflectorServerCodegen", date = "2016-05-24T19:10:53.647Z")
public class CrimeCluster {

	private Double latitude = null;
	private Double longitude = null;
	private Long count = null;


	/**
	 **/
	public CrimeCluster latitude(Double latitude) {
		this.latitude = latitude;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
	@JsonProperty("latitude")
	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}


	/**
	 **/
	public CrimeCluster longitude(Double longitude) {
		this.longitude = longitude;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
	@JsonProperty("longitude")
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}


	/**
	 **/
	public CrimeCluster count(Long count) {
		this.count = count;
		return this;
	}


	@ApiModelProperty(example = "null", required = true, value = "")
	@JsonProperty("count")
	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
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
		return Objects.equals(latitude, crimeCluster.latitude) &&
				Objects.equals(longitude, crimeCluster.longitude) &&
				Objects.equals(count, crimeCluster.count);
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude, count);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CrimeCluster {\n");

		sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
		sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
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

